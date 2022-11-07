package com.example.menshanalla.Model

/**
 * Data class to save objects for recyclerView to save projects
 *
 * @author Amir Azim
 *
 *
 */
data class PrjectData(
    val costs: String? = null,
    val description: String? = null,
    val duration: String? = null,
    val imageUri: String? = null,
    val paid: String? = null,
    val projectName: String? = null,
    val restPayment: String? = null
)
