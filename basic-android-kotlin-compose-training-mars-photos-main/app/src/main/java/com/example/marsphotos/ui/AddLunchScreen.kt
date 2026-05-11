package com.example.marsphotos.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable

@Composable
fun AddLunchScreen() {
    Column {
        Text(text = "ランチ登録画面")
        TextField(value = "", onValueChange = {}, label = { Text("店名") })
    }
}
