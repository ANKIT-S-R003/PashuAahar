package com.example.pashuaahar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pashuaahar.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

enum class Season { Summer, Winter, Monsoon }

class PashuViewModel(private val repository: AppRepository) : ViewModel() {

    val allProfiles: StateFlow<List<CowProfile>> = repository.allCowProfiles.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val history: StateFlow<List<RecipeHistory>> = repository.recipeHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val tips: StateFlow<List<VeterinaryTip>> = repository.allTips.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedSeason = MutableStateFlow(Season.Summer)
    val selectedSeason: StateFlow<Season> = _selectedSeason.asStateFlow()

    private val _recipe = MutableStateFlow<List<FeedIngredient>>(emptyList())
    val recipe: StateFlow<List<FeedIngredient>> = _recipe.asStateFlow()

    private val _savings = MutableStateFlow(0.0)
    val savings: StateFlow<Double> = _savings.asStateFlow()

    private val _homeCost = MutableStateFlow(0.0)
    val homeCost: StateFlow<Double> = _homeCost.asStateFlow()

    private val _marketCost = MutableStateFlow(450.0) // For 10kg batch
    val marketCost: StateFlow<Double> = _marketCost.asStateFlow()
    
    private val _seasonMessage = MutableStateFlow("")
    val seasonMessage: StateFlow<String> = _seasonMessage.asStateFlow()

    val dashboardSummary: StateFlow<DashboardSummary> = combine(allProfiles, history) { profiles, historyList ->
        DashboardSummary(
            totalCows = profiles.size,
            totalMilkYield = profiles.sumOf { it.milkYield },
            totalSavings = historyList.sumOf { it.savings }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardSummary()
    )

    init {
        seedTips()
    }

    private fun seedTips() {
        viewModelScope.launch {
            val currentTips = repository.allTips.first()
            if (currentTips.isEmpty()) {
                repository.saveTips(listOf(
                    VeterinaryTip(title = "Hydration", content = "Cows need 100-150L of water daily in Summer for peak milk.", category = "Water"),
                    VeterinaryTip(title = "Deworming", content = "Deworm every 6 months to prevent 20% milk loss.", category = "Health"),
                    VeterinaryTip(title = "Mineral Mix", content = "Add 50g daily to improve conception rate.", category = "Nutrition"),
                    VeterinaryTip(title = "Comfort", content = "A soft rubber mat increases rest time and milk yield.", category = "Management")
                ))
            }
        }
    }

    private val _selectedCow = MutableStateFlow<CowProfile?>(null)
    val selectedCow: StateFlow<CowProfile?> = _selectedCow.asStateFlow()

    fun selectCow(cow: CowProfile) {
        _selectedCow.value = cow
        generateRecipe(cow)
    }

    fun setSeason(season: Season) {
        _selectedSeason.value = season
        _seasonMessage.value = when(season) {
            Season.Summer -> "High Energy & Water-rich diet suggested to beat the heat."
            Season.Winter -> "Protein-rich diet suggested to maintain body temperature."
            Season.Monsoon -> "Fiber-rich diet with mineral balance to prevent infections."
        }
        _selectedCow.value?.let { generateRecipe(it) } ?: allProfiles.value.firstOrNull()?.let { selectCow(it) }
    }

    fun saveProfile(profile: CowProfile) {
        viewModelScope.launch {
            repository.saveCowProfile(profile)
            if (_selectedCow.value == null) {
                selectCow(profile)
            }
        }
    }

    fun deleteProfile(profile: CowProfile) {
        viewModelScope.launch {
            repository.deleteCowProfile(profile)
            if (_selectedCow.value?.id == profile.id) {
                _selectedCow.value = null
            }
        }
    }

    fun generateRecipe(profile: CowProfile) {
        val totalKg = 100.0
        val seasonFactor = when (_selectedSeason.value) {
            Season.Summer -> 1.15
            Season.Winter -> 1.05
            Season.Monsoon -> 0.95
        }

        val maize = (35.0 + (profile.milkYield * 0.5)) * seasonFactor
        val cake = 30.0 + (profile.milkYield * 0.7)
        val bran = (totalKg - (maize + cake)).coerceAtLeast(10.0)
        
        val actualTotal = maize + cake + bran
        val ingredients = listOf(
            FeedIngredient("Maize", 22.0, (maize / actualTotal) * 100),
            FeedIngredient("Cottonseed Cake", 38.0, (cake / actualTotal) * 100),
            FeedIngredient("Wheat Bran", 18.0, (bran / actualTotal) * 100)
        )
        
        _recipe.value = ingredients
        val homemadeCostPerKg = ingredients.sumOf { it.costPerKg * (it.ratio / 100.0) }
        val marketCostPerKg = 45.0
        
        val batchSize = 10.0 // Standard 10kg comparison
        val totalHomeCost = homemadeCostPerKg * batchSize
        val totalMarketCost = marketCostPerKg * batchSize
        val currentSavings = totalMarketCost - totalHomeCost
        
        _homeCost.value = totalHomeCost
        _marketCost.value = totalMarketCost
        _savings.value = currentSavings
    }

    fun saveCurrentRecipeToHistory() {
        val cow = _selectedCow.value ?: return
        val ingredients = _recipe.value
        if (ingredients.isEmpty()) return

        viewModelScope.launch {
            repository.saveRecipeHistory(
                RecipeHistory(
                    cowId = cow.id,
                    cowName = cow.name,
                    homeCost = _homeCost.value,
                    marketCost = _marketCost.value,
                    savings = _savings.value,
                    recipeSummary = ingredients.joinToString { "${it.name}: ${String.format(Locale.getDefault(), "%.1f", it.ratio)}kg" }
                )
            )
        }
    }
}

data class DashboardSummary(
    val totalCows: Int = 0,
    val totalMilkYield: Double = 0.0,
    val totalSavings: Double = 0.0
)

class PashuViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PashuViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PashuViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
