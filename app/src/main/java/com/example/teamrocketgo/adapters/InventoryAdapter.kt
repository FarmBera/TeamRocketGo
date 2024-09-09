package com.example.teamrocketgo.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.teamrocketgo.DBHelper
import com.example.teamrocketgo.MainActivity
import com.example.teamrocketgo.R
import com.example.teamrocketgo.classes.My_Pokemon
import com.example.teamrocketgo.classes.Pokedex
import com.example.teamrocketgo.databinding.RecyclerInventoryBinding

class InventoryAdapter(private val myPokemons: ArrayList<My_Pokemon>) : RecyclerView.Adapter<InventoryAdapter.ViewHolder>() {

    private lateinit var binding: RecyclerInventoryBinding

    class ViewHolder(val binding: RecyclerInventoryBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = RecyclerInventoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pokemon = myPokemons.get(position)

        val imageResourceId = holder.itemView.resources.getIdentifier(Pokedex.getName(pokemon.num!!).lowercase(), "drawable", holder.itemView.context.packageName)
        Glide.with(holder.binding.root.context)
            .load(imageResourceId)
            .placeholder(R.drawable.tmp)
            .into(holder.binding.InventoryViewIcon)

        holder.binding.InventoryViewName.text = pokemon.name
        holder.binding.InventoryViewDesc.text = "Lv. " + pokemon.level.toString()

        holder.binding.InventoryContainer.setOnClickListener {
            //val intent = Intent(holder.itemView.context, MonsterInfoActivity::class.java)
            // 선택한 아이템에 대한 정보를 전달하려면 필요한 경우 아래 주석처리된 부분을 활용할 수 있습니다.
            // intent.putExtra("itemId", item.id)
            // intent.putExtra("itemName", item.text)
            //holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return myPokemons.size
    }

}
