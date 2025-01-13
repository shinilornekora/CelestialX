package com.example.celestialx.presentation.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.celestialx.R
import com.example.celestialx.databinding.FullscreenMediaFragmentBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FullscreenMediaFragment : Fragment() {
    private var _binding: FullscreenMediaFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var mediaUri: Uri
    private var updateJob: Job? = null
    private var isPlaying = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FullscreenMediaFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val walker = findNavController()
        mediaUri = requireArguments().getParcelable("mediaUri")!!
        log("Preview has started. Media URI - $mediaUri")

        binding.goBack.setOnClickListener {
            walker.navigate(R.id.action_fullscreen_to_gallery)
        }

        val mimeType = requireContext().contentResolver.getType(mediaUri)
        val shouldShowPhoto = mediaUri.toString().endsWith(".jpg")
        val shouldShowVideo = mediaUri.toString().endsWith(".mp4")

        if (shouldShowPhoto) {
            log("Ready to open image...")
            binding.imageView.visibility = View.VISIBLE
            binding.videoView.visibility = View.GONE
            binding.seekBar.visibility = View.GONE
            binding.playPauseButton.visibility = View.GONE
            binding.imageView.setImageURI(mediaUri)
            return
        }

        if (shouldShowVideo) {
            log("Ready to open pre-settings process of video opening...")
            binding.imageView.visibility = View.GONE
            binding.videoView.visibility = View.VISIBLE
            binding.seekBar.visibility = View.VISIBLE
            binding.playPauseButton.visibility = View.VISIBLE
            log("Ready to setup the video...")
            setupVideoPlayer()

            return
        }

        log("No backdoors there you naughty hacker! Your file extension is: $mimeType")
    }

    private fun setupVideoPlayer() {
        log("Video player setup has started.")

        binding.videoView.setVideoURI(mediaUri)
        binding.videoView.setOnPreparedListener { mediaPlayer ->
            binding.seekBar.max = mediaPlayer.duration
            binding.seekBar.progress = 0

            log("Video player settings are going to be calculated...")

            binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        binding.videoView.seekTo(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    stopSeekBarUpdates()
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    if (binding.videoView.isPlaying) {
                        startSeekBarUpdates()
                    }
                }
            })

            binding.playPauseButton.setOnClickListener {
                log("Hey, pause/play button was clicked!")
                togglePlayback()
            }

            binding.videoView.start()
            updatePlaybackState(true)
        }

        binding.videoView.setOnCompletionListener {
            binding.videoView.seekTo(0)
            binding.seekBar.progress = 0
            updatePlaybackState(false)
        }
    }

    private fun togglePlayback() {
        val doesItPlay = binding.videoView.isPlaying

        log("Toggling the state - is it playing now? $doesItPlay")

        if (doesItPlay) {
            binding.videoView.pause()
            updatePlaybackState(false)

            return
        }

        binding.videoView.start()
        updatePlaybackState(true)
    }

    private fun updatePlaybackState(isPlayingNow: Boolean) {
        isPlaying = isPlayingNow
        binding.playPauseButton.text = if (isPlayingNow) {
            getString(R.string.pause)
        } else {
            getString(R.string.play)
        }

        if (isPlayingNow) {
            startSeekBarUpdates()
        } else {
            stopSeekBarUpdates()
        }
    }

    private fun startSeekBarUpdates() {
        stopSeekBarUpdates()
        updateJob = lifecycleScope.launch {
            while (binding.videoView.isPlaying) {
                binding.seekBar.progress = binding.videoView.currentPosition
                delay(500)
            }
        }
    }

    private fun stopSeekBarUpdates() {
        updateJob?.cancel()
        updateJob = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        stopSeekBarUpdates()
    }

    companion object {
        private val TAG = "FMF"

        fun log(message: String) {
            Log.d(TAG, message)
        }

        fun newInstance(mediaUri: Uri): FullscreenMediaFragment {
            val fragment = FullscreenMediaFragment()
            val bundle = Bundle()
            bundle.putParcelable("mediaUri", mediaUri)
            fragment.arguments = bundle
            return fragment
        }
    }
}
