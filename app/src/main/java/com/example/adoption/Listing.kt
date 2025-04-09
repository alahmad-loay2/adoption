package com.example.adoption

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adoption.databinding.ActivityListingBinding
import com.google.firebase.firestore.FirebaseFirestore

class Listing : AppCompatActivity() {
    private lateinit var binding: ActivityListingBinding
    private val pets = mutableListOf<Pet>()
    private lateinit var adapter: PetsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityListingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        adapter = PetsAdapter(pets)
        binding.petsRecycler.layoutManager = LinearLayoutManager(this)
        binding.petsRecycler.adapter = adapter

        FirebaseFirestore.getInstance().collection("pets").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val pet = Pet(
                        id = document.id,
                        userId = document.getString("userId") ?: "",
                        name = document.getString("name") ?: "",
                        content = document.getString("content") ?: "",
                        phone = document.getString("phone") ?: "",
                        imageUrl = document.getString("imageUrl") ?: ""
                    )
                    pets.add(pet)
                }
                adapter.notifyDataSetChanged()
            }

        binding.addBtn.setOnClickListener{
            val intent = Intent(this, AddPet::class.java)
            startActivity(intent)
            finish()
        }

        binding.backHome.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}