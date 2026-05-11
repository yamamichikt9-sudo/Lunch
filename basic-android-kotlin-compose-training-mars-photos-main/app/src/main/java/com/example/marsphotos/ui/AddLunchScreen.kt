package com.example.marsphotos.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun AddLunchScreen() {
    var shopName by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "ランチ登録", style = MaterialTheme.typography.headlineMedium)

        Button(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("写真選択")
        }
    }

    OutlinedTextField(
        value = shopName,
        onValueChange = { shopName = it },
        label = { Text("店名") },
        modifier = Modifier.fillMaxWidth()
    )

    Text("評価")
    Row {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null,
                tint = if (i <= rating) Color(0xFFFFC107) else Color.Gray,
                modifier = Modifier
                    .size(40.dp)
                    .clickable { rating = i }
            )
        }
    }

    OutlinedTextField(
        value = comment,
        onValueChange = { comment = it },
        label = { Text("コメント") },
        modifier = Modifier.fillMaxWidth().height(120.dp)
    )

    Button(
        onClick = { },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("この内容で登録")
    }
}

