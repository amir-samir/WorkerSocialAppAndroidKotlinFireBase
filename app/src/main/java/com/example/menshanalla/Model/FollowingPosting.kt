package com.example.menshanalla.Model

/**
 * Data class to save objects for recyclerView for Posts of persons the user follow.
 *
 * @author Amir Azim
 *
 *
 */
data class FollowingPosting(
    val uid: String? = null,
    val thePosts: MutableList<Posts>? = null

)
