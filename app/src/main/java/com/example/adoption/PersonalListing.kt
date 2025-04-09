package com.example.adoption

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adoption.databinding.ActivityPersonalListingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PersonalListing : AppCompatActivity() {
    private lateinit var binding: ActivityPersonalListingBinding
    private val pets = mutableListOf<Pet>()
    private lateinit var adapter: PersonalPetsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPersonalListingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        adapter = PersonalPetsAdapter(this, pets)
        binding.personalPetsRecycler.layoutManager = LinearLayoutManager(this)
        binding.personalPetsRecycler.adapter = adapter

        binding.backHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        fetchItems()
    }

    override fun onResume() {
        super.onResume()
        fetchItems()
    }

    private fun fetchItems() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            FirebaseFirestore.getInstance().collection("pets")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener { result ->
                    pets.clear()
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
        }
    }
}
