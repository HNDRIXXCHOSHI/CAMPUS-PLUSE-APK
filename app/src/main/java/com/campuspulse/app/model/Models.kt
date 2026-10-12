package com.campuspulse.app.model

import androidx.compose.ui.graphics.Color
import com.campuspulse.app.ui.theme.CategoryAcademic
import com.campuspulse.app.ui.theme.CategoryAdvocacy
import com.campuspulse.app.ui.theme.CategoryCulture
import com.campuspulse.app.ui.theme.CategoryIdentity
import com.campuspulse.app.ui.theme.CategorySport

enum class Category(val label: String, val color: Color) {
    SPORT("Sport", CategorySport),
    CULTURE("Culture & Arts", CategoryCulture),
    ADVOCACY("Advocacy & Politics", CategoryAdvocacy),
    IDENTITY("Identity & Community", CategoryIdentity),
    ACADEMIC("Faith & Academic", CategoryAcademic)
}

data class InterestOption(
    val title: String,
    val subtitle: String
)

data class Club(
    val id: String,
    val name: String,
    val category: Category,
    val members: Int,
    val description: String,
    val hasOpenSlots: Boolean = true,
    val founded: String = "",
    val meets: String = "",
    val contact: String = "",
    val location: String = "Mbombela Campus"
)

data class EventItem(
    val id: String,
    val title: String,
    val category: Category,
    val hostClub: String,
    val schedule: String,
    val venue: String,
    val description: String,
    val rating: Double,
    val going: Int,
    val isFree: Boolean = true
) {
    /*val imageResId: Int*/
}

data class ClubLeader(
    val name: String,
    val role: String
)

data class ClubPost(
    val text: String,
    val meta: String
)

data class RequestedClub(
    val name: String,
    val category: Category,
    val submittedBy: String,
    val hasConstitution: Boolean,
    val hasSignatories: Boolean,
    val hasAdvisor: Boolean
)
