package com.example.menshanalla.Chat

/**
 * this is the Message data class to save the data of the message.
 *
 * @author Mohammed Abou
 *
 *
 */
class Message {

    var message: String? = null
    var senderId: String? = null

    constructor(){

    }

    constructor(message : String?, senderId:String?){
        this.message= message
        this.senderId=senderId
    }


}