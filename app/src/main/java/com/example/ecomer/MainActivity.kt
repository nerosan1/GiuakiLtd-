package com.example.ecomer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecomer.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var b: ActivityMainBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: ProductAdapter
    private var existingImage = ""
    private var editingId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uid = auth.currentUser?.uid
        if (uid == null) {
            goLogin()
            return
        }

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.getString("role") != "admin") {
                    startActivity(Intent(this, UserActivity::class.java))
                    finish()
                    return@addOnSuccessListener
                }
                showAdminScreen()
            }
            .addOnFailureListener {
                toast("Không kiểm tra được quyền: ${it.message}")
                goLogin()
            }
    }

    private fun showAdminScreen() {
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        adapter = ProductAdapter(emptyList(), ::startEdit, ::deleteProduct)
        b.rvProducts.layoutManager = LinearLayoutManager(this)
        b.rvProducts.adapter = adapter

        b.btnSave.setOnClickListener { saveProduct() }
        b.btnLogout.setOnClickListener {
            auth.signOut()
            goLogin()
        }
        listenProducts()
    }

    private fun listenProducts() {
        db.collection("products").addSnapshotListener { snap, err ->
            if (err != null) {
                toast("Lỗi đọc Firestore: ${err.message}")
                return@addSnapshotListener
            }
            val items = snap?.documents?.mapNotNull { d ->
                d.toObject(Product::class.java)?.also { it.id = d.id }
            } ?: emptyList()
            adapter.submit(items)
        }
    }

    private fun saveProduct() {
        val name = b.etName.text.toString().trim()
        val type = b.etType.text.toString().trim()
        val price = b.etPrice.text.toString().toLongOrNull()
        if (name.isEmpty() || type.isEmpty() || price == null) {
            toast("Nhập đủ tên, loại và giá")
            return
        }
        val imageUrl = b.etImageUrl.text.toString().trim()
        val image = imageUrl.ifEmpty { existingImage }
        if (image.isEmpty()) {
            toast("Nhập link ảnh sản phẩm")
            return
        }
        if (imageUrl.isNotEmpty()) {
            val uri = Uri.parse(imageUrl)
            if (uri.scheme !in listOf("http", "https") || uri.host.isNullOrBlank()) {
                toast("Link ảnh phải bắt đầu bằng http:// hoặc https://")
                return
            }
        }
        val data = hashMapOf(
            "name" to name,
            "type" to type,
            "price" to price,
            "image" to image
        )
        val id = editingId
        val task = if (id == null) db.collection("products").add(data)
        else db.collection("products").document(id).set(data)
        task.addOnSuccessListener {
            toast(if (id == null) "Đã thêm sản phẩm" else "Đã cập nhật")
            clearForm()
        }.addOnFailureListener { toast("Lỗi: ${it.message}") }
    }

    private fun startEdit(p: Product) {
        editingId = p.id
        b.etName.setText(p.name)
        b.etType.setText(p.type)
        b.etPrice.setText(p.price.toString())
        existingImage = p.image
        b.etImageUrl.setText(if (p.image.startsWith("http://") || p.image.startsWith("https://")) p.image else "")
        b.btnSave.text = "CẬP NHẬT"
    }

    private fun deleteProduct(p: Product) {
        AlertDialog.Builder(this)
            .setMessage("Xóa ${p.name}?")
            .setPositiveButton("Xóa") { _, _ ->
                db.collection("products").document(p.id).delete()
                    .addOnFailureListener { toast("Lỗi xóa: ${it.message}") }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun clearForm() {
        editingId = null
        existingImage = ""
        b.etName.text.clear()
        b.etType.text.clear()
        b.etPrice.text.clear()
        b.etImageUrl.text.clear()
        b.btnSave.text = "THÊM SẢN PHẨM"
    }

    private fun goLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun toast(m: String) = Toast.makeText(this, m, Toast.LENGTH_SHORT).show()
}
