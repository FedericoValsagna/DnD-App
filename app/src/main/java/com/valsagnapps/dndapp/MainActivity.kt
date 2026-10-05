package com.valsagnapps.dndapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.valsagnapps.dndapp.ui.DnDNavigation
import com.valsagnapps.dndapp.ui.theme.DnDAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as DnDApplication).container
        setContent {
            DnDAppTheme {
                DnDNavigation(container.characterRepository)
            }
        }
    }
}
