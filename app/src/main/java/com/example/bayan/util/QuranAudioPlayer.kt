package com.example.bayan.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.PowerManager
import android.util.Log
import com.example.bayan.data.quran.QuranData
import com.example.bayan.service.QuranAudioService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

enum class AudioPlaybackState {
    IDLE,
    LOADING,
    PLAYING,
    PAUSED,
    ERROR
}

data class QuranAudioState(
    val playbackState: AudioPlaybackState = AudioPlaybackState.IDLE,
    val currentSurah: Int? = null,
    val currentAyah: Int? = null,
    val surahName: String = "",
    val totalVersesInSurah: Int = 0,
    val autoAdvance: Boolean = true,
    val skipSilenceGap: Boolean = true, // Gapless playback: skips trailing & leading dead air
    val playbackSpeed: Float = 1.0f,
    val errorMessage: String? = null,
    val reciterName: String = "Sheikh Mishary Rashid Alafasy",
    val progressFraction: Float = 0f,
    val isOfflineAudio: Boolean = false // True when recitation plays from local offline storage
) {
    val isPlaying: Boolean get() = playbackState == AudioPlaybackState.PLAYING
    val isLoading: Boolean get() = playbackState == AudioPlaybackState.LOADING
    val isPaused: Boolean get() = playbackState == AudioPlaybackState.PAUSED
    val isActive: Boolean get() = playbackState != AudioPlaybackState.IDLE

    fun isVersePlaying(surah: Int, ayah: Int): Boolean {
        return currentSurah == surah && currentAyah == ayah && isPlaying
    }

    fun isVerseLoading(surah: Int, ayah: Int): Boolean {
        return currentSurah == surah && currentAyah == ayah && isLoading
    }

    fun isVerseActive(surah: Int, ayah: Int): Boolean {
        return currentSurah == surah && currentAyah == ayah && (isPlaying || isLoading || isPaused)
    }
}

/**
 * Audio player for Quran recitation by Sheikh Mishary Rashid Alafasy.
 * Features:
 *  - Gapless recitation: pre-buffers the upcoming verse and skips trailing/leading silence
 *  - Background playback: integrates with QuranAudioService & WAKE_LOCK
 *  - High clarity 128kbps audio streaming with dual CDN fallback
 */
