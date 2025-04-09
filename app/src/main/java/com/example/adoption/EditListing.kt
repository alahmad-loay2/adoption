package com.example.adoption

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.adoption.databinding.ActivityEditListingBinding
import com.google.firebase.firestore.FirebaseFirestore

class EditListing : AppCompatActivity() {

    private lateinit var binding: ActivityEditListingBinding
    private var petId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditListingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        petId = intent.getStringExtra("petId")
        val petName = intent.getStringExtra("petName")
        val petDescription = intent.getStringExtra("petDescription")

        binding.petNameInput.setText(petName)
        binding.petDescriptionInput.setText(petDescription)

        binding.editPetButton.setOnClickListener {
            val newName = binding.petNameInput.text.toString().trim()
            val newDescription = binding.petDescriptionInput.text.toString().trim()

            if (newName.isEmpty() || newDescription.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            petId?.let {
                val updateMap = mapOf(
                    "name" to newName,
                    "content" to newDescription
                )

                FirebaseFirestore.getInstance().collection("pets")
                    .document(it)
                    .update(updateMap)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Pet updated successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
        binding.backListing.setOnClickListener {
            val intent = Intent(this, PersonalListing::class.java)
            startActivity(intent)
            finish()
        }
    }
}