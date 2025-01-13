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
import com.example.celestialx.databinding.PhotoFragmentBinding

class PhotoFragment : Fragment() {
    private lateinit var cameraHelper: CameraHelper;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = PhotoFragmentBinding.inflate(inflater, container, false)
        val activity = requireActivity() as MainActivity
        val walker = findNavController()

        cameraHelper = activity.cameraHelper

        binding.photoFooter.captureButton.setOnClickListener {
            cameraHelper.takePhoto()
        }

        binding.switchButton.setOnClickListener {
            walker.navigate(R.id.action_photo_to_video)
        }

        binding.cameraSwitcher.setOnClickListener {
            cameraHelper.switchCamera()
        }

        binding.photoFooter.galleryButton.setOnClickListener {
            walker.navigate(R.id.action_photo_to_gallery)
        }

        return binding.root
    }
}
