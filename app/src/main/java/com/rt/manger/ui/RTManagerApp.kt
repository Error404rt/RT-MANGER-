package com.rt.manger.ui

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.rt.manger.export.ZipExporter
import com.rt.manger.model.*
import java.util.UUID

@Composable
fun RTManagerApp(vm: AppViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<InstalledApp?>(null) }
    var selectedPack by remember { mutableStateOf<IconPack?>(null) }
    val apps by vm.apps.collectAsState()
    val packs by vm.packs.collectAsState()
    val loading by vm.loading.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri != null) ZipExporter.writePack(context, uri, apps, selectedPack, vm.projects)
    }
    val filtered = remember(apps, query) {
        apps.filter { it.label.contains(query, true) || it.packageName.contains(query, true) }
    }

    if (selected != null) {
        EditorScreen(selected!!, selectedPack, vm.projects) { selected = null }
    } else {
        HomeScreen(
            query = query,
            onQuery = { query = it },
            apps = filtered,
            packs = packs,
            selectedPack = selectedPack,
            loading = loading,
            onPack = { selectedPack = it },
            onRefresh = vm::refresh,
            onEdit = { selected = it },
            onExport = { export.launch((selectedPack?.label ?: "RTManager") + "-custom.zip") }
        )
    }
}

