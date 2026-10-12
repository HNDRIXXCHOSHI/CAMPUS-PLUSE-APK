package com.campuspulse.app.data

import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

// ============================================================
// FIRESTORE HOME POST
// ============================================================

data class FirestorePost(
    val id: String = "",
    val clubId: String = "",
    val clubName: String = "",
    val clubShortName: String = "",
    val type: String = "",
    val text: String = "",
    val imageUrl: String = "",
    val videoUrl: String = "",
    val thumbnailUrl: String = "",
    val status: String = "",
    val isActive: Boolean = false,
    val createdAt: Timestamp? = null
)


// ============================================================
// FIRESTORE HOME EVENT
// ============================================================

data class FirestoreEvent(
    val id: String = "",
    val clubId: String = "",
    val clubName: String = "",
    val clubShortName: String = "",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val eventDate: Timestamp? = null,
    val eventEndDate: Timestamp? = null,
    val posterImageUrl: String = "",
    val status: String = "",
    val isActive: Boolean = false,
    val createdAt: Timestamp? = null,
    val history: String
)


// ============================================================
// FIRESTORE CLUB
// ============================================================

data class FirestoreClub(
    val id: String = "",
    val name: String = "",
    val shortName: String = "",
    val category: String = "",
    val description: String = "",
    val history: String = "",
    val location: String = "",
    val profileImageUrl: String = "",
    val bannerImageUrl: String = "",
    val status: String = "",
    val isActive: Boolean = false,
    val organisationType: String = "CLUB",
    val memberCount: Long? = null
)


// ============================================================
// FIRESTORE CLUB LEADER
// ============================================================

data class FirestoreClubLeader(
    val id: String = "",
    val name: String = "",
    val role: String = "",
    val photoUrl: String = "",
    val bio: String = "",
    val order: Long = 0L,
    val isActive: Boolean = false
)


// ============================================================
// HOME REPOSITORY
// ============================================================

class HomeRepository {

    private val firestore =
        FirebaseFirestore.getInstance()


    // ========================================================
    // LISTEN FOR PUBLISHED POSTS
    // ========================================================

