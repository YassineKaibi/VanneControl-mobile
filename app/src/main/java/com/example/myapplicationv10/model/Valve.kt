package com.example.myapplicationv10.model

/**
 * Valve - Simple UI display model for active valves on dashboard
 * Different from Piston which is the API response model
 */
data class Valve(
    val name: String,
    val lastChanged: String
)