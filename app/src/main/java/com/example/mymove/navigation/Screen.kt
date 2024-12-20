package com.example.mymove.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Category : Screen("category")
    object Settings : Screen("settings")
    object VideoDetail : Screen("video/{videoId}/{videoName}/{typeId}") {
        fun createRoute(videoId: Int, videoName: String, typeId: Int) = 
            "video/$videoId/$videoName/$typeId"
    }
    object VideoList : Screen("videoList/{categoryId}/{categoryName}") {
        fun createRoute(categoryId: Int, categoryName: String) = 
            "videoList/$categoryId/$categoryName"
    }
    object Search : Screen("search/{query}") {
        fun createRoute(query: String) = "search/$query"
    }
}
