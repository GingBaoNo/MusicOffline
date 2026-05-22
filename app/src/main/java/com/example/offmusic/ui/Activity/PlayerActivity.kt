package com.example.offmusic.ui.Activity

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.net.Uri
import android.os.Bundle
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.offmusic.R
import com.example.offmusic.databinding.ActivityPlayerBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private var player: ExoPlayer? = null

    // Animator để làm đĩa nhạc xoay tròn
    private var discAnimator: ObjectAnimator? = null
    private var isUserTrackingSeekBar = false

    // Danh sách lưu trữ hàng đợi phát nhạc nhận từ MainActivity/Fragment
    private var songPaths: List<String> = emptyList()
    private var songTitles: List<String> = emptyList()
    private var songArtists: List<String> = emptyList()
    private var songIds: List<String> = emptyList()
    private var clickedPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Nhận toàn bộ hàng đợi phát nhạc và vị trí bài hát được bấm từ Intent
        songPaths = intent.getStringArrayListExtra("LIST_PATHS") ?: emptyList()
        songTitles = intent.getStringArrayListExtra("LIST_TITLES") ?: emptyList()
        songArtists = intent.getStringArrayListExtra("LIST_ARTISTS") ?: emptyList()
        songIds = intent.getStringArrayListExtra("LIST_IDS") ?: emptyList()
        clickedPosition = intent.getIntExtra("CLICKED_POSITION", 0)

        // 🔥 ĐỒNG BỘ UI NGAY LẬP TỨC: Ép hiển thị tên bài hát/ca sĩ đã bấm ngay khi mở màn hình,
        // không để giao diện bị hiện chữ mặc định ban đầu.
        if (songTitles.isNotEmpty() && songArtists.isNotEmpty()) {
            binding.tvPlayerTitle.text = songTitles.getOrNull(clickedPosition) ?: "Unknown Title"
            binding.tvPlayerArtist.text = songArtists.getOrNull(clickedPosition) ?: "Unknown Artist"
        }

        setupDiscAnimation()

        initializePlayerWithQueue()

        setupControlButtons()

        setupSeekBar()
    }

    private fun initializePlayerWithQueue() {
        player = ExoPlayer.Builder(this).build().apply {
            // Chuyển đổi mảng dữ liệu thô nhận được thành List<MediaItem> để nạp vào ExoPlayer
            val mediaItems = songPaths.mapIndexed { index, path ->
                MediaItem.Builder()
                    .setMediaId(songIds.getOrNull(index) ?: "")
                    .setUri(Uri.parse(path))
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(songTitles.getOrNull(index) ?: "Unknown Title")
                            .setArtist(songArtists.getOrNull(index) ?: "Unknown Artist")
                            .build()
                    )
                    .build()
            }

            setMediaItems(mediaItems)

            seekTo(clickedPosition, 0L)

            prepare()
            play()
        }

        player?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    binding.fabPlayPause.setImageResource(android.R.drawable.ic_media_pause)
                    discAnimator?.resume()
                    updateSeekBarProgress()
                } else {
                    binding.fabPlayPause.setImageResource(android.R.drawable.ic_media_play)
                    discAnimator?.pause()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)

                player?.let {
                    clickedPosition = it.currentMediaItemIndex
                }

                mediaItem?.mediaMetadata?.let { metadata ->
                    binding.tvPlayerTitle.text = metadata.title ?: "Unknown Title"
                    binding.tvPlayerArtist.text = metadata.artist ?: "Unknown Artist"
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    val duration = player?.duration ?: 0L
                    binding.seekBar.max = duration.toInt()
                    binding.tvTotalTime.text = formatTime(duration)
                }
            }
        })
    }

    private fun setupControlButtons() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.fabPlayPause.setOnClickListener {
            player?.let {
                if (it.isPlaying) it.pause() else it.play()
            }
        }

        binding.btnNext.setOnClickListener {
            player?.let {
                if (it.hasNextMediaItem()) {
                    it.seekToNextMediaItem()
                } else {
                    Toast.makeText(this, "Đã đến bài cuối cùng trong danh sách", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnPrev.setOnClickListener {
            player?.let {
                if (it.currentPosition > 3000) {
                    it.seekTo(0L)
                } else if (it.hasPreviousMediaItem()) {
                    it.seekToPreviousMediaItem()
                } else {
                    Toast.makeText(this, "Đã ở bài đầu tiên trong danh sách", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnShuffle.setOnClickListener {
            player?.let {
                val isShuffleOn = !it.shuffleModeEnabled
                it.shuffleModeEnabled = isShuffleOn

                if (isShuffleOn) {
                    binding.btnShuffle.setColorFilter(ContextCompat.getColor(this, R.color.color_active))
                    Toast.makeText(this, "Đã bật chế độ trộn bài ngẫu nhiên", Toast.LENGTH_SHORT).show()
                } else {
                    binding.btnShuffle.setColorFilter(ContextCompat.getColor(this, R.color.text_secondary))
                    Toast.makeText(this, "Đã tắt chế độ trộn bài", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnRepeat.setOnClickListener {
            player?.let {
                when (it.repeatMode) {
                    Player.REPEAT_MODE_OFF -> {
                        it.repeatMode = Player.REPEAT_MODE_ALL
                        binding.btnRepeat.setColorFilter(ContextCompat.getColor(this, R.color.color_active))
                        Toast.makeText(this, "Lặp lại toàn bộ danh sách phát", Toast.LENGTH_SHORT).show()
                    }
                    Player.REPEAT_MODE_ALL -> {
                        it.repeatMode = Player.REPEAT_MODE_ONE
                        binding.btnRepeat.setColorFilter(ContextCompat.getColor(this, R.color.color_active)) // Có thể đổi màu khác nếu muốn tách biệt
                        Toast.makeText(this, "Lặp lại bài hát hiện tại", Toast.LENGTH_SHORT).show()
                    }
                    Player.REPEAT_MODE_ONE -> {
                        it.repeatMode = Player.REPEAT_MODE_OFF
                        binding.btnRepeat.setColorFilter(ContextCompat.getColor(this, R.color.text_secondary))
                        Toast.makeText(this, "Đã tắt chế độ lặp bài", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupSeekBar() {
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {

                    binding.tvCurrentTime.text = formatTime(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUserTrackingSeekBar = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isUserTrackingSeekBar = false
                seekBar?.let {
                    player?.seekTo(it.progress.toLong())
                }
            }
        })
    }

    private fun updateSeekBarProgress() {
        lifecycleScope.launch {
            while (player?.isPlaying == true && !isUserTrackingSeekBar) {
                val currentPosition = player?.currentPosition ?: 0L
                binding.seekBar.progress = currentPosition.toInt()
                binding.tvCurrentTime.text = formatTime(currentPosition)
                delay(1000)
            }
        }
    }

    private fun setupDiscAnimation() {
        discAnimator = ObjectAnimator.ofFloat(binding.cvAlbumArt, "rotation", 0f, 360f).apply {
            duration = 15000
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
        }
        discAnimator?.start()
        discAnimator?.pause()
    }

    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        discAnimator?.cancel()
        player?.release()
        player = null
    }
}