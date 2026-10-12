@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.campuspulse.app.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.SubcomposeAsyncImage
import com.campuspulse.app.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campuspulse.app.data.FirestoreClub
import com.campuspulse.app.data.HomeRepository
import com.campuspulse.app.model.Category
import com.campuspulse.app.model.Club
import com.campuspulse.app.ui.components.NavDestination
import com.campuspulse.app.ui.theme.CampusPulseTheme


// =============================================================================
// CAMPUS PULSE / UMP DARK MODE COLOURS
//
// SAME RULES AS HOME + MY CLUBS:
//
// PAGE              = #021D38
// CARDS / CONTROLS  = #05294B
// SELECTED          = #0D426F
// BORDER            = #315574
// PRIMARY TEXT      = WHITE
// SECONDARY TEXT    = #B9C9D8
// ORANGE ACCENT     = #F7941D
// RED ACCENT        = #E11111
// BLUE ACCENT       = #38A9E0
// =============================================================================

private val DarkBackground = Color(0xFF021D38)

private val CardBackground = Color(0xFF05294B)

private val TextWhite = Color(0xFFFFFFFF)
private val TextMuted = Color(0xFFB9C9D8)

private val ControlBackground = Color(0xFF05294B)
private val ControlSelected = Color(0xFF0D426F)

private val BorderColor = Color(0xFF315574)

private val UmpNavy = Color(0xFF00254A)
private val UmpNavyLight = Color(0xFF0D426F)

private val AccentOrange = Color(0xFFF7941D)
private val AccentRed = Color(0xFFE11111)
private val AccentBlue = Color(0xFF38A9E0)

private val SuccessDotColor = Color(0xFF4CAF50)


// =============================================================================
// VIEW TYPE
// =============================================================================

enum class ViewType {
    GRID,
    LIST
}


// =============================================================================
// DIRECTORY SCREEN
// =============================================================================

@Composable
fun DirectoryScreen(
    onOpenClub: (Club) -> Unit,
    onNavigate: (NavDestination) -> Unit,
    scrollToTopSignal: Int = 0
) {

    // Kept because your navigation signature already expects it.
    // Navigation remains controlled by your existing app navigation.
    @Suppress("UNUSED_VARIABLE")
    val navigate = onNavigate

    var selectedTab by remember {
        mutableIntStateOf(0)
    }

    var currentViewType by remember {
        mutableStateOf(ViewType.GRID)
    }

    var selectedCategory by remember {
        mutableStateOf("All")
    }

    var filterMenuExpanded by remember {
        mutableStateOf(false)
    }

    var searchVisible by remember {
        mutableStateOf(false)
    }

    var searchText by remember {
        mutableStateOf("")
    }

    val focusRequester = remember {
        FocusRequester()
    }

    val gridState = rememberLazyGridState()
    val listState = rememberLazyListState()

    LaunchedEffect(scrollToTopSignal) {
        if (scrollToTopSignal > 0) {
            gridState.animateScrollToItem(0)
            listState.animateScrollToItem(0)
        }
    }

    val focusManager = LocalFocusManager.current

    val categories = listOf(
        "All",
        "Sport",
        "Culture",
        "Advocacy",
        "Identity",
        "Religious",
        "Academic",
        "Other"
    )


    // =========================================================================
    // FIRESTORE CLUBS + FILTER + SEARCH
    // =========================================================================

    val preview = LocalInspectionMode.current
    val repository = remember(preview) { if (preview) null else HomeRepository() }
    var retry by remember { mutableIntStateOf(0) }
    var organisationTypes by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var rawCategories by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    var clubs by remember {
        mutableStateOf<List<Club>>(emptyList())
    }

    var clubPhotoUrls by remember {
        mutableStateOf<Map<String, String>>(emptyMap())
    }

    var clubsLoading by remember {
        mutableStateOf(true)
    }

    var clubsError by remember {
        mutableStateOf<String?>(null)
    }

    DisposableEffect(repository, retry) {
        if (repository == null) { clubsLoading = false; return@DisposableEffect onDispose {} }
        clubsLoading = true
        clubsError = null
        val registration = repository.listenToClubs(
            onSuccess = { firestoreClubs ->
                clubs = firestoreClubs.mapNotNull { it.toDirectoryClubOrNull() }.sortedBy { it.name.lowercase() }
                organisationTypes = firestoreClubs.associate { it.id to it.organisationType }
                rawCategories = firestoreClubs.associate { it.id to directoryCategory(it.category) }
                clubPhotoUrls = firestoreClubs.associate { it.id to it.profileImageUrl.trim() }
                clubsLoading = false
                clubsError = null
            },
            onError = { message ->
                clubsLoading = false
                clubsError = message
            }
        )

        onDispose {
            registration.remove()
        }
    }

    val filteredClubs = remember(clubs, rawCategories, organisationTypes, selectedTab, selectedCategory, searchText) {
        val query = searchText.trim()
        clubs.filter { club ->
            val category = rawCategories[club.id] ?: "Other"
            val categoryMatches = selectedCategory == "All" || category == selectedCategory
            val searchMatches = query.isEmpty() || club.name.contains(query, true)
                    || category.contains(query, true) || club.category.label.contains(query, true)
            val society = organisationTypes[club.id] in listOf("SOCIETY", "SOCIETIES")
            val typeMatches = if (selectedTab == 1) society else !society
            typeMatches && categoryMatches && searchMatches
        }
    }

    // =========================================================================
    // AUTO FOCUS SEARCH
    // =========================================================================

    LaunchedEffect(searchVisible) {

        if (searchVisible) {
            focusRequester.requestFocus()
        }
    }


    // =========================================================================
    // SCREEN
    // =========================================================================

    Scaffold(
        containerColor = DarkBackground
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(padding)
        ) {


            // =================================================================
            // PAGE HEADER
            // =================================================================

            Text(
                text = "Search",
                color = TextWhite,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 22.dp,
                    bottom = 18.dp
                )
            )


            // =================================================================
            // CLUBS / SOCIETIES + CONTROLS
            // =================================================================

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        bottom = 16.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {


                // =============================================================
                // CLUBS / SOCIETIES
                // =============================================================

                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "clubs",
                        fontSize = 16.sp,
                        fontWeight =
                            if (selectedTab == 0) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Normal
                            },
                        color =
                            if (selectedTab == 0) {
                                TextWhite
                            } else {
                                TextMuted
                            },
                        modifier = Modifier.clickable {
                            selectedTab = 0
                        }
                    )

                    Text(
                        text = "  •  ",
                        fontSize = 16.sp,
                        color = TextMuted
                    )

                    Text(
                        text = "societies",
                        fontSize = 16.sp,
                        fontWeight =
                            if (selectedTab == 1) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Normal
                            },
                        color =
                            if (selectedTab == 1) {
                                TextWhite
                            } else {
                                TextMuted
                            },
                        modifier = Modifier.clickable {
                            selectedTab = 1
                        }
                    )
                }


                // =============================================================
                // SEARCH BUTTON
                // =============================================================

                CircleControlButton(
                    selected = searchVisible,
                    onClick = {

                        if (searchVisible) {

                            searchVisible = false
                            searchText = ""

                            focusManager.clearFocus()

                        } else {

                            searchVisible = true
                        }
                    }
                ) {

                    Icon(
                        imageVector =
                            if (searchVisible) {
                                Icons.Filled.Close
                            } else {
                                Icons.Filled.Search
                            },
                        contentDescription =
                            if (searchVisible) {
                                "Close search"
                            } else {
                                "Search"
                            },
                        tint = TextWhite,
                        modifier = Modifier.size(19.dp)
                    )
                }


                Spacer(
                    modifier = Modifier.width(8.dp)
                )


                // =============================================================
                // FILTER + GRID/LIST PILL
                // =============================================================

                Surface(
                    color = ControlBackground,
                    shape = RoundedCornerShape(100.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = BorderColor
                    )
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {


                        // =====================================================
                        // FILTER
                        // =====================================================

                        Box {

                            Box(
                                modifier = Modifier
                                    .height(42.dp)
                                    .clickable(
                                        interactionSource = remember {
                                            MutableInteractionSource()
                                        },
                                        indication = null
                                    ) {
                                        filterMenuExpanded = true
                                    }
                                    .padding(
                                        start = 13.dp,
                                        end = 9.dp
                                    ),
                                contentAlignment = Alignment.Center
                            ) {

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Text(
                                        text =
                                            if (selectedCategory == "All") {
                                                "Filter"
                                            } else {
                                                selectedCategory
                                            },
                                        color =
                                            if (selectedCategory == "All") {
                                                TextMuted
                                            } else {
                                                AccentOrange
                                            },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Spacer(
                                        modifier = Modifier.width(2.dp)
                                    )

                                    Icon(
                                        imageVector = Icons.Filled.ArrowDropDown,
                                        contentDescription = "Filter category",
                                        tint =
                                            if (selectedCategory == "All") {
                                                TextMuted
                                            } else {
                                                AccentOrange
                                            },
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }


                            // =================================================
                            // FILTER MENU
                            // =================================================

                            DropdownMenu(
                                expanded = filterMenuExpanded,
                                onDismissRequest = {
                                    filterMenuExpanded = false
                                },
                                modifier = Modifier
                                    .background(CardBackground)
                            ) {

                                categories.forEach { category ->

                                    DropdownMenuItem(
                                        text = {

                                            Text(
                                                text = category,
                                                color =
                                                    if (
                                                        category ==
                                                        selectedCategory
                                                    ) {
                                                        AccentOrange
                                                    } else {
                                                        TextWhite
                                                    },
                                                fontWeight =
                                                    if (
                                                        category ==
                                                        selectedCategory
                                                    ) {
                                                        FontWeight.Bold
                                                    } else {
                                                        FontWeight.Normal
                                                    }
                                            )
                                        },
                                        onClick = {

                                            selectedCategory = category
                                            filterMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }


                        // =====================================================
                        // DIVIDER
                        // =====================================================

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(22.dp)
                                .background(BorderColor)
                        )


                        // =====================================================
                        // GRID / LIST SWITCH
                        // =====================================================

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clickable(
                                    interactionSource = remember {
                                        MutableInteractionSource()
                                    },
                                    indication = null
                                ) {

                                    currentViewType =
                                        if (
                                            currentViewType ==
                                            ViewType.GRID
                                        ) {
                                            ViewType.LIST
                                        } else {
                                            ViewType.GRID
                                        }
                                },
                            contentAlignment = Alignment.Center
                        ) {

                            Icon(
                                imageVector =
                                    if (
                                        currentViewType ==
                                        ViewType.GRID
                                    ) {
                                        Icons.Filled.List
                                    } else {
                                        Icons.Filled.GridView
                                    },
                                contentDescription = "Change view",
                                tint = TextWhite,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }


            // =================================================================
            // ANIMATED SEARCH FIELD
            // =================================================================

            AnimatedVisibility(
                visible = searchVisible,
                enter =
                    fadeIn() +
                            slideInVertically(
                                initialOffsetY = {
                                    -it / 2
                                }
                            ),
                exit =
                    fadeOut() +
                            slideOutVertically(
                                targetOffsetY = {
                                    -it / 2
                                }
                            )
            ) {

                DirectorySearchField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                    },
                    focusRequester = focusRequester,
                    onClear = {
                        searchText = ""
                    }
                )
            }


            // =================================================================
            // ACTIVE FILTER / SEARCH INFORMATION
            // =================================================================

            if (
                selectedCategory != "All" ||
                searchText.isNotBlank()
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 20.dp,
                            end = 20.dp,
                            top = 2.dp,
                            bottom = 10.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text =
                            if (filteredClubs.size == 1) {
                                "1 result"
                            } else {
                                "${filteredClubs.size} results"
                            },
                        color = TextMuted,
                        fontSize = 11.sp
                    )

                    Spacer(
                        modifier = Modifier.weight(1f)
                    )

                    if (
                        selectedCategory != "All"
                    ) {

                        Text(
                            text = "Clear filter",
                            color = AccentOrange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable {

                                selectedCategory = "All"
                            }
                        )
                    }
                }
            }


            // =================================================================
            // DIVIDER
            // =================================================================

            HorizontalDivider(
                color = BorderColor,
                thickness = 1.dp
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )


            // =================================================================
            // RESULTS
            // =================================================================

            if (clubsLoading) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Loading clubs...",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                }

            } else if (clubsError != null) {
                Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Unable to load clubs. Check your connection and try again.", color = TextMuted,
                        textAlign = TextAlign.Center)
                    TextButton(onClick = { retry++ }) { Text("Retry", color = AccentOrange) }
                }

            } else if (filteredClubs.isEmpty()) {

                EmptyDirectoryState(
                    searchText = searchText,
                    category = selectedCategory
                )

            } else {


                // =============================================================
                // GRID VIEW
                // =============================================================

                if (
                    currentViewType ==
                    ViewType.GRID
                ) {

                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        ),
                        horizontalArrangement =
                            Arrangement.spacedBy(14.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(20.dp)
                    ) {

                        items(
                            items = filteredClubs,
                            key = {
                                it.id
                            }
                        ) { club ->

                            ClubSquareGridItem(
                                club = club,
                                profileImageUrl = clubPhotoUrls[club.id].orEmpty(),
                                categoryLabel = rawCategories[club.id] ?: "Other",
                                onClick = {
                                    onOpenClub(club)
                                }
                            )
                        }
                    }

                } else {


                    // =========================================================
                    // LIST VIEW
                    // =========================================================

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            horizontal = 20.dp,
                            vertical = 8.dp
                        ),
                        verticalArrangement =
                            Arrangement.spacedBy(10.dp)
                    ) {

                        items(
                            items = filteredClubs,
                            key = {
                                it.id
                            }
                        ) { club ->

                            ClubListItem(
                                club = club,
                                profileImageUrl = clubPhotoUrls[club.id].orEmpty(),
                                categoryLabel = rawCategories[club.id] ?: "Other",
                                onClick = {
                                    onOpenClub(club)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


// =============================================================================
// SEARCH FIELD
// =============================================================================

@Composable
private fun DirectorySearchField(
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester,
    onClear: () -> Unit
) {

    val searchFocus = LocalFocusManager.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 20.dp,
                end = 20.dp,
                bottom = 14.dp
            )
    ) {

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = ControlBackground,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(
                width = 1.dp,
                color = BorderColor
            )
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(
                        horizontal = 15.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(19.dp)
                )

                Spacer(
                    modifier = Modifier.width(11.dp)
                )

                Box(
                    modifier = Modifier.weight(1f)
                ) {

                    if (value.isEmpty()) {

                        Text(
                            text = "Find clubs and societies...",
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                    }

                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = true,
                        textStyle = TextStyle(
                            color = TextWhite,
                            fontSize = 14.sp
                        ),

                        // Orange cursor to match Campus Pulse accent
                        cursorBrush = SolidColor(
                            AccentOrange
                        ),

                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                searchFocus.clearFocus()
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(
                                focusRequester
                            )
                    )
                }


                // =============================================================
                // CLEAR SEARCH
                // =============================================================

                if (value.isNotEmpty()) {

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                ControlSelected
                            )
                            .clickable(
                                onClick = onClear
                            ),
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear search",
                            tint = TextWhite,
                            modifier = Modifier.size(
                                14.dp
                            )
                        )
                    }
                }
            }
        }
    }
}


