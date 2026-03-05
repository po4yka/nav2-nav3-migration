package com.example.navigationlab.recipes.helpers

import androidx.lifecycle.ViewModel
import com.example.navigationlab.recipes.keys.TabGammaDetail

class RecipeViewModel(
    key: TabGammaDetail,
) : ViewModel() {
    val result: String = key.result
}
