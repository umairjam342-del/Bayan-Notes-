package com.example.bayan.ui.quran

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bayan.data.model.Bookmark
import com.example.bayan.data.quran.QuranData
import com.example.bayan.util.QuranAudioPlayer
import com.example.bayan.util.QuranAudioState
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * Immersive Full Screen Quran Recitation Layout.
 *
 * Displays:
 *  - Full screen verse presentation with large ornate Arabic text and Urdu translation.
 *  - Automatically autohides the audio player controls and top bar during playback
 *    after 3.5s of user inactivity for distraction-free listening and tadabbur.
 *  - Hides all bottom navigation tabs and top app bars.
 *  - Tapping anywhere on screen instantly reveals the player controls and resets the autohide timer.
 *  - Full playback controls: play/pause, next/prev ayah, gapless toggle, speed adjustment,
 *    auto-advance, bookmarking, and study note creation.
 */
@Composable
fun FullScreenRecitationLayout(
    audioState: QuranAudioState,
    audioPlayer: QuranAudioPlayer,
    onExitFullScreen: () -> Unit,
    onBookmark: ((surahNumber: Int, ayahNumber: Int, surahName: String) -> Unit)? = null,
    isBookmarked: Boolean = false,
    onCreateNote: ((surahNumber: Int, ayahNumber: Int, arabic: String, translation: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Load ayahs for the currently playing Surah
    val ayahs = remember(audioState.currentSurah) {
        if (audioState.currentSurah != null && audioState.currentSurah!! in 1..114) {
            QuranData.getAyahsForSurah(context, audioState.currentSurah!!)
        } else emptyList()
    }

    val currentAyahData = remember(ayahs, audioState.currentAyah) {
        ayahs.find { it.ayahNumber == audioState.currentAyah }
    }

    val surahInfo = remember(audioState.currentSurah) {
        if (audioState.currentSurah != null && audioState.currentSurah!! in 1..114) {
            QuranData.surahs.find { it.number == audioState.currentSurah }
        } else null
    }

    // Controls visibility and autohide state
    var isControlsVisible by remember { mutableStateOf(true) }
    var autohideEnabled by remember { mutableStateOf(true) }
    var lastInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showSpeedDialog by remember { mutableStateOf(false) }

    // Autohide countdown: when playing and autohide is enabled, hide controls after 3.5 seconds
    LaunchedEffect(isControlsVisible, audioState.isPlaying, lastInteractionTime, autohideEnabled) {
        if (isControlsVisible && audioState.isPlaying && autohideEnabled) {
            delay(3500L)
            isControlsVisible = false
        }
    }

    // Force controls visible if playback pauses or enters error
    LaunchedEffect(audioState.isPlaying) {
        if (!audioState.isPlaying) {
            isControlsVisible = true
        }
    }

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("fullscreen_recitation_layout")
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                    )
                )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                // Tapping anywhere toggles controls and resets autohide timer
                isControlsVisible = !isControlsVisible
                lastInteractionTime = System.currentTimeMillis()
            }
    ) {
        // Center Main Content: Current Recited Ayah
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // Surah & Ayah Chip Banner
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (audioState.isPlaying) Icons.Default.GraphicEq else Icons.Default.Headphones,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Surah ${audioState.surahName.ifBlank { surahInfo?.nameTransliterated ?: "Quran" }} • Ayah ${audioState.currentAyah ?: 1}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (surahInfo != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${surahInfo.nameArabic})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Big Illuminated Arabic Verse Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                ),
                border = BorderStroke(
                    width = if (audioState.isPlaying) 2.dp else 1.dp,
                    color = if (audioState.isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (audioState.isPlaying) 6.dp else 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val rawArabic = currentAyahData?.arabicText ?: "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"
                    val ayahNum = audioState.currentAyah ?: 1

                    Text(
                        text = "$rawArabic ${formatAyahEndMarker(ayahNum)}",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 30.sp,
                            lineHeight = 52.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.Serif
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("fullscreen_arabic_text")
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Urdu Translation with elegant styling
                    val translationText = currentAyahData?.translation ?: "اللہ کے نام سے جو بڑا مہربان نہایت رحم والا ہے"
                    Text(
                        text = translationText,
                        style = MaterialTheme.typography.titleMedium.copy(
                            lineHeight = 28.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("fullscreen_translation_text")
                    )

                    // Optional Tafsir / reflection excerpt
                    if (!currentAyahData?.tafsirExcerpt.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = currentAyahData?.tafsirExcerpt ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    // Quick in-fullscreen actions: Bookmark & Note creation
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 18.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (onBookmark != null) {
                            OutlinedButton(
                                onClick = {
                                    val sNum = audioState.currentSurah ?: 1
                                    val aNum = audioState.currentAyah ?: 1
                                    val sName = audioState.surahName.ifBlank { "Quran" }
                                    onBookmark(sNum, aNum, sName)
                                    lastInteractionTime = System.currentTimeMillis()
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("fullscreen_bookmark_button")
                            ) {
                                Icon(
                                    if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isBookmarked) "Bookmarked" else "Bookmark Ayah", style = MaterialTheme.typography.labelSmall)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                        }

                        if (onCreateNote != null) {
                            OutlinedButton(
                                onClick = {
                                    val sNum = audioState.currentSurah ?: 1
                                    val aNum = audioState.currentAyah ?: 1
                                    val arabic = currentAyahData?.arabicText ?: ""
                                    val transl = currentAyahData?.translation ?: ""
                                    onCreateNote(sNum, aNum, arabic, transl)
                                    onExitFullScreen()
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("fullscreen_create_note_button")
                            ) {
                                Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Study Note", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(130.dp))
        }

        // Top Fullscreen Header (Autohides)
        AnimatedVisibility(
            visible = isControlsVisible,
            enter = fadeIn(tween(250)) + slideInVertically(tween(250)) { -it },
            exit = fadeOut(tween(250)) + slideOutVertically(tween(250)) { -it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                tonalElevation = 6.dp,
                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("fullscreen_top_bar")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onExitFullScreen,
                            modifier = Modifier.testTag("exit_fullscreen_button")
                        ) {
                            Icon(
                                Icons.Default.FullscreenExit,
                                contentDescription = "Exit Fullscreen Recitation",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Column {
                            Text(
                                text = "Surah ${audioState.surahName} • Ayah ${audioState.currentAyah ?: 1}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = audioState.reciterName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (audioState.isOfflineAudio) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Text(
                                            text = "⚡ Offline Audio",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Autohide Toggle and Manual Hide Button
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                autohideEnabled = !autohideEnabled
                                lastInteractionTime = System.currentTimeMillis()
                            },
                            modifier = Modifier.testTag("toggle_autohide_button")
                        ) {
                            Icon(
                                if (autohideEnabled) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (autohideEnabled) "Autohide enabled" else "Autohide disabled (controls pinned)",
                                tint = if (autohideEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        }

                        IconButton(
                            onClick = {
                                isControlsVisible = false
                            },
                            modifier = Modifier.testTag("toggle_player_visibility_button")
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Hide player controls",
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }

        // Unobtrusive Autohide Hint Pill when player controls are hidden
        AnimatedVisibility(
            visible = !isControlsVisible,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                modifier = Modifier
                    .testTag("autohide_hint_pill")
                    .clickable {
                        isControlsVisible = true
                        lastInteractionTime = System.currentTimeMillis()
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tap screen to show player controls",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Bottom Fullscreen Audio Player Bar (Autohides)
        AnimatedVisibility(
            visible = isControlsVisible,
            enter = fadeIn(tween(250)) + slideInVertically(tween(250)) { it },
            exit = fadeOut(tween(250)) + slideOutVertically(tween(250)) { it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("fullscreen_audio_player_bar"),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    // Timeline Progress Indicator
                    if (audioState.progressFraction > 0f) {
                        LinearProgressIndicator(
                            progress = { audioState.progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.5.dp)
                                .padding(bottom = 6.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Playback Speed Selector
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier
                                .testTag("fullscreen_speed_button")
                                .clickable {
                                    showSpeedDialog = true
                                    lastInteractionTime = System.currentTimeMillis()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Speed,
                                    contentDescription = "Speed",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${String.format(Locale.US, "%.2f", audioState.playbackSpeed).trimEnd('0').trimEnd('.')}x",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Gapless toggle
                        IconButton(
                            onClick = {
                                audioPlayer.toggleSkipSilenceGap()
                                lastInteractionTime = System.currentTimeMillis()
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("fullscreen_gapless_toggle")
                        ) {
                            Icon(
                                Icons.Default.Bolt,
                                contentDescription = if (audioState.skipSilenceGap) "Gapless ON" else "Gapless OFF",
                                tint = if (audioState.skipSilenceGap) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Auto-advance toggle
                        IconButton(
                            onClick = {
                                audioPlayer.toggleAutoAdvance()
                                lastInteractionTime = System.currentTimeMillis()
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("fullscreen_autoadvance_toggle")
                        ) {
                            Icon(
                                Icons.Default.Repeat,
                                contentDescription = if (audioState.autoAdvance) "Auto-advance ON" else "Auto-advance OFF",
                                tint = if (audioState.autoAdvance) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Previous Ayah
                        IconButton(
                            onClick = {
                                audioPlayer.previousAyah()
                                lastInteractionTime = System.currentTimeMillis()
                            },
                            enabled = (audioState.currentAyah ?: 1) > 1 || (audioState.currentSurah ?: 1) > 1,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("fullscreen_prev_button")
                        ) {
                            Icon(
                                Icons.Default.SkipPrevious,
                                contentDescription = "Previous Ayah",
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Compact Play / Pause Button
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(38.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    if (audioState.isPlaying) {
                                        audioPlayer.pause()
                                    } else {
                                        audioPlayer.resume()
                                    }
                                    lastInteractionTime = System.currentTimeMillis()
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("fullscreen_play_pause_button")
                            ) {
                                if (audioState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else if (audioState.isPlaying) {
                                    Icon(
                                        Icons.Default.Pause,
                                        contentDescription = "Pause",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // Next Ayah
                        IconButton(
                            onClick = {
                                audioPlayer.nextAyah()
                                lastInteractionTime = System.currentTimeMillis()
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("fullscreen_next_button")
                        ) {
                            Icon(
                                Icons.Default.SkipNext,
                                contentDescription = "Next Ayah",
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Stop / Close
                        IconButton(
                            onClick = {
                                audioPlayer.stop()
                                onExitFullScreen()
                            },
                            modifier = Modifier
                                .size(30.dp)
                                .testTag("fullscreen_close_button")
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Stop recitation",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Playback Speed Dialog
    if (showSpeedDialog) {
        val speeds = listOf(0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)
        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            title = {
                Text("Recitation Speed", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Adjust playback speed for Mishary Alafasy's recitation:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        speeds.take(3).forEach { speed ->
                            OutlinedButton(
                                onClick = {
                                    audioPlayer.setPlaybackSpeed(speed)
                                    showSpeedDialog = false
                                    lastInteractionTime = System.currentTimeMillis()
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("${speed}x")
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        speeds.drop(3).forEach { speed ->
                            OutlinedButton(
                                onClick = {
                                    audioPlayer.setPlaybackSpeed(speed)
                                    showSpeedDialog = false
                                    lastInteractionTime = System.currentTimeMillis()
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("${speed}x")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSpeedDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
