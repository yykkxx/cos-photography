package com.cos.photography

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cos.photography.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val images = mutableListOf<Uri>()
    private val adapter = PhotoAdapter(images) { uri ->
        binding.previewImage.setImageURI(uri)
    }

    private val pickImages = registerForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            images.clear()
            images.addAll(uris.take(9))
            adapter.notifyDataSetChanged()
            binding.previewImage.setImageURI(images.first())
        } else {
            Toast.makeText(this, R.string.no_images_selected, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.photosRecycler.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        binding.photosRecycler.adapter = adapter

        binding.actionPick.setOnClickListener {
            pickImages.launch(arrayOf("image/*"))
        }

        binding.actionEdit.setOnClickListener {
            applySimpleEdit()
        }

        binding.actionGrid.setOnClickListener {
            generateNineGrid()?.let { gridUri ->
                binding.previewImage.setImageURI(gridUri)
                binding.previewImage.tag = gridUri
                Toast.makeText(this, R.string.nine_grid_done, Toast.LENGTH_SHORT).show()
            }
        }

        binding.actionShare.setOnClickListener {
            shareCurrentImage()
        }
    }

    private fun applySimpleEdit() {
        val drawable = binding.previewImage.drawable ?: run {
            Toast.makeText(this, R.string.no_preview, Toast.LENGTH_SHORT).show()
            return
        }
        val bitmap = drawable.toBitmap().copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(bitmap)
        val overlay = Paint().apply {
            color = Color.parseColor("#33FF4081")
        }
        canvas.drawRect(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat(), overlay)
        val output = saveBitmap(bitmap, "edited")
        binding.previewImage.setImageURI(output)
        binding.previewImage.tag = output
        Toast.makeText(this, R.string.edit_applied, Toast.LENGTH_SHORT).show()
    }

    private fun generateNineGrid(): Uri? {
        if (images.isEmpty()) {
            Toast.makeText(this, R.string.no_images_selected, Toast.LENGTH_SHORT).show()
            return null
        }
        val gridSize = 3
        val cellSize = 360
        val bitmap = Bitmap.createBitmap(cellSize * gridSize, cellSize * gridSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        images.take(9).forEachIndexed { index, uri ->
            val input = contentResolver.openInputStream(uri)
            val source = input?.use { BitmapFactory.decodeStream(it) } ?: return@forEachIndexed
            val scaled = Bitmap.createScaledBitmap(source, cellSize, cellSize, true)
            val x = (index % gridSize) * cellSize
            val y = (index / gridSize) * cellSize
            canvas.drawBitmap(scaled, x.toFloat(), y.toFloat(), paint)
        }

        return saveBitmap(bitmap, "nine_grid")
    }

    private fun saveBitmap(bitmap: Bitmap, prefix: String): Uri {
        val file = File(cacheDir, "$prefix-${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
        return FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
    }

    private fun shareCurrentImage() {
        val drawable = binding.previewImage.drawable ?: run {
            Toast.makeText(this, R.string.no_preview, Toast.LENGTH_SHORT).show()
            return
        }
        val uri = (binding.previewImage.tag as? Uri) ?: run {
            val bitmap = drawable.toBitmap()
            saveBitmap(bitmap, "share")
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share_with)))
    }

    private class PhotoAdapter(
        private val items: List<Uri>,
        private val onClick: (Uri) -> Unit
    ) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_photo, parent, false)
            return PhotoViewHolder(view)
        }

        override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
            val uri = items[position]
            holder.bind(uri, onClick)
        }

        override fun getItemCount(): Int = items.size

        class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val imageView: ImageView = itemView.findViewById(R.id.photoThumbnail)

            fun bind(uri: Uri, onClick: (Uri) -> Unit) {
                imageView.setImageURI(uri)
                itemView.setOnClickListener { onClick(uri) }
            }
        }
    }
}
