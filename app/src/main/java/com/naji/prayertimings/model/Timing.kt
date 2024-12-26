package com.naji.prayertimings.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timing")
data class Timing(
    val name: String,
    val time: String,
    val day: Int,
    val month: Int,
    val year: Int,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)