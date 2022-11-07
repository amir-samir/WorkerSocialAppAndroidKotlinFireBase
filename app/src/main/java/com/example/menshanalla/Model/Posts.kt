package com.example.menshanalla.Model
/**
 * Data class to save objects for recyclerView for posts
 *
 * @author Amir Azim
 *
 *
 */
data class Posts(
    val posts: PostsX?= null
)

data class PostsX(
    val description: String = "",
    val posting: List<Any>? = null
)