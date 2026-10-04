package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.TradeDatabase
import com.example.data.model.Trade
import com.example.data.model.TradingTaskEntity
import com.example.data.repository.TradeRepository
import com.example.util.TimeFilter
import com.example.util.TradingDateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

enum class MainTab {
    HOME,
    TRADE,
    TASKS,
    WORK
}

enum class ScreenDestination {
    MAIN_CONTAINER,
    ADD_EDIT_TRADE,
    TRADE_DETAILS
}

data class TradeSummary(
    val totalPnl: Double = 0.0,
    val pnlPercentage: Double = 0.0,
    val totalTrades: Int = 0,
    val profitableTrades: Int = 0,
    val lossTrades: Int = 0,
    val winRate: Int = 0,
    val totalCharges: Double = 0.0,
    val investedAmount: Double = 0.0,
    val activePositions: Int = 0,
    val totalLossAmount: Double = 0.0
)

class TradeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TradeRepository
    private val profilePrefs = application.getSharedPreferences("user_profile_prefs", android.content.Context.MODE_PRIVATE)

    private val _userName = MutableStateFlow(profilePrefs.getString("user_name", "Kingston Abraham") ?: "Kingston Abraham")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _profileImagePath = MutableStateFlow(profilePrefs.getString("profile_image_path", null))
    val profileImagePath: StateFlow<String?> = _profileImagePath.asStateFlow()

    fun updateUserName(name: String) {
        val clean = name.trim()
        if (clean.isNotEmpty()) {
            _userName.value = clean
            profilePrefs.edit().putString("user_name", clean).apply()
        }
    }

    fun updateProfilePhoto(uri: android.net.Uri, context: android.content.Context) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val avatarFile = java.io.File(context.filesDir, "profile_avatar_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    avatarFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                val path = avatarFile.absolutePath
                _profileImagePath.value = path
                profilePrefs.edit().putString("profile_image_path", path).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun removeProfilePhoto() {
        _profileImagePath.value = null
        profilePrefs.edit().remove("profile_image_path").apply()
    }

    init {
        val database = TradeDatabase.getDatabase(application, viewModelScope)
        repository = TradeRepository(database.tradeDao(), database.taskDao())
    }

    // Navigation State
    private val _currentTab = MutableStateFlow(MainTab.HOME)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    private val _currentScreen = MutableStateFlow(ScreenDestination.MAIN_CONTAINER)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    private val _selectedTradeId = MutableStateFlow<Long?>(null)
    val selectedTradeId: StateFlow<Long?> = _selectedTradeId.asStateFlow()

    private val _editingTrade = MutableStateFlow<Trade?>(null)
    val editingTrade: StateFlow<Trade?> = _editingTrade.asStateFlow()

    // Time Filters for Trade Management Screen (strictly DAY, WEEK, MONTH)
    private val _timeFilter = MutableStateFlow(TimeFilter.DAY)
    val timeFilter: StateFlow<TimeFilter> = _timeFilter.asStateFlow()

    // Default trading date set to 2026-10-04 (or current date)
    private val _selectedDate = MutableStateFlow(LocalDate.of(2026, 10, 4))
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _selectedMonth = MutableStateFlow(YearMonth.of(2026, 10))
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    // Market segment filter on Home screen
    private val _homeMarketSegment = MutableStateFlow("All")
    val homeMarketSegment: StateFlow<String> = _homeMarketSegment.asStateFlow()

    fun setHomeMarketSegment(segment: String) {
        _homeMarketSegment.value = segment
    }

    // Market segment filter on Trade Management screen (Indian Stocks, US Stocks, Forex, All)
    private val _tradeMarketSegment = MutableStateFlow("All")
    val tradeMarketSegment: StateFlow<String> = _tradeMarketSegment.asStateFlow()

    fun setTradeMarketSegment(segment: String) {
        _tradeMarketSegment.value = segment
    }

    // All Trades Flow
    val allTrades: StateFlow<List<Trade>> = repository.allTrades
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Trades filtered by market segment for Home page
    val homeTrades: StateFlow<List<Trade>> = combine(allTrades, _homeMarketSegment) { trades, segment ->
        if (segment == "All") trades else trades.filter { it.marketSegment.equals(segment, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val homeSummary: StateFlow<TradeSummary> = homeTrades.combine(allTrades) { filtered, all ->
        calculateSummary(filtered, all)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TradeSummary())

    // Filtered trades based on Day, Week, or Month and Market Segment
    val displayedTrades: StateFlow<List<Trade>> = combine(
        allTrades,
        _timeFilter,
        _selectedDate,
        _selectedMonth,
        _tradeMarketSegment
    ) { trades, filter, date, month, marketSeg ->
        val timeFiltered = when (filter) {
            TimeFilter.DAY -> {
                val isoDate = date.format(TradingDateUtils.ISO_DATE_FORMATTER)
                trades.filter { it.entryDate == isoDate }
            }
            TimeFilter.WEEK -> {
                val (monday, friday) = TradingDateUtils.getTradingWeek(date)
                val mondayIso = monday.format(TradingDateUtils.ISO_DATE_FORMATTER)
                val fridayIso = friday.format(TradingDateUtils.ISO_DATE_FORMATTER)
                trades.filter { it.entryDate in mondayIso..fridayIso }
            }
            TimeFilter.MONTH -> {
                val (start, end) = TradingDateUtils.getCalendarMonth(month)
                val startIso = start.format(TradingDateUtils.ISO_DATE_FORMATTER)
                val endIso = end.format(TradingDateUtils.ISO_DATE_FORMATTER)
                trades.filter { it.entryDate in startIso..endIso }
            }
        }
        if (marketSeg == "All") timeFiltered
        else timeFiltered.filter { it.marketSegment.equals(marketSeg, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Summary calculation for the active filter
    val currentSummary: StateFlow<TradeSummary> = displayedTrades.combine(allTrades) { filteredTrades, all ->
        calculateSummary(filteredTrades, all)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TradeSummary())

    // Tasks Management with Automatic Carry-Forward Across Days
    val allTasks: StateFlow<List<TradingTaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentDayTasks: StateFlow<List<TradingTaskEntity>> = combine(
        allTasks,
        _selectedDate
    ) { tasks, date ->
        val currentDayIso = date.format(TradingDateUtils.ISO_DATE_FORMATTER)
        tasks.filter { task ->
            // If uncompleted and created on or before current date -> carry forward!
            (!task.isCompleted && task.createdDate <= currentDayIso) ||
            // If completed on this specific date -> show on this date
            (task.isCompleted && task.completedDate == currentDayIso)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addNewTask(title: String, category: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val todayIso = _selectedDate.value.format(TradingDateUtils.ISO_DATE_FORMATTER)
            val newTask = TradingTaskEntity(
                title = title.trim(),
                category = category,
                createdDate = todayIso,
                isCompleted = false
            )
            repository.insertTask(newTask)
        }
    }

    fun swipeCompleteTask(task: TradingTaskEntity) {
        viewModelScope.launch {
            val todayIso = _selectedDate.value.format(TradingDateUtils.ISO_DATE_FORMATTER)
            repository.updateTask(
                task.copy(
                    isCompleted = true,
                    completedDate = todayIso
                )
            )
        }
    }

    fun swipeUncompleteTask(task: TradingTaskEntity) {
        viewModelScope.launch {
            repository.updateTask(
                task.copy(
                    isCompleted = false,
                    completedDate = null
                )
            )
        }
    }

    fun swipeDeleteTask(task: TradingTaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    private fun calculateSummary(trades: List<Trade>, all: List<Trade>): TradeSummary {
        val totalPnl = trades.sumOf { it.netPnl }
        val invested = trades.sumOf { it.investmentAmount }
        val charges = trades.sumOf { it.totalCharges }
        val profitable = trades.count { it.netPnl > 0 }
        val loss = trades.count { it.netPnl < 0 }
        val closedTrades = profitable + loss
        val winRate = if (closedTrades > 0) ((profitable.toDouble() / closedTrades) * 100).toInt() else 0
        val pnlPct = if (invested > 0) (totalPnl / invested) * 100 else 0.0

        val activePositions = all.count { it.isOpen }
        val totalLossSum = trades.filter { it.netPnl < 0 }.sumOf { kotlin.math.abs(it.netPnl) }

        return TradeSummary(
            totalPnl = totalPnl,
            pnlPercentage = pnlPct,
            totalTrades = trades.size,
            profitableTrades = profitable,
            lossTrades = loss,
            winRate = winRate,
            totalCharges = charges,
            investedAmount = invested,
            activePositions = activePositions,
            totalLossAmount = totalLossSum
        )
    }

    // Navigation Actions
    fun selectTab(tab: MainTab) {
        _currentTab.value = tab
        _currentScreen.value = ScreenDestination.MAIN_CONTAINER
    }

    fun openTradeManagement() {
        _currentTab.value = MainTab.TRADE
        _currentScreen.value = ScreenDestination.MAIN_CONTAINER
    }

    fun openAddTrade(tradeToEdit: Trade? = null) {
        _editingTrade.value = tradeToEdit
        _currentScreen.value = ScreenDestination.ADD_EDIT_TRADE
    }

    fun openTradeDetails(tradeId: Long) {
        _selectedTradeId.value = tradeId
        _currentScreen.value = ScreenDestination.TRADE_DETAILS
    }

    fun navigateBack() {
        if (_currentScreen.value != ScreenDestination.MAIN_CONTAINER) {
            _currentScreen.value = ScreenDestination.MAIN_CONTAINER
            _editingTrade.value = null
        }
    }

    // Time filter actions
    fun setTimeFilter(filter: TimeFilter) {
        _timeFilter.value = filter
    }

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
        _selectedMonth.value = YearMonth.from(date)
    }

    fun nextDay() {
        _selectedDate.value = _selectedDate.value.plusDays(1)
        _selectedMonth.value = YearMonth.from(_selectedDate.value)
    }

    fun previousDay() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
        _selectedMonth.value = YearMonth.from(_selectedDate.value)
    }

    fun nextWeek() {
        _selectedDate.value = _selectedDate.value.plusWeeks(1)
        _selectedMonth.value = YearMonth.from(_selectedDate.value)
    }

    fun previousWeek() {
        _selectedDate.value = _selectedDate.value.minusWeeks(1)
        _selectedMonth.value = YearMonth.from(_selectedDate.value)
    }

    fun setSelectedMonth(yearMonth: YearMonth) {
        _selectedMonth.value = yearMonth
        _selectedDate.value = yearMonth.atDay(1)
    }

    fun nextMonth() {
        val next = _selectedMonth.value.plusMonths(1)
        setSelectedMonth(next)
    }

    fun previousMonth() {
        val prev = _selectedMonth.value.minusMonths(1)
        setSelectedMonth(prev)
    }

    // Database Actions
    fun saveTrade(trade: Trade, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (trade.id > 0) {
                repository.updateTrade(trade)
            } else {
                repository.insertTrade(trade)
            }
            TradingDateUtils.parseIsoDate(trade.entryDate)?.let {
                _selectedDate.value = it
                _selectedMonth.value = YearMonth.from(it)
            }
            _currentScreen.value = ScreenDestination.MAIN_CONTAINER
            _editingTrade.value = null
            onSuccess()
        }
    }

    fun deleteTrade(tradeId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.deleteTradeById(tradeId)
            _currentScreen.value = ScreenDestination.MAIN_CONTAINER
            _selectedTradeId.value = null
            onSuccess()
        }
    }
}