@Composable
private fun HomeScreen(
    query: String,
    onQuery: (String) -> Unit,
    apps: List<InstalledApp>,
    packs: List<IconPack>,
    selectedPack: IconPack?,
    loading: Boolean,
    onPack: (IconPack?) -> Unit,
    onRefresh: () -> Unit,
    onEdit: (InstalledApp) -> Unit,
    onExport: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("RT Manager", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onExport) { Icon(Icons.Default.AutoAwesome, "Export pack") }
            IconButton(onClick = onRefresh) { Icon(Icons.Default.Refresh, "Rescan") }
        }
        OutlinedTextField(
            value = query,
            onValueChange = onQuery,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, null) },
            placeholder = { Text("Search installed apps") }
        )
        Spacer(Modifier.height(12.dp))
        Text("Icon packs", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = selectedPack == null,
                    onClick = { onPack(null) },
                    label = { Text("Original") }
                )
            }
            items(packs, key = { it.packageName }) { p ->
                FilterChip(
                    selected = selectedPack?.packageName == p.packageName,
                    onClick = { onPack(p) },
                    label = { Text(p.label) }
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        if (selectedPack != null) {
            Text(
                selectedPack.label + "  •  " +
                    apps.count { selectedPack.supports(it) } + " supported  •  " +
                    apps.count { !selectedPack.supports(it) } + " missing",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(8.dp))
        }
        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(apps, key = { it.packageName }) { app ->
                    val shown = selectedPack?.iconFor(app) ?: app.icon
                    Surface(
                        Modifier.fillMaxWidth(),
                        RoundedCornerShape(16.dp),
                        tonalElevation = 1.dp
                    ) {
                        Row(
                            Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppIcon(shown, app.primaryColor)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(app.label, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    if (selectedPack?.supports(app) == true) "Supported"
                                    else if (selectedPack == null) "Original"
                                    else "Missing from pack",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            IconButton(onClick = { onEdit(app) }) {
                                Icon(Icons.Default.Edit, "Edit")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppIcon(bitmap: Bitmap?, fallback: Color?) {
    Box(
        Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(fallback ?: Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        bitmap?.let { Image(it.asImageBitmap(), null, Modifier.size(44.dp)) }
    }
}

@Composable
private fun EditorScreen(
    app: InstalledApp,
    pack: IconPack?,
    store: com.rt.manger.data.ProjectStore,
    onBack: () -> Unit
) {
    val layers = remember(app.packageName) {
        mutableStateListOf<IconLayer>().apply {
            addAll(store.load(app.packageName))
            if (isEmpty()) add(IconLayer("source", "Original icon", LayerType.RASTER))
        }
    }
    var selectedIndex by remember { mutableIntStateOf(layers.lastIndex) }
    var showColors by remember { mutableStateOf(false) }
    val source = pack?.iconFor(app) ?: app.icon

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
            Column {
                Text(app.label, style = MaterialTheme.typography.titleLarge)
                Text(pack?.label ?: "Original", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.weight(1f))
            Button(onClick = { store.save(app.packageName, layers) }) { Text("Save") }
        }

        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            Box(
                Modifier.size(300.dp).clip(RoundedCornerShape(32.dp)).background(Color(0xFFF3F3F3))
            ) {
                source?.let { Image(it.asImageBitmap(), null, Modifier.fillMaxSize().padding(12.dp)) }
                CanvasOverlay(layers)
            }
        }

        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = {
                layers.add(
                    IconLayer(
                        UUID.randomUUID().toString(),
                        "Line " + (layers.count { it.type == LayerType.LINE } + 1),
                        LayerType.LINE
                    )
                )
                selectedIndex = layers.lastIndex
            }) {
                Icon(Icons.Default.Add, null)
                Text("Line")
            }
            OutlinedButton(onClick = { showColors = true }) { Text("Color") }
            OutlinedButton(onClick = { store.save(app.packageName, layers) }) { Text("Save project") }
        }

        if (layers.isNotEmpty()) {
            val currentIndex = selectedIndex.coerceIn(0, layers.lastIndex)
            val current = layers[currentIndex]
            Column(Modifier.fillMaxWidth().height(210.dp).padding(12.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Layers, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Layers", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = {
                        if (current.type == LayerType.LINE) {
                            layers[currentIndex] = current.copy(
                                strokeWidth = (current.strokeWidth + 4f).coerceAtMost(80f)
                            )
                        }
                    }) { Text("+") }
                    IconButton(onClick = {
                        if (layers.size > 1) {
                            layers.removeAt(currentIndex)
                            selectedIndex = (currentIndex - 1).coerceAtLeast(0)
                        }
                    }) { Icon(Icons.Default.Delete, null) }
                }
                LazyColumn {
                    items(layers, key = { it.id }) { layer ->
                        ListItem(
                            headlineContent = { Text(layer.name) },
                            supportingContent = { Text(layer.type.name) },
                            leadingContent = {
                                IconButton(onClick = {
                                    val i = layers.indexOfFirst { it.id == layer.id }
                                    layers[i] = layer.copy(visible = !layer.visible)
                                }) {
                                    Icon(
                                        if (layer.visible) Icons.Default.Visibility
                                        else Icons.Default.VisibilityOff,
                                        null
                                    )
                                }
                            },
                            modifier = Modifier.clickable {
                                selectedIndex = layers.indexOfFirst { it.id == layer.id }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showColors) {
        AlertDialog(
            onDismissRequest = { showColors = false },
            confirmButton = {
                TextButton(onClick = { showColors = false }) { Text("Done") }
            },
            title = { Text("Layer color") },
            text = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        0xFF111111.toInt(),
                        0xFFFFFFFF.toInt(),
                        0xFFF44336.toInt(),
                        0xFF2196F3.toInt(),
                        0xFF4CAF50.toInt(),
                        0xFFFFC107.toInt()
                    ).forEach { argb ->
                        Button(
                            onClick = {
                                layers[selectedIndex.coerceIn(0, layers.lastIndex)] =
                                    layers[selectedIndex.coerceIn(0, layers.lastIndex)].copy(colorArgb = argb)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(argb))
                        ) { Text(" ") }
                    }
                }
            }
        )
    }
}

@Composable
private fun CanvasOverlay(layers: List<IconLayer>) {
    Canvas(Modifier.fillMaxSize()) {
        layers.filter { it.visible && it.type == LayerType.LINE }.forEach { layer ->
            drawLine(
                color = Color(layer.colorArgb),
                start = androidx.compose.ui.geometry.Offset(
                    layer.x1 * size.width,
                    layer.y1 * size.height
                ),
                end = androidx.compose.ui.geometry.Offset(
                    layer.x2 * size.width,
                    layer.y2 * size.height
                ),
                strokeWidth = layer.strokeWidth
            )
        }
    }
}