    fun listenToPosts(
        onSuccess: (List<FirestorePost>) -> Unit,
        onError: (String) -> Unit,
        onCacheState: (Boolean) -> Unit = {}
    ): ListenerRegistration {

        return firestore
            .collection("posts")
            .whereEqualTo(
                "status",
                "PUBLISHED"
            )
            .whereEqualTo(
                "isActive",
                true
            )
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (snapshot != null) onCacheState(snapshot.metadata.isFromCache)

                if (error != null) {

                    onError(
                        error.message
                            ?: "Unable to load posts."
                    )

                    return@addSnapshotListener
                }

                if (snapshot == null) {

                    onSuccess(
                        emptyList()
                    )

                    return@addSnapshotListener
                }

                val posts =
                    snapshot.documents
                        .map { document ->

                            FirestorePost(
                                id = document.id,

                                clubId =
                                    document.getString(
                                        "clubId"
                                    ) ?: "",

                                clubName =
                                    document.getString(
                                        "clubName"
                                    ) ?: "",

                                clubShortName =
                                    document.getString(
                                        "clubShortName"
                                    ) ?: "",

                                type =
                                    document.getString(
                                        "type"
                                    ) ?: "",

                                text =
                                    document.getString(
                                        "text"
                                    ) ?: "",

                                imageUrl = if (document.getString("type").orEmpty().uppercase().contains("VIDEO")
                                    || document.getString("mediaType").orEmpty().uppercase() == "VIDEO") "" else {
                                    listOf("imageUrl", "postImageUrl", "photoUrl", "mediaUrl")
                                        .firstNotNullOfOrNull { key -> document.getString(key)?.takeIf { it.isNotBlank() } }.orEmpty()
                                },
                                videoUrl = document.getString("videoUrl")?.takeIf { it.isNotBlank() }
                                    ?: if (document.getString("type").orEmpty().uppercase().contains("VIDEO")
                                        || document.getString("mediaType").orEmpty().uppercase() == "VIDEO") {
                                        document.getString("mediaUrl")?.takeIf { it.isNotBlank() }
                                            ?: document.getString("imageUrl").orEmpty()
                                    } else "",
                                thumbnailUrl = document.getString("thumbnailUrl")?.takeIf { it.isNotBlank() }
                                    ?: document.getString("posterUrl").orEmpty(),

                                status =
                                    document.getString(
                                        "status"
                                    ) ?: "",

                                isActive =
                                    document.getBoolean(
                                        "isActive"
                                    ) ?: false,

                                createdAt =
                                    document.getTimestamp(
                                        "createdAt"
                                    )
                            )
                        }
                        .sortedByDescending { post ->

                            post.createdAt
                                ?.seconds
                                ?: 0L
                        }

                onSuccess(
                    posts
                )
            }
    }


    // ========================================================
    // LISTEN FOR PUBLISHED EVENTS
    // ========================================================

    fun listenToEvents(
        onSuccess: (List<FirestoreEvent>) -> Unit,
        onError: (String) -> Unit,
        onCacheState: (Boolean) -> Unit = {}
    ): ListenerRegistration {

        return firestore
            .collection("events")
            .whereEqualTo(
                "status",
                "PUBLISHED"
            )
            .whereEqualTo(
                "isActive",
                true
            )
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (snapshot != null) onCacheState(snapshot.metadata.isFromCache)

                if (error != null) {

                    onError(
                        error.message
                            ?: "Unable to load events."
                    )

                    return@addSnapshotListener
                }

                if (snapshot == null) {

                    onSuccess(
                        emptyList()
                    )

                    return@addSnapshotListener
                }

                val events =
                    snapshot.documents
                        .map { document ->

                            FirestoreEvent(
                                id = document.id,

                                clubId =
                                    document.getString(
                                        "clubId"
                                    ) ?: "",

                                clubName =
                                    document.getString(
                                        "clubName"
                                    ) ?: "",

                                clubShortName =
                                    document.getString(
                                        "clubShortName"
                                    ) ?: "",

                                title =
                                    document.getString(
                                        "title"
                                    ) ?: "",

                                description =
                                    document.getString(
                                        "description"
                                    ) ?: "",

                                history =
                                    document.getString(
                                        "history"
                                    ) ?: "",

                                location =
                                    document.getString(
                                        "location"
                                    ) ?: "",

                                eventDate =
                                    document.getTimestamp(
                                        "eventDate"
                                    ),

                                eventEndDate = document.getTimestamp("eventEndDate"),
                                posterImageUrl = document.getString("posterUrl")?.takeIf { it.isNotBlank() }
                                    ?: document.getString("imageUrl")?.takeIf { it.isNotBlank() }
                                    ?: if (document.getString("mediaType")?.uppercase() != "VIDEO") {
                                        document.getString("mediaUrl").orEmpty()
                                    } else "",

                                status =
                                    document.getString(
                                        "status"
                                    ) ?: "",

                                isActive =
                                    document.getBoolean(
                                        "isActive"
                                    ) ?: false,

                                createdAt =
                                    document.getTimestamp(
                                        "createdAt"
                                    )
                            )
                        }
                        .sortedBy { event ->

                            event.eventDate
                                ?.seconds
                                ?: Long.MAX_VALUE
                        }

                onSuccess(
                    events
                )
            }
    }


    // ========================================================
    // LISTEN FOR APPROVED CLUBS
    // ========================================================

    fun listenToClubs(
        onSuccess: (List<FirestoreClub>) -> Unit,
        onError: (String) -> Unit,
        onCacheState: (Boolean) -> Unit = {}
    ): ListenerRegistration {

        return firestore
            .collection("clubs")
            .whereEqualTo(
                "status",
                "APPROVED"
            )
            .whereEqualTo(
                "isActive",
                true
            )
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (snapshot != null) onCacheState(snapshot.metadata.isFromCache)

                if (error != null) {

                    onError(
                        error.message
                            ?: "Unable to load clubs."
                    )

                    return@addSnapshotListener
                }

                if (snapshot == null) {

                    onSuccess(
                        emptyList()
                    )

                    return@addSnapshotListener
                }

                val clubs =
                    snapshot.documents
                        .map { document ->

                            FirestoreClub(
                                id = document.id,
                                memberCount = document.getLong("memberCount"),
                                organisationType = (document.getString("organisationType")
                                    ?: document.getString("organizationType") ?: "CLUB").trim().uppercase(),

                                name =
                                    document.getString(
                                        "name"
                                    ) ?: "",

                                // Your current Coding Club document
                                // uses "Shortname" with a capital S.
                                // We support that now, while also
                                // supporting the preferred "shortName".
                                shortName =
                                    document.getString(
                                        "shortName"
                                    )
                                        ?: document.getString(
                                            "Shortname"
                                        )
                                        ?: "",

                                category =
                                    document.getString(
                                        "category"
                                    ) ?: "",

                                description =
                                    document.getString(
                                        "description"
                                    ) ?: "",

                                location =
                                    document.getString(
                                        "location"
                                    ) ?: "",

                                profileImageUrl =
                                    document.getString(
                                        "profileImageUrl"
                                    ) ?: "",

                                bannerImageUrl =
                                    document.getString(
                                        "bannerImageUrl"
                                    ) ?: "",

                                status =
                                    document.getString(
                                        "status"
                                    ) ?: "",

                                isActive =
                                    document.getBoolean(
                                        "isActive"
                                    ) ?: false
                            )
                        }
                        .sortedBy { club ->
                            club.name.lowercase()
                        }

                onSuccess(
                    clubs
                )
            }
    }

    // ========================================================
    // LISTEN FOR ACTIVE CLUB LEADERSHIP
    // ========================================================

    fun listenToClubLeadership(
        clubId: String,
        onSuccess: (List<FirestoreClubLeader>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration {

        return firestore
            .collection("clubs")
            .document(clubId)
            .collection("leadership")
            .whereEqualTo(
                "isActive",
                true
            )
            .addSnapshotListener { snapshot, error ->

                if (error != null) {

                    onError(
                        error.message
                            ?: "Unable to load club leadership."
                    )

                    return@addSnapshotListener
                }

                if (snapshot == null) {

                    onSuccess(
                        emptyList()
                    )

                    return@addSnapshotListener
                }

                val leaders =
                    snapshot.documents
                        .map { document ->

                            FirestoreClubLeader(
                                id = document.id,
                                photoUrl = document.getString("photoUrl").orEmpty(),
                                bio = document.getString("bio").orEmpty(),

                                name =
                                    document.getString(
                                        "name"
                                    ) ?: "",

                                role =
                                    document.getString(
                                        "role"
                                    ) ?: "",

                                order =
                                    document.getLong(
                                        "order"
                                    ) ?: 0L,

                                isActive =
                                    document.getBoolean(
                                        "isActive"
                                    ) ?: false
                            )
                        }
                        .sortedBy { leader ->
                            leader.order
                        }

                onSuccess(
                    leaders
                )
            }
    }

}