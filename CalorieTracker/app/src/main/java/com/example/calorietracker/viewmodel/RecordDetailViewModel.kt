package com.example.calorietracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.calorietracker.data.repository.CalorieTrackerRepository
import com.example.calorietracker.domain.model.DietRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecordDetailUiState(
    val isLoading: Boolean = true,
    val record: DietRecord? = null,
    val errorMessage: String? = null
)

class RecordDetailViewModel(
    private val recordId: String,
    private val repository: CalorieTrackerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordDetailUiState())
    val uiState: StateFlow<RecordDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val record = repository.getRecord(recordId)
            _uiState.update {
                RecordDetailUiState(
                    isLoading = false,
                    record = record,
                    errorMessage = if (record == null) "This food record could not be found." else null
                )
            }
        }
    }

    companion object {
        fun provideFactory(recordId: String) = viewModelFactory {
            initializer {
                RecordDetailViewModel(
                    recordId = recordId,
                    repository = calorieTrackerApplication().container.repository
                )
            }
        }
    }
}
