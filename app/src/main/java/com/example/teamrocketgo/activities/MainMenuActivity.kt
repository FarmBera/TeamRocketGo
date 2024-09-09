package com.example.teamrocketgo.activities

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.annotation.RequiresApi
import com.example.teamrocketgo.R
import com.example.teamrocketgo.databinding.ActivityMainMenuBinding

class MainMenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainMenuBinding

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        val anim_menu_to_up = AnimationUtils.loadAnimation(this, R.anim.menu_move_to_up)
        val anim_menu_to_left = AnimationUtils.loadAnimation(this, R.anim.menu_move_to_left)
        val anim_menu_to_right = AnimationUtils.loadAnimation(this, R.anim.menu_move_to_right)
        binding.menuSettings.startAnimation(anim_menu_to_up)
        binding.menuInventory.startAnimation(anim_menu_to_left)
        binding.menuPokedex.startAnimation(anim_menu_to_right)

        binding.mainMenu.setOnClickListener { MenuCloseEffect() }
        binding.menuClose.setOnClickListener { MenuCloseEffect() }

        binding.menuSettings.setOnClickListener {
            val intent = Intent(this@MainMenuActivity, SettingsActivity::class.java)
            startActivity(intent)
        }
        binding.menuInventory.setOnClickListener {
            val intent = Intent(this@MainMenuActivity, InventoryActivity::class.java)
            startActivity(intent)
        }
        binding.menuPokedex.setOnClickListener {
            val intent = Intent(this@MainMenuActivity, PokedexActivity::class.java)
            startActivity(intent)
        }

    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun MenuCloseEffect() {
        val anim_menu_to_down = AnimationUtils.loadAnimation(this, R.anim.menu_move_to_down)
        val anim_menu_to_left = AnimationUtils.loadAnimation(this, R.anim.menu_move_to_left1)
        val anim_menu_to_right = AnimationUtils.loadAnimation(this, R.anim.menu_move_to_right1)
        binding.menuSettings.startAnimation(anim_menu_to_down)
        binding.menuInventory.startAnimation(anim_menu_to_left)
        binding.menuPokedex.startAnimation(anim_menu_to_right)
        finish()
        overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE,
            R.anim.none,
            R.anim.load_fade_out
        )
    }
}