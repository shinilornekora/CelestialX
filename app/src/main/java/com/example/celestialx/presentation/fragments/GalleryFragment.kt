package com.example.celestialx.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.celestialx.databinding.GalleryFragmentBinding
import com.example.celestialx.presentation.adapters.GalleryAdapter

class GalleryFragment : Fragment() {

    private var _binding: GalleryFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = GalleryFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()
        val adapter = GalleryAdapter(emptyList()) { item ->
            Toast.makeText(context, item.mediaId, Toast.LENGTH_SHORT).show()
        }

        binding.galleryRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.galleryRecyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}