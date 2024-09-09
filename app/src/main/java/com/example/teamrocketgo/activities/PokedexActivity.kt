package com.example.teamrocketgo.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teamrocketgo.DBHelper
import com.example.teamrocketgo.adapters.PokedexAdapter
import com.example.teamrocketgo.classes.ColorSet
import com.example.teamrocketgo.classes.Pokedex_Pokemon
import com.example.teamrocketgo.databinding.ActivityPokedexBinding

class PokedexActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPokedexBinding
    private lateinit var adapter: PokedexAdapter

    private lateinit var db: DBHelper
    private var pokedex: ArrayList<Pokedex_Pokemon> = ArrayList<Pokedex_Pokemon>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPokedexBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "Pokedex";

        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        db = DBHelper.getInstance(this)!!
        pokedex = db.DEX_GetAll()

        adapter = PokedexAdapter(pokedex)

        binding.RecyclerPokedex.layoutManager = LinearLayoutManager(this)
        binding.RecyclerPokedex.adapter = adapter

    }

    override fun onStart() {
        super.onStart()

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val backColor = sharedPreferences.getString("backcolor", "0")

        val colorsetup = ColorSet()
        val COLOR = colorsetup.ColorSetup(backColor!!)
        binding.background.setBackgroundColor(
            ContextCompat.getColor(applicationContext, COLOR)
        )
    }
}