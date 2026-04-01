package com.example.rentscope.data.geo

import android.content.Context

fun loadAssetText(context: Context, fileName: String): String {
    return context.assets.open(fileName).bufferedReader().use { it.readText() }
}