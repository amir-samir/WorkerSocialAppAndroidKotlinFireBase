package com.example.menshanalla.Model

/**
 * Data class to save objects for initialising a worker
 *
 * @author Amir Azim
 *
 *
 */
data class WorkerModel(
    var workerId: String? = null,
    var workerName:String? = null,
    var workerJob:String? = null,
    var workPlaces:String? = null,
    var userType: String? = null,
    var profileImage:String? = null,
    var latitude:Number? = null,
    var longtitude:Number? = null,
    var backFromEditProfilePressed:String?=null

)
