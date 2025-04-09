package com.example.adoption

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class PersonalPetsAdapter(
    private val context: Context,
    private val pets: MutableList<Pet>
) : RecyclerView.Adapter<PersonalPetsAdapter.PetViewHolder>() {

    class PetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val petName: TextView = itemView.findViewById(R.id.petName)
        val petContent: TextView = itemView.findViewById(R.id.petContent)
        val petPhone: TextView = itemView.findViewById(R.id.petPhone)
        val petImage: ImageView = itemView.findViewById(R.id.petImage)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
        val editButton: ImageView = itemView.findViewById(R.id.editButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.personal_listing_item, parent, false)
        return PetViewHolder(view)
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        val pet = pets[position]

        holder.petName.text = pet.name
        holder.petContent.text = pet.content
        holder.petPhone.text = "Phone: ${pet.phone}"

        Glide.with(holder.itemView.context)
            .load(pet.imageUrl)
            .into(holder.petImage)

        holder.deleteButton.setOnClickListener {
            FirebaseFirestore.getInstance().collection("pets")
                .document(pet.id)
                .delete()
                .addOnSuccessListener {
                    pets.removeAt(holder.adapterPosition)
                    notifyItemRemoved(holder.adapterPosition)

                }
        }

        holder.editButton.setOnClickListener {
            val intent = Intent(context, EditListing::class.java).apply {
                putExtra("petId", pet.id)
                putExtra("petName", pet.name)
                putExtra("petDescription", pet.content)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = pets.size

}
