package com.example.bayan.ui.mindmap

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bayan.data.model.MapEdge
import com.example.bayan.data.model.MapNode
import com.example.bayan.data.model.MindMap
import com.example.bayan.data.quran.QuranData
import java.util.UUID
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MindMapStudioScreen(
    userMindMaps: List<MindMap>,
    onSaveMindMap: (MindMap) -> Unit,
    onDeleteMindMap: (String) -> Unit,
    onEditorActiveChange: (Boolean) -> Unit = {}
) {
    var activeMap by remember { mutableStateOf<MindMap?>(null) }
    var showNewMapDialog by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(activeMap) {
        onEditorActiveChange(activeMap != null)
    }

    if (activeMap != null) {
        BackHandler {
            activeMap = null
            onEditorActiveChange(false)
        }
        MindMapCanvasEditor(
            initialMap = activeMap!!,
            onBack = {
                activeMap = null
                onEditorActiveChange(false)
            },
            onSave = { updated ->
                onSaveMindMap(updated)
                activeMap = updated
            }
        )
    } else {
        MindMapListView(
            userMaps = userMindMaps,
            onSelectMap = { activeMap = it },
            onCreateNewMap = { showNewMapDialog = true },
            onDeleteMap = onDeleteMindMap
        )
    }

    if (showNewMapDialog) {
        NewMindMapDialog(
            onDismiss = { showNewMapDialog = false },
            onCreate = { newMap ->
                showNewMapDialog = false
                onSaveMindMap(newMap)
                activeMap = newMap
            }
        )
    }
}

