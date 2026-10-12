package com.campuspulse.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campuspulse.app.data.FirestoreEvent
import com.campuspulse.app.data.HomeRepository
import com.campuspulse.app.model.Category
import com.campuspulse.app.model.EventItem
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val CalendarNavy = Color(0xFF021D38)
private val CalendarPanel = Color(0xFF062F59)
private val CalendarBorder = Color(0xFF315A7D)
private val CalendarOrange = Color(0xFFFF7A00)
private val CalendarMuted = Color(0xFFB8C7D9)
private val CampusTimeZone: TimeZone get() = TimeZone.getTimeZone("Africa/Johannesburg")

internal fun calendarDayStart(value: Long): Long = Calendar.getInstance(CampusTimeZone).apply {
    timeInMillis = value
    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
}.timeInMillis

internal fun calendarMonthStart(value: Long): Long = Calendar.getInstance(CampusTimeZone).apply {
    timeInMillis = calendarDayStart(value)
    set(Calendar.DAY_OF_MONTH, 1)
}.timeInMillis

internal fun calendarCells(month: Long): List<Long> {
    val first = Calendar.getInstance(CampusTimeZone).apply { timeInMillis = calendarMonthStart(month) }
    val offset = (first.get(Calendar.DAY_OF_WEEK) + 5) % 7
    val count = ((offset + first.getActualMaximum(Calendar.DAY_OF_MONTH) + 6) / 7) * 7
    first.add(Calendar.DAY_OF_MONTH, -offset)
    return List(count) { index -> (first.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, index) }.timeInMillis }
}

internal fun calendarEventOnDay(start: Long?, end: Long?, day: Long): Boolean {
    if (start == null) return false
    val lower = calendarDayStart(day)
    val upper = Calendar.getInstance(CampusTimeZone).apply { timeInMillis = lower; add(Calendar.DAY_OF_MONTH, 1) }.timeInMillis
    return if (end != null && end > start) start < upper && end > lower else calendarDayStart(start) == lower
}

internal fun calendarEventStatus(start: Long?, end: Long?, now: Long): String = when {
    start == null -> "Schedule not set"
    now < start -> "Upcoming"
    end == null || end <= start -> "End time not set"
    now < end -> "Ongoing"
    else -> "Passed"
}

private fun calendarFormat(value: Long, pattern: String): String = SimpleDateFormat(pattern, Locale.getDefault()).apply {
    timeZone = CampusTimeZone
}.format(Date(value))

private fun calendarClubColor(clubId: String): Color {
    val colors = listOf(CalendarOrange, Color(0xFF27D79C), Color(0xFF65B8FF), Color(0xFFCDA7FF), Color(0xFFFFA4C4))
    return colors[(clubId.hashCode() and Int.MAX_VALUE) % colors.size]
}

private fun FirestoreEvent.calendarItem(): EventItem = EventItem(
    id = id, title = title, category = Category.ACADEMIC, hostClub = clubName.ifBlank { "Campus Club" },
    schedule = eventDate?.toDate()?.time?.let { calendarFormat(it, "EEE, d MMM · HH:mm") } ?: "Date to be confirmed · Time to be confirmed",
    venue = location, description = description, rating = 0.0, going = 0, isFree = true
)

