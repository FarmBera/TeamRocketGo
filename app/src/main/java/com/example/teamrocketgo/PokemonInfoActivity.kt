package com.example.teamrocketgo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.teamrocketgo.activities.ProfileActivity
import com.example.teamrocketgo.classes.My_Pokemon
import com.example.teamrocketgo.databinding.ActivityPokemonInfoBinding


class PokemonInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPokemonInfoBinding

    private lateinit var db: DBHelper
    private lateinit var pokemon: My_Pokemon

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPokemonInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)


        db = DBHelper.getInstance(this)!!

        val id = intent.getStringExtra("id")

        if (id == "0") pokemon = db.MP_GetNew()
        else pokemon = db.MP_Get(id!!)



        when (pokemon.num) {
            1 -> binding.pkmInfoPokemon.setImageResource(R.drawable.pikachu)
            2 -> binding.pkmInfoPokemon.setImageResource(R.drawable.squirtle)
            3 -> binding.pkmInfoPokemon.setImageResource(R.drawable.charizard)
            4 -> binding.pkmInfoPokemon.setImageResource(R.drawable.rayquaza)
        }

        binding.pkmInfoLv.text = pokemon.level.toString()

        binding.pkmInfoName.text = pokemon.name

        binding.pkmInfoHp.text = pokemon.hp.toString()

        binding.pkmInfoAtk.text = pokemon.attack.toString()

        binding.pkmInfoDef.text = pokemon.defense.toString()


        binding.pkmInfoExit.setOnClickListener {
            if (id == "0")
            {
                val intent = Intent(this@PokemonInfoActivity, MainActivity::class.java)
                startActivity(intent)
            }
            else finish()
        }



    }
}