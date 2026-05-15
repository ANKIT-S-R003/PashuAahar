package com.example.pashuaahar.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val dao: AppDao) {
    val allCowProfiles: Flow<List<CowProfile>> = dao.getAllCowProfiles()
    val recipeHistory: Flow<List<RecipeHistory>> = dao.getRecipeHistory()
    val allTips: Flow<List<VeterinaryTip>> = dao.getAllTips()

    suspend fun saveCowProfile(profile: CowProfile) = dao.insertCowProfile(profile)
    suspend fun deleteCowProfile(profile: CowProfile) = dao.deleteCowProfile(profile)
    suspend fun saveRecipeHistory(history: RecipeHistory) = dao.insertRecipeHistory(history)
    suspend fun saveTips(tips: List<VeterinaryTip>) = dao.insertTips(tips)
}
