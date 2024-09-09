package com.example.teamrocketgo.classes

import com.example.teamrocketgo.R

class ColorSet {
    fun ColorSetup(backColor: String): Int {
        val result = when(backColor) {
            "0" -> R.color.black
            "1" -> R.color.light_yellow
            "2" -> R.color.light_blue
            "3" -> R.color.light_purple
            else -> R.color.error
        }
        return result
    }
}