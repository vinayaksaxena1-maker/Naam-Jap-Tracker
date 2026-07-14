package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mantras")
data class Mantra(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val totalCount: Int = 0,
    val todayCount: Int = 0,
    val dailyGoal: Int = 1000,
    val createdDate: Long = System.currentTimeMillis(),
    val isActive: Boolean = false,
    val colorTheme: String = "RamOrange" // "KrishnaBlue", "ShivaGrey", "RamOrange", "GayatriGold"
)

@Entity(tableName = "history")
data class History(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mantraId: Int,
    val mantraName: String,
    val count: Int,
    val date: String, // "YYYY-MM-DD"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val unlocked: Boolean = false,
    val unlockDate: Long? = null,
    val target: Int, // Target count or target streak
    val type: String // "COUNT" or "STREAK"
)

@Entity(tableName = "journal")
data class Journal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val note: String,
    val date: String, // "YYYY-MM-DD"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_preferences")
data class AppPreferences(
    @PrimaryKey val id: Int = 1,
    val hapticEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val keepScreenAwake: Boolean = true,
    val counterSize: String = "Normal", // "Normal", "Large"
    val activeTheme: String = "RamOrange", // "Light", "Dark", "KrishnaBlue", "ShivaGrey", "RamOrange", "AmoledBlack"
    val language: String = "English" // "English", "Hindi"
)

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Sadhak",
    val bio: String = "Spiritual traveler on the path of Naam Jap.",
    val avatarKey: String = "lotus", // "lotus", "diya", "om", "temple", "mala", "yogi"
    val customStatus: String = "Beginner Seeker"
)

