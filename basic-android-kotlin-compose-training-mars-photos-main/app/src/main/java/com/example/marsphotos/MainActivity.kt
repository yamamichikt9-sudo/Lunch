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

package com.example.marsphotos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.marsphotos.ui.AddLunchScreen
import com.example.marsphotos.ui.screens.LunchViewModel
import com.example.marsphotos.ui.theme.MarsPhotosTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.Color



class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            MarsPhotosTheme {
                // 1.
                val lunchViewModel: LunchViewModel = viewModel(factory = LunchViewModel.Factory)
                var currentScreen by remember { mutableStateOf("main") }

                Surface(modifier = Modifier.fillMaxSize()) {
                    if (currentScreen == "main") {

                        Scaffold(
                            topBar = { CenterAlignedTopAppBar(title = { Text("ランチログ") }) },
                            floatingActionButton = {
                                FloatingActionButton(onClick = { currentScreen = "add" }) {
                                    Text("+", style = MaterialTheme.typography.headlineMedium)
                                }
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

                                val filteredList =
                                    remember(currentCategory, lunchViewModel.lunchList) {
                                        if (currentCategory == "すべて") {
                                            lunchViewModel.lunchList
                                        } else {
                                            lunchViewModel.lunchList.filter { it.category == currentCategory }
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
                        }
                    } else {
                        AddLunchScreen(
                            viewModel = lunchViewModel,
                            onBack = { currentScreen = "main" }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun LunchCard(lunch: com.example.marsphotos.data.LunchEntity) {
    Card(
        modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {

            androidx.compose.foundation.Image(
                painter = coil.compose.rememberAsyncImagePainter(lunch.photoUrl),
                contentDescription = null,
                modifier = androidx.compose.ui.Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )

            Column(modifier = androidx.compose.ui.Modifier.padding(16.dp)) {
                Row(
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(text = lunch.name, style = MaterialTheme.typography.titleLarge)
                    Badge { Text(lunch.category) }
                }

                Row(modifier = androidx.compose.ui.Modifier.padding(vertical = 4.dp)) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (index < lunch.rating) Color(0xFFFFC107) else Color.LightGray,
                            modifier = androidx.compose.ui.Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = androidx.compose.ui.Modifier.height(4.dp))

                Text(
                    text = lunch.comment,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
            }
        }
    }
}
