package com.example.tysyacha_calc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.tysyacha_calc.ui.navigation.AppNavGraph
import com.example.tysyacha_calc.ui.theme.TysyachacalcTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TysyachacalcTheme {
                AppNavGraph()
            }
        }
    }
}