@Composable
fun MindMapListView(
    userMaps: List<MindMap>,
    onSelectMap: (MindMap) -> Unit,
    onCreateNewMap: () -> Unit,
    onDeleteMap: (String) -> Unit
) {
    val templates = remember { QuranData.getPrebuiltMindMaps() }
    var mapToDelete by remember { mutableStateOf<MindMap?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateNewMap,
                modifier = Modifier.testTag("create_mindmap_fab"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Mind Map")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(54.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Hub,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Thematic Mind Mapping",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Visually structure Quranic discourse, narrative arcs, and theological principles with interactive graphs.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Curated Quranic Templates Section
            item {
                Text(
                    text = "Scholarly Templates",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(templates, key = { it.id }) { template ->
                TemplateMapCard(
                    template = template,
                    onOpen = { onSelectMap(template) }
                )
            }

            // User Saved Maps Section
            item {
                Text(
                    text = "My Study Mind Maps (${userMaps.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            if (userMaps.isEmpty()) {
                item {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Timeline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No personal mind maps yet",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Create your first thematic diagram or explore the templates above.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(userMaps, key = { it.id }) { userMap ->
                    UserMapCard(
                        mindMap = userMap,
                        onOpen = { onSelectMap(userMap) },
                        onDelete = { mapToDelete = userMap }
                    )
                }
            }
        }
    }

    mapToDelete?.let { map ->
        AlertDialog(
            onDismissRequest = { mapToDelete = null },
            title = { Text("Delete Mind Map") },
            text = { Text("Delete '${map.title}'? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteMap(map.id)
                        mapToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { mapToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TemplateMapCard(
    template: MindMap,
    onOpen: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = template.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = template.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onOpen,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Open")
            }
        }
    }
}

@Composable
fun UserMapCard(
    mindMap: MindMap,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Hub,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mindMap.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (mindMap.description.isNotBlank()) {
                    Text(
                        text = mindMap.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                val nodeCount = QuranData.deserializeNodes(mindMap.nodesJson).size
                Text(
                    text = "$nodeCount Concept Nodes",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete MindMap",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MindMapCanvasEditor(
    initialMap: MindMap,
    onBack: () -> Unit,
    onSave: (MindMap) -> Unit
) {
    var title by remember { mutableStateOf(initialMap.title) }
    var nodes by remember { mutableStateOf(QuranData.deserializeNodes(initialMap.nodesJson)) }
    var edges by remember { mutableStateOf(QuranData.deserializeEdges(initialMap.edgesJson)) }

    var selectedNode by remember { mutableStateOf<MapNode?>(null) }
    var showAddNodeDialog by remember { mutableStateOf(false) }
    var showConnectNodesDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "${nodes.size} Nodes • ${edges.size} Connections (Drag nodes to reposition)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val updated = initialMap.copy(
                                title = title,
                                nodesJson = QuranData.serializeNodes(nodes),
                                edgesJson = QuranData.serializeEdges(edges)
                            )
                            onSave(updated)
                        },
                        modifier = Modifier.testTag("save_mindmap_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save Map", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 4.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showAddNodeDialog = true },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Node")
                    }

                    OutlinedButton(
                        onClick = { showConnectNodesDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        enabled = nodes.size >= 2
                    ) {
                        Icon(Icons.Default.LinearScale, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Connect")
                    }

                    Button(
                        onClick = {
                            // Auto-arrange in an elegant circular radial pattern
                            val centerX = 200f
                            val centerY = 280f
                            val radius = 160f
                            val count = nodes.size
                            if (count > 0) {
                                val updatedNodes = nodes.mapIndexed { idx, n ->
                                    if (idx == 0) {
                                        n.copy(posX = centerX, posY = centerY)
                                    } else {
                                        val angle = (2 * Math.PI * (idx - 1) / (count - 1)).toFloat()
                                        n.copy(
                                            posX = centerX + radius * kotlin.math.cos(angle.toDouble()).toFloat(),
                                            posY = centerY + radius * kotlin.math.sin(angle.toDouble()).toFloat()
                                        )
                                    }
                                }
                                nodes = updatedNodes
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Arrange")
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Background Canvas drawing connection lines
            Canvas(modifier = Modifier.fillMaxSize()) {
                val nodeMap = nodes.associateBy { it.id }
                edges.forEach { edge ->
                    val fromNode = nodeMap[edge.fromNodeId]
                    val toNode = nodeMap[edge.toNodeId]
                    if (fromNode != null && toNode != null) {
                        val start = Offset(fromNode.posX + 80f, fromNode.posY + 40f)
                        val end = Offset(toNode.posX + 80f, toNode.posY + 40f)

                        drawLine(
                            color = Color(0xFF0D5341).copy(alpha = 0.6f),
                            start = start,
                            end = end,
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            // Draggable Node Components
            nodes.forEach { node ->
                CanvasNodeView(
                    node = node,
                    onNodeMoved = { newX, newY ->
                        nodes = nodes.map { if (it.id == node.id) it.copy(posX = newX, posY = newY) else it }
                    },
                    onClick = { selectedNode = node }
                )
            }
        }
    }

    // Node Inspector / Editor Dialog
    selectedNode?.let { node ->
        NodeInspectorDialog(
            node = node,
            onDismiss = { selectedNode = null },
            onUpdate = { updated ->
                nodes = nodes.map { if (it.id == updated.id) updated else it }
                selectedNode = null
            },
            onDelete = {
                nodes = nodes.filterNot { it.id == node.id }
                edges = edges.filterNot { it.fromNodeId == node.id || it.toNodeId == node.id }
                selectedNode = null
            }
        )
    }

    // Add Node Dialog
    if (showAddNodeDialog) {
        AddNodeDialog(
            onDismiss = { showAddNodeDialog = false },
            onAdd = { newNode ->
                nodes = nodes + newNode
                showAddNodeDialog = false
            }
        )
    }

    // Connect Nodes Dialog
    if (showConnectNodesDialog) {
        ConnectNodesDialog(
            nodes = nodes,
            onDismiss = { showConnectNodesDialog = false },
            onConnect = { fromId, toId, label ->
                val newEdge = MapEdge(
                    id = "edge_${UUID.randomUUID()}",
                    fromNodeId = fromId,
                    toNodeId = toId,
                    label = label
                )
                edges = edges + newEdge
                showConnectNodesDialog = false
            }
        )
    }
}

@Composable
fun CanvasNodeView(
    node: MapNode,
    onNodeMoved: (Float, Float) -> Unit,
    onClick: () -> Unit
) {
    var offsetX by remember(node.id) { mutableStateOf(node.posX) }
    var offsetY by remember(node.id) { mutableStateOf(node.posY) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(node.id) {
                detectDragGestures(
                    onDragEnd = {
                        onNodeMoved(offsetX, offsetY)
                    }
                ) { change, dragAmount ->
                    change.consume()
                    offsetX = (offsetX + dragAmount.x).coerceAtLeast(10f)
                    offsetY = (offsetY + dragAmount.y).coerceAtLeast(10f)
                }
            }
            .clickable(onClick = onClick)
            .shadow(4.dp, RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
            .border(
                1.5.dp,
                parseHexColor(node.colorHex),
                RoundedCornerShape(14.dp)
            )
            .padding(12.dp)
            .widthIn(min = 140.dp, max = 200.dp)
    ) {
        Column {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = parseHexColor(node.colorHex).copy(alpha = 0.2f)
            ) {
                Text(
                    text = node.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = parseHexColor(node.colorHex),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (node.arabicText.isNotBlank()) {
                Text(
                    text = node.arabicText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = parseHexColor(node.colorHex),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = node.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (node.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = node.notes,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun NodeInspectorDialog(
    node: MapNode,
    onDismiss: () -> Unit,
    onUpdate: (MapNode) -> Unit,
    onDelete: () -> Unit
) {
    var label by remember { mutableStateOf(node.label) }
    var arabicText by remember { mutableStateOf(node.arabicText) }
    var notes by remember { mutableStateOf(node.notes) }
    var category by remember { mutableStateOf(node.category) }
    var colorHex by remember { mutableStateOf(node.colorHex) }

    val presetColors = listOf("#0D5341", "#907000", "#245F6D", "#7B1FA2", "#C2185B", "#1E88E5", "#D84315")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Concept Node") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Concept Label *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = arabicText,
                    onValueChange = { arabicText = it },
                    label = { Text("Arabic Concept / Ayah Reference") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Thematic Category") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Theological Notes / Significance") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Node Accent Color",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    presetColors.forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(parseHexColor(hex))
                                .clickable { colorHex = hex }
                                .then(
                                    if (colorHex == hex) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    else Modifier
                                )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onUpdate(
                        node.copy(
                            label = label.trim(),
                            arabicText = arabicText.trim(),
                            notes = notes.trim(),
                            category = category.trim(),
                            colorHex = colorHex
                        )
                    )
                },
                enabled = label.isNotBlank()
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDelete) {
                    Text("Delete Node", color = MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
fun AddNodeDialog(
    onDismiss: () -> Unit,
    onAdd: (MapNode) -> Unit
) {
    var label by remember { mutableStateOf("") }
    var arabicText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Core Theme") }
    var notes by remember { mutableStateOf("") }
    var colorHex by remember { mutableStateOf("#0D5341") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Concept Node") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Concept Label *") },
                    placeholder = { Text("e.g. Covenant, Taqwa, Divine Mercy") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = arabicText,
                    onValueChange = { arabicText = it },
                    label = { Text("Arabic Text") },
                    placeholder = { Text("e.g. الميثاق, التقوى") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Theological Context") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdd(
                        MapNode(
                            id = "node_${UUID.randomUUID()}",
                            label = label.trim(),
                            arabicText = arabicText.trim(),
                            category = category.trim(),
                            notes = notes.trim(),
                            colorHex = colorHex,
                            posX = (100..250).random().toFloat(),
                            posY = (150..350).random().toFloat()
                        )
                    )
                },
                enabled = label.isNotBlank()
            ) {
                Text("Add Node")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectNodesDialog(
    nodes: List<MapNode>,
    onDismiss: () -> Unit,
    onConnect: (fromId: String, toId: String, label: String) -> Unit
) {
    var fromNode by remember { mutableStateOf(nodes.firstOrNull()) }
    var toNode by remember { mutableStateOf(nodes.getOrNull(1) ?: nodes.firstOrNull()) }
    var relationLabel by remember { mutableStateOf("Relates to") }

    val presetRelations = listOf("Reveals", "Affirms", "Contrasts with", "Manifests as", "Synthesizes", "Leads to prayer for")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Connect Nodes") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("From Node:", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(nodes) { node ->
                        FilterChip(
                            selected = fromNode?.id == node.id,
                            onClick = { fromNode = node },
                            label = { Text(node.label, maxLines = 1) }
                        )
                    }
                }

                Text("To Node:", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(nodes) { node ->
                        FilterChip(
                            selected = toNode?.id == node.id,
                            onClick = { toNode = node },
                            label = { Text(node.label, maxLines = 1) }
                        )
                    }
                }

                OutlinedTextField(
                    value = relationLabel,
                    onValueChange = { relationLabel = it },
                    label = { Text("Relationship Label") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(presetRelations) { rel ->
                        FilterChip(
                            selected = relationLabel == rel,
                            onClick = { relationLabel = rel },
                            label = { Text(rel) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fromNode != null && toNode != null && fromNode!!.id != toNode!!.id) {
                        onConnect(fromNode!!.id, toNode!!.id, relationLabel.trim())
                    }
                },
                enabled = fromNode != null && toNode != null && fromNode!!.id != toNode!!.id
            ) {
                Text("Connect")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun NewMindMapDialog(
    onDismiss: () -> Unit,
    onCreate: (MindMap) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var surahNumber by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Mind Map") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Mind Map Title *") },
                    placeholder = { Text("e.g. Core Structure of Surah Yusuf") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("Overview of narrative arcs & theological motifs") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = surahNumber,
                    onValueChange = { surahNumber = it.filter { c -> c.isDigit() } },
                    label = { Text("Surah # (Optional)") },
                    placeholder = { Text("1-114") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rootNode = MapNode(
                        id = "node_root",
                        label = title.trim(),
                        category = "Central Theme",
                        posX = 200f,
                        posY = 100f
                    )
                    val newMap = MindMap(
                        id = "map_${UUID.randomUUID()}",
                        title = title.trim(),
                        description = description.trim(),
                        surahNumber = surahNumber.toLongOrNull() ?: 0L,
                        nodesJson = QuranData.serializeNodes(listOf(rootNode)),
                        edgesJson = "[]"
                    )
                    onCreate(newMap)
                },
                enabled = title.isNotBlank()
            ) {
                Text("Create Canvas")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun parseHexColor(hex: String): Color {
    return try {
        val clean = hex.removePrefix("#")
        val colorInt = clean.toLong(16)
        if (clean.length == 6) {
            Color(colorInt or 0x00000000FF000000)
        } else {
            Color(colorInt)
        }
    } catch (_: Exception) {
        Color(0xFF0D5341)
    }
}