@Composable
fun EventsCalendarScreen(
    onBack: () -> Unit,
    onOpenEvent: (EventItem, String) -> Unit
) {
    val preview = LocalInspectionMode.current
    val repository = remember(preview) { if (preview) null else HomeRepository() }
    var events by remember { mutableStateOf(emptyList<FirestoreEvent>()) }
    var loading by remember { mutableStateOf(!preview) }
    var failed by remember { mutableStateOf(false) }
    var retry by remember { mutableIntStateOf(0) }
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    var month by rememberSaveable { mutableStateOf(calendarMonthStart(System.currentTimeMillis())) }
    var selectedDay by rememberSaveable { mutableStateOf(calendarDayStart(System.currentTimeMillis())) }
    val cells = remember(month) { calendarCells(month) }
    val today = calendarDayStart(now)

    LaunchedEffect(Unit) {
        while (true) { now = System.currentTimeMillis(); delay(1000L) }
    }
    DisposableEffect(repository, retry) {
        loading = repository != null
        failed = false
        val listener = repository?.listenToEvents(
            onSuccess = { events = it; loading = false; failed = false },
            onError = { loading = false; failed = true }
        )
        onDispose { listener?.remove() }
    }
    val selectedEvents = events.filter {
        calendarEventOnDay(it.eventDate?.toDate()?.time, it.eventEndDate?.toDate()?.time, selectedDay)
    }.sortedWith(compareBy<FirestoreEvent> { it.eventDate?.toDate()?.time ?: Long.MAX_VALUE }.thenBy { it.id })
    val eventsByDay = remember(events, cells) {
        cells.associateWith { day -> events.filter {
            calendarEventOnDay(it.eventDate?.toDate()?.time, it.eventEndDate?.toDate()?.time, day)
        }.sortedWith(compareBy<FirestoreEvent> { it.eventDate?.toDate()?.time ?: Long.MAX_VALUE }.thenBy { it.id }) }
    }
    fun moveMonth(amount: Int) {
        month = Calendar.getInstance(CampusTimeZone).apply { timeInMillis = month; add(Calendar.MONTH, amount) }.timeInMillis
        selectedDay = month
    }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val cellHeight = ((screenWidth - 24.dp) / 7 * 1.35f).coerceIn(56.dp, 76.dp)

    Box(modifier = Modifier.fillMaxSize().background(CalendarNavy).statusBarsPadding()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) }
                    Column(Modifier.weight(1f)) {
                        Text("Events Calendar", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("Campus events · South African time", color = CalendarMuted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = CalendarPanel),
                    shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, CalendarBorder)) {
                    Column(Modifier.padding(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(calendarFormat(month, "MMMM yyyy"), color = Color.White, fontSize = 17.sp,
                                fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f).padding(start = 6.dp))
                            IconButton(onClick = { moveMonth(-1) }, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Filled.ChevronLeft, "Previous month", tint = Color.White)
                            }
                            IconButton(onClick = { moveMonth(1) }, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Filled.ChevronRight, "Next month", tint = Color.White)
                            }
                            TextButton(onClick = { month = calendarMonthStart(now); selectedDay = today }) { Text("Today", color = CalendarOrange, fontSize = 12.sp) }
                        }
                        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach {
                                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { Text(it, color = CalendarMuted, fontSize = 10.sp) }
                            }
                        }
                        cells.chunked(7).forEach { week ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                week.forEach { day ->
                                    val dayEvents = eventsByDay[day].orEmpty()
                                    val selected = day == selectedDay
                                    val inMonth = calendarMonthStart(day) == month
                                    Column(
                                        modifier = Modifier.weight(1f).height(cellHeight).padding(bottom = 2.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (selected) CalendarOrange.copy(alpha = 0.2f) else CalendarNavy.copy(alpha = if (inMonth) 0.55f else 0.25f))
                                            .clickable { selectedDay = day; if (!inMonth) month = calendarMonthStart(day) }
                                            .semantics { contentDescription = calendarFormat(day, "EEEE, d MMMM yyyy") + ", ${dayEvents.size} events" }
                                            .padding(3.dp),
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Text(calendarFormat(day, "d"), fontSize = 12.sp,
                                            color = if (day == today || selected) CalendarOrange else if (inMonth) Color.White else CalendarMuted.copy(alpha = 0.45f),
                                            fontWeight = if (day == today || selected) FontWeight.Bold else FontWeight.Normal)
                                        dayEvents.take(2).forEach { event ->
                                            Text(event.title, color = CalendarNavy, fontSize = 8.sp, maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(3.dp))
                                                    .background(calendarClubColor(event.clubId)).padding(horizontal = 2.dp, vertical = 2.dp))
                                        }
                                        if (dayEvents.size > 2) Text("+${dayEvents.size - 2}", color = CalendarMuted, fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                Text(calendarFormat(selectedDay, "EEEE, d MMMM yyyy"), color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("${selectedEvents.size} ${if (selectedEvents.size == 1) "event" else "events"}", color = CalendarMuted, fontSize = 12.sp)
            }
            if (loading) item {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = CalendarOrange, strokeWidth = 2.dp)
                }
            }
            if (failed) item {
                Column {
                    Text("Events could not load. Previously loaded events stay visible.", color = CalendarMuted, fontSize = 13.sp)
                    TextButton(onClick = { retry++ }) { Text("Retry", color = CalendarOrange) }
                }
            }
            if (!loading && !failed && selectedEvents.isEmpty()) item {
                Text("No events scheduled for this date.", color = CalendarMuted, modifier = Modifier.padding(vertical = 20.dp))
            }
            items(selectedEvents, key = { it.id }) { event ->
                val start = event.eventDate?.toDate()?.time
                val end = event.eventEndDate?.toDate()?.time
                val status = calendarEventStatus(start, end, now)
                val color = when (status) { "Ongoing" -> Color(0xFF27D79C); "Passed" -> CalendarMuted; else -> CalendarOrange }
                Card(onClick = { onOpenEvent(event.calendarItem(), event.clubId) },
                    shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, CalendarBorder),
                    colors = CardDefaults.cardColors(containerColor = CalendarPanel)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(status, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(event.title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Text(event.clubName.ifBlank { "Campus Club" }, color = calendarClubColor(event.clubId), fontSize = 12.sp)
                        Text(start?.let { calendarFormat(it, "d MMM, HH:mm") } ?: "Start time not set", color = CalendarMuted, fontSize = 12.sp)
                        if (end != null) Text("Ends ${calendarFormat(end, "d MMM, HH:mm")}", color = CalendarMuted, fontSize = 12.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LocationOn, null, tint = CalendarOrange, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(event.location.ifBlank { "Location to be confirmed" }, color = CalendarMuted, fontSize = 12.sp)
                        }
                        if (event.description.isNotBlank()) Text(event.description, color = CalendarMuted, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            val unscheduled = events.count { it.eventDate == null }
            if (unscheduled > 0) item { Text("$unscheduled ${if (unscheduled == 1) "event has" else "events have"} no scheduled date and cannot appear in the grid.", color = CalendarMuted, fontSize = 11.sp) }
        }
    }
}
