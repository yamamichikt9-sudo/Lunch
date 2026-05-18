package com.example.marsphotos.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.marsphotos.ui.screens.LunchViewModel
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.draw.scale
import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.foundation.background


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLunchScreen(viewModel: LunchViewModel, onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current

    var selectedReactions: Set<String> by remember { mutableStateOf(viewModel.reactionsInput.toSet())}
    // --- ダイアログの状態管理 ---
    var showConfirmDialog by remember { mutableStateOf(false) }

    // 編集か新規かを判定
    val isEdit = viewModel.editingLunchId != null
    val dialogTitle = if (isEdit) "編集の確認" else "登録の確認"
    val dialogMessage =
        if (isEdit) "この内容で上書き保存しますか？" else "この内容で登録しますがよろしいですか？"
    val confirmButtonText = if (isEdit) "保存" else "登録"

    var expanded by remember { mutableStateOf(false) }
    val genres = listOf("和食", "洋食", "イタリアン", "ラーメン", "カフェ", "その他")

    val calendar = Calendar.getInstance()

    val formattedDate = SimpleDateFormat(
        "yyyy/MM/dd",
        Locale.getDefault()
    ).format(viewModel.dateInput)

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            viewModel.updateDate(calendar.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { viewModel.updatePhoto(it.toString()) }
    }

    // --- 登録・編集確認ダイアログ ---
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(dialogTitle) },
            text = { Text(dialogMessage) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false

                        viewModel.photoUriInput?.let { originalUri ->
                            val permanentUri =
                                viewModel.saveImageToInternalStorage(context, originalUri)
                            viewModel.updatePhoto(permanentUri)
                        }

                        viewModel.updateReactions(selectedReactions.toList())

                        viewModel.saveLunch()
                        onBack()
                    }
                ) {
                    Text(confirmButtonText)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("キャンセル")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "ランチ編集" else "ランチ登録") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        },
        // ★ ここを追加：ボタンを画面下部に固定
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 2.dp, // ほんのり色をつけて境界をわかりやすく
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = { showConfirmDialog = true },
                    enabled = viewModel.canSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp) // ボタンの周りに余白
                ) {
                    Text(confirmButtonText)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp)) // 上に少し余裕

            Text(
                text = if (isEdit) "ランチ編集" else "ランチ登録",
                style = MaterialTheme.typography.headlineMedium
            )

            // --- 画像選択 ---
            if (viewModel.photoUriInput == null) {
                Button(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
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

            // --- 各種入力フィールド ---
            OutlinedTextField(
                value = viewModel.nameInput,
                onValueChange = { viewModel.updateName(it) },
                label = { Text("店名") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = viewModel.addressInput,
                onValueChange = { viewModel.updateAddress(it) },
                label = { Text("住所") },
                placeholder = { Text("例：東京都千代田区...") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = viewModel.phoneNumberInput,
                onValueChange = { viewModel.updatePhoneNumber(it) },
                label = { Text("電話番号 (任意)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            // --- ジャンル選択 ---
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
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
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

            OutlinedButton(
                onClick = {
                    datePickerDialog.show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "来店日: $formattedDate")
            }

            // --- 評価（アニメーション付き） ---
            Text("評価")
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                var lastSelectedStar by remember { mutableStateOf(-1) }
                for (i in 1..5) {
                    val isSelected = i <= viewModel.ratingInput.toInt()
                    val starTargetScale = if (lastSelectedStar == i) 1.3f else 1.0f
                    val scale by animateFloatAsState(
                        targetValue = starTargetScale,
                        animationSpec = androidx.compose.animation.core.spring(
                            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                        ),
                        finishedListener = { if (lastSelectedStar == i) lastSelectedStar = -1 }
                    )
                    Icon(
                        imageVector = if (isSelected) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = "$i 点",
                        tint = if (isSelected) Color(0xFFFFC107) else Color.Gray,
                        modifier = Modifier
                            .size(40.dp)
                            .scale(scale)
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) {
                                lastSelectedStar = i
                                viewModel.updateRating(i.toFloat())
                            }
                    )
                }
            }

            // --- コメント ---
            OutlinedTextField(
                value = viewModel.commentInput,
                onValueChange = { viewModel.updateComment(it) },
                label = { Text("コメント") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Text(
                text = " ",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 3.dp, bottom = 8.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                val availableReactions = listOf(
                    "🔥" to "リピートしたい！",
                    "👛" to "コスパがいい！",
                    "✨" to "おしゃれ！",
                    "🍖" to "ボリューム満点！"
                )

                availableReactions.forEach { (emoji, label) ->
                    val isSelected = selectedReactions.contains(emoji)

                    Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        IconToggleButton(
                            checked = isSelected,
                            onCheckedChange = { checked ->
                                selectedReactions = if (checked) {
                                    selectedReactions + emoji
                                } else {
                                    selectedReactions - emoji
                                }
                            },
                            modifier = Modifier.size(50.dp) // ボタンの大きさ
                        ) {
                            Box(
                                contentAlignment = androidx.compose.ui.Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(
                                            alpha = 0.4f
                                        ),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(
                                            8.dp)
                                    )
                            ) {
                                Text(text = emoji, style = MaterialTheme.typography.titleLarge)
                            }
                        }

                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray
                        )
                    }
                }
            } // ★ ここにあった元の Button は消去しました。代わりに Scaffold の bottomBar に配置しています。

                Spacer(modifier = Modifier.height(32.dp)) // スクロールした時の最後の余白
            }
        }
    }
