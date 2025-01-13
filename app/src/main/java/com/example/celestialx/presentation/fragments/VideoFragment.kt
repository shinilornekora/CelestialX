package com.example.celestialx.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.celestialx.MainActivity
import com.example.celestialx.R
import com.example.celestialx.data.camera.CameraHelper
import com.example.celestialx.databinding.VideoFragmentBinding

class VideoFragment : Fragment() {
    private lateinit var cameraHelper: CameraHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = VideoFragmentBinding.inflate(inflater, container, false)
        val activity = requireActivity() as MainActivity
        val walker = findNavController()

        cameraHelper = activity.cameraHelper

        binding.videoFooter.captureButton.setOnClickListener {
            cameraHelper.captureVideo(binding.videoFooter.captureButton)
        }

        binding.switchButton.setOnClickListener {
            walker.navigate(R.id.action_video_to_photo)
        }

        binding.cameraSwitcher.setOnClickListener {
            cameraHelper.switchCamera()
            binding.cameraSwitcher.scaleX = if (binding.cameraSwitcher.scaleX == 1f) -1f else 1f
        }

        binding.videoFooter.galleryButton.setOnClickListener {
            walker.navigate(R.id.action_video_to_gallery)
        }

        return binding.root
    }
}
