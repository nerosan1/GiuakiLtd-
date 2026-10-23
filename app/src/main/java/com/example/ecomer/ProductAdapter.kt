package com.example.ecomer

import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ecomer.databinding.ItemProductBinding

class ProductAdapter(
    private var list: List<Product>,
    private val onEdit: (Product) -> Unit,
    private val onDelete: (Product) -> Unit,
    private val showActions: Boolean = true
) : RecyclerView.Adapter<ProductAdapter.VH>() {

    inner class VH(val b: ItemProductBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(h: VH, position: Int) {
        val p = list[position]
        h.b.tvName.text = "Tên sp: ${p.name}"
        h.b.tvPrice.text = "Giá sp: ${p.price}"
        h.b.tvType.text = "Loại sp: ${p.type}"
        if (p.image.isNotEmpty()) {
            if (p.image.startsWith("http://") || p.image.startsWith("https://")) {
                com.bumptech.glide.Glide.with(h.b.ivProduct)
                    .load(p.image)
                    .centerCrop()
                    .into(h.b.ivProduct)
            } else {
                try {
                    val bytes = Base64.decode(p.image, Base64.DEFAULT)
                    val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    h.b.ivProduct.setImageBitmap(bitmap)
                } catch (_: IllegalArgumentException) {
                    h.b.ivProduct.setImageDrawable(null)
                }
            }
        } else {
            h.b.ivProduct.setImageDrawable(null)
        }
        val actionVisibility = if (showActions) View.VISIBLE else View.GONE
        h.b.btnEdit.visibility = actionVisibility
        h.b.btnDelete.visibility = actionVisibility
        h.b.btnEdit.setOnClickListener { onEdit(p) }
        h.b.btnDelete.setOnClickListener { onDelete(p) }
    }

    override fun getItemCount() = list.size

    fun submit(newList: List<Product>) {
        list = newList
        notifyDataSetChanged()
    }
}
