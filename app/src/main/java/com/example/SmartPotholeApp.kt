package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.FirebaseAuthRepository
import com.example.data.repository.PotholeRepository
import com.example.services.location.LocationTrackingService
import com.example.services.network.NetworkObserver
import com.example.services.sensor.SensorDetectionEngine

class SmartPotholeApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: PotholeRepository
        private set

    lateinit var authRepository: FirebaseAuthRepository
        private set

    lateinit var sensorEngine: SensorDetectionEngine
        private set

    lateinit var locationService: LocationTrackingService
        private set

    lateinit var networkObserver: NetworkObserver
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        repository = PotholeRepository(database.potholeDao())
        authRepository = FirebaseAuthRepository(this)
        sensorEngine = SensorDetectionEngine(this)
        locationService = LocationTrackingService(this)
        networkObserver = NetworkObserver(this)
    }
}
