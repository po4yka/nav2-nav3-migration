package com.example.navigationlab.di

import com.example.navigationlab.engine.NavigationLabEngine
import org.koin.dsl.module

val labModule = module {
    single { NavigationLabEngine() }
}
