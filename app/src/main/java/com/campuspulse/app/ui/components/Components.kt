package com.campuspulse.app.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.campuspulse.app.model.Category
import com.campuspulse.app.model.Club
import com.campuspulse.app.model.EventItem

// =============================================================================
// SHARE EVENT
// =============================================================================

fun shareEvent(
    context: Context,
    event: EventItem,
    rsvpUrl: String = "https://campuspulse.app/rsvp"
) {

    val message = buildString {

        append(event.title)
        append(" — ")
        append(event.schedule)
        append(" at ")
        append(event.venue)
        append(". Hosted by ${event.hostClub}.\n\n")
        append(event.description)
        append("\n\nRSVP: $rsvpUrl")
    }

    val sendIntent = Intent(
        Intent.ACTION_SEND
    ).apply {

        type = "text/plain"

        putExtra(
            Intent.EXTRA_SUBJECT,
            event.title
        )

        putExtra(
            Intent.EXTRA_TEXT,
            message
        )
    }

    context.startActivity(
        Intent.createChooser(
            sendIntent,
            "Share \"${event.title}\" via"
        )
    )
}

// =============================================================================
// SHARE CLUB
// =============================================================================

fun shareClub(
    context: Context,
    club: Club,
    profileUrl: String = "https://campuspulse.app/clubs"
) {

    val message = buildString {

        append(club.name)
        append(" — ")
        append(club.category.label)
        append(" · ${club.members} members\n\n")
        append(club.description)
        append("\n\nFind them on Campus Pulse: $profileUrl/${club.id}")
    }

    val sendIntent = Intent(
        Intent.ACTION_SEND
    ).apply {

        type = "text/plain"

        putExtra(
            Intent.EXTRA_SUBJECT,
            club.name
        )

        putExtra(
            Intent.EXTRA_TEXT,
            message
        )
    }

    context.startActivity(
        Intent.createChooser(
            sendIntent,
            "Share \"${club.name}\" via"
        )
    )
}

// =============================================================================
// COLORS
// =============================================================================

private val NavBackground = Color(0xFF00172E)
private val NavBorder = Color(0xFF14334F)

private val NavActive = Color(0xFFF7941D)
private val NavInactive = Color.White

private val Ink = Color(0xFF121212)
private val Muted = Color(0xFF8E8E93)
private val Line = Color(0xFF2C2C2E)

// =============================================================================
// NAVIGATION DESTINATIONS
// =============================================================================

enum class NavDestination(
    val label: String
) {

    HOME("Home"),

    SEARCH("Search"),

    MY_CLUBS("My Clubs"),

    NOTIFICATIONS("Notifications"),

    ACCOUNT("Profile")
}

// =============================================================================
// CATEGORY DOT
// =============================================================================

@Composable
fun CategoryDot(
    color: Color,
    size: Int = 10
) {

    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color)
    )
}

// =============================================================================
// CATEGORY CHIP
// =============================================================================

@Composable
fun CategoryChip(
    category: Category,
    filled: Boolean = true
) {

    Surface(
        color = if (filled) {
            category.color
        } else {
            Color.White
        },
        contentColor = if (filled) {
            Color.White
        } else {
            category.color
        },
        shape = RoundedCornerShape(100),
        border = if (!filled) {
            androidx.compose.foundation.BorderStroke(
                1.2.dp,
                category.color
            )
        } else {
            null
        }
    ) {

        Text(
            text = category.label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 4.dp
            )
        )
    }
}

// =============================================================================
// PRIMARY BUTTON
// =============================================================================

@Composable
fun PrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(100),
        modifier = modifier.fillMaxWidth()
    ) {

        Box(
            modifier = Modifier.padding(
                vertical = 14.dp
            ),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = text,
                color = Ink,
                fontWeight = FontWeight.Bold,
                fontSize = MaterialTheme.typography.labelLarge.fontSize
            )
        }
    }
}

// =============================================================================
// GHOST BUTTON
// =============================================================================

