package com.example.pagovoz

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ActivationViewModelFactory(
    private val licenseRepository: LicenseRepository,
    private val emptyCodeError: String,
    private val invalidCodeError: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActivationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ActivationViewModel(
                licenseRepository = licenseRepository,
                emptyCodeError = emptyCodeError,
                invalidCodeError = invalidCodeError
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

class HomeViewModelFactory(
    private val sessionRepository: SessionRepository,
    private val licenseRepository: LicenseRepository,
    private val isNotificationEnabled: () -> Boolean
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(
                sessionRepository = sessionRepository,
                licenseRepository = licenseRepository,
                isNotificationEnabled = isNotificationEnabled
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

class ReportsViewModelFactory(
    private val sessionRepository: SessionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReportsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReportsViewModel(sessionRepository = sessionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

class AppNavigationViewModelFactory(
    private val sessionRepository: SessionRepository,
    private val licenseRepository: LicenseRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppNavigationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppNavigationViewModel(
                sessionRepository = sessionRepository,
                licenseRepository = licenseRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

class UpdateViewModelFactory(
    private val appContext: Context,
    private val updateRepository: UpdateRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UpdateViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UpdateViewModel(
                appContext = appContext.applicationContext,
                updateRepository = updateRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

fun defaultSessionRepository(context: Context): SessionRepository =
    DefaultSessionRepository(context.applicationContext)

fun defaultLicenseRepository(context: Context): LicenseRepository =
    DefaultLicenseRepository(context.applicationContext)

fun defaultUpdateRepository(): UpdateRepository = DefaultUpdateRepository()
