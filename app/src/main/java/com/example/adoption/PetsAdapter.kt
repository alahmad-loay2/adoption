package com.example.adoption

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PetsAdapter(var pets: List<Pet>)
    : RecyclerView.Adapter<PetsAdapter.PetViewHolder>() {

    class PetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val petName: TextView = itemView.findViewById(R.id.petName)
        val petContent: TextView = itemView.findViewById(R.id.petContent)
        val petPhone: TextView = itemView.findViewById(R.id.petPhone)
        val petImage: ImageView = itemView.findViewById(R.id.petImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.listing_item, parent, false)
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
    }

    override fun getItemCount(): Int {
        return pets.size
    }
}
