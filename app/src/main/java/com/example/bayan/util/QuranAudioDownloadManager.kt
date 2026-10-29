package com.example.bayan.util

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

enum class DownloadState {
    NOT_DOWNLOADED,
    DOWNLOADING,
    DOWNLOADED,
    ERROR
}

data class SurahDownloadStatus(
    val surahNumber: Int,
    val state: DownloadState = DownloadState.NOT_DOWNLOADED,
    val downloadedAyahs: Int = 0,
    val totalAyahs: Int = 0,
    val progressFraction: Float = 0f,
    val errorMessage: String? = null
)

/**
 * Manages downloading and offline caching of Quran recitation audio
 * by Sheikh Mishary Rashid Alafasy.
 * Stores audio files in private app storage (zero permissions required).
 */
object QuranAudioDownloadManager {
    private const val TAG = "QuranAudioDownloader"

    private val activeDownloadJobs = ConcurrentHashMap<Int, Job>()
    private val _downloadStatuses = MutableStateFlow<Map<Int, SurahDownloadStatus>>(emptyMap())
    val downloadStatuses: StateFlow<Map<Int, SurahDownloadStatus>> = _downloadStatuses.asStateFlow()

    private val downloadScope = CoroutineScope(Dispatchers.IO)

    /**
     * Resolves the target audio file path for a specific Surah and Ayah.
     */
    fun getOfflineAudioFile(context: Context, surah: Int, ayah: Int): File {
        val surahDir = File(context.filesDir, "quran_audio/$surah")
        if (!surahDir.exists()) {
            surahDir.mkdirs()
        }
        return File(surahDir, String.format(Locale.US, "%03d%03d.mp3", surah, ayah))
    }

    /**
     * Checks if a specific verse audio is downloaded and valid (> 1KB).
     */
    fun isVerseDownloaded(context: Context, surah: Int, ayah: Int): Boolean {
        val file = getOfflineAudioFile(context, surah, ayah)
        return file.exists() && file.length() > 1024L
    }

    /**
     * Checks whether an entire Surah's recitation is downloaded offline.
     */
    fun isSurahFullyDownloaded(context: Context, surah: Int, totalVerses: Int): Boolean {
        if (totalVerses <= 0) return false
        for (ayah in 1..totalVerses) {
            if (!isVerseDownloaded(context, surah, ayah)) {
                return false
            }
        }
        return true
    }

    /**
     * Counts how many verses of a Surah are currently downloaded.
     */
    fun getDownloadedVersesCount(context: Context, surah: Int, totalVerses: Int): Int {
        if (totalVerses <= 0) return 0
        var count = 0
        for (ayah in 1..totalVerses) {
            if (isVerseDownloaded(context, surah, ayah)) {
                count++
            }
        }
        return count
    }

    /**
     * Syncs and refreshes the in-memory download status for a Surah.
     */
    fun syncSurahStatus(context: Context, surah: Int, totalVerses: Int): SurahDownloadStatus {
        val current = _downloadStatuses.value[surah]
        if (current?.state == DownloadState.DOWNLOADING) {
            return current
        }

        val downloadedCount = getDownloadedVersesCount(context, surah, totalVerses)
        val isFull = totalVerses > 0 && downloadedCount >= totalVerses
        val fraction = if (totalVerses > 0) downloadedCount.toFloat() / totalVerses.toFloat() else 0f

        val newStatus = SurahDownloadStatus(
            surahNumber = surah,
            state = if (isFull) DownloadState.DOWNLOADED else DownloadState.NOT_DOWNLOADED,
            downloadedAyahs = downloadedCount,
            totalAyahs = totalVerses,
            progressFraction = fraction
        )

        _downloadStatuses.update { it + (surah to newStatus) }
        return newStatus
    }

    /**
     * Downloads all verses of a Surah for offline playback.
     */
    fun startSurahDownload(
        context: Context,
        surah: Int,
        totalVerses: Int,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        if (activeDownloadJobs[surah]?.isActive == true) {
            Log.d(TAG, "Download already in progress for Surah $surah")
            return
        }

        val initialDownloaded = getDownloadedVersesCount(context, surah, totalVerses)
        _downloadStatuses.update {
            it + (surah to SurahDownloadStatus(
                surahNumber = surah,
                state = DownloadState.DOWNLOADING,
                downloadedAyahs = initialDownloaded,
                totalAyahs = totalVerses,
                progressFraction = if (totalVerses > 0) initialDownloaded.toFloat() / totalVerses else 0f
            ))
        }

        val job = downloadScope.launch {
            var downloaded = initialDownloaded
            var hasError = false
            var errorMessage: String? = null

            for (ayah in 1..totalVerses) {
                if (!isActive) break

                if (isVerseDownloaded(context, surah, ayah)) {
                    continue
                }

                val success = downloadSingleVerse(context, surah, ayah)
                if (success) {
                    downloaded++
                    val fraction = if (totalVerses > 0) downloaded.toFloat() / totalVerses else 0f
                    _downloadStatuses.update {
                        it + (surah to SurahDownloadStatus(
                            surahNumber = surah,
                            state = DownloadState.DOWNLOADING,
                            downloadedAyahs = downloaded,
                            totalAyahs = totalVerses,
                            progressFraction = fraction
                        ))
                    }
                } else {
                    hasError = true
                    errorMessage = "Failed to download Ayah $ayah"
                    break
                }
            }

            activeDownloadJobs.remove(surah)

            withContext(Dispatchers.Main) {
                if (hasError) {
                    _downloadStatuses.update {
                        it + (surah to SurahDownloadStatus(
                            surahNumber = surah,
                            state = DownloadState.ERROR,
                            downloadedAyahs = downloaded,
                            totalAyahs = totalVerses,
                            progressFraction = if (totalVerses > 0) downloaded.toFloat() / totalVerses else 0f,
                            errorMessage = errorMessage
                        ))
                    }
                    onComplete?.invoke(false)
                } else {
                    val isFull = downloaded >= totalVerses
                    _downloadStatuses.update {
                        it + (surah to SurahDownloadStatus(
                            surahNumber = surah,
                            state = if (isFull) DownloadState.DOWNLOADED else DownloadState.NOT_DOWNLOADED,
                            downloadedAyahs = downloaded,
                            totalAyahs = totalVerses,
                            progressFraction = if (isFull) 1.0f else (downloaded.toFloat() / totalVerses)
                        ))
                    }
                    onComplete?.invoke(isFull)
                }
            }
        }

        activeDownloadJobs[surah] = job
    }

    /**
     * Downloads an individual verse file with automatic primary and fallback CDN support.
     */
    private fun downloadSingleVerse(context: Context, surah: Int, ayah: Int): Boolean {
        val destFile = getOfflineAudioFile(context, surah, ayah)
        val tempFile = File(destFile.parentFile, String.format(Locale.US, "%03d%03d.tmp", surah, ayah))

        // Try Primary CDN
        val primaryUrl = QuranAudioPlayer.buildAudioUrl(surah, ayah, fallback = false)
        if (downloadUrlToFile(primaryUrl, tempFile)) {
            if (tempFile.renameTo(destFile)) {
                return true
            }
        }

        // Try Fallback CDN if primary failed
        val fallbackUrl = QuranAudioPlayer.buildAudioUrl(surah, ayah, fallback = true)
        if (downloadUrlToFile(fallbackUrl, tempFile)) {
            if (tempFile.renameTo(destFile)) {
                return true
            }
        }

        try { tempFile.delete() } catch (_: Exception) {}
        return false
    }

    private fun downloadUrlToFile(urlString: String, destination: File): Boolean {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 10000
                readTimeout = 15000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "BayanNotesQuran/1.0")
            }
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                Log.w(TAG, "Server returned HTTP ${connection.responseCode} for $urlString")
                return false
            }

            connection.inputStream.use { input ->
                FileOutputStream(destination).use { output ->
                    input.copyTo(output)
                }
            }

            destination.exists() && destination.length() > 1024L
        } catch (e: Exception) {
            Log.w(TAG, "Download error for $urlString: ${e.message}")
            try { destination.delete() } catch (_: Exception) {}
            false
        } finally {
            try { connection?.disconnect() } catch (_: Exception) {}
        }
    }

    /**
     * Cancels an ongoing download job for a Surah.
     */
    fun cancelDownload(surah: Int) {
        activeDownloadJobs[surah]?.cancel()
        activeDownloadJobs.remove(surah)
        _downloadStatuses.update {
            it + (surah to (it[surah]?.copy(state = DownloadState.NOT_DOWNLOADED) ?: SurahDownloadStatus(surah)))
        }
    }

    /**
     * Deletes all offline audio files for a specific Surah to free up device storage.
     */
    fun deleteSurahAudio(context: Context, surah: Int): Boolean {
        cancelDownload(surah)
        return try {
            val surahDir = File(context.filesDir, "quran_audio/$surah")
            if (surahDir.exists()) {
                surahDir.deleteRecursively()
            }
            _downloadStatuses.update {
                it + (surah to SurahDownloadStatus(surahNumber = surah, state = DownloadState.NOT_DOWNLOADED))
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting offline audio for Surah $surah", e)
            false
        }
    }
}
