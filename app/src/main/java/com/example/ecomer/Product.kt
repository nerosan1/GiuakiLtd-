package com.example.ecomer

data class Product(
    var id: String = "",
    val name: String = "",
    val type: String = "",
    val price: Long = 0,
    val image: String = ""   // chuỗi Base64
)