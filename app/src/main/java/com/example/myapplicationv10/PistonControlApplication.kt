package com.example.myapplicationv10

import android.app.Application
import androidx.lifecycle.LifecycleObserver
import com.example.myapplicationv10.network.ApiClient

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
