package com.example.navigationlab.di

import com.example.navigationlab.catalog.LabScenarioCatalog
import com.example.navigationlab.engine.NavigationLabEngine
import com.example.navigationlab.launch.CaseHostLauncher
import org.koin.dsl.module

val labModule = module {
    single {
        NavigationLabEngine().apply {
            registerAll(LabScenarioCatalog.scenarios)
        }
    }

    single {
        CaseHostLauncher(
            launchByCaseCode = LabScenarioCatalog.launchByCaseCode,
        )
    }
}
