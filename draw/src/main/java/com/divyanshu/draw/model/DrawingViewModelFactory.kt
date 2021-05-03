package com.divyanshu.draw.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DrawingViewModelFactory(private val filepath: String) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DrawingViewModel(filepath) as T
    }
}