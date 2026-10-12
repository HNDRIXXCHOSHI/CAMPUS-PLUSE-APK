package com.campuspulse.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campuspulse.app.R


// =============================================================
// CAMPUS PULSE COLOUR SYSTEM
// =============================================================

private val CampusNavy =
    Color(0xFF0B3768)

private val DeepNavy =
    Color(0xFF061D38)

private val CardNavy =
    Color(0xFF102F50)

private val CardNavySelected =
    Color(0xFF123B63)

private val AccentOrange =
    Color(0xFFFF9800)

private val TextPrimary =
    Color.White

private val TextSecondary =
    Color(0xFFD4DCE5)

private val BorderColor =
    Color.White.copy(alpha = 0.16f)

private val CornerShape: CornerBasedShape =
    RoundedCornerShape(16.dp)


// =============================================================
// MODELS
// =============================================================

data class InterestCategory(
    val sectionTitle: String,
    val items: List<InterestChipData>
)


data class InterestChipData(
    val id: String,
    val title: String,
    val icon: ImageVector
)


// =============================================================
// INTERESTS SCREEN
// =============================================================

@Composable
fun InterestsScreen(
    onContinue: (List<String>) -> Unit = {}
) {

    // =========================================================
    // INTEREST CATEGORIES
    // =========================================================

    val categories =
        remember {

            listOf(

                InterestCategory(

                    sectionTitle =
                        "Academics & Innovation",

                    items =
                        listOf(

                            InterestChipData(
                                "1",
                                "Coding & Tech",
                                Icons.Default.Code
                            ),

                            InterestChipData(
                                "2",
                                "Book Club",
                                Icons.Default.Book
                            ),

                            InterestChipData(
                                "3",
                                "Philosophy",
                                Icons.Default.Psychology
                            ),

                            InterestChipData(
                                "4",
                                "Debate & Law",
                                Icons.Default.Gavel
                            )
                        )
                ),


                InterestCategory(

                    sectionTitle =
                        "Campus Life & Community",

                    items =
                        listOf(

                            InterestChipData(
                                "5",
                                "Awareness",
                                Icons.Default.Public
                            ),

                            InterestChipData(
                                "6",
                                "Sports & Fitness",
                                Icons.Default.SportsSoccer
                            ),

                            InterestChipData(
                                "7",
                                "Arts & Culture",
                                Icons.Default.Palette
                            ),

                            InterestChipData(
                                "8",
                                "Music & Performing",
                                Icons.Default.MusicNote
                            ),

                            InterestChipData(
                                "9",
                                "Student Governance",
                                Icons.Default.Groups
                            ),

                            InterestChipData(
                                "10",
                                "Social & Networking",
                                Icons.Default.Diversity3
                            ),

                            InterestChipData(
                                "11",
                                "Faith & Fellowship",
                                Icons.Default.AutoAwesome
                            ),

                            InterestChipData(
                                "12",
                                "Competitions",
                                Icons.Default.EmojiEvents
                            )
                        )
                )
            )
        }


    // =========================================================
    // SELECTED INTERESTS
    // =========================================================

    var selectedIds by remember {
        mutableStateOf(
            emptySet<String>()
        )
    }


    // =========================================================
    // MAIN SCREEN
    // =========================================================

    Box(
        modifier =
            Modifier.fillMaxSize()
    ) {


        // =====================================================
        // CAMPUS BACKGROUND IMAGE
        // =====================================================

        Image(
            painter =
                painterResource(
                    id =
                        R.drawable.login_campus_background
                ),

            contentDescription =
                "University Campus",

            modifier =
                Modifier.fillMaxSize(),

            contentScale =
                ContentScale.Crop
        )


        // =====================================================
        // DARKEN IMAGE
        // =====================================================

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(
                            alpha = 0.10f
                        )
                    )
        )


        // =====================================================
        // NAVY GRADIENT
        // =====================================================

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(

                        Brush.verticalGradient(

                            colorStops =
                                arrayOf(

                                    0.00f to
                                            CampusNavy.copy(
                                                alpha = 0.55f
                                            ),

                                    0.18f to
                                            CampusNavy.copy(
                                                alpha = 0.78f
                                            ),

                                    0.38f to
                                            DeepNavy.copy(
                                                alpha = 0.92f
                                            ),

                                    0.62f to
                                            DeepNavy.copy(
                                                alpha = 0.98f
                                            ),

                                    1.00f to
                                            DeepNavy
                                )
                        )
                    )
        )


        // =====================================================
        // CONTENT
        // =====================================================

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
        ) {


            // =================================================
            // SCROLLABLE CONTENT
            // =================================================

            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),

                contentPadding =
                    PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 20.dp,
                        bottom = 24.dp
                    ),

                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {


                // =============================================
                // LOGO + HEADING
                // =============================================

                item {

                    Image(
                        painter =
                            painterResource(
                                id =
                                    R.drawable.campus_pulse_logo
                            ),

                        contentDescription =
                            "Campus Pulse Logo",

                        modifier =
                            Modifier
                                .size(64.dp)
                                .clip(
                                    RoundedCornerShape(
                                        16.dp
                                    )
                                ),

                        contentScale =
                            ContentScale.Crop
                    )


                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )


                    Text(
                        text =
                            "CAMPUS PULSE",

                        color =
                            TextPrimary,

                        fontSize =
                            16.sp,

                        fontWeight =
                            FontWeight.Bold,

                        letterSpacing =
                            2.sp
                    )


                    Spacer(
                        modifier =
                            Modifier.height(28.dp)
                    )


                    Text(
                        text =
                            "What are your interests?",

                        color =
                            TextPrimary,

                        fontSize =
                            28.sp,

                        fontWeight =
                            FontWeight.Bold,

                        textAlign =
                            TextAlign.Center
                    )


                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )


                    Text(
                        text =
                            "Pick three or more to discover clubs and communities that match you.",

                        color =
                            TextSecondary,

                        fontSize =
                            14.sp,

                        lineHeight =
                            20.sp,

                        textAlign =
                            TextAlign.Center,

                        modifier =
                            Modifier.padding(
                                horizontal = 16.dp
                            )
                    )


                    Spacer(
                        modifier =
                            Modifier.height(28.dp)
                    )
                }


                // =============================================
                // CATEGORIES
                // =============================================

                categories.forEach { category ->


                    item {

                        Text(
                            text =
                                category.sectionTitle,

                            color =
                                TextPrimary,

                            fontSize =
                                17.sp,

                            fontWeight =
                                FontWeight.Bold,

                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        top = 10.dp,
                                        bottom = 10.dp
                                    )
                        )
                    }


                    val chunkedItems =
                        category.items.chunked(2)


                    items(
                        count =
                            chunkedItems.size
                    ) { rowIndex ->

                        val row =
                            chunkedItems[rowIndex]


                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        vertical = 5.dp
                                    ),

                            horizontalArrangement =
                                Arrangement.spacedBy(
                                    10.dp
                                )
                        ) {


                            row.forEach { chip ->

                                val isSelected =
                                    selectedIds.contains(
                                        chip.id
                                    )


                                InterestChip(
                                    chip = chip,

                                    isSelected =
                                        isSelected,

                                    modifier =
                                        Modifier.weight(
                                            1f
                                        ),

                                    onClick = {

                                        selectedIds =
                                            if (isSelected) {

                                                selectedIds -
                                                        chip.id

                                            } else {

                                                selectedIds +
                                                        chip.id
                                            }
                                    }
                                )
                            }


                            if (row.size == 1) {

                                Spacer(
                                    modifier =
                                        Modifier.weight(
                                            1f
                                        )
                                )
                            }
                        }
                    }


                    item {

                        Spacer(
                            modifier =
                                Modifier.height(
                                    14.dp
                                )
                        )
                    }
                }
            }


            // =================================================
            // SELECTION STATUS
            // =================================================

            Text(
                text =
                    when {

                        selectedIds.isEmpty() ->
                            "Choose at least 3 interests"

                        selectedIds.size < 3 ->
                            "${selectedIds.size} selected • Pick ${3 - selectedIds.size} more"

                        else ->
                            "${selectedIds.size} interests selected"
                    },

                color =
                    if (selectedIds.size >= 3) {

                        AccentOrange

                    } else {

                        TextSecondary
                    },

                fontSize =
                    13.sp,

                fontWeight =
                    FontWeight.Medium,

                textAlign =
                    TextAlign.Center,

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 24.dp,
                            vertical = 8.dp
                        )
            )


            // =================================================
            // CONTINUE BUTTON
            // =================================================

            Button(

                onClick = {

                    // Convert the selected IDs into the actual
                    // interest names that will be saved to
                    // Firestore.

                    val selectedInterestNames =
                        categories
                            .flatMap {
                                it.items
                            }
                            .filter {
                                selectedIds.contains(
                                    it.id
                                )
                            }
                            .map {
                                it.title
                            }


                    onContinue(
                        selectedInterestNames
                    )
                },

                enabled =
                    selectedIds.size >= 3,

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 24.dp,
                            end = 24.dp,
                            top = 6.dp,
                            bottom = 20.dp
                        )
                        .height(56.dp),

                shape =
                    RoundedCornerShape(
                        28.dp
                    ),

                colors =
                    ButtonDefaults.buttonColors(

                        containerColor =
                            AccentOrange,

                        contentColor =
                            Color.White,

                        disabledContainerColor =
                            CardNavy,

                        disabledContentColor =
                            TextSecondary.copy(
                                alpha = 0.65f
                            )
                    )
            ) {

                Text(
                    text =
                        if (selectedIds.size >= 3) {

                            "Show Matching Clubs"

                        } else {

                            "Select 3 Interests"
                        },

                    fontSize =
                        16.sp,

                    fontWeight =
                        FontWeight.Bold
                )
            }
        }
    }
}


