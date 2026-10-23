package com.example.ecomer

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecomer.databinding.ActivityUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/** User thường: chỉ xem danh sách (phân quyền). */
class UserActivity : AppCompatActivity() {
    private lateinit var b: ActivityUserBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        b = ActivityUserBinding.inflate(layoutInflater)
        setContentView(b.root)

        adapter = ProductAdapter(emptyList(), {}, {}, showActions = false)
        b.rvProducts.layoutManager = LinearLayoutManager(this)
        b.rvProducts.adapter = adapter

        b.btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        db.collection("products").addSnapshotListener { snap, err ->
            if (err != null) {
                Toast.makeText(this, "Lỗi đọc dữ liệu: ${err.message}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }
            val items = snap?.documents?.mapNotNull { d ->
                d.toObject(Product::class.java)?.also { it.id = d.id }
            } ?: emptyList()
            adapter.submit(items)
        }
    }
}
