package com.example.skycut.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.skycut.data.ProjectRepository
import com.example.skycut.data.SkyCutDatabase
import com.example.skycut.model.AspectRatio
import com.example.skycut.model.SkyCutProject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProjectRepository(SkyCutDatabase.getDatabase(application))

    val projects: StateFlow<List<SkyCutProject>> = repository.allProjects.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Seed default starter projects if empty
        viewModelScope.launch {
            repository.allProjects.collect { list ->
                if (list.isEmpty()) {
                    val travel = ProjectRepository.createSampleTravelProject()
                    val cyberpunk = ProjectRepository.createSampleCyberpunkProject()
                    repository.saveProject(travel)
                    repository.saveProject(cyberpunk)
                }
            }
        }
    }

    fun createNewProject(title: String = "New Cut", aspectRatio: AspectRatio = AspectRatio.RATIO_9_16): SkyCutProject {
        val newProj = SkyCutProject(
            id = UUID.randomUUID().toString(),
            title = title,
            aspectRatio = aspectRatio
        )
        viewModelScope.launch {
            repository.saveProject(newProj)
        }
        return newProj
    }

    fun createFromTemplate(templateName: String): SkyCutProject {
        val proj = when (templateName) {
            "Cyberpunk" -> ProjectRepository.createSampleCyberpunkProject().copy(
                id = UUID.randomUUID().toString(),
                title = "Cyberpunk Reel"
            )
            else -> ProjectRepository.createSampleTravelProject().copy(
                id = UUID.randomUUID().toString(),
                title = "Pacific Sunset Vlog"
            )
        }
        viewModelScope.launch {
            repository.saveProject(proj)
        }
        return proj
    }

    fun duplicateProject(project: SkyCutProject) {
        viewModelScope.launch {
            val copy = project.copy(
                id = UUID.randomUUID().toString(),
                title = "${project.title} (Copy)",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.saveProject(copy)
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
        }
    }
}