@Composable
fun GhostButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    Surface(
        onClick = onClick,
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(
            1.4.dp,
            Ink
        ),
        shape = RoundedCornerShape(100),
        modifier = modifier.fillMaxWidth()
    ) {

        Box(
            modifier = Modifier.padding(
                vertical = 14.dp
            ),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = text,
                color = Ink,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// =============================================================================
// FIXED BOTTOM NAVIGATION
//
// Instagram / TikTok style:
// - Full width
// - Attached to bottom
// - No floating pill
// - No rounded outer container
// - Five equal navigation items
// =============================================================================

@Composable
fun CampusPulseBottomNav(
    current: NavDestination,
    onSelect: (NavDestination) -> Unit
) {

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        color = NavBackground,
        shadowElevation = 0.dp
    ) {

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            // -----------------------------------------------------------------
            // TOP BORDER
            // -----------------------------------------------------------------

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(NavBorder)
            )

            // -----------------------------------------------------------------
            // NAV ITEMS
            // -----------------------------------------------------------------

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .selectableGroup(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // HOME

                BottomNavItem(
                    destination = NavDestination.HOME,
                    selectedIcon = Icons.Outlined.Home,
                    unselectedIcon = Icons.Outlined.Home,
                    current = current,
                    onSelect = onSelect,
                    modifier = Modifier.weight(1f)
                )

                // SEARCH

                BottomNavItem(
                    destination = NavDestination.SEARCH,
                    selectedIcon = Icons.Outlined.Search,
                    unselectedIcon = Icons.Outlined.Search,
                    current = current,
                    onSelect = onSelect,
                    modifier = Modifier.weight(1f)
                )

                // MY CLUBS
                //
                // Consistent outline club icon; selection changes only the colour.

                BottomNavItem(
                    destination = NavDestination.MY_CLUBS,
                    selectedIcon = Icons.Outlined.Groups,
                    unselectedIcon = Icons.Outlined.Groups,
                    current = current,
                    onSelect = onSelect,
                    modifier = Modifier.weight(1f)
                )

                // NOTIFICATIONS

                BottomNavItem(
                    destination = NavDestination.NOTIFICATIONS,
                    selectedIcon = Icons.Outlined.Notifications,
                    unselectedIcon = Icons.Outlined.Notifications,
                    current = current,
                    onSelect = onSelect,
                    modifier = Modifier.weight(1f)
                )

                // PROFILE

                BottomNavItem(
                    destination = NavDestination.ACCOUNT,
                    selectedIcon = Icons.Outlined.PersonOutline,
                    unselectedIcon = Icons.Outlined.PersonOutline,
                    current = current,
                    onSelect = onSelect,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// =============================================================================
// NAVIGATION ITEM
// =============================================================================

@Composable
private fun BottomNavItem(
    destination: NavDestination,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    current: NavDestination,
    onSelect: (NavDestination) -> Unit,
    modifier: Modifier = Modifier
) {

    val selected = destination == current

    val iconColor by animateColorAsState(
        targetValue = if (selected) {
            NavActive
        } else {
            NavInactive
        },
        label = "BottomNavIconColor"
    )

    Box(
        modifier = modifier
            .height(71.dp)
            .selectable(
                selected = selected,
                role = Role.Tab,
                onClick = {
                    if (!selected) onSelect(destination)
                }
            ),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Icon(
                imageVector = if (selected) {
                    selectedIcon
                } else {
                    unselectedIcon
                },
                contentDescription = destination.label,
                tint = iconColor,
                modifier = Modifier.size(25.dp)
            )


        }
    }
}

// =============================================================================
// MOCK FIELD
// =============================================================================

@Composable
fun MockField(
    hint: String,
    modifier: Modifier = Modifier
) {

    Surface(
        color = Color(0xFFFBFAF6),
        border = androidx.compose.foundation.BorderStroke(
            1.4.dp,
            Line
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {

        Text(
            text = hint,
            color = Muted,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 13.dp
            )
        )
    }
}

// =============================================================================
// SECTION LABEL
// =============================================================================

@Composable
fun SectionLabel(
    text: String
) {

    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = Muted
    )
}