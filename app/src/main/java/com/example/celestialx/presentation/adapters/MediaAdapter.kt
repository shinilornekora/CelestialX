import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.celestialx.databinding.ItemGalleryBinding
import java.io.File

class MediaAdapter(
    private var mediaFiles: MutableList<File>,
    private val onLongClick: (File) -> Unit,
    private val onClick: (File) -> Unit
) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {
    private val selectedFiles = mutableSetOf<File>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding = ItemGalleryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MediaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val file = mediaFiles[position]
        holder.bind(file)

        holder.itemView.setOnClickListener {
            onClick(file)
        }

        holder.itemView.setOnLongClickListener {
            onLongClick(file)
            true
        }

        if (selectedFiles.contains(file)) {
            holder.itemView.alpha = 0.5F
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    override fun getItemCount(): Int = mediaFiles.size

    fun updateMediaFiles(newMediaFiles: List<File>) {
        mediaFiles.clear()
        mediaFiles.addAll(newMediaFiles)
        notifyDataSetChanged()
    }

    fun updateSelectedFiles(selectedFiles: Set<File>) {
        this.selectedFiles.clear()
        this.selectedFiles.addAll(selectedFiles)
        notifyDataSetChanged()
    }

    inner class MediaViewHolder(private val binding: ItemGalleryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(file: File) {
            val uri = Uri.fromFile(file)
            Glide.with(binding.root.context)
                .load(uri)
                .into(binding.thumbnail)
        }
    }
}
