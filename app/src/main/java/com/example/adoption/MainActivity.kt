package com.example.adoption

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.adoption.databinding.ActivityLoginBinding
import com.example.adoption.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val username = document.getString("username")
                        binding.usernameGreeting.text = "Hello, $username!"
                    } else {
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error fetching username: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        binding.viewYourListingsButton.setOnClickListener{
            val intent = Intent(this, PersonalListing::class.java)
            startActivity(intent)
        }

        binding.seeAll.setOnClickListener{
            startActivity(Intent(this, Listing::class.java))
        }

        binding.deleteAccountButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        binding.logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, login::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun showDeleteConfirmationDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
            .setMessage("Are you sure you want to delete your account?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                deleteAccount()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }

        val alert = dialogBuilder.create()
        alert.show()
    }

    private fun deleteAccount() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            val userId = currentUser.uid
            FirebaseFirestore.getInstance().collection("pets")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        FirebaseFirestore.getInstance().collection("pets")
                            .document(document.id)
                            .delete()
                    }
                    FirebaseFirestore.getInstance().collection("users")
                        .document(userId)
                        .delete()
                        .addOnSuccessListener {
                            currentUser.delete()
                                .addOnSuccessListener {
                                    FirebaseAuth.getInstance().signOut()

                                    Toast.makeText(this, "Account, listings, and user data deleted", Toast.LENGTH_SHORT).show()

                                    val intent = Intent(this, login::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Error deleting account:", Toast.LENGTH_LONG).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error deleting user data", Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error deleting listings", Toast.LENGTH_LONG).show()
                }
        }
    }

}