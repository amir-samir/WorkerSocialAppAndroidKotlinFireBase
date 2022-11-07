package com.example.menshanalla.Model

/**
 * Data class to save objects  for saving a worker
 *
 * @author Amir Azim
 *
 *
 */
class Worker {
    private var workerId: String = ""
    private var workerName: String = ""
    private var workerJob: String = ""
    private var workerPlaces: String = ""
    private var userType: String = ""
    private var profileImage: String = ""
   // private var latitude: Number = 0.0
   // private var longtitude: Number = 0.0

    constructor()

    constructor(workerId: String, workerName: String, workerJob: String, workerPlaces: String, userType: String, profileImage: String){
        this.workerId = workerId
        this.workerName = workerName
        this.workerJob = workerJob
        this.workerPlaces = workerPlaces
        this.userType = userType
        this.profileImage = profileImage
    }

    fun getWorkerId() : String{
        return workerId
    }
    fun setWorkerId(workerId: String){
        this.workerId = workerId
    }

    fun getWorkerName() : String{
        return workerName
    }
    fun setWorkerName(workerName: String){
        this.workerName = workerName
    }

    fun getWorkerJob() : String{
        return workerJob
    }
    fun setWorkerJob(workerJob: String){
        this.workerJob = workerJob
    }


    fun getWorkerPlaces() : String{
        return workerPlaces
    }
    fun setWorkerPlaces(workerPlaces: String){
        this.workerPlaces = workerPlaces
    }

    fun getWorkerType() : String{
        return userType
    }
    fun setWorkerType(userType: String){
        this.userType = userType
    }

    fun getProfileImage() : String{
        return profileImage
    }
    fun setProfileImage(profileImage: String){
        this.profileImage = profileImage
    }

    /*fun getLatitude() : Number{
        return latitude
    }
    fun setLatitude(latitude: Number){
        this.latitude = latitude
    }

    fun getLongtitude() : Number{
        return longtitude
    }
    fun setLongtitude(longtitude: Number){
        this.longtitude = longtitude
    }*/
}