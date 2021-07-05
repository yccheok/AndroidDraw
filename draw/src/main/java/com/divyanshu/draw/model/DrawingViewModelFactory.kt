package com.divyanshu.draw.model

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

class DrawingViewModelFactory(private val filepath: String?) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DrawingViewModel(filepath) as T
    }
}