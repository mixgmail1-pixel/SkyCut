package com.example.skycut.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "skycut_projects")
data class ProjectEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val durationMs: Long,
    val aspectRatio: String,
    val projectJson: String,
    val coverThumbnail: String? = null
)
