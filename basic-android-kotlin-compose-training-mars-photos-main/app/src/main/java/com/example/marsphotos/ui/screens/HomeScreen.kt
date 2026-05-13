package com.example.marsphotos.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.marsphotos.R
import com.example.marsphotos.data.LunchEntity

// --- 状態管理用のSealed Interfaceを定義 (エラー回避のため) ---

@Composable
fun HomeScreen(
    marsUiState: MarsUiState, // MarsPhotosAppからの呼び出しに合わせる
    onCardClick: (LunchEntity) -> Unit,
    onEditClick: (LunchEntity) -> Unit,
    onDeleteClick: (LunchEntity) -> Unit,
    retryAction: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    // 以前のMarsUiStateの仕組みを維持しつつ、ランチリストを表示する
    when (marsUiState) {
        is MarsUiState.Loading -> LoadingScreen(modifier = modifier.fillMaxSize())
        is MarsUiState.Success -> LunchGridScreen(
            lunches = marsUiState.photos,
            onCardClick = onCardClick,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick,
            modifier = modifier.fillMaxWidth(),
            contentPadding = contentPadding
        )
        is MarsUiState.Error -> ErrorScreen(retryAction, modifier = modifier.fillMaxSize())
    }
}

/**
 * 読み込み中画面
 */
@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        CircularProgressIndicator() // 画像がない場合のエラー回避に標準のぐるぐるを表示
    }
}

/**
 * エラー画面
 */
@Composable
fun ErrorScreen(retryAction: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(R.string.loading_failed), modifier = Modifier.padding(16.dp))
        Button(onClick = retryAction) {
            Text(stringResource(R.string.retry))
        }
    }
}

/**
 * ランチ一覧のグリッド表示
 */
@Composable
fun LunchGridScreen(
    lunches: List<LunchEntity>,
    onCardClick: (LunchEntity) -> Unit,
    onEditClick: (LunchEntity) -> Unit,
    onDeleteClick: (LunchEntity) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(150.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp),
        contentPadding = contentPadding,
    ) {
        items(items = lunches, key = { lunch -> lunch.id }) { lunch ->
            LunchPhotoCard(
                lunch = lunch,
                onCardClick = onCardClick,
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick,
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
            )
        }
    }
}

/**
 * 三点リーダー付きのランチカードUI
 */
@Composable
fun LunchPhotoCard(
    lunch: LunchEntity,
    onCardClick: (LunchEntity) -> Unit,
    onEditClick: (LunchEntity) -> Unit,
    onDeleteClick: (LunchEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.padding(8.dp).fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        // Row (横並び) にすることで、写真とボタンが重ならないようにします
        Row(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左側：写真
            AsyncImage(
                model = lunch.photoUrl,
                contentDescription = null,
                modifier = Modifier.size(80.dp).aspectRatio(1f),
                contentScale = ContentScale.Crop
            )

            // 中央：名前
            Text(
                text = lunch.name,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            )

            // 右側：三点リーダーボタン（ここが重要！）
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "メニュー",
                        tint = MaterialTheme.colorScheme.primary // 目立つ色にする
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(text = { Text("削除") }, onClick = { onDeleteClick(lunch) })
                }
            }
        }
    }
}

/**
 * 詳細画面用のUI
 */
@Composable
fun LunchDetailScreen(
    lunch: LunchEntity,
    onEditClick: (LunchEntity) -> Unit,
    onDeleteClick: (LunchEntity) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context = LocalContext.current)
                    .data(lunch.photoUrl)
                    .crossfade(true)
                    .build(),
                error = painterResource(R.drawable.ic_broken_image),
                placeholder = painterResource(R.drawable.loading_img),
                contentDescription = lunch.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // 右上の三点リーダー
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                IconButton(
                    onClick = { expanded = true },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "メニュー",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("編集") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            expanded = false
                            onEditClick(lunch)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("削除") },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                        onClick = {
                            expanded = false
                            onDeleteClick(lunch)
                        }
                    )
                }
            }

            // 戻るボタン (リソース依存を避けるため標準アイコンに変更)
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack, // 標準の矢印アイコンに変更
                    contentDescription = "戻る"
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = lunch.name,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "評価: ${lunch.rating} ⭐",
                style = MaterialTheme.typography.bodyLarge
            )

            // Divider (古いバージョンへの配慮でDividerに変更)
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                text = "カテゴリ: ${lunch.category}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = lunch.comment.ifBlank { "コメントなし" },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onBackClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("一覧に戻る")
            }
        }
    }
}