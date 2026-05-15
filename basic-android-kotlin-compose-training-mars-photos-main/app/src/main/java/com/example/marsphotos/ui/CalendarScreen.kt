package com.example.marsphotos.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import com.example.marsphotos.ui.screens.LunchViewModel
import java.util.*
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.clickable
import com.example.marsphotos.ui.components.LunchCard
import com.example.marsphotos.data.LunchEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: LunchViewModel,
    onBack: () -> Unit,
    onLunchClick: (LunchEntity) -> Unit   // ←追加
){
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    var selectedDate by remember { mutableStateOf<Long?>(null) }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)

            val selected = calendar.timeInMillis
            selectedDate = selected

            viewModel.setFilterDate(selected) // ←ここ！！
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val filteredList = remember(selectedDate, viewModel.lunchList.toList()) {
        if (selectedDate == null) {
            viewModel.lunchList
        } else {
            viewModel.lunchList.filter { lunch ->
                val c1 = Calendar.getInstance().apply { timeInMillis = lunch.date }
                val c2 = Calendar.getInstance().apply { timeInMillis = selectedDate!! }

                c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                        c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("カレンダー検索") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.setFilterDate(null)   // ← これ追加
                        onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { datePickerDialog.show() }) {
                Icon(Icons.Default.DateRange, contentDescription = null)
            }
        }
    ) { padding ->

        Column(modifier = Modifier.padding(padding)) {

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredList) { lunch ->
                    LunchCard(
                        lunch = lunch,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onLunchClick(lunch)
                            }
                    )
                }
            }
        }
    }
}