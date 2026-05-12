package com.example.marsphotos.ui

import androidx.compose.foundation.Image
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
import coil.compose.rememberAsyncImagePainter
import com.example.marsphotos.ui.screens.LunchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun AddLunchScreen() {
    var expanded by remember { mutableStateOf(false) }
    var selectedGenre by remember { mutableStateOf("選択してください") }
fun AddLunchScreen(viewModel: LunchViewModel) {
    var expanded by remember { mutableStateOf(false) } // メニューが開いているか
    var selectedGenre by remember { mutableStateOf("選択してください") } // 選ばれた項目
    val genres = listOf("和食", "洋食", "イタリアン", "ラーメン", "カフェ", "その他")
    var shopName by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(0) }

    val selectedImageUri = remember { mutableStateOf<android.net.Uri?>(null) }
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        selectedImageUri.value = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "ランチ登録", style = MaterialTheme.typography.headlineMedium)

        // 1. 写真選択
        if (selectedImageUri.value == null) {
            Button(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("写真選択")
            }
        } else {
            Image(
                painter = rememberAsyncImagePainter(selectedImageUri.value),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(200.dp).clickable { launcher.launch("image/*") }
            )
        }

        // 2. 店名入力

    OutlinedTextField(
        value = viewModel.nameInput, // UI担当の変数ではなく、あなたのViewModelの変数を使う
        onValueChange = { viewModel.updateName(it) }, // 文字が変わったらViewModelに報告する
        label = { Text("店名") },
        modifier = Modifier.fillMaxWidth()
    )

    Text("")
    @OptIn(ExperimentalMaterial3Api::class)
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {

        OutlinedTextField(
            value = shopName,
            onValueChange = { shopName = it },
            label = { Text("店名") },
            modifier = Modifier.fillMaxWidth()
        )

        // 3. ジャンル選択
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedGenre,
                onValueChange = {},
                readOnly = true,
                label = { Text("ジャンル") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor(),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                genres.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = {
                            selectedGenre = item
                            expanded = false
                        }
                    )
                }
            }
        }

        // 4. 評価
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

        // 5. コメント
        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            label = { Text("コメント") },
            modifier = Modifier.fillMaxWidth().height(120.dp)
        )

        // 6. 登録ボタン
        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("この内容で登録")
        }
    }
}
