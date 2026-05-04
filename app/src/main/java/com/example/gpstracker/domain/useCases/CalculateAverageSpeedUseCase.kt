package com.example.gpstracker.domain.useCases

class CalculateAverageSpeedUseCase {
    operator fun invoke(distanceMeters: Float, elapsedMillis: Long): Float {
        if (elapsedMillis <= 0 || distanceMeters <= 0) return 0f
        val elapsedSeconds = elapsedMillis / 1000f
        val speedMps = distanceMeters / elapsedSeconds
        return speedMps * 3.6f  // конвертация в км/ч
    }
}