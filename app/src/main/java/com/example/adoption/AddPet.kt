package com.example.adoption

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.adoption.databinding.ActivityAddPetBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

class AddPet : AppCompatActivity() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: ActivityAddPetBinding
    private lateinit var auth: FirebaseAuth
    private var selectedImageUri: Uri? = null
    private val imgbbApiKey = BuildConfig.IMGBB_API_KEY

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                binding.petImage.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddPetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        binding.petImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.backListing.setOnClickListener {
            val intent = Intent(this, Listing::class.java)
            startActivity(intent)
            finish()
        }

        binding.addPetButton.setOnClickListener {
            val name = binding.petNameInput.text.toString().trim()
            val description = binding.petDescriptionInput.text.toString().trim()
            val userId = auth.currentUser?.uid
            val imageUri = selectedImageUri

            if (userId.isNullOrEmpty() || name.isEmpty() || description.isEmpty() || imageUri == null) {
                Toast.makeText(this, "Fill all fields and select an image!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.addPetButton.isEnabled = false
            binding.addPetButton.text = "Uploading..."

            lifecycleScope.launch {
                val base64Image = convertImageToBase64(imageUri)
                if (base64Image == null) {
                    Toast.makeText(this@AddPet, "Failed to convert image", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val imageResponse = uploadToImgBB(base64Image)
                if (imageResponse == null) {
                    Toast.makeText(this@AddPet, "Failed to upload image", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val imageJson = JSONObject(imageResponse)
                val imageUrl = imageJson.getJSONObject("data").getString("url")

                firestore.collection("users").document(userId).get()
                    .addOnSuccessListener { document ->
                        val phone = document.getString("phone") ?: ""

                        val petData = hashMapOf(
                            "userId" to userId,
                            "name" to name,
                            "content" to description,
                            "phone" to phone,
                            "imageUrl" to imageUrl
                        )

                        firestore.collection("pets")
                            .add(petData)
                            .addOnSuccessListener {
                                Toast.makeText(this@AddPet, "Pet added successfully!", Toast.LENGTH_SHORT).show()
                                binding.petNameInput.text.clear()
                                binding.petDescriptionInput.text.clear()
                                val intent = Intent(this@AddPet, Listing::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@AddPet, "Failed to add pet", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this@AddPet, "Failed to get user data", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private suspend fun convertImageToBase64(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val outputStream = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (inputStream?.read(buffer).also { bytesRead = it ?: -1 } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            val imageBytes = outputStream.toByteArray()
            Base64.encodeToString(imageBytes, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun uploadToImgBB(base64Image: String): String? = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val requestBody = FormBody.Builder()
                .add("key", imgbbApiKey)
                .add("image", base64Image)
                .build()
            val request = Request.Builder()
                .url("https://api.imgbb.com/1/upload")
                .post(requestBody)
                .build()
            val response = client.newCall(request).execute()
            response.body?.string()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
