package com.example.marsphotos.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.marsphotos.ui.screens.LunchViewModel
import androidx.compose.ui.text.input.KeyboardType // インポートを忘れずに
import androidx.compose.foundation.text.KeyboardOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun AddLunchScreen(viewModel: LunchViewModel,onBack: () -> Unit) {

    var expanded by remember { mutableStateOf(false) }
    val genres = listOf("和食", "洋食", "イタリアン", "ラーメン", "カフェ", "その他")


    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { viewModel.updatePhoto(it.toString()) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ランチ登録") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = "戻る"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
                Text(text = "ランチ登録", style = MaterialTheme.typography.headlineMedium)


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


                OutlinedTextField(
                    value = viewModel.nameInput, // UI担当の変数ではなく、あなたのViewModelの変数を使う
                    onValueChange = { viewModel.updateName(it) }, // 文字が変わったらViewModelに報告する
                    label = { Text("店名") },
                    modifier = Modifier.fillMaxWidth()
                )

            // ★住所
            OutlinedTextField(
                value = viewModel.addressInput,
                onValueChange = { viewModel.updateAddress(it) },
                label = { Text("住所") },
                placeholder = { Text("例：東京都千代田区...") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = viewModel.phoneNumberInput, // ViewModelの変数名
                onValueChange = { viewModel.updatePhoneNumber(it) }, // ViewModelの関数名
                label = { Text("電話番号 (任意)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )


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


                Text("評価")
                Row {
                    for (i in 1..5) {
                        Icon(
                            imageVector = if (i <= viewModel.ratingInput) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = null,
                            tint = if (i <= viewModel.ratingInput) Color(0xFFFFC107) else Color.Gray,
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { viewModel.updateRating(i.toFloat()) }
                        )
                    }
                }


                OutlinedTextField(
                    value = viewModel.commentInput,
                    onValueChange = { viewModel.updateComment(it) },
                    label = { Text("コメント") },
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                )


                Button(
                    onClick = {
                        viewModel.saveLunch()
                        onBack()
                    },
                    enabled = viewModel.canSave, // OKな時だけ光る
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("この内容で登録")
                }
            }
        }
    }
