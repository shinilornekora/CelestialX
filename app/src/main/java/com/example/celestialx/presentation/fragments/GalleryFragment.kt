package com.example.celestialx.presentation.fragments

import MediaAdapter
import android.content.ContentResolver
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.celestialx.R
import com.example.celestialx.databinding.GalleryFragmentBinding
import java.io.File

class GalleryFragment : Fragment() {
    private var _binding: GalleryFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var mediaAdapter: MediaAdapter
    private val selectedFiles = mutableSetOf<File>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = GalleryFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupGallery()
    }

    private fun setupGallery() {
        val mediaFiles = getMediaFiles()
        val walker = findNavController()

        if (mediaFiles.isEmpty()) {
            Toast.makeText(requireContext(), "Ничего не найдено!", Toast.LENGTH_SHORT).show()
        }

        mediaAdapter = MediaAdapter(mediaFiles,
            { file ->
                selectFileWithLongPress(file)
            },
            { file ->
                openFileInFullScreen(file)
                Toast.makeText(requireContext(), "Выбрано: ${file.name}", Toast.LENGTH_SHORT).show()
            }
        )

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = mediaAdapter
        }

        binding.photoButton.setOnClickListener {
            walker.navigate(R.id.action_gallery_to_photo)
        }

        binding.videoButton.setOnClickListener {
            walker.navigate(R.id.action_gallery_to_video)
        }

        binding.deleteButton.setOnClickListener {
            deleteSelectedFiles()
        }

        binding.deleteButton.visibility = if (selectedFiles.isEmpty()) View.GONE else View.VISIBLE

    }

    private fun selectFileWithLongPress(file: File) {
        if (selectedFiles.contains(file)) {
            selectedFiles.remove(file)
        } else {
            selectedFiles.add(file)
        }

        binding.deleteButton.visibility = if (selectedFiles.isEmpty()) View.GONE else View.VISIBLE
        mediaAdapter.updateSelectedFiles(selectedFiles)
    }

    private fun openFileInFullScreen(file: File) {
        val uri = Uri.fromFile(file)

        findNavController().navigate(
            R.id.action_gallery_to_preview,
            Bundle().apply {
                putParcelable("mediaUri", uri)
            }
        )
    }

    private fun deleteSelectedFiles() {
        selectedFiles.forEach { file ->
            if (file.exists()) {
                file.delete()
            }
        }

        selectedFiles.clear()
        val updatedMediaFiles = getMediaFiles()
        mediaAdapter.updateMediaFiles(updatedMediaFiles)

        binding.deleteButton.visibility = View.GONE
        Toast.makeText(requireContext(), "Выбранные файлы удалены", Toast.LENGTH_SHORT).show()
    }


    private fun openFileWithIntent(file: File) {
        val uri = Uri.fromFile(file)

        val mimeType = when (file.extension) {
            "jpg", "jpeg", "png" -> "image/*"
            "mp4" -> "video/*"
            else -> "*/*"
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Нет доступных приложений для открытия файла", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getMediaFiles(): MutableList<File> {
        val mediaFiles = mutableListOf<File>()

        val imageUri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val imageProjection = arrayOf(
            MediaStore.Images.Media.DATA
        )

        val videoUri: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val videoProjection = arrayOf(
            MediaStore.Video.Media.DATA
        )

        mediaFiles.addAll(queryMedia(imageUri, imageProjection))
        mediaFiles.addAll(queryMedia(videoUri, videoProjection))

        return mediaFiles
    }

    private fun queryMedia(uri: Uri, projection: Array<String>): List<File> {
        val mediaFiles = mutableListOf<File>()
        val resolver: ContentResolver = requireContext().contentResolver

        val cursor: Cursor? = resolver.query(uri, projection, null, null, null)

        cursor?.use {
            val columnIndex = it.getColumnIndexOrThrow(projection[0])
            while (it.moveToNext()) {
                val filePath = it.getString(columnIndex)
                val file = File(filePath)
                if (file.exists()) {
                    mediaFiles.add(file)
                }
            }
        }

        return mediaFiles
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
