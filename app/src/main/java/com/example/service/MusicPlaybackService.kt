package com.example.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.BlackDudeMusicApplication
import com.example.MainActivity
import com.example.model.LocalSong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

enum class ServiceRepeatMode {
    OFF, ALL, ONE
}

data class PlaybackState(
    val currentSong: LocalSong? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val queue: List<LocalSong> = emptyList(),
    val queueIndex: Int = -1,
    val isShuffle: Boolean = false,
    val repeatMode: ServiceRepeatMode = ServiceRepeatMode.ALL,
    val isBuffering: Boolean = false
)

class MusicPlaybackService : Service(), MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var mediaPlayer: MediaPlayer? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private var progressJob: Job? = null
    private var isReceiverRegistered = false

    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                pause()
            }
        }
    }

    inner class LocalBinder : Binder() {
        fun getService(): MusicPlaybackService = this@MusicPlaybackService
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BlackDudeMusic::WakeLock")

        val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        registerReceiver(noisyReceiver, filter)
        isReceiverRegistered = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> togglePlayPause()
            ACTION_NEXT -> next()
            ACTION_PREVIOUS -> previous()
            ACTION_STOP -> stopPlayback()
        }
        return START_NOT_STICKY
    }

    fun playSong(song: LocalSong, queue: List<LocalSong> = emptyList(), index: Int = -1) {
        val repoSongs = BlackDudeMusicApplication.instance.musicRepository.currentSongs
        val activeQueue = when {
            queue.size > 1 -> queue
            repoSongs.size > 1 -> repoSongs
            queue.isNotEmpty() -> queue
            repoSongs.isNotEmpty() -> repoSongs
            else -> listOf(song)
        }

        val targetIndex = if (index in activeQueue.indices && activeQueue[index].id == song.id) {
            index
        } else {
            val found = activeQueue.indexOfFirst { it.id == song.id }
            if (found >= 0) found else 0
        }

        _playbackState.value = _playbackState.value.copy(
            currentSong = song,
            queue = activeQueue,
            queueIndex = targetIndex,
            isBuffering = true,
            currentPositionMs = 0L,
            durationMs = song.durationMs
        )

        serviceScope.launch(Dispatchers.Main) {
            stopProgressUpdates()
            releaseMediaPlayer()

            val prepared = createAndPreparePlayer(song)
            if (!prepared) {
                Log.e(TAG, "Could not prepare player for ${song.title}, attempting fallback to next song")
                _playbackState.value = _playbackState.value.copy(isBuffering = false, isPlaying = false)
                delay(600)
                if (!_playbackState.value.isPlaying && _playbackState.value.queue.size > 1) {
                    next()
                }
            }
        }
    }

    private fun releaseMediaPlayer() {
        try {
            mediaPlayer?.apply {
                try {
                    if (isPlaying) stop()
                } catch (_: Exception) {}
                try {
                    reset()
                    release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
        mediaPlayer = null
    }

    private fun createAndPreparePlayer(song: LocalSong): Boolean {
        return try {
            val player = MediaPlayer().apply {
                setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setOnPreparedListener(this@MusicPlaybackService)
                setOnCompletionListener(this@MusicPlaybackService)
                setOnErrorListener(this@MusicPlaybackService)
            }

            var attached = false

            // 1. Try ContentResolver file descriptor
            try {
                val uri = Uri.parse(song.contentUriString)
                if (song.contentUriString.startsWith("content://")) {
                    applicationContext.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                        player.setDataSource(pfd.fileDescriptor)
                        attached = true
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "ContentResolver FD failed for ${song.title}: ${e.message}")
            }

            // 2. Try content URI string directly
            if (!attached && song.contentUriString.isNotBlank()) {
                try {
                    player.setDataSource(applicationContext, Uri.parse(song.contentUriString))
                    attached = true
                } catch (e: Exception) {
                    Log.d(TAG, "Content URI direct failed for ${song.title}: ${e.message}")
                }
            }

            // 3. Try file path
            if (!attached && song.path.isNotBlank()) {
                try {
                    val file = File(song.path)
                    if (file.exists()) {
                        player.setDataSource(song.path)
                        attached = true
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "File path failed for ${song.title}: ${e.message}")
                }
            }

            if (!attached) {
                try {
                    player.release()
                } catch (_: Exception) {}
                return false
            }

            mediaPlayer = player
            player.prepareAsync()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error in createAndPreparePlayer: ${e.message}", e)
            false
        }
    }

    override fun onPrepared(mp: MediaPlayer?) {
        try {
            requestAudioFocus()
            mp?.start()
            wakeLock?.acquire(10 * 60 * 1000L /* 10 minutes */)
            _playbackState.value = _playbackState.value.copy(
                isPlaying = true,
                isBuffering = false,
                durationMs = mp?.duration?.toLong()?.takeIf { it > 0 } ?: _playbackState.value.currentSong?.durationMs ?: 0L
            )
            startProgressUpdates()
            updateNotification()

            // Record to history via repository
            _playbackState.value.currentSong?.id?.let { songId ->
                serviceScope.launch {
                    BlackDudeMusicApplication.instance.musicRepository.recordSongPlayed(songId)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting playback in onPrepared: ${e.message}", e)
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        serviceScope.launch(Dispatchers.Main) {
            val state = _playbackState.value
            when (state.repeatMode) {
                ServiceRepeatMode.ONE -> {
                    state.currentSong?.let { playSong(it, state.queue, state.queueIndex) }
                }
                ServiceRepeatMode.ALL -> {
                    next()
                }
                ServiceRepeatMode.OFF -> {
                    val repoSongs = BlackDudeMusicApplication.instance.musicRepository.currentSongs
                    val queue = when {
                        state.queue.size > 1 -> state.queue
                        repoSongs.size > 1 -> repoSongs
                        state.queue.isNotEmpty() -> state.queue
                        repoSongs.isNotEmpty() -> repoSongs
                        else -> emptyList()
                    }
                    if (queue.isEmpty()) {
                        _playbackState.value = _playbackState.value.copy(isPlaying = false, currentPositionMs = 0L)
                        stopProgressUpdates()
                        updateNotification()
                        return@launch
                    }

                    val currentIndex = if (state.queueIndex in queue.indices && queue[state.queueIndex].id == state.currentSong?.id) {
                        state.queueIndex
                    } else {
                        val found = queue.indexOfFirst { it.id == state.currentSong?.id }
                        if (found >= 0) found else 0
                    }

                    if (currentIndex + 1 < queue.size) {
                        next()
                    } else {
                        _playbackState.value = _playbackState.value.copy(isPlaying = false, currentPositionMs = 0L)
                        stopProgressUpdates()
                        updateNotification()
                    }
                }
            }
        }
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
        stopProgressUpdates()
        _playbackState.value = _playbackState.value.copy(isPlaying = false, isBuffering = false)
        releaseMediaPlayer()

        // Auto advance to next song if decoding / player error occurred
        serviceScope.launch(Dispatchers.Main) {
            delay(800)
            if (!_playbackState.value.isPlaying && _playbackState.value.queue.size > 1) {
                next()
            }
        }
        return true
    }

    fun play() {
        if (mediaPlayer != null) {
            try {
                if (!mediaPlayer!!.isPlaying) {
                    requestAudioFocus()
                    mediaPlayer?.start()
                    _playbackState.value = _playbackState.value.copy(isPlaying = true)
                    startProgressUpdates()
                    updateNotification()
                }
            } catch (e: Exception) {
                Log.w(TAG, "play() error: ${e.message}, reloading track")
                _playbackState.value.currentSong?.let {
                    playSong(it, _playbackState.value.queue, _playbackState.value.queueIndex)
                }
            }
        } else if (_playbackState.value.currentSong != null) {
            playSong(_playbackState.value.currentSong!!, _playbackState.value.queue, _playbackState.value.queueIndex)
        }
    }

    fun pause() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        } catch (_: Exception) {}
        _playbackState.value = _playbackState.value.copy(isPlaying = false)
        stopProgressUpdates()
        updateNotification()
    }

    fun togglePlayPause() {
        if (_playbackState.value.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun seekTo(positionMs: Long) {
        val clamped = positionMs.coerceIn(0L, _playbackState.value.durationMs.coerceAtLeast(1L))
        try {
            mediaPlayer?.seekTo(clamped.toInt())
        } catch (_: Exception) {}
        _playbackState.value = _playbackState.value.copy(currentPositionMs = clamped)
    }

    fun next() {
        serviceScope.launch(Dispatchers.Main) {
            val state = _playbackState.value
            val repoSongs = BlackDudeMusicApplication.instance.musicRepository.currentSongs
            val queue = when {
                state.queue.size > 1 -> state.queue
                repoSongs.size > 1 -> repoSongs
                state.queue.isNotEmpty() -> state.queue
                repoSongs.isNotEmpty() -> repoSongs
                else -> emptyList()
            }
            if (queue.isEmpty()) return@launch

            val currentIndex = if (state.queueIndex in queue.indices && queue[state.queueIndex].id == state.currentSong?.id) {
                state.queueIndex
            } else {
                val found = queue.indexOfFirst { it.id == state.currentSong?.id }
                if (found >= 0) found else 0
            }

            val nextIndex = if (state.isShuffle) {
                val unplayed = queue.indices.filter { it != currentIndex }
                if (unplayed.isNotEmpty()) unplayed.random() else (currentIndex + 1) % queue.size
            } else {
                (currentIndex + 1) % queue.size
            }

            val nextSong = queue[nextIndex]
            playSong(nextSong, queue, nextIndex)
        }
    }

    fun previous() {
        serviceScope.launch(Dispatchers.Main) {
            val state = _playbackState.value
            if (state.currentPositionMs > 3000L) {
                seekTo(0L)
                return@launch
            }
            val repoSongs = BlackDudeMusicApplication.instance.musicRepository.currentSongs
            val queue = when {
                state.queue.size > 1 -> state.queue
                repoSongs.size > 1 -> repoSongs
                state.queue.isNotEmpty() -> state.queue
                repoSongs.isNotEmpty() -> repoSongs
                else -> emptyList()
            }
            if (queue.isEmpty()) return@launch

            val currentIndex = if (state.queueIndex in queue.indices && queue[state.queueIndex].id == state.currentSong?.id) {
                state.queueIndex
            } else {
                val found = queue.indexOfFirst { it.id == state.currentSong?.id }
                if (found >= 0) found else 0
            }

            val prevIndex = if (currentIndex - 1 < 0) queue.size - 1 else currentIndex - 1
            val prevSong = queue[prevIndex]
            playSong(prevSong, queue, prevIndex)
        }
    }

    fun toggleShuffle() {
        _playbackState.value = _playbackState.value.copy(isShuffle = !_playbackState.value.isShuffle)
    }

    fun toggleRepeat() {
        val nextMode = when (_playbackState.value.repeatMode) {
            ServiceRepeatMode.OFF -> ServiceRepeatMode.ALL
            ServiceRepeatMode.ALL -> ServiceRepeatMode.ONE
            ServiceRepeatMode.ONE -> ServiceRepeatMode.OFF
        }
        _playbackState.value = _playbackState.value.copy(repeatMode = nextMode)
    }

    fun setQueue(newQueue: List<LocalSong>, startIndex: Int = 0) {
        if (newQueue.isNotEmpty() && startIndex in newQueue.indices) {
            playSong(newQueue[startIndex], newQueue, startIndex)
        } else {
            _playbackState.value = _playbackState.value.copy(queue = newQueue)
        }
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = serviceScope.launch {
            while (isActive) {
                delay(250)
                try {
                    mediaPlayer?.let { player ->
                        if (player.isPlaying) {
                            val current = player.currentPosition.toLong()
                            val total = _playbackState.value.durationMs
                            _playbackState.value = _playbackState.value.copy(currentPositionMs = current)

                            // Track completion watchdog in case native completion callback is delayed
                            if (total > 1000L && current >= total - 150L) {
                                delay(200)
                                if (isActive && _playbackState.value.isPlaying) {
                                    onCompletion(player)
                                }
                            }
                        }
                    }
                } catch (_: Exception) {}
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
    }

    private fun requestAudioFocus(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val playbackAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
                audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener { focusChange ->
                        when (focusChange) {
                            AudioManager.AUDIOFOCUS_LOSS -> pause()
                            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pause()
                            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> try { mediaPlayer?.setVolume(0.3f, 0.3f) } catch (_: Exception) {}
                            AudioManager.AUDIOFOCUS_GAIN -> {
                                try { mediaPlayer?.setVolume(1.0f, 1.0f) } catch (_: Exception) {}
                                play()
                            }
                        }
                    }
                    .build()
                val result = audioManager?.requestAudioFocus(audioFocusRequest!!) ?: AudioManager.AUDIOFOCUS_REQUEST_GRANTED
                result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            } else {
                @Suppress("DEPRECATION")
                audioManager?.requestAudioFocus(
                    { focusChange ->
                        if (focusChange == AudioManager.AUDIOFOCUS_LOSS || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                            pause()
                        }
                    },
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
                ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            }
        } catch (_: Exception) {
            true
        }
    }

    private fun updateNotification() {
        val song = _playbackState.value.currentSong ?: return
        val isPlaying = _playbackState.value.isPlaying

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prevIntent = Intent(this, MusicPlaybackService::class.java).apply { action = ACTION_PREVIOUS }
        val prevPending = PendingIntent.getService(this, 1, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val playIntent = Intent(this, MusicPlaybackService::class.java).apply { action = ACTION_PLAY_PAUSE }
        val playPending = PendingIntent.getService(this, 2, playIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val nextIntent = Intent(this, MusicPlaybackService::class.java).apply { action = ACTION_NEXT }
        val nextPending = PendingIntent.getService(this, 3, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val playPauseIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play

        val notification: Notification = NotificationCompat.Builder(this, BlackDudeMusicApplication.CHANNEL_ID)
            .setContentTitle(song.displayTitle)
            .setContentText("${song.displayArtist} • ${song.displayAlbum}")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(openAppPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(isPlaying)
            .setShowWhen(false)
            .addAction(android.R.drawable.ic_media_previous, "Previous", prevPending)
            .addAction(playPauseIcon, if (isPlaying) "Pause" else "Play", playPending)
            .addAction(android.R.drawable.ic_media_next, "Next", nextPending)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.w(TAG, "startForeground notification error: ${e.message}")
        }
    }

    private fun stopPlayback() {
        pause()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
        }
        stopProgressUpdates()
        if (isReceiverRegistered) {
            unregisterReceiver(noisyReceiver)
            isReceiverRegistered = false
        }
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
        releaseMediaPlayer()
    }

    companion object {
        const val TAG = "MusicPlaybackService"
        const val NOTIFICATION_ID = 1001

        var instance: MusicPlaybackService? = null
            private set

        const val ACTION_PLAY_PAUSE = "com.example.action.PLAY_PAUSE"
        const val ACTION_NEXT = "com.example.action.NEXT"
        const val ACTION_PREVIOUS = "com.example.action.PREVIOUS"
        const val ACTION_STOP = "com.example.action.STOP"
    }
}

