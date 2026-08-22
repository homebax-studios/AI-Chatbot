package com.homebax.axionis.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AxionisRoute : NavKey {
    @Serializable
    data object Setup : AxionisRoute

    @Serializable
    data object Chat : AxionisRoute
    
    @Serializable
    data object Settings : AxionisRoute

    @Serializable
    data object Speech : AxionisRoute
}
