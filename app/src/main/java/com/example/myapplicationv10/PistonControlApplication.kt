package com.example.myapplicationv10

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.myapplicationv10.network.ApiClient
import com.example.myapplicationv10.utils.TokenManager

/**
 * PistonControlApplication - Classe Application principale
 *
 * Initialise les composants globaux de l'application au démarrage
 */
class PistonControlApplication : Application() , LifecycleObserver{

    override fun onCreate() {
        super.onCreate()

        // Initialiser le client API avec le contexte de l'application
        ApiClient.initialize(this)
    }
}
