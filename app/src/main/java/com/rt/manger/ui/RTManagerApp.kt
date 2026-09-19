package com.rt.manger.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rt.manger.model.IconLayer
import com.rt.manger.model.InstalledApp
import com.rt.manger.model.LayerType
import java.util.UUID

@Composable
fun RTManagerApp(viewModel: AppViewModel = viewModel()) {
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<InstalledApp?>(null) }
    val apps by viewModel.apps.collectAsState()
    val loading by viewModel.loading.collectAsState()

    val filteredApps = remember(apps, query) {
        apps.filter {
            it.label.contains(query, true) || it.packageName.contains(query, true)
        }
    }

    if (selected == null) {
        HomeScreen(
            query = query,
            onQueryChange = { query = it },
            apps = filteredApps,
            loading = loading,
            onRefresh = viewModel::refresh,
            onEdit = { selected = it }
        )
    } else {
        EditorScreen(app = selected!!, onBack = { selected = null })
    }
}

@Composable
private fun HomeScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    apps: List<InstalledApp>,
    loading: Boolean,
    onRefresh: () -> Unit,
    onEdit: (InstalledApp) -> Unit
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("RT Manager", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "Rescan")
            }
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, null) },
            placeholder = { Text("Search installed apps") }
        )
        Spacer(Modifier.height(16.dp))
        Text("Applications", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        if (loading) {
            Box(Modifier.fillMaxWidth().padding(32.dp), Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(apps, key = { it.packageName }) { app ->
                    AppRow(app) { onEdit(app) }
                }
            }
        }
    }
}

@Composable
private fun AppRow(app: InstalledApp, onEdit: () -> Unit) {
    Surface(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            AppIcon(app.icon, app.primaryColor)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(app.label, style = MaterialTheme.typography.titleMedium)
                Text(app.packageName, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
        }
    }
}

@Composable
private fun AppIcon(bitmap: Bitmap?, fallback: Color?) {
    Box(
        Modifier.size(52.dp).clip(RoundedCornerShape(14.dp))
            .background(fallback ?: Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        bitmap?.let {
            Image(it.asImageBitmap(), null, Modifier.size(44.dp))
        }
    }
}

@Composable
private fun EditorScreen(app: InstalledApp, onBack: () -> Unit) {
    val layers = remember {
        mutableStateListOf(
            IconLayer("source", "Original icon", LayerType.RASTER)
        )
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Text("‹", style = MaterialTheme.typography.headlineMedium)
            }
            Column {
                Text(app.label, style = MaterialTheme.typography.titleLarge)
                Text("Icon editor", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { }) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "Finish and export")
            }
        }

        Box(
            Modifier.weight(1f).fillMaxWidth().padding(20.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            app.icon?.let {
                Image(it.asImageBitmap(), null, Modifier.size(220.dp))
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Layers, null)
            Spacer(Modifier.width(8.dp))
            Text("Layers", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = {
                val lineNumber = layers.count { it.type == LayerType.LINE } + 1
                layers.add(
                    IconLayer(
                        id = UUID.randomUUID().toString(),
                        name = "Line $lineNumber",
                        type = LayerType.LINE
                    )
                )
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add layer")
            }
        }

        LazyColumn(
            Modifier.height(180.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(layers, key = { it.id }) { layer ->
                Surface(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Visibility, null)
                        Spacer(Modifier.width(8.dp))
                        Text(layer.name, Modifier.weight(1f))
                        Text(layer.type.name)
                    }
                }
            }
        }
    }
}
