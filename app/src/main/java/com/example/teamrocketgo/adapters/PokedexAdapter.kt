package com.example.teamrocketgo.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.teamrocketgo.R
import com.example.teamrocketgo.classes.Pokedex
import com.example.teamrocketgo.classes.Pokedex_Pokemon
import com.example.teamrocketgo.databinding.RecyclerPokedexBinding

class PokedexAdapter(private val pokedex: ArrayList<Pokedex_Pokemon>): RecyclerView.Adapter<PokedexAdapter.ViewHolder>() {

    class ViewHolder(val binding: RecyclerPokedexBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecyclerPokedexBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pokemon = pokedex.get(position)

        if (pokemon.seen == 0)
        {
            Glide.with(holder.binding.root.context)
                .load(R.drawable.tmp)
                .into(holder.binding.libIcon)
        }
        else
        {
            val imageResourceId = holder.itemView.resources.getIdentifier(Pokedex.getName(pokemon.num!!).lowercase(), "drawable", holder.itemView.context.packageName)
            Glide.with(holder.binding.root.context)
                .load(imageResourceId)
                .into(holder.binding.libIcon)
        }

    }

    override fun getItemCount(): Int {
        return pokedex.size
    }
}