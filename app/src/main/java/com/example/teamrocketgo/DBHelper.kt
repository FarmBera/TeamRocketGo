package com.example.teamrocketgo

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.teamrocketgo.classes.My_Pokemon
import com.example.teamrocketgo.classes.Pokedex
import com.example.teamrocketgo.classes.Pokedex_Pokemon
import com.example.teamrocketgo.classes.Wild_Pokemon
import java.time.LocalDateTime

class DBHelper(context: Context?): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private var sInstance: DBHelper? = null
        @Synchronized
        fun getInstance(context: Context): DBHelper? {
            if (sInstance == null) {
                sInstance = DBHelper(context.applicationContext)
            }
            return sInstance!!
        }

        const val DATABASE_NAME = "pokemonDB"
        const val DATABASE_VERSION = 1

        const val WILD_POKEMON_TABLE = "Wild_pokemon"
        const val WILD_POKEMON_ID = "id"
        const val WILD_POKEMON_NUM = "num"
        const val WILD_POKEMON_LATITUDE = "latitude"
        const val WILD_POKEMON_LONGITUDE = "longitude"
        const val WILD_POKEMON_TIME = "time"
        const val WILD_POKEMON_LEVEL = "level"
        const val WILD_POKEMON_HP = "hp"
        const val WILD_POKEMON_ATTACK = "attack"
        const val WILD_POKEMON_DEFENSE = "defense"

        const val MY_POKEMON_TABLE = "My_pokemon"
        const val MY_POKEMON_ID = "id"
        const val MY_POKEMON_NUM = "num"
        const val MY_POKEMON_NAME = "name"
        const val MY_POKEMON_FAVORITE = "favorite"
        const val MY_POKEMON_LATITUDE = "latitude"
        const val MY_POKEMON_LONGITUDE = "longitude"
        const val MY_POKEMON_TIME = "time"
        const val MY_POKEMON_LEVEL = "level"
        const val MY_POKEMON_EXP = "exp"
        const val MY_POKEMON_HP = "hp"
        const val MY_POKEMON_CURRENT_HP = "current_hp"
        const val MY_POKEMON_ATTACK = "attack"
        const val MY_POKEMON_DEFENSE = "defense"

        const val POKEDEX_TABLE = "Pokedex"
        const val POKEDEX_ID = "id"
        const val POKEDEX_NUM = "num"
        const val POKEDEX_SEEN = "seen"
        const val POKEDEX_CAUGHT = "caught"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val SQLQuery =
            "CREATE TABLE " + WILD_POKEMON_TABLE +
                    "(" +
                    WILD_POKEMON_ID + " INTEGER PRIMARY KEY, " +
                    WILD_POKEMON_NUM + " INTEGER, " +
                    WILD_POKEMON_LATITUDE + " DOUBLE, " +
                    WILD_POKEMON_LONGITUDE + " DOUBLE, " +
                    WILD_POKEMON_TIME + " DATETIME, " +
                    WILD_POKEMON_LEVEL + " INTEGER, " +
                    WILD_POKEMON_HP + " INTEGER, " +
                    WILD_POKEMON_ATTACK + " INTEGER, " +
                    WILD_POKEMON_DEFENSE + " INTEGER" +
                    ")"
        db?.execSQL(SQLQuery);

        val SQLQuery2 =
            "CREATE TABLE " + MY_POKEMON_TABLE +
                    "(" +
                    MY_POKEMON_ID + " INTEGER PRIMARY KEY," +
                    MY_POKEMON_NUM + " INTEGER," +
                    MY_POKEMON_NAME + " TEXT," +
                    MY_POKEMON_FAVORITE + " INTEGER DEFAULT 0," +
                    MY_POKEMON_LATITUDE + " DOUBLE," +
                    MY_POKEMON_LONGITUDE + " DOUBLE," +
                    MY_POKEMON_TIME + " DATETIME," +
                    MY_POKEMON_LEVEL + " INTEGER," +
                    MY_POKEMON_EXP + " INTEGER DEFAULT 0," +
                    MY_POKEMON_HP + " INTEGER," +
                    MY_POKEMON_CURRENT_HP + " INTEGER," +
                    MY_POKEMON_ATTACK + " INTEGER," +
                    MY_POKEMON_DEFENSE + " INTEGER" +
                    ")"
        db?.execSQL(SQLQuery2);

        val SQLQuery3 =
            "CREATE TABLE " + POKEDEX_TABLE +
                    "(" +
                    POKEDEX_ID + " INTEGER PRIMARY KEY," +
                    POKEDEX_NUM + " INTEGER," +
                    POKEDEX_SEEN + " INTEGER," +
                    POKEDEX_CAUGHT + " INTEGER" +
                    ")"
        db?.execSQL(SQLQuery3);
        //DEX_Initialize()
        for ((num, name) in Pokedex.dex)
        {
            val query =
                "INSERT INTO " + POKEDEX_TABLE +
                        " (" +
                        POKEDEX_NUM + ", " +
                        POKEDEX_SEEN + ", " +
                        POKEDEX_CAUGHT +
                        ") " +
                "VALUES (" + num.toString() + ", 0, 0)"
            db?.execSQL(query);
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion != newVersion)
        {
            db?.execSQL("DROP TABLE IF EXISTS " + WILD_POKEMON_TABLE)
            db?.execSQL("DROP TABLE IF EXISTS " + MY_POKEMON_TABLE)
            db?.execSQL("DROP TABLE IF EXISTS " + POKEDEX_TABLE)
            onCreate(db)
        }
    }



    fun WP_Add(_p: Wild_Pokemon) {
        val db = writableDatabase

        db.beginTransaction()
        try
        {
            val value = ContentValues()
            value.put(WILD_POKEMON_NUM, _p.num)
            value.put(WILD_POKEMON_LATITUDE, _p.latitude)
            value.put(WILD_POKEMON_LONGITUDE, _p.longitude)
            value.put(WILD_POKEMON_TIME, _p.time.toString())
            value.put(WILD_POKEMON_LEVEL, _p.level)
            value.put(WILD_POKEMON_HP, _p.hp)
            value.put(WILD_POKEMON_ATTACK, _p.attack)
            value.put(WILD_POKEMON_DEFENSE, _p.defense)

            db.insertOrThrow(WILD_POKEMON_TABLE, null, value)
            db.setTransactionSuccessful()
        }
        catch (e: Exception) { Log.d("console", "addPokemon() Error") }
        finally { db.endTransaction() }
    }

    @SuppressLint("Range")
    fun WP_Get(_id: String): Wild_Pokemon {
        val db = readableDatabase

        val cursor: Cursor = db.query(WILD_POKEMON_TABLE, null, "$WILD_POKEMON_ID = ?", arrayOf(_id), null, null, null)

        val pokemon = Wild_Pokemon()
        try
        {
            if (cursor.moveToFirst()) {
                pokemon.id = cursor.getInt(cursor.getColumnIndex(WILD_POKEMON_ID))
                pokemon.num = cursor.getInt(cursor.getColumnIndex(WILD_POKEMON_NUM))
                pokemon.latitude = cursor.getDouble(cursor.getColumnIndex(WILD_POKEMON_LATITUDE))
                pokemon.longitude = cursor.getDouble(cursor.getColumnIndex(WILD_POKEMON_LONGITUDE))
                pokemon.time = LocalDateTime.parse(cursor.getString(cursor.getColumnIndex(WILD_POKEMON_TIME)))
                pokemon.level = cursor.getInt(cursor.getColumnIndex(WILD_POKEMON_LEVEL))
                pokemon.hp = cursor.getInt(cursor.getColumnIndex(WILD_POKEMON_HP))
                pokemon.attack = cursor.getInt(cursor.getColumnIndex(WILD_POKEMON_ATTACK))
                pokemon.defense = cursor.getInt(cursor.getColumnIndex(WILD_POKEMON_DEFENSE))
            }
        }
        catch (e: Exception) { Log.d("console", "getAllPokemon() Error") }
        finally { if (cursor != null && !cursor.isClosed) cursor.close() }

        return pokemon
    }

    @SuppressLint("Range")
    fun WP_GetAll(): ArrayList<Wild_Pokemon> {
        val arr = ArrayList<Wild_Pokemon>()

        val db = readableDatabase
        val SQLQuery = "SELECT * FROM " + WILD_POKEMON_TABLE

        val cursor = db.rawQuery(SQLQuery, null)
        try
        {
            if (cursor.moveToFirst()) {
                do {
                    val pokemon = Wild_Pokemon()
                    pokemon.id = cursor.getInt(cursor.getColumnIndex(WILD_POKEMON_ID))
                    pokemon.num = cursor.getInt(cursor.getColumnIndex(WILD_POKEMON_NUM))
                    pokemon.latitude = cursor.getDouble(cursor.getColumnIndex(WILD_POKEMON_LATITUDE))
                    pokemon.longitude = cursor.getDouble(cursor.getColumnIndex(WILD_POKEMON_LONGITUDE))
                    pokemon.time = LocalDateTime.parse(cursor.getString(cursor.getColumnIndex(WILD_POKEMON_TIME)))
                    pokemon.level = cursor.getInt(cursor.getColumnIndex(WILD_POKEMON_LEVEL))
                    pokemon.hp = cursor.getInt(cursor.getColumnIndex(WILD_POKEMON_HP))
                    pokemon.attack = cursor.getInt(cursor.getColumnIndex(WILD_POKEMON_ATTACK))
                    pokemon.defense = cursor.getInt(cursor.getColumnIndex(WILD_POKEMON_DEFENSE))
                    arr.add(pokemon)
                } while (cursor.moveToNext())
            }
        }
        catch (e: Exception) { Log.d("console", "getAllPokemon() Error") }
        finally { if (cursor != null && !cursor.isClosed) cursor.close() }

        return arr
    }

    fun WP_Delete(_id: Int) {
        val db = writableDatabase

        db.beginTransaction()
        try
        {
            db?.delete(WILD_POKEMON_TABLE, "id = $_id", null)
            db.setTransactionSuccessful()
        }
        catch (e: Exception) { Log.d("console", "deletePokemon() Error") }
        finally { db.endTransaction() }
    }



    fun MP_Add(_p: My_Pokemon) {
        val db = writableDatabase

        db.beginTransaction()
        try
        {
            val value = ContentValues()
            value.put(MY_POKEMON_NUM, _p.num)
            value.put(MY_POKEMON_NAME, _p.name)
            value.put(MY_POKEMON_FAVORITE, _p.favorite)
            value.put(MY_POKEMON_LATITUDE, _p.latitude)
            value.put(MY_POKEMON_LONGITUDE, _p.longitude)
            value.put(MY_POKEMON_TIME, _p.time.toString())
            value.put(MY_POKEMON_LEVEL, _p.level)
            value.put(MY_POKEMON_EXP, _p.exp)
            value.put(MY_POKEMON_HP, _p.hp)
            value.put(MY_POKEMON_CURRENT_HP, _p.current_hp)
            value.put(MY_POKEMON_ATTACK, _p.attack)
            value.put(MY_POKEMON_DEFENSE, _p.defense)

            db.insertOrThrow(MY_POKEMON_TABLE, null, value)
            db.setTransactionSuccessful()
        }
        catch (e: Exception) { Log.d("console", "addPokemon() Error") }
        finally { db.endTransaction() }
    }

    @SuppressLint("Range")
    fun MP_GetNew(): My_Pokemon {
        val db = readableDatabase

        val SQLQuery ="SELECT * FROM " + MY_POKEMON_TABLE + " ORDER BY " + MY_POKEMON_ID + " DESC LIMIT 1"
        val cursor: Cursor = db.rawQuery(SQLQuery, null)

        val pokemon = My_Pokemon()
        try
        {
            if (cursor.moveToFirst()) {
                pokemon.id = cursor.getInt(cursor.getColumnIndex(MY_POKEMON_ID))
                pokemon.num = cursor.getInt(cursor.getColumnIndex(MY_POKEMON_NUM))
                pokemon.name = cursor.getString(cursor.getColumnIndex(MY_POKEMON_NAME))
                pokemon.favorite = cursor.getInt(cursor.getColumnIndex(MY_POKEMON_FAVORITE))
                pokemon.latitude = cursor.getDouble(cursor.getColumnIndex(MY_POKEMON_LATITUDE))
                pokemon.longitude = cursor.getDouble(cursor.getColumnIndex(MY_POKEMON_LONGITUDE))
                pokemon.time = LocalDateTime.parse(cursor.getString(cursor.getColumnIndex(MY_POKEMON_TIME)))
                pokemon.level = cursor.getInt(cursor.getColumnIndex(MY_POKEMON_LEVEL))
                pokemon.exp = cursor.getInt(cursor.getColumnIndex(MY_POKEMON_EXP))
                pokemon.hp = cursor.getInt(cursor.getColumnIndex(MY_POKEMON_HP))
                pokemon.current_hp = cursor.getInt(cursor.getColumnIndex(MY_POKEMON_CURRENT_HP))
                pokemon.attack = cursor.getInt(cursor.getColumnIndex(MY_POKEMON_ATTACK))
                pokemon.defense = cursor.getInt(cursor.getColumnIndex(MY_POKEMON_DEFENSE))
            }
        }
        catch (e: Exception) { Log.d("console", "getAllPokemon() Error") }
        finally { if (cursor != null && !cursor.isClosed) cursor.close() }

        return pokemon
    }

    @SuppressLint("Range")
    fun MP_Get(_id: String): My_Pokemon {
        val db = readableDatabase

        val cursor: Cursor = db.query(WILD_POKEMON_TABLE, null, "$WILD_POKEMON_ID = ?", arrayOf(_id), null, null, null)

        val pokemon = My_Pokemon()
        try
        {
            if (cursor.moveToFirst()) {
                pokemon.id = cursor.getInt(cursor.getColumnIndex(MY_POKEMON_ID))
                pokemon.num = cursor.getInt(cursor.getColumnIndex(MY_POKEMON_NUM))
                pokemon.name = cursor.getString(cursor.getColumnIndex(MY_POKEMON_NAME))
                pokemon.favorite = cursor.getInt(cursor.getColumnIndex(MY_POKEMON_FAVORITE))
                pokemon.latitude = cursor.getDouble(cursor.getColumnIndex(MY_POKEMON_LATITUDE))
                pokemon.longitude = cursor.getDouble(cursor.getColumnIndex(MY_POKEMON_LONGITUDE))
                pokemon.time = LocalDateTime.parse(cursor.getString(cursor.getColumnIndex(MY_POKEMON_TIME)))
                pokemon.level = cursor.getInt(cursor.getColumnIndex(MY_POKEMON_LEVEL))
                pokemon.exp = cursor.getInt(cursor.getColumnIndex(MY_POKEMON_EXP))
                pokemon.hp = cursor.getInt(cursor.getColumnIndex(MY_POKEMON_HP))
                pokemon.current_hp = cursor.getInt(cursor.getColumnIndex(MY_POKEMON_CURRENT_HP))
                pokemon.attack = cursor.getInt(cursor.getColumnIndex(MY_POKEMON_ATTACK))
                pokemon.defense = cursor.getInt(cursor.getColumnIndex(MY_POKEMON_DEFENSE))
            }
        }
        catch (e: Exception) { Log.d("console", "getAllPokemon() Error") }
        finally { if (cursor != null && !cursor.isClosed) cursor.close() }

        return pokemon
    }

    @SuppressLint("Range")
    fun MP_GetAll(): ArrayList<My_Pokemon> {
        val arr = ArrayList<My_Pokemon>()

        val db = readableDatabase
        val SQLQuery = "SELECT * FROM " + MY_POKEMON_TABLE

        val cursor = db.rawQuery(SQLQuery, null)
        try
        {
            if (cursor.moveToFirst()) {
                do {
                    val pokemon = My_Pokemon()
                    pokemon.id = cursor.getInt(cursor.getColumnIndex(MY_POKEMON_ID))
                    pokemon.num = cursor.getInt(cursor.getColumnIndex(MY_POKEMON_NUM))
                    pokemon.name = cursor.getString(cursor.getColumnIndex(MY_POKEMON_NAME))
                    pokemon.favorite = cursor.getInt(cursor.getColumnIndex(MY_POKEMON_FAVORITE))
                    pokemon.latitude = cursor.getDouble(cursor.getColumnIndex(MY_POKEMON_LATITUDE))
                    pokemon.longitude = cursor.getDouble(cursor.getColumnIndex(MY_POKEMON_LONGITUDE))
                    pokemon.time = LocalDateTime.parse(cursor.getString(cursor.getColumnIndex(MY_POKEMON_TIME)))
                    pokemon.level = cursor.getInt(cursor.getColumnIndex(MY_POKEMON_LEVEL))
                    pokemon.exp = cursor.getInt(cursor.getColumnIndex(MY_POKEMON_EXP))
                    pokemon.hp = cursor.getInt(cursor.getColumnIndex(MY_POKEMON_HP))
                    pokemon.current_hp = cursor.getInt(cursor.getColumnIndex(MY_POKEMON_CURRENT_HP))
                    pokemon.attack = cursor.getInt(cursor.getColumnIndex(MY_POKEMON_ATTACK))
                    pokemon.defense = cursor.getInt(cursor.getColumnIndex(MY_POKEMON_DEFENSE))
                    arr.add(pokemon)
                } while (cursor.moveToNext())
            }
        }
        catch (e: Exception) { Log.d("console", "getAllPokemon() Error") }
        finally { if (cursor != null && !cursor.isClosed) cursor.close() }

        return arr
    }

    fun MP_Edit(_id: String, _p: My_Pokemon) {
        val db = writableDatabase

        val value = ContentValues()
        value.put(MY_POKEMON_NUM, _p.num)
        value.put(MY_POKEMON_NAME, _p.name)
        value.put(MY_POKEMON_FAVORITE, _p.favorite)
        value.put(MY_POKEMON_LATITUDE, _p.latitude)
        value.put(MY_POKEMON_LONGITUDE, _p.longitude)
        value.put(MY_POKEMON_TIME, _p.time.toString())
        value.put(MY_POKEMON_LEVEL, _p.level)
        value.put(MY_POKEMON_EXP, _p.exp)
        value.put(MY_POKEMON_HP, _p.hp)
        value.put(MY_POKEMON_CURRENT_HP, _p.current_hp)
        value.put(MY_POKEMON_ATTACK, _p.attack)
        value.put(MY_POKEMON_DEFENSE, _p.defense)

        db.update(MY_POKEMON_TABLE, value, MY_POKEMON_ID + " = $_id", null)
    }

    fun MP_Delete(_id: String) {
        val db = writableDatabase

        db.beginTransaction()
        try
        {
            db?.delete(MY_POKEMON_TABLE, "id = $_id", null)
            db.setTransactionSuccessful()
        }
        catch (e: Exception) { Log.d("console", "deletePokemon() Error") }
        finally { db.endTransaction() }
    }



    @SuppressLint("Range")
    fun DEX_GetAll(): ArrayList<Pokedex_Pokemon> {
        val arr = ArrayList<Pokedex_Pokemon>()

        val db = readableDatabase
        val SQLQuery = "SELECT * FROM " + POKEDEX_TABLE

        val cursor = db.rawQuery(SQLQuery, null)
        try
        {
            if (cursor.moveToFirst()) {
                do {
                    val pokemon = Pokedex_Pokemon()
                    pokemon.id = cursor.getInt(cursor.getColumnIndex(POKEDEX_ID))
                    pokemon.num = cursor.getInt(cursor.getColumnIndex(POKEDEX_NUM))
                    pokemon.seen = cursor.getInt(cursor.getColumnIndex(POKEDEX_SEEN))
                    pokemon.num = cursor.getInt(cursor.getColumnIndex(POKEDEX_CAUGHT))
                    arr.add(pokemon)
                } while (cursor.moveToNext())
            }
        }
        catch (e: Exception) { Log.d("console", "getAllPokemon() Error") }
        finally { if (cursor != null && !cursor.isClosed) cursor.close() }

        return arr
    }

    fun DEX_SEEN(_id: String) {
        val db = writableDatabase

        val value = ContentValues()
        value.put(POKEDEX_SEEN, 1)

        db.update(POKEDEX_TABLE, value, POKEDEX_SEEN + " = $_id", null)
    }

}