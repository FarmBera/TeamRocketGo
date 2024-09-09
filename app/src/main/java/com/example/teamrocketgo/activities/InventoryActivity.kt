package com.example.teamrocketgo.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teamrocketgo.DBHelper
import com.example.teamrocketgo.MainActivity
import com.example.teamrocketgo.adapters.InventoryAdapter
import com.example.teamrocketgo.classes.ColorSet
import com.example.teamrocketgo.classes.My_Pokemon
import com.example.teamrocketgo.databinding.ActivityInventoryBinding

class InventoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInventoryBinding
    private lateinit var adapter: InventoryAdapter

    private lateinit var db: DBHelper
    private var myPokemons: ArrayList<My_Pokemon> = ArrayList<My_Pokemon>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "My Inventory"

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val toolbar: Toolbar = binding.toolbar
        setSupportActionBar(toolbar) // 액션바로 설정
        supportActionBar?.apply {
            title = "My Pokemon" // 원하는 툴바 제목으로 설정
        }

        binding.toolbar.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
//            finish()
//            Toast.makeText(applicationContext, "Clicked", Toast.LENGTH_SHORT).show()
        }

        db = DBHelper.getInstance(this)!!
        myPokemons = db.MP_GetAll()

        adapter = InventoryAdapter(myPokemons)

        binding.RecyclerInventory.layoutManager = LinearLayoutManager(this)
        binding.RecyclerInventory.adapter = adapter

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
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