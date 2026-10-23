package com.example.ecomer

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ecomer.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var b: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(b.root)

        // Đã đăng nhập từ trước thì vào thẳng
        auth.currentUser?.let { routeByRole(it.uid) }

        b.btnLogin.setOnClickListener {
            val email = b.etEmail.text.toString().trim()
            val pass = b.etPassword.text.toString()
            if (email.isEmpty() || pass.isEmpty()) {
                toast("Nhập email và mật khẩu"); return@setOnClickListener
            }
            auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener { routeByRole(it.user!!.uid) }
                .addOnFailureListener { toast("Sai email hoặc mật khẩu") }
        }

        b.btnRegister.setOnClickListener {
            val email = b.etEmail.text.toString().trim()
            val pass = b.etPassword.text.toString()
            if (email.isEmpty() || pass.length < 6) {
                toast("Email không được trống, mật khẩu ít nhất 6 ký tự"); return@setOnClickListener
            }
            auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener { result ->
                    val uid = result.user!!.uid
                    db.collection("users").document(uid)
                        .set(mapOf("role" to "user"))
                        .addOnSuccessListener { routeByRole(uid) }
                        .addOnFailureListener { toast("Lỗi lưu quyền: ${it.message}") }
                }
                .addOnFailureListener { toast(it.message ?: "Lỗi đăng ký") }
        }
    }

    private fun routeByRole(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val role = doc.getString("role") ?: "user"
                val target = if (role == "admin") MainActivity::class.java
                else UserActivity::class.java
                startActivity(Intent(this, target))
                finish()
            }
            .addOnFailureListener { toast("Không đọc được quyền: ${it.message}") }
    }

    private fun toast(m: String) = Toast.makeText(this, m, Toast.LENGTH_SHORT).show()
}