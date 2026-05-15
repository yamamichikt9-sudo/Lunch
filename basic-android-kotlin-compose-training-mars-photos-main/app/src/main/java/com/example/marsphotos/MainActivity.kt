package com.example.marsphotos

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Place // マップピンのアイコン
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext // Context取得用
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.marsphotos.data.LunchEntity
import com.example.marsphotos.ui.AddLunchScreen
import com.example.marsphotos.ui.screens.LunchViewModel
import com.example.marsphotos.ui.theme.MarsPhotosTheme
import android.app.DatePickerDialog
import com.example.marsphotos.ui.CalendarScreen
import com.example.marsphotos.ui.components.LunchCard
import java.util.Calendar

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
                                onCalendarClick = { currentScreen = "calendar" },
                                onLunchClick = { lunch ->
                                    lunchViewModel.onLunchSelected(lunch)
                                    currentScreen = "detail"
                                }
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
                                onBack = { currentScreen = "main" },
                                onEditClick = { currentScreen = "add" }
                            )
                        }
                        "calendar" -> CalendarScreen(
                            viewModel = lunchViewModel,
                            onBack = { currentScreen = "main" },
                            onLunchClick = { lunch ->
                                lunchViewModel.onLunchSelected(lunch)
                                currentScreen = "detail"
                            }
                        )
                    }
                }
            }
        }
    }
}

enum class SortOption(val title: String) {
    LATEST("新しい順"),
    OLDEST("古い順"),
    NAME("名前順"),
    RATING("評価順")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    viewModel: LunchViewModel,
    onAddClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onLunchClick: (LunchEntity) -> Unit
) {
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var currentCategory by remember { mutableStateOf("すべて") }
    var expanded by remember { mutableStateOf(false) }
    var currentSort by remember { mutableStateOf(SortOption.LATEST) }
    var sortExpanded by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            selectedDate = calendar.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val categories = remember(viewModel.lunchList.toList()) {
        listOf("すべて") + viewModel.lunchList.map { it.category }.distinct()
    }

    val filteredList = remember(
        currentCategory,
        searchQuery,
        currentSort,
        viewModel.lunchList.toList(),
        viewModel.filterDate
    ) {

        val categoryFiltered = if (currentCategory == "すべて") {
            viewModel.lunchList
        } else {
            viewModel.lunchList.filter { it.category == currentCategory }
        }

        val searchFiltered = if (searchQuery.isBlank()) {
            categoryFiltered
        } else {
            categoryFiltered.filter { lunch ->
                lunch.name.contains(searchQuery, ignoreCase = true) ||
                        lunch.comment.contains(searchQuery, ignoreCase = true)
            }
        }

        val dateFiltered = viewModel.filterDate?.let { selected ->
            searchFiltered.filter { lunch ->

                val c1 = Calendar.getInstance().apply {
                    timeInMillis = lunch.date
                }

                val c2 = Calendar.getInstance().apply {
                    timeInMillis = selected
                }

                c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                        c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
            }
        } ?: searchFiltered

        when (currentSort) {
            SortOption.LATEST -> dateFiltered.sortedByDescending { it.id }
            SortOption.OLDEST -> dateFiltered.sortedBy { it.id }
            SortOption.NAME -> dateFiltered.sortedBy { it.name }
            SortOption.RATING -> dateFiltered.sortedByDescending { it.rating }
        }
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("ランチログ") }) },
        floatingActionButton = {
            Row {
                FloatingActionButton(
                    onClick = onCalendarClick
                ) {
                    Text("📅")
                }

                FloatingActionButton(onClick = onAddClick) {
                    Text("+", style = MaterialTheme.typography.headlineMedium)
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.weight(1f)
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

                ExposedDropdownMenuBox(
                    expanded = sortExpanded,
                    onExpandedChange = { sortExpanded = !sortExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = currentSort.title,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("並び替え") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sortExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = sortExpanded, onDismissRequest = { sortExpanded = false }) {
                        SortOption.values().forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.title) },
                                onClick = { currentSort = option; sortExpanded = false }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("キーワード検索") },
                placeholder = { Text("店名やコメントを入力") },
                singleLine = true,
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        Box(modifier = Modifier.padding(end = 12.dp).clickable { searchQuery = "" }) {
                            Text(text = "×", style = MaterialTheme.typography.titleLarge, color = Color.Gray)
                        }
                    }
                },
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
fun LunchDetailContent(
    viewModel: LunchViewModel,
    onBack: () -> Unit,
    onEditClick: () -> Unit
) {
    val lunch = viewModel.selectedLunch

    if (lunch == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("データが選択されていません")
        }
        return
    }
    val context = LocalContext.current // マップ起動用

    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("削除の確認") },
            text = { Text("「${lunch.name}」を削除しますか？\nこの操作は取り消せません。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteLunch(lunch)
                        onBack()
                    }
                ) {
                    Text("削除", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("キャンセル")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("詳細情報") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "戻る")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "メニュー")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("編集") },
                                onClick = {
                                    showMenu = false
                                    viewModel.prepareEdit(lunch)
                                    onEditClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("削除", color = Color.Red) },
                                onClick = {
                                    showMenu = false
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
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
                Row(modifier = Modifier.padding(vertical = 8.dp)) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (index < lunch.rating) Color(0xFFFFC107) else Color.LightGray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // --- 住所セクション（マップ連携ボタン付き） ---
                Text(text = "📍 住所", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = lunch.address.ifBlank { "未登録" },
                        modifier = Modifier.weight(1f)
                    )
                    if (lunch.address.isNotBlank()) {
                        IconButton(
                            onClick = {
                                val uri = Uri.parse("geo:0,0?q=${lunch.address}")
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                intent.setPackage("com.google.android.apps.maps")
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = "マップで見る",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "📞 電話番号", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                Text(text = lunch.phoneNumber?.ifBlank { "未登録" } ?: "未登録")

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "💬 コメント", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                Text(text = lunch.comment.ifBlank { "コメントなし" })

                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("一覧に戻る") }
            }
        }
    }
}
