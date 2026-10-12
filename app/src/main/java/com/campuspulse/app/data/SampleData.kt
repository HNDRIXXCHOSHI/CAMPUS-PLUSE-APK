package com.campuspulse.app.data

import com.campuspulse.app.model.Category
import com.campuspulse.app.model.Club
import com.campuspulse.app.model.ClubLeader
import com.campuspulse.app.model.ClubPost
import com.campuspulse.app.model.EventItem
import com.campuspulse.app.model.InterestOption
import com.campuspulse.app.model.RequestedClub

object SampleData {

    val interestOptions = listOf(
        InterestOption("Sport & fitness", "Tennis, football, athletics"),
        InterestOption("Culture & arts", "Choirs, dance, heritage"),
        InterestOption("Advocacy", "SASCO, student gov"),
        InterestOption("Identity", "LGBTQ+, faith, home societies"),
        InterestOption("Academic", "Debate, subject socs"),
        InterestOption("Media & tech", "Radio, dev, design")
    )

    val clubs = listOf(
        Club(
            id = "tennis",
            name = "Indoor Tennis Club",
            category = Category.SPORT,
            members = 84,
            description = "Casual and competitive tennis for every skill level."
        ),
        Club(
            id = "zulu-pride",
            name = "Zulu Pride Cultural Society",
            category = Category.CULTURE,
            members = 212,
            description = "Heritage, choir & dance — celebrating Zulu culture on campus since 2014.",
            founded = "2014",
            meets = "Wed & Thu, Music Room",
            contact = "@zulupride_uni"
        ),
        Club(
            id = "sasco",
            name = "SASCO Branch",
            category = Category.ADVOCACY,
            members = 340,
            description = "Student political organisation representing campus interests.",
            hasOpenSlots = false
        ),
        Club(
            id = "rainbow",
            name = "Rainbow Society",
            category = Category.IDENTITY,
            members = 96,
            description = "A safe, welcoming space for LGBTQ+ students and allies."
        ),
        Club(
            id = "debate",
            name = "Debate & Public Speaking",
            category = Category.ACADEMIC,
            members = 58,
            description = "Weekly debates, adjudicated rounds, and public speaking coaching."
        ),
        Club(
            id = "football",
            name = "Football Club",
            category = Category.SPORT,
            members = 401,
            description = "The university's largest sporting society.",
            hasOpenSlots = false
        )
    )

    val events = listOf(
        EventItem(
            id = "heritage-night",
            title = "Zulu Pride Heritage Night",
            category = Category.CULTURE,
            hostClub = "Zulu Pride Cultural Society",
            schedule = "Wed · 18:00",
            venue = "Great Hall",
            description = "Choir, dance & food, open to everyone.",
            rating = 4.8,
            going = 142
        ),
        EventItem(
            id = "tennis-social",
            title = "Indoor Tennis Open Social",
            category = Category.SPORT,
            hostClub = "Indoor Tennis Club",
            schedule = "Thu · 16:30",
            venue = "Sports Centre, Court 2",
            description = "Racquets provided for beginners. Come through even if you've never played — this is the one most people at this university find out about too late.",
            rating = 4.6,
            going = 58
        ),
        EventItem(
            id = "debate-open-mic",
            title = "Debate Society Open Mic",
            category = Category.ACADEMIC,
            hostClub = "Debate & Public Speaking",
            schedule = "Mon · 17:00",
            venue = "Humanities Building, Room 12",
            description = "No experience needed — grab the mic and argue any side of a topic drawn on the night.",
            rating = 4.5,
            going = 34
        ),
        EventItem(
            id = "rainbow-coffee-hour",
            title = "Rainbow Society Coffee Hour",
            category = Category.IDENTITY,
            hostClub = "Rainbow Society",
            schedule = "Tue · 15:00",
            venue = "Student Centre Courtyard",
            description = "A relaxed weekly meetup — come chat, meet new members, no agenda.",
            rating = 4.9,
            going = 41
        ),
        EventItem(
            id = "sasco-town-hall",
            title = "SASCO Town Hall",
            category = Category.ADVOCACY,
            hostClub = "SASCO Branch",
            schedule = "Fri · 13:00",
            venue = "Great Hall",
            description = "Open forum on campus fees and residence allocation — bring your questions.",
            rating = 4.2,
            going = 210
        )
    )

    val clubLeaders = listOf(
        ClubLeader("Nomvula", "Chairperson"),
        ClubLeader("Sipho", "Treasurer"),
        ClubLeader("Ayanda", "Secretary")
    )

    val clubPosts = listOf(
        ClubPost("Rehearsals moved to Thu 17:00", "Posted by the committee · 2 days ago"),
        ClubPost("Congrats to our dance team 🎉", "Posted by Nomvula · 5 days ago")
    )

    val myClubs = listOf(clubs[0], clubs[3])

    val pendingRequests = listOf(
        RequestedClub(
            name = "Chess Club",
            category = Category.ACADEMIC,
            submittedBy = "T. Nkosi, 2 days ago",
            hasConstitution = true,
            hasSignatories = true,
            hasAdvisor = true
        ),
        RequestedClub(
            name = "K-Pop Dance Crew",
            category = Category.CULTURE,
            submittedBy = "M. van Wyk, today",
            hasConstitution = true,
            hasSignatories = true,
            hasAdvisor = false
        )
    )
}