// =============================================================================
// CIRCLE CONTROL BUTTON
// =============================================================================

@Composable
private fun CircleControlButton(
    selected: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {

    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(
                if (selected) {
                    ControlSelected
                } else {
                    ControlBackground
                }
            )
            .border(
                width = 1.dp,
                color =
                    if (selected) {
                        AccentOrange.copy(
                            alpha = 0.60f
                        )
                    } else {
                        BorderColor
                    },
                shape = CircleShape
            )
            .clickable(
                interactionSource = remember {
                    MutableInteractionSource()
                },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {

        content()
    }
}


// =============================================================================
// GRID CLUB
// =============================================================================

@Composable
private fun ClubSquareGridItem(
    club: Club,
    profileImageUrl: String,
    categoryLabel: String,
    onClick: () -> Unit
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick
            )
    ) {


        // =====================================================================
        // CLUB TILE
        //
        // Category colour is intentionally retained.
        // It identifies each club/category while the surrounding UI follows
        // the UMP dark-mode colour system.
        // =====================================================================

        Box(
            modifier = Modifier
                .size(92.dp)
                .clip(
                    RoundedCornerShape(22.dp)
                )
                .background(
                    club.category.color
                )
                .border(
                    width = 1.dp,
                    color = BorderColor,
                    shape = RoundedCornerShape(
                        22.dp
                    )
                ),
            contentAlignment = Alignment.Center
        ) {

            DirectoryClubPhoto(club, profileImageUrl)
        }


        Spacer(
            modifier = Modifier.height(8.dp)
        )


        Text(
            text = club.name,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = TextWhite,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )


        Spacer(
            modifier = Modifier.height(2.dp)
        )


        Text(
            text = categoryLabel,
            fontSize = 11.sp,
            color = TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}


// =============================================================================
// LIST CLUB
// =============================================================================

@Composable
private fun ClubListItem(
    club: Club,
    profileImageUrl: String,
    categoryLabel: String,
    onClick: () -> Unit
) {

    Surface(
        color = CardBackground,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = BorderColor
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick
            )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {


            // =================================================================
            // CLUB ICON
            // =================================================================

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(
                        RoundedCornerShape(15.dp)
                    )
                    .background(
                        club.category.color
                    ),
                contentAlignment = Alignment.Center
            ) {

                DirectoryClubPhoto(club, profileImageUrl)
            }


            // =================================================================
            // CLUB DETAILS
            // =================================================================

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = 12.dp
                    )
            ) {

                Text(
                    text = club.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text = categoryLabel,
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }


            // =================================================================
            // OPEN SLOTS INDICATOR
            // =================================================================

            if (club.hasOpenSlots) {

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            SuccessDotColor
                        )
                )
            }
        }
    }
}


