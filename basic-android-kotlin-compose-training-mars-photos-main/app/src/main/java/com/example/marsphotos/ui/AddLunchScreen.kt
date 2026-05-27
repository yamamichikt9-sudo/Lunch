package com.example.marsphotos.ui

import android.app.DatePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.marsphotos.ui.screens.LunchViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLunchScreen(viewModel: LunchViewModel, onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current

    // 💡 エラーの原因だったリアクションの状態管理を、一番安全な「.value」形式で定義します
    val selectedReactions = remember { mutableStateOf(viewModel.reactionsInput.toSet()) }

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

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
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

                        // リアクションの確定
                        viewModel.updateReactions(selectedReactions.value.toList())

                        // 💡 【ここを修正！】
                        // 編集のときも新規登録のときも、終わったらしっかり「onBack」を実行するように
                        // どちらにも「onSaveComplete = onBack」を渡してあげます！
                        if (isEdit) {
                            viewModel.saveLunch(context = context, onSaveComplete = onBack)
                        } else {
                            viewModel.saveLunch(context = context, onSaveComplete = onBack)
                        }
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

    // --- 店舗候補選択ダイアログ ---
    if (viewModel.searchCandidates.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { viewModel.clearCandidates() },
            title = {
                Text(
                    text = "該当する店舗を選択してください",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    viewModel.searchCandidates.forEach { item ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectCandidate(item)
                                },
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = item.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.clearCandidates() }) {
                    Text("キャンセル")
                }
            }
        )
    }

    // 🪙 --- 100LP獲得！貯金箱アニメーションダイアログ ---
    if (viewModel.showCoinAnimation) {
        var startAnimation by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            startAnimation = true
        }

        val coinOffsetY by animateFloatAsState(
            targetValue = if (startAnimation) 40f else -150f,
            animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing),
            label = "CoinDrop"
        )

        val coinAlpha by animateFloatAsState(
            targetValue = if (coinOffsetY >= 30f) 0f else 1f,
            animationSpec = tween(durationMillis = 100),
            label = "CoinAlpha"
        )

        val pigScale by animateFloatAsState(
            targetValue = if (coinOffsetY >= 20f) {
                if (coinOffsetY >= 35f) 1.0f else 1.3f
            } else {
                1.0f
            },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "PigScale"
        )

        AlertDialog(
            onDismissRequest = { /* アニメーション中は閉じさせない */ },
            title = null,
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "登録ありがとう！",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "🪙 +100 LP 獲得！",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(180.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(200.dp)
                    ) {
                        Text(
                            text = "🪙",
                            style = MaterialTheme.typography.displayMedium,
                            modifier = Modifier
                                .offset(y = coinOffsetY.dp)
                                .graphicsLayer(alpha = coinAlpha)
                        )

                        Text(
                            text = "🐖",
                            style = MaterialTheme.typography.displayLarge,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .graphicsLayer(
                                    scaleX = pigScale,
                                    scaleY = pigScale
                                )
                        )
                    }
                }
            },
            confirmButton = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            viewModel.completeCoinAnimation(onSaveComplete = onBack)
                        }
                    ) {
                        Text("ポイントを受け取る")
                    }
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
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 2.dp,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = { showConfirmDialog = true },
                    enabled = viewModel.canSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
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
            Spacer(modifier = Modifier.height(8.dp))

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
                label = {
                    Row {
                        Text("店名")
                        Text(
                            text = " *必須",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = {
                    viewModel.searchAndAutoFillShop(context)
                },
                enabled = viewModel.nameInput.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text("✨ 店名から住所・電話番号を自動入力")
            }

            OutlinedTextField(
                value = viewModel.addressInput,
                onValueChange = { viewModel.updateAddress(it) },
                label = { Text("住所") },
                placeholder = { Text("例：東京都千代田区...") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (viewModel.nameInput.isNotBlank()) {
                                val encodedName = java.net.URLEncoder.encode(viewModel.nameInput, "UTF-8")
                                val intent = android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse("https://www.google.com/maps/search/?api=1&query=$encodedName")
                                )
                                context.startActivity(intent)
                            }
                        }
                    ) {
                        Text("🌐", fontSize = 20.sp)
                    }
                }
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

            // --- 評価 ---
            Text("評価")
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                var lastSelectedStar by remember { mutableStateOf(-1) }
                for (i in 1..5) {
                    val isSelected = i <= viewModel.ratingInput.toInt()
                    val starTargetScale = if (lastSelectedStar == i) 1.3f else 1.0f
                    val scale by animateFloatAsState(
                        targetValue = starTargetScale,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
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

            var isReactionExpanded by remember { mutableStateOf(false) }

            OutlinedButton(
                onClick = { isReactionExpanded = !isReactionExpanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isReactionExpanded) "リアクション一覧を閉じる" else "🍚リアクションを追加",
                        fontSize = 16.sp
                    )

                    Icon(
                        imageVector = if (isReactionExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = isReactionExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val goodReactions = listOf(
                        "💖" to "リピートしたい！",
                        "👛" to "コスパがいい！",
                        "✨" to "おしゃれ！",
                        "🍖" to "ボリューム満点！",
                        "🍀" to "ヘルシー！",
                        "⚡️" to "提供が早い！"
                    )
                    val badReactions = listOf(
                        "💔" to "リピなし",
                        "💸" to "高すぎる",
                        "⏳" to "提供が遅い",
                        "😖" to "口に合わない"
                    )

                    ReactionSection(
                        title = "Good 👍",
                        reactions = goodReactions,
                        selectedReactions = selectedReactions.value,
                        onReactionChange = { emoji, isSelected ->
                            selectedReactions.value =
                                if (isSelected) selectedReactions.value + emoji else selectedReactions.value - emoji
                        }
                    )

                    ReactionSection(
                        title = "Bad 👎",
                        reactions = badReactions,
                        selectedReactions = selectedReactions.value,
                        onReactionChange = { emoji, isSelected ->
                            selectedReactions.value =
                                if (isSelected) selectedReactions.value + emoji else selectedReactions.value - emoji
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// 💡 ファイルの末尾に、定義が迷子になっていた ReactionSection を完全結合！
@Composable
fun ReactionSection(
    title: String,
    reactions: List<Pair<String, String>>,
    selectedReactions: Set<String>,
    onReactionChange: (String, Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = RoundedCornerShape(12.dp))
                .background(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            reactions.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    rowItems.forEach { (emoji, label) ->
                        val isSelected = selectedReactions.contains(emoji)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onReactionChange(emoji, !isSelected) }
                                .padding(vertical = 6.dp, horizontal = 4.dp)
                        ) {
                            IconToggleButton(
                                checked = isSelected,
                                onCheckedChange = { checked -> onReactionChange(emoji, checked) },
                                modifier = Modifier
                                    .size(44.dp)
                                    .aspectRatio(1F)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.White,
                                            shape = RoundedCornerShape(50)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                            shape = RoundedCornerShape(50)
                                        )
                                ) {
                                    Text(text = emoji, style = MaterialTheme.typography.titleMedium)
                                }
                            }
                            Text(text = label, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                        }
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}