package com.example.celestialx.presentation.adapters

import android.media.browse.MediaBrowser
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.celestialx.databinding.ItemGalleryBinding
import com.bumptech.glide.Glide

class GalleryAdapter(val items: List<MediaBrowser.MediaItem>, private val onClick: (MediaBrowser.MediaItem) -> Unit) :
    RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val binding = ItemGalleryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GalleryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class GalleryViewHolder(private val binding: ItemGalleryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MediaBrowser.MediaItem) {
            Glide.with(binding.hedgehogImage.context)
                .load(item.description.iconUri)
                .into(binding.hedgehogImage)

            binding.galleryTitle.text = item.description.title
            binding.root.setOnClickListener { onClick(item) }
        }
    }
}
