package com.example.celestialx.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.celestialx.MainActivity
import com.example.celestialx.R
import com.example.celestialx.data.camera.CameraHelper
import com.example.celestialx.databinding.VideoFragmentBinding

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView

class VideoFragment : Fragment() {
    private lateinit var cameraHelper: CameraHelper
    private var timerHandler: Handler? = null
    private var recordingStartTime: Long = 0
    private var isRecording = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = VideoFragmentBinding.inflate(inflater, container, false)
        val activity = requireActivity() as MainActivity
        val walker = findNavController()

        cameraHelper = activity.cameraHelper

        val timerTextView: TextView = binding.timer

        binding.videoFooter.captureButton.setOnClickListener {
            if (isRecording) {
                binding.videoFooter.captureButton.apply {
                    setImageResource(R.drawable.circle_156)
                }
                stopRecording(timerTextView)
            } else {
                binding.videoFooter.captureButton.apply {
                    setImageResource(R.drawable.circle_156_red)
                }
                startRecording(timerTextView)
            }
        }

        binding.switchButton.setOnClickListener {
            walker.navigate(R.id.action_video_to_photo)
        }

        binding.videoFooter.cameraSwitcher.setOnClickListener {
            cameraHelper.switchCamera()
            binding.videoFooter.cameraSwitcher.scaleX = if (binding.videoFooter.cameraSwitcher.scaleX == 1f) -1f else 1f
        }

        binding.videoFooter.galleryButton.setOnClickListener {
            walker.navigate(R.id.action_video_to_gallery)
        }

        return binding.root
    }

    private fun startRecording(timerTextView: TextView) {
        cameraHelper.captureVideo()
        timerTextView.visibility = View.VISIBLE

        isRecording = true
        recordingStartTime = System.currentTimeMillis()
        timerHandler = Handler(Looper.getMainLooper())

        timerHandler?.post(object : Runnable {
            override fun run() {
                val elapsedTime = System.currentTimeMillis() - recordingStartTime
                timerTextView.text = formatElapsedTime(elapsedTime)
                if (isRecording) {
                    timerHandler?.postDelayed(this, 1000)
                }
            }
        })
    }

    private fun stopRecording(timerTextView: TextView) {
        cameraHelper.captureVideo()
        isRecording = false
        timerHandler?.removeCallbacksAndMessages(null)
        timerTextView.text = "00:00"
        timerTextView.visibility = View.GONE
    }

    private fun formatElapsedTime(elapsedTimeMillis: Long): String {
        val seconds = (elapsedTimeMillis / 1000) % 60
        val minutes = (elapsedTimeMillis / 1000) / 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timerHandler?.removeCallbacksAndMessages(null)
    }
}
