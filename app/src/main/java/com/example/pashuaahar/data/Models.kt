package com.example.pashuaahar.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cow_profiles")
data class CowProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val breed: String,
    val age: Int,
    val weight: Double,
    val milkYield: Double
)

data class FeedIngredient(
    val name: String,
    val costPerKg: Double,
    val ratio: Double = 0.0
)

@Entity(tableName = "recipe_history")
data class RecipeHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cowId: Int,
    val cowName: String,
    val date: Long = System.currentTimeMillis(),
    val homeCost: Double,
    val marketCost: Double,
    val savings: Double,
    val recipeSummary: String // e.g. "Maize: 40kg, Cake: 30kg..."
)

@Entity(tableName = "veterinary_tips")
data class VeterinaryTip(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val category: String
)
