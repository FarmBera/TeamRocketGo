package com.example.teamrocketgo.classes

class Pokedex {
    companion object {
        val dex = mapOf<Int, String>(
            1 to "Pikachu",
            2 to "Squirtle",
            3 to "Charizard",
            4 to "Rayquaza"
        )

        fun getName(num: Int): String {
            return dex.getValue(num)
        }
    }
}