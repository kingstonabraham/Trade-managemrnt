package com.example.data.sync

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.R
import com.example.data.model.Trade
import com.example.data.model.TradingTaskEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed class SyncStatus {
    object Idle : SyncStatus()
    object Syncing : SyncStatus()
    data class Success(val message: String, val lastSyncTime: Long = System.currentTimeMillis()) : SyncStatus()
    data class Error(val errorMessage: String) : SyncStatus()
}

class FirebaseSyncManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("firebase_user_sync_prefs", Context.MODE_PRIVATE)

    private val _userEmail = MutableStateFlow(
        prefs.getString(KEY_USER_EMAIL, "kingstonabraham181309@gmail.com") ?: "kingstonabraham181309@gmail.com"
    )
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val firestore: FirebaseFirestore by lazy {
        try {
            FirebaseApp.initializeApp(context)
            val databaseId = context.getString(R.string.firestore_database_id)
            FirebaseFirestore.getInstance(FirebaseApp.getInstance(), databaseId)
        } catch (e: Exception) {
            Log.w("FirebaseSync", "Falling back to default Firestore instance: ${e.message}")
            FirebaseFirestore.getInstance()
        }
    }

    fun setUserEmail(email: String) {
        val clean = email.trim().lowercase()
        if (clean.isNotEmpty()) {
            _userEmail.value = clean
            prefs.edit().putString(KEY_USER_EMAIL, clean).apply()
        }
    }

    /**
     * Sanitizes email to be safe as Firestore document key
     */
    fun getDocumentKey(email: String = _userEmail.value): String {
        return email.trim().lowercase().replace(".", "_").replace("@", "_at_")
    }

    /**
     * Uploads local trades, tasks, and overall profit to Firestore.
     * Complies strictly with free-tier limits using minimal batch writes.
     */
    suspend fun syncUpToCloud(
        displayName: String,
        trades: List<Trade>,
        tasks: List<TradingTaskEntity>,
        overallProfit: Double
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            _syncStatus.value = SyncStatus.Syncing
            val email = _userEmail.value.ifBlank { "kingstonabraham181309@gmail.com" }
            val docKey = getDocumentKey(email)
            val userDocRef = firestore.collection("users").document(docKey)

            // 1. Write User Profile & Overall Profit Summary Document
            val profileData = hashMapOf(
                "email" to email,
                "displayName" to displayName,
                "overallProfit" to overallProfit,
                "totalTrades" to trades.size,
                "totalTasks" to tasks.size,
                "lastSyncTimestamp" to System.currentTimeMillis()
            )
            userDocRef.set(profileData, SetOptions.merge()).await()

            // 2. Upload trades in batch
            if (trades.isNotEmpty()) {
                val batch = firestore.batch()
                trades.take(100).forEach { trade ->
                    val tradeRef = userDocRef.collection("trades").document(trade.id.toString())
                    val tradeMap = hashMapOf(
                        "id" to trade.id,
                        "symbol" to trade.symbol,
                        "exchange" to trade.exchange,
                        "tradeType" to trade.tradeType,
                        "marketSegment" to trade.marketSegment,
                        "buySell" to trade.buySell,
                        "leverage" to trade.leverage,
                        "entryPrice" to trade.entryPrice,
                        "exitPrice" to trade.exitPrice,
                        "quantity" to trade.quantity,
                        "investmentAmount" to trade.investmentAmount,
                        "totalExposure" to trade.totalExposure,
                        "entryDate" to trade.entryDate,
                        "entryTime" to trade.entryTime,
                        "exitDate" to trade.exitDate,
                        "exitTime" to trade.exitTime,
                        "brokerage" to trade.brokerage,
                        "taxesAndCharges" to trade.taxesAndCharges,
                        "totalCharges" to trade.totalCharges,
                        "stopLoss" to trade.stopLoss,
                        "target" to trade.target,
                        "notes" to trade.notes,
                        "grossPnl" to trade.grossPnl,
                        "netPnl" to trade.netPnl,
                        "status" to trade.status
                    )
                    batch.set(tradeRef, tradeMap, SetOptions.merge())
                }
                batch.commit().await()
            }

            // 3. Upload tasks in batch
            if (tasks.isNotEmpty()) {
                val taskBatch = firestore.batch()
                tasks.take(100).forEach { task ->
                    val taskRef = userDocRef.collection("tasks").document(task.id.toString())
                    val taskMap = hashMapOf(
                        "id" to task.id,
                        "title" to task.title,
                        "category" to task.category,
                        "createdDate" to task.createdDate,
                        "isCompleted" to task.isCompleted,
                        "completedDate" to task.completedDate
                    )
                    taskBatch.set(taskRef, taskMap, SetOptions.merge())
                }
                taskBatch.commit().await()
            }

            val msg = "Synced ${trades.size} trades to cloud under $email"
            _syncStatus.value = SyncStatus.Success(msg)
            Result.success(msg)
        } catch (e: Exception) {
            val err = e.localizedMessage ?: "Cloud sync failed"
            Log.e("FirebaseSync", "Sync failed", e)
            _syncStatus.value = SyncStatus.Error(err)
            Result.failure(e)
        }
    }

    /**
     * Downloads existing trades from Firestore if a user logs in on a new device.
     */
    suspend fun downloadTradesFromCloud(): List<Trade> = withContext(Dispatchers.IO) {
        try {
            val email = _userEmail.value.ifBlank { return@withContext emptyList() }
            val docKey = getDocumentKey(email)
            val snapshot = firestore.collection("users")
                .document(docKey)
                .collection("trades")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    Trade(
                        id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L,
                        symbol = doc.getString("symbol") ?: "Unknown",
                        exchange = doc.getString("exchange") ?: "NSE",
                        tradeType = doc.getString("tradeType") ?: "Equity",
                        marketSegment = doc.getString("marketSegment") ?: "Indian Stocks",
                        buySell = doc.getString("buySell") ?: "BUY",
                        leverage = doc.getLong("leverage")?.toInt() ?: 1,
                        entryPrice = doc.getDouble("entryPrice") ?: 0.0,
                        exitPrice = doc.getDouble("exitPrice"),
                        quantity = doc.getLong("quantity")?.toInt() ?: 1,
                        investmentAmount = doc.getDouble("investmentAmount") ?: 0.0,
                        totalExposure = doc.getDouble("totalExposure") ?: 0.0,
                        entryDate = doc.getString("entryDate") ?: "",
                        entryTime = doc.getString("entryTime") ?: "10:00 AM",
                        exitDate = doc.getString("exitDate"),
                        exitTime = doc.getString("exitTime"),
                        brokerage = doc.getDouble("brokerage") ?: 0.0,
                        taxesAndCharges = doc.getDouble("taxesAndCharges") ?: 0.0,
                        totalCharges = doc.getDouble("totalCharges") ?: 0.0,
                        stopLoss = doc.getDouble("stopLoss"),
                        target = doc.getDouble("target"),
                        notes = doc.getString("notes") ?: "",
                        grossPnl = doc.getDouble("grossPnl") ?: 0.0,
                        netPnl = doc.getDouble("netPnl") ?: 0.0,
                        status = doc.getString("status") ?: "OPEN"
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Failed to download trades: ${e.message}")
            emptyList()
        }
    }

    /**
     * Downloads existing tasks from the cloud database.
     */
    suspend fun downloadTasksFromCloud(): List<TradingTaskEntity> = withContext(Dispatchers.IO) {
        try {
            val email = _userEmail.value.ifBlank { return@withContext emptyList() }
            val docKey = getDocumentKey(email)
            val snapshot = firestore.collection("users")
                .document(docKey)
                .collection("tasks")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    TradingTaskEntity(
                        id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L,
                        title = doc.getString("title") ?: "Task",
                        category = doc.getString("category") ?: "Routine",
                        createdDate = doc.getString("createdDate") ?: "",
                        isCompleted = doc.getBoolean("isCompleted") ?: false,
                        completedDate = doc.getString("completedDate")
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Failed to download tasks: ${e.message}")
            emptyList()
        }
    }

    /**
     * Downloads user profile summary from the database.
     */
    suspend fun downloadUserProfile(): Map<String, Any>? = withContext(Dispatchers.IO) {
        try {
            val email = _userEmail.value.ifBlank { return@withContext null }
            val docKey = getDocumentKey(email)
            val doc = firestore.collection("users").document(docKey).get().await()
            if (doc.exists()) doc.data else null
        } catch (e: Exception) {
            null
        }
    }

    fun isUserLoggedIn(): Boolean {
        return prefs.getBoolean("user_logged_in", false)
    }

    fun setUserLoggedIn(loggedIn: Boolean) {
        prefs.edit().putBoolean("user_logged_in", loggedIn).apply()
    }

    companion object {
        private const val KEY_USER_EMAIL = "firebase_user_email"

        @Volatile
        private var instance: FirebaseSyncManager? = null

        fun getInstance(context: Context): FirebaseSyncManager {
            return instance ?: synchronized(this) {
                instance ?: FirebaseSyncManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
