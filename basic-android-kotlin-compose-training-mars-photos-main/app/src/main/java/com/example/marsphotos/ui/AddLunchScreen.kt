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

fun AddLunchScreen(viewModel: LunchViewModel) {
// UIの表示制御用の状態（これはUI側で持っていてもOKなものです）
    var expanded by remember { mutableStateOf(false) }
    val genres = listOf("和食", "洋食", "イタリアン", "ラーメン", "カフェ", "その他")

    // 画像選択のランチャー
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { viewModel.updatePhoto(it.toString()) } // 選択されたらViewModelへ！
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
        if (viewModel.photoUriInput == null) {
            Button(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("写真選択")
            }
        } else {
            Image(
                painter = rememberAsyncImagePainter(viewModel.photoUriInput),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable { launcher.launch("image/*") }
            )
        }

        // 2. 店名入力 (ViewModelとガッチャンコ！)
    OutlinedTextField(
        value = viewModel.nameInput, // UI担当の変数ではなく、あなたのViewModelの変数を使う
        onValueChange = { viewModel.updateName(it) }, // 文字が変わったらViewModelに報告する
        label = { Text("店名") },
        modifier = Modifier.fillMaxWidth()
    )

        // 3. ジャンル選択 (viewModelのselectedGenreを使います)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = viewModel.selectedGenre,
                onValueChange = {},
                readOnly = true,
                label = { Text("ジャンル") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor(),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                genres.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = {
                            viewModel.updateGenre(item)
                            expanded = false
                        }
                    )
                }
            }
        }

        // 4. 評価（星の数もViewModelで管理）
        Text("評価")
        Row {
            for (i in 1..5) {
                Icon(
                    imageVector = if (i <= viewModel.ratingInput) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = null,
                    tint = if (i <= viewModel.ratingInput) Color(0xFFFFC107) else Color.Gray,
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { viewModel.ratingInput = (i.toFloat()) }
                )
            }
        }

        // 5. コメント
        OutlinedTextField(
            value = viewModel.commentInput,
            onValueChange = { viewModel.updateComment(it) },
            label = { Text("コメント") },
            modifier = Modifier.fillMaxWidth().height(120.dp)
        )

        // 6. 登録ボタン
        Button(
            onClick = { },
            enabled = viewModel.canSave, // OKな時だけ光る
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("この内容で登録")
        }
    }
}