class QuranAudioPlayer(
    private val context: Context? = null,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val TAG = "QuranAudioPlayer"

    private var mediaPlayer: MediaPlayer? = null
    private var nextMediaPlayer: MediaPlayer? = null
    private var nextPlayerSurah: Int? = null
    private var nextPlayerAyah: Int? = null
    private var isNextPlayerReady = false

    private var currentTrackIsOffline = false
    private var nextPlayerIsOffline = false
    private var isUsingFallback = false
    private var monitorJob: Job? = null

    private val _audioState = MutableStateFlow(QuranAudioState())
    val audioState: StateFlow<QuranAudioState> = _audioState.asStateFlow()

    companion object {
        @Volatile
        private var instance: QuranAudioPlayer? = null

        val SUPPORTED_SPEEDS = listOf(0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)

        fun getInstance(context: Context? = null): QuranAudioPlayer {
            return instance ?: synchronized(this) {
                instance ?: QuranAudioPlayer(context?.applicationContext).also { instance = it }
            }
        }

        fun buildAudioUrl(surah: Int, ayah: Int, fallback: Boolean = false): String {
            return if (!fallback) {
                String.format(Locale.US, "https://everyayah.com/data/Alafasy_128kbps/%03d%03d.mp3", surah, ayah)
            } else {
                String.format(Locale.US, "https://verses.quran.com/Alafasy/mp3/%03d%03d.mp3", surah, ayah)
            }
        }
    }

    /**
     * Start playing or resume recitation for specified verse.
     */
    fun playVerse(surah: Int, ayah: Int, surahName: String = "", totalVerses: Int = 0) {
        val currentState = _audioState.value

        // If already playing this verse, do nothing
        if (currentState.currentSurah == surah && currentState.currentAyah == ayah && currentState.isPlaying) {
            return
        }

        // If paused on this verse, resume
        if (currentState.currentSurah == surah && currentState.currentAyah == ayah && currentState.isPaused) {
            resume()
            return
        }

        val resolvedSurahName = if (surahName.isNotBlank()) {
            surahName
        } else {
            QuranData.surahs.find { it.number == surah }?.nameTransliterated ?: "Surah $surah"
        }

        val resolvedTotalVerses = if (totalVerses > 0) {
            totalVerses
        } else {
            QuranData.surahs.find { it.number == surah }?.versesCount ?: 0
        }

        isUsingFallback = false
        startPlayback(surah, ayah, resolvedSurahName, resolvedTotalVerses, fallback = false)
    }

    /**
     * Toggle play/pause for a specific verse.
     */
    fun togglePlayPause(surah: Int, ayah: Int, surahName: String = "", totalVerses: Int = 0) {
        val state = _audioState.value
        if (state.currentSurah == surah && state.currentAyah == ayah) {
            if (state.isPlaying) {
                pause()
            } else if (state.isPaused) {
                resume()
            } else {
                playVerse(surah, ayah, surahName, totalVerses)
            }
        } else {
            playVerse(surah, ayah, surahName, totalVerses)
        }
    }

    /**
     * Toggle the "Skip Silence Gap" feature for gapless recitation.
     */
    fun toggleSkipSilenceGap() {
        _audioState.update { it.copy(skipSilenceGap = !it.skipSilenceGap) }
        notifyService()
    }

    /**
     * Cycle through supported playback speeds (0.75x -> 1.0x -> 1.25x -> 1.5x -> 1.75x -> 2.0x -> 0.75x).
     */
    fun cycleSpeed() {
        val current = _audioState.value.playbackSpeed
        val currentIndex = SUPPORTED_SPEEDS.indexOfFirst { kotlin.math.abs(it - current) < 0.05f }
        val nextIndex = if (currentIndex == -1 || currentIndex >= SUPPORTED_SPEEDS.size - 1) 0 else currentIndex + 1
        setPlaybackSpeed(SUPPORTED_SPEEDS[nextIndex])
    }

    /**
     * Increase playback speed by one step.
     */
    fun increaseSpeed() {
        val current = _audioState.value.playbackSpeed
        val nextSpeed = SUPPORTED_SPEEDS.firstOrNull { it > current + 0.05f } ?: SUPPORTED_SPEEDS.last()
        setPlaybackSpeed(nextSpeed)
    }

    /**
     * Decrease playback speed by one step.
     */
    fun decreaseSpeed() {
        val current = _audioState.value.playbackSpeed
        val prevSpeed = SUPPORTED_SPEEDS.lastOrNull { it < current - 0.05f } ?: SUPPORTED_SPEEDS.first()
        setPlaybackSpeed(prevSpeed)
    }

    private fun startPlayback(surah: Int, ayah: Int, surahName: String, totalVerses: Int, fallback: Boolean) {
        stopMonitor()

        val isDownloaded = context != null && QuranAudioDownloadManager.isVerseDownloaded(context, surah, ayah)

        _audioState.update {
            it.copy(
                playbackState = AudioPlaybackState.LOADING,
                currentSurah = surah,
                currentAyah = ayah,
                surahName = surahName,
                totalVersesInSurah = totalVerses,
                errorMessage = null,
                progressFraction = 0f,
                isOfflineAudio = isDownloaded
            )
        }
        notifyService()

        // Check if pre-buffered nextMediaPlayer matches this exact verse!
        if (isNextPlayerReady && nextMediaPlayer != null && nextPlayerSurah == surah && nextPlayerAyah == ayah) {
            try {
                releaseCurrentMediaPlayerOnly()
                mediaPlayer = nextMediaPlayer
                nextMediaPlayer = null
                isNextPlayerReady = false
                nextPlayerSurah = null
                nextPlayerAyah = null
                currentTrackIsOffline = nextPlayerIsOffline

                val mp = mediaPlayer!!
                applyPlaybackSpeed(mp, _audioState.value.playbackSpeed)
                if (_audioState.value.skipSilenceGap) {
                    try { mp.seekTo(80) } catch (_: Exception) {}
                }
                mp.start()

                _audioState.update {
                    it.copy(
                        playbackState = AudioPlaybackState.PLAYING,
                        isOfflineAudio = currentTrackIsOffline
                    )
                }
                notifyService()
                startMonitor()
                schedulePrebufferNextVerse(surah, ayah, totalVerses)
                return
            } catch (e: Exception) {
                Log.w(TAG, "Pre-buffered player failed to start, falling back to prepare", e)
            }
        }

        coroutineScope.launch {
            try {
                releaseAllMediaPlayers()

                val player = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    context?.let { ctx ->
                        try {
                            setWakeMode(ctx, PowerManager.PARTIAL_WAKE_LOCK)
                        } catch (_: Exception) {}
                    }

                    // Check if local file exists, otherwise stream from CDN
                    if (isDownloaded && context != null) {
                        val file = QuranAudioDownloadManager.getOfflineAudioFile(context, surah, ayah)
                        setDataSource(file.absolutePath)
                        currentTrackIsOffline = true
                    } else {
                        val url = buildAudioUrl(surah, ayah, fallback)
                        setDataSource(url)
                        currentTrackIsOffline = false
                    }

                    setOnPreparedListener { mp ->
                        try {
                            applyPlaybackSpeed(mp, _audioState.value.playbackSpeed)
                            if (_audioState.value.skipSilenceGap) {
                                try { mp.seekTo(80) } catch (_: Exception) {}
                            }
                            mp.start()
                            _audioState.update {
                                it.copy(
                                    playbackState = AudioPlaybackState.PLAYING,
                                    isOfflineAudio = currentTrackIsOffline
                                )
                            }
                            notifyService()
                            startMonitor()
                            schedulePrebufferNextVerse(surah, ayah, totalVerses)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error starting MediaPlayer", e)
                            handlePlaybackError("Failed to play audio")
                        }
                    }
                    setOnCompletionListener {
                        handlePlaybackCompletion()
                    }
                    setOnErrorListener { _, what, extra ->
                        Log.w(TAG, "MediaPlayer error: what=$what extra=$extra")
                        if (!isUsingFallback) {
                            isUsingFallback = true
                            startPlayback(surah, ayah, surahName, totalVerses, fallback = true)
                        } else {
                            handlePlaybackError("Unable to stream recitation by Mishary Alafasy. Check your connection.")
                        }
                        true
                    }
                }

                mediaPlayer = player
                player.prepareAsync()
            } catch (e: Exception) {
                Log.e(TAG, "Error preparing playback", e)
                if (!isUsingFallback) {
                    isUsingFallback = true
                    startPlayback(surah, ayah, surahName, totalVerses, fallback = true)
                } else {
                    handlePlaybackError(e.message ?: "Playback failed")
                }
            }
        }
    }

    /**
     * Pre-buffers the next verse in the background so there is zero network delay
     * between verses.
     */
    private fun schedulePrebufferNextVerse(currentSurah: Int, currentAyah: Int, totalVerses: Int) {
        if (!_audioState.value.autoAdvance) return

        val (nextSurah, nextAyah, nextSurahName, nextTotalVerses) = getNextVerseTarget(
            currentSurah,
            currentAyah,
            totalVerses
        ) ?: return

        coroutineScope.launch {
            try {
                releaseNextMediaPlayerOnly()
                isNextPlayerReady = false
                nextPlayerSurah = nextSurah
                nextPlayerAyah = nextAyah

                val isNextDownloaded = context != null && QuranAudioDownloadManager.isVerseDownloaded(context, nextSurah, nextAyah)
                nextPlayerIsOffline = isNextDownloaded

                val prePlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    context?.let { ctx ->
                        try {
                            setWakeMode(ctx, PowerManager.PARTIAL_WAKE_LOCK)
                        } catch (_: Exception) {}
                    }

                    if (isNextDownloaded && context != null) {
                        val file = QuranAudioDownloadManager.getOfflineAudioFile(context, nextSurah, nextAyah)
                        setDataSource(file.absolutePath)
                    } else {
                        val url = buildAudioUrl(nextSurah, nextAyah, fallback = false)
                        setDataSource(url)
                    }

                    setOnPreparedListener { mp ->
                        isNextPlayerReady = true
                        mp.setOnCompletionListener {
                            handlePlaybackCompletion()
                        }
                    }
                    setOnErrorListener { _, _, _ ->
                        isNextPlayerReady = false
                        true
                    }
                }
                nextMediaPlayer = prePlayer
                prePlayer.prepareAsync()
            } catch (e: Exception) {
                Log.w(TAG, "Could not pre-buffer next verse: ${e.message}")
                isNextPlayerReady = false
            }
        }
    }

    /**
     * Determines the next verse coordinates (within the current surah, or advancing to next surah).
     */
    private fun getNextVerseTarget(
        currentSurah: Int,
        currentAyah: Int,
        totalVerses: Int
    ): Quad<Int, Int, String, Int>? {
        val total = if (totalVerses > 0) {
            totalVerses
        } else {
            QuranData.surahs.find { it.number == currentSurah }?.versesCount ?: 0
        }

        return if (total > 0 && currentAyah < total) {
            val sName = QuranData.surahs.find { it.number == currentSurah }?.nameTransliterated ?: "Surah $currentSurah"
            Quad(currentSurah, currentAyah + 1, sName, total)
        } else if (currentSurah < 114) {
            val nextSurah = currentSurah + 1
            val nextSurahObj = QuranData.surahs.find { it.number == nextSurah }
            val nextTotal = nextSurahObj?.versesCount ?: 0
            val nextName = nextSurahObj?.nameTransliterated ?: "Surah $nextSurah"
            Quad(nextSurah, 1, nextName, nextTotal)
        } else {
            null
        }
    }

    /**
     * Monitors active playback position for:
     *  1. UI progress bar updates.
     *  2. Gapless Handover: skips trailing silence (typically 350-450ms) and immediately starts next verse!
     */
    private fun startMonitor() {
        stopMonitor()
        monitorJob = coroutineScope.launch {
            while (isActive) {
                delay(60)
                try {
                    val player = mediaPlayer
                    if (player != null && player.isPlaying) {
                        val duration = player.duration
                        val position = player.currentPosition
                        val fraction = if (duration > 0) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f
                        _audioState.update { it.copy(progressFraction = fraction) }

                        // Check for silence gap skip
                        val state = _audioState.value
                        if (state.skipSilenceGap && isNextPlayerReady && nextMediaPlayer != null && duration > 600) {
                            // Alafasy recitation tracks typically contain 380-450ms of silence at the end
                            if (position >= duration - 420) {
                                triggerGaplessHandover()
                                break
                            }
                        }
                    }
                } catch (_: Exception) {
                    // MediaPlayer may be in transition or released
                }
            }
        }
    }

    /**
     * Handover to pre-buffered player to eliminate silence gap seamlessly.
     */
    private fun triggerGaplessHandover() {
        val state = _audioState.value
        val curSurah = state.currentSurah ?: return
        val curAyah = state.currentAyah ?: return
        val target = getNextVerseTarget(curSurah, curAyah, state.totalVersesInSurah) ?: return

        try {
            val oldPlayer = mediaPlayer
            val nextMp = nextMediaPlayer

            if (nextMp != null && isNextPlayerReady) {
                mediaPlayer = nextMp
                nextMediaPlayer = null
                isNextPlayerReady = false
                nextPlayerSurah = null
                nextPlayerAyah = null
                currentTrackIsOffline = nextPlayerIsOffline

                // Release old player safely
                try { oldPlayer?.stop() } catch (_: Exception) {}
                try { oldPlayer?.release() } catch (_: Exception) {}

                applyPlaybackSpeed(nextMp, state.playbackSpeed)
                if (state.skipSilenceGap) {
                    try { nextMp.seekTo(80) } catch (_: Exception) {} // skip initial silence
                }
                nextMp.start()

                _audioState.update {
                    it.copy(
                        playbackState = AudioPlaybackState.PLAYING,
                        currentSurah = target.first,
                        currentAyah = target.second,
                        surahName = target.third,
                        totalVersesInSurah = target.fourth,
                        progressFraction = 0f,
                        isOfflineAudio = currentTrackIsOffline
                    )
                }
                notifyService()
                startMonitor()
                schedulePrebufferNextVerse(target.first, target.second, target.fourth)
            } else {
                // If next player was not ready yet, perform standard next ayah
                nextAyah()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during gapless handover", e)
            nextAyah()
        }
    }

    private fun stopMonitor() {
        monitorJob?.cancel()
        monitorJob = null
    }

    fun pause() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                }
            }
            _audioState.update { it.copy(playbackState = AudioPlaybackState.PAUSED) }
            notifyService()
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing playback", e)
        }
    }

    fun resume() {
        try {
            mediaPlayer?.let {
                it.start()
                _audioState.update { it.copy(playbackState = AudioPlaybackState.PLAYING) }
                notifyService()
                startMonitor()
            } ?: run {
                val state = _audioState.value
                if (state.currentSurah != null && state.currentAyah != null) {
                    playVerse(state.currentSurah, state.currentAyah, state.surahName, state.totalVersesInSurah)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming playback", e)
            val state = _audioState.value
            if (state.currentSurah != null && state.currentAyah != null) {
                playVerse(state.currentSurah, state.currentAyah, state.surahName, state.totalVersesInSurah)
            }
        }
    }

    fun stop() {
        try {
            stopMonitor()
            releaseAllMediaPlayers()
            _audioState.update {
                it.copy(
                    playbackState = AudioPlaybackState.IDLE,
                    currentSurah = null,
                    currentAyah = null,
                    errorMessage = null,
                    progressFraction = 0f
                )
            }
            context?.let { QuranAudioService.stop(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping playback", e)
        }
    }

    fun nextAyah() {
        val state = _audioState.value
        val surah = state.currentSurah ?: return
        val ayah = state.currentAyah ?: return
        val target = getNextVerseTarget(surah, ayah, state.totalVersesInSurah)
        if (target != null) {
            playVerse(target.first, target.second, target.third, target.fourth)
        }
    }

    fun previousAyah() {
        val state = _audioState.value
        val surah = state.currentSurah ?: return
        val ayah = state.currentAyah ?: return

        if (ayah > 1) {
            playVerse(surah, ayah - 1, state.surahName, state.totalVersesInSurah)
        } else if (surah > 1) {
            // Previous surah's last ayah
            val prevSurah = surah - 1
            val prevSurahObj = QuranData.surahs.find { it.number == prevSurah }
            val prevTotal = prevSurahObj?.versesCount ?: 1
            val prevName = prevSurahObj?.nameTransliterated ?: "Surah $prevSurah"
            playVerse(prevSurah, prevTotal, prevName, prevTotal)
        }
    }

    fun toggleAutoAdvance() {
        val newAutoAdvance = !_audioState.value.autoAdvance
        _audioState.update { it.copy(autoAdvance = newAutoAdvance) }
        if (newAutoAdvance) {
            val state = _audioState.value
            if (state.currentSurah != null && state.currentAyah != null) {
                schedulePrebufferNextVerse(state.currentSurah, state.currentAyah, state.totalVersesInSurah)
            }
        } else {
            releaseNextMediaPlayerOnly()
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        _audioState.update { it.copy(playbackSpeed = speed) }
        mediaPlayer?.let { applyPlaybackSpeed(it, speed) }
    }

    private fun applyPlaybackSpeed(mp: MediaPlayer, speed: Float) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                val params = mp.playbackParams
                params.speed = speed
                mp.playbackParams = params
            }
        } catch (e: Exception) {
            Log.w(TAG, "Cannot set playback speed: ${e.message}")
        }
    }

    private fun handlePlaybackCompletion() {
        val state = _audioState.value
        if (!state.autoAdvance) {
            _audioState.update { it.copy(playbackState = AudioPlaybackState.IDLE, progressFraction = 0f) }
            context?.let { QuranAudioService.stop(it) }
            return
        }

        val surah = state.currentSurah ?: return
        val ayah = state.currentAyah ?: return
        val target = getNextVerseTarget(surah, ayah, state.totalVersesInSurah)

        if (target != null) {
            playVerse(target.first, target.second, target.third, target.fourth)
        } else {
            _audioState.update { it.copy(playbackState = AudioPlaybackState.IDLE, progressFraction = 0f) }
            context?.let { QuranAudioService.stop(it) }
        }
    }

    private fun handlePlaybackError(message: String) {
        stopMonitor()
        releaseAllMediaPlayers()
        _audioState.update {
            it.copy(
                playbackState = AudioPlaybackState.ERROR,
                errorMessage = message,
                progressFraction = 0f
            )
        }
        context?.let { QuranAudioService.stop(it) }
    }

    private fun notifyService() {
        val ctx = context ?: return
        val state = _audioState.value
        if (state.isActive && state.currentAyah != null) {
            QuranAudioService.startOrUpdate(
                ctx,
                state.surahName.ifBlank { "Quran" },
                state.currentAyah,
                state.isPlaying,
                state.skipSilenceGap
            )
        }
    }

    private fun releaseCurrentMediaPlayerOnly() {
        try {
            mediaPlayer?.stop()
        } catch (_: Exception) {}
        try {
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
    }

    private fun releaseNextMediaPlayerOnly() {
        try {
            nextMediaPlayer?.stop()
        } catch (_: Exception) {}
        try {
            nextMediaPlayer?.release()
        } catch (_: Exception) {}
        nextMediaPlayer = null
        isNextPlayerReady = false
        nextPlayerSurah = null
        nextPlayerAyah = null
    }

    private fun releaseAllMediaPlayers() {
        releaseCurrentMediaPlayerOnly()
        releaseNextMediaPlayerOnly()
    }

    fun release() {
        stopMonitor()
        releaseAllMediaPlayers()
        _audioState.update { it.copy(playbackState = AudioPlaybackState.IDLE, progressFraction = 0f) }
        context?.let { QuranAudioService.stop(it) }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
