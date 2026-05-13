<<<<<<< HEAD
/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
=======
>>>>>>> 6f9a8bb8dbdc3e156f10f56528197c18460df865
package com.example.marsphotos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
<<<<<<< HEAD
=======
import androidx.compose.foundation.Image
>>>>>>> 6f9a8bb8dbdc3e156f10f56528197c18460df865
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.marsphotos.data.LunchEntity
import com.example.marsphotos.ui.AddLunchScreen
import com.example.marsphotos.ui.screens.LunchViewModel
import com.example.marsphotos.ui.theme.MarsPhotosTheme
<<<<<<< HEAD
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.Color
=======
>>>>>>> 6f9a8bb8dbdc3e156f10f56528197c18460df865

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            MarsPhotosTheme {
                val lunchViewModel: LunchViewModel = viewModel(factory = LunchViewModel.Factory)
                var currentScreen by remember { mutableStateOf("main") }

                Surface(modifier = Modifier.fillMaxSize()) {
                    when (currentScreen) {
                        "main" -> {
                            MainContent(
                                viewModel = lunchViewModel,
                                onAddClick = { currentScreen = "add" },
                                onLunchClick = { lunch ->
                                    lunchViewModel.onLunchSelected(lunch)
                                    currentScreen = "detail"
                                }
<<<<<<< HEAD
                            }
                        ) { innerPadding ->

                            if (lunchViewModel.lunchList.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("ランチを登録してみよう！")
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(onClick = { currentScreen = "add" }) {
                                            Text("ランチを登録")
                                        }
                                    }
                                }
                            } else {

                                val categories = remember(lunchViewModel.lunchList.toList()) {
                                    listOf("すべて") + lunchViewModel.lunchList.map { it.category }.distinct()
                                }
                                var currentCategory by remember { mutableStateOf("すべて") }
                                var expanded by remember { mutableStateOf(false) }

                                var searchQuery by remember { mutableStateOf("") }

                                val filteredList =
                                    remember(currentCategory, searchQuery, lunchViewModel.lunchList.toList()) {
                                        val genreFiltered = if (currentCategory == "すべて") {
                                            lunchViewModel.lunchList
                                        } else {
                                            lunchViewModel.lunchList.filter { it.category == currentCategory }
                                        }

                                        if (searchQuery.isBlank()) {
                                            genreFiltered
                                        } else {
                                            genreFiltered.filter { lunch ->
                                                lunch.name.contains(searchQuery, ignoreCase = true) ||
                                                        lunch.comment.contains(searchQuery, ignoreCase = true)
                                            }
                                        }
                                    }

                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                ) {
                                    ExposedDropdownMenuBox(
                                        expanded = expanded,
                                        onExpandedChange = { expanded = !expanded },
                                        modifier = Modifier.padding(
                                            start = 16.dp,
                                            top = 8.dp,
                                            end = 16.dp
                                        )
                                    ) {
                                        OutlinedTextField(
                                            value = currentCategory,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("ジャンル") },
                                            trailingIcon = {
                                                ExposedDropdownMenuDefaults.TrailingIcon(
                                                    expanded = expanded
                                                )
                                            },
                                            modifier = Modifier.menuAnchor()
                                        )
                                        ExposedDropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false }
                                        ) {
                                            categories.forEach { category ->
                                                DropdownMenuItem(
                                                    text = { Text(category) },
                                                    onClick = {
                                                        currentCategory = category
                                                        expanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    // エラーが絶対出ないように整理した検索バー
                                    OutlinedTextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        label = { Text("キーワード検索") },
                                        placeholder = { Text("店名やコメントを入力") },
                                        singleLine = true,
                                        trailingIcon = {
                                            if (searchQuery.isNotEmpty()) {
                                                Box(
                                                    modifier = Modifier
                                                        .padding(end = 12.dp)
                                                        .clickable { searchQuery = "" }
                                                ) {
                                                    Text(
                                                        text = "×",
                                                        style = MaterialTheme.typography.titleLarge,
                                                        color = Color.Gray
                                                    )
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    )

                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        items(filteredList) { lunch ->
                                            LunchCard(lunch = lunch)
                                        }
                                    }
                                }

                            }
=======
                            )
                        }
                        "add" -> {
                            AddLunchScreen(
                                viewModel = lunchViewModel,
                                onBack = { currentScreen = "main" }
                            )
                        }
                        "detail" -> {
                            LunchDetailContent(
                                viewModel = lunchViewModel,
                                onBack = { currentScreen = "main" }
                            )
>>>>>>> 6f9a8bb8dbdc3e156f10f56528197c18460df865
                        }
                    }
                }
            }
        }
    }
}

<<<<<<< HEAD
@Composable
fun LunchCard(lunch: com.example.marsphotos.data.LunchEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            androidx.compose.foundation.Image(
                painter = coil.compose.rememberAsyncImagePainter(lunch.photoUrl),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
=======
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    viewModel: LunchViewModel,
    onAddClick: () -> Unit,
    onLunchClick: (LunchEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var currentCategory by remember { mutableStateOf("すべて") }
    var expanded by remember { mutableStateOf(false) }

    val categories = remember(viewModel.lunchList.toList()) {
        listOf("すべて") + viewModel.lunchList.map { it.category }.distinct()
    }

    // ジャンルとキーワードの両方でフィルタリング
    val filteredList = remember(currentCategory, searchQuery, viewModel.lunchList.toList()) {
        val genreFiltered = if (currentCategory == "すべて") {
            viewModel.lunchList
        } else {
            viewModel.lunchList.filter { it.category == currentCategory }
        }

        if (searchQuery.isBlank()) {
            genreFiltered
        } else {
            genreFiltered.filter { lunch ->
                lunch.name.contains(searchQuery, ignoreCase = true) ||
                        lunch.comment.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("ランチログ") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Text("+", style = MaterialTheme.typography.headlineMedium)
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // ジャンル選択ドロップダウン
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = currentCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("ジャンル") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = { currentCategory = category; expanded = false }
                        )
                    }
                }
            }

            // キーワード検索バー
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("キーワード検索") },
                placeholder = { Text("店名やコメントを入力") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (filteredList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("見つかりませんでした")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
>>>>>>> 6f9a8bb8dbdc3e156f10f56528197c18460df865
                ) {
                    items(filteredList) { lunch ->
                        LunchCard(lunch = lunch, modifier = Modifier.clickable { onLunchClick(lunch) })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LunchDetailContent(viewModel: LunchViewModel, onBack: () -> Unit) {
    val lunch = viewModel.selectedLunch ?: return
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("詳細情報") },
                navigationIcon = { IconButton(onClick = onBack) { Text("◀") } }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState())
        ) {
            Image(
                painter = rememberAsyncImagePainter(lunch.photoUrl),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(250.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = lunch.name, style = MaterialTheme.typography.headlineMedium)
                    Badge { Text(lunch.category) }
                }
<<<<<<< HEAD

                Row(modifier = Modifier.padding(vertical = 4.dp)) {
=======
                Row(modifier = Modifier.padding(vertical = 8.dp)) {
>>>>>>> 6f9a8bb8dbdc3e156f10f56528197c18460df865
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (index < lunch.rating) Color(0xFFFFC107) else Color.LightGray,
<<<<<<< HEAD
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = lunch.comment,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
=======
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(text = "📍 住所", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                Text(text = lunch.address.ifBlank { "未登録" })
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "📞 電話番号", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                Text(text = lunch.phoneNumber?.ifBlank { "未登録" } ?: "未登録")
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "💬 コメント", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                Text(text = lunch.comment.ifBlank { "コメントなし" })
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("一覧に戻る") }
>>>>>>> 6f9a8bb8dbdc3e156f10f56528197c18460df865
            }
        }
    }
}

<<<<<<< HEAD
=======
@Composable
fun LunchCard(lunch: LunchEntity, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Image(
                painter = rememberAsyncImagePainter(lunch.photoUrl),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(180.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = lunch.name, style = MaterialTheme.typography.titleLarge)
                    Badge { Text(lunch.category) }
                }
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (index < lunch.rating) Color(0xFFFFC107) else Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(text = lunch.comment, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
            }
        }
    }
}
>>>>>>> 6f9a8bb8dbdc3e156f10f56528197c18460df865
