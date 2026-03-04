package com.example.navigationlab.di

import com.example.navigationlab.engine.NavigationLabEngine
import com.example.navigationlab.launch.CaseHostLauncher
import com.example.navigationlab.scenarios.AppScenarioCatalog
import org.koin.dsl.module

val labModule = module {
    single {
        NavigationLabEngine().apply {
            registerAll(AppScenarioCatalog.scenarios)
        }
    }

    single {
        CaseHostLauncher(
            launchByCaseCode = AppScenarioCatalog.launchByCaseCode,
        )
    }
}