// =============================================================================
// EMPTY STATE
// =============================================================================

@Composable
private fun EmptyDirectoryState(
    searchText: String,
    category: String
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 80.dp,
                start = 30.dp,
                end = 30.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        // =====================================================================
        // EMPTY ICON
        // =====================================================================

        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(
                    ControlBackground
                )
                .border(
                    width = 1.dp,
                    color = BorderColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {

            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = AccentBlue,
                modifier = Modifier.size(
                    27.dp
                )
            )
        }


        Spacer(
            modifier = Modifier.height(15.dp)
        )


        // =====================================================================
        // EMPTY TITLE
        // =====================================================================

        Text(
            text = "No clubs found",
            color = TextWhite,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )


        Spacer(
            modifier = Modifier.height(6.dp)
        )


        // =====================================================================
        // EMPTY DESCRIPTION
        // =====================================================================

        Text(
            text =
                when {

                    searchText.isNotBlank() &&
                            category != "All" -> {

                        "Try another search or category."
                    }

                    searchText.isNotBlank() -> {

                        "Try searching for another club."
                    }

                    else -> {

                        "No clubs are available in this category."
                    }
                },
            color = TextMuted,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}


// =============================================================================
// CLUB INITIALS
//
// Removed the old "CP" fallback.
// =============================================================================

// =============================================================================
// PREVIEW
// =============================================================================


// =============================================================================
// FIRESTORE -> EXISTING DIRECTORY UI MODEL
// =============================================================================

private fun FirestoreClub.toDirectoryClubOrNull(): Club? {
    val mappedCategory = when (category.trim().lowercase()) {
        "sport", "sports" -> Category.SPORT
        "culture", "culture & arts", "arts" -> Category.CULTURE
        "advocacy", "advocacy & politics", "politics" -> Category.ADVOCACY
        "identity", "identity & community", "community" -> Category.IDENTITY
        "religious", "religion", "faith", "academic", "faith & academic" -> Category.ACADEMIC
        else -> Category.ACADEMIC
    }

    return Club(
        id = id,
        name = name,
        category = mappedCategory,
        members = 0,
        description = description,
        location = location.ifBlank { "Mbombela Campus" }
    )
}

@Preview(
    name = "Directory - UMP Dark Mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
private fun DirectoryScreenPreview() {

    CampusPulseTheme {

        DirectoryScreen(
            onOpenClub = {},
            onNavigate = {}
        )
    }
}

@Composable
private fun DirectoryClubPhoto(club: Club, profileImageUrl: String) {
    if (profileImageUrl.isBlank()) {
        DirectoryClubPhotoFallback(club)
    } else {
        SubcomposeAsyncImage(
            model = profileImageUrl.trim(),
            contentDescription = "${club.name} profile photo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            loading = {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = TextWhite,
                        strokeWidth = 2.dp
                    )
                }
            },
            error = { DirectoryClubPhotoFallback(club) }
        )
    }
}

@Composable
private fun DirectoryClubPhotoFallback(club: Club) {
    if (club.id.equals("Coding_club", ignoreCase = true) ||
        club.name.trim().equals("Coding Club", ignoreCase = true)
    ) {
        Image(
            painter = painterResource(R.drawable.code),
            contentDescription = "${club.name} profile photo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Groups,
                contentDescription = "Club profile photo unavailable",
                tint = TextWhite,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

private fun directoryCategory(raw: String): String = when (raw.trim().lowercase()) {
    "sport", "sports" -> "Sport"
    "culture", "culture & arts", "arts" -> "Culture"
    "advocacy", "advocacy & politics", "politics" -> "Advocacy"
    "identity", "identity & community", "community" -> "Identity"
    "religious", "religion", "faith" -> "Religious"
    "academic", "faith & academic" -> "Academic"
    else -> "Other"
}