// =============================================================
// INTEREST CHIP
// =============================================================

@Composable
private fun InterestChip(
    chip: InterestChipData,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Row(
        modifier =
            modifier
                .height(58.dp)
                .clip(
                    CornerShape
                )
                .background(
                    if (isSelected) {

                        CardNavySelected

                    } else {

                        CardNavy.copy(
                            alpha = 0.92f
                        )
                    }
                )
                .border(
                    width =
                        if (isSelected) {
                            1.5.dp
                        } else {
                            1.dp
                        },

                    color =
                        if (isSelected) {
                            AccentOrange
                        } else {
                            BorderColor
                        },

                    shape =
                        CornerShape
                )
                .clickable {

                    onClick()
                }
                .padding(
                    horizontal = 10.dp
                ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {


        // =====================================================
        // ICON
        // =====================================================

        Box(
            modifier =
                Modifier
                    .size(36.dp)
                    .clip(
                        CircleShape
                    )
                    .background(

                        if (isSelected) {

                            AccentOrange.copy(
                                alpha = 0.18f
                            )

                        } else {

                            Color.White.copy(
                                alpha = 0.07f
                            )
                        }
                    ),

            contentAlignment =
                Alignment.Center
        ) {

            Icon(
                imageVector =
                    chip.icon,

                contentDescription =
                    chip.title,

                tint =
                    if (isSelected) {

                        AccentOrange

                    } else {

                        TextSecondary
                    },

                modifier =
                    Modifier.size(
                        19.dp
                    )
            )
        }


        Spacer(
            modifier =
                Modifier.width(
                    9.dp
                )
        )


        // =====================================================
        // LABEL
        // =====================================================

        Text(
            text =
                chip.title,

            color =
                if (isSelected) {

                    AccentOrange

                } else {

                    TextPrimary
                },

            fontSize =
                12.sp,

            fontWeight =
                FontWeight.SemiBold,

            maxLines =
                1
        )
    }
}


// =============================================================
// PREVIEW
// =============================================================

@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Campus Pulse Interests"
)
@Composable
fun InterestsScreenPreview() {

    MaterialTheme {

        Surface {

            InterestsScreen()
        }
    }
}