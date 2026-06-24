package com.steply.app.ui.navigation

object Routes {
    const val Onboarding = "onboarding"
    const val ProfileList = "profiles"
    const val AddEditProfile = "profiles/edit?profileId={profileId}"
    const val Home = "home"
    const val SafetySetup = "safety_setup"
    const val ChairStandCheck = "chair_stand_check"
    const val Result = "result/{resultId}"
    const val Recommendation = "recommendations"
    const val RecommendationForSession = "recommendations/session/{sessionId}"
    const val History = "history"
    const val Settings = "settings"

    fun addProfile(): String = "profiles/edit"
    fun editProfile(profileId: String): String = "profiles/edit?profileId=$profileId"
    fun result(resultId: String): String = "result/$resultId"
    fun recommendationForSession(sessionId: String): String = "recommendations/session/$sessionId"
}
