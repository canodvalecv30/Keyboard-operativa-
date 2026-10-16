package com.example.ui.keyboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardReturn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryEntity
import com.example.data.model.MacroProcessor
import com.example.data.model.SnippetEntity

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AutoPasteKeyboardLayout(
    categories: List<CategoryEntity>,
    snippets: List<SnippetEntity>,
    autoSpace: Boolean = true,
    autoEnter: Boolean = false,
    hapticsEnabled: Boolean = true,
    keyHeightScale: Float = 1.0f,
    isTikTokDetected: Boolean = false,
    tikTokLiveOnlyMode: Boolean = false,
    initialBurstDelayMs: Long = 50L,
    initialPasteSpeedDelayMs: Long = 10L,
    initialShowToastOnPaste: Boolean = false,
    initialUltraFastMode: Boolean = true,
    initialGridMode: Boolean = true,
    initialAutoSendActive: Boolean = true,
    initialColumnCount: Int = 2,
    initialButtonsReversed: Boolean = false,
    onUpdateBurstDelay: (Long) -> Unit = {},
    onUpdatePasteSpeedDelay: (Long) -> Unit = {},
    onUpdateShowToastOnPaste: (Boolean) -> Unit = {},
    onUpdateUltraFastMode: (Boolean) -> Unit = {},
    onUpdateButtonsReversed: (Boolean) -> Unit = {},
    onFastPasteAndSend: ((String, Boolean) -> Unit)? = null,
    onInsertText: (String) -> Unit,
    onDeleteLastChar: () -> Unit,
    onInsertNewline: () -> Unit,
    onClearAllText: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onSnippetUsed: (SnippetEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val configuration = LocalConfiguration.current

    val screenWidthDp = configuration.screenWidthDp
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    var currentBurstDelayMs by remember(initialBurstDelayMs) { mutableStateOf(initialBurstDelayMs) }
    var currentPasteSpeedDelayMs by remember(initialPasteSpeedDelayMs) { mutableStateOf(initialPasteSpeedDelayMs) }
    var showToastOnPaste by remember(initialShowToastOnPaste) { mutableStateOf(initialShowToastOnPaste) }
    var ultraFastMode by remember(initialUltraFastMode) { mutableStateOf(initialUltraFastMode) }

    // Auto calculate default columns for current phone screen width
    val defaultColumns = remember(screenWidthDp, isLandscape, initialColumnCount) {
        if (initialColumnCount in 2..4) initialColumnCount
        else when {
            screenWidthDp < 360 -> 2
            screenWidthDp in 360..599 -> if (isLandscape) 3 else 2
            else -> if (isLandscape) 4 else 3
        }
    }

    fun triggerHaptic() {
        if (hapticsEnabled) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    var selectedCategoryId by remember(categories) {
        mutableIntStateOf(if (categories.isNotEmpty()) categories.first().id else -1)
    }

    var showFavoritesOnly by remember { mutableStateOf(false) }
    var repeatMultiplier by remember { mutableIntStateOf(1) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var isGridMode by remember(initialGridMode) { mutableStateOf(initialGridMode) }
    var isAutoSendActive by remember(initialAutoSendActive) { mutableStateOf(initialAutoSendActive) }
    var gridColumnCount by remember(defaultColumns) { mutableIntStateOf(defaultColumns) }
    var isButtonsReversed by remember(initialButtonsReversed) { mutableStateOf(initialButtonsReversed) }

    // Ráfaga & Custom Multi-Send Mode States
    val coroutineScope = rememberCoroutineScope()
    var activeBurstSnippetId by remember { mutableStateOf<Int?>(null) }
    var burstJob by remember { mutableStateOf<Job?>(null) }

    // Custom Batch Send States (Pide texto y cuántos enviar uno por uno)
    var showCustomBatchDialog by remember { mutableStateOf(false) }
    var customBatchText by remember { mutableStateOf("") }
    var customBatchCountText by remember { mutableStateOf("10") }
    var isCustomBatchSending by remember { mutableStateOf(false) }
    var customBatchProgress by remember { mutableIntStateOf(0) }
    var customBatchTotal by remember { mutableIntStateOf(0) }

    // Single Paste Debouncing States (Prevents accidental double paste)
    var lastPasteTimestamp by remember { androidx.compose.runtime.mutableLongStateOf(0L) }
    var lastPastedSnippetId by remember { mutableIntStateOf(-1) }
    var singlePasteJob by remember { mutableStateOf<Job?>(null) }

    val filteredSnippets = remember(snippets, selectedCategoryId, showFavoritesOnly, searchQuery, isSearchActive) {
        if (isSearchActive && searchQuery.isNotBlank()) {
            snippets.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.content.contains(searchQuery, ignoreCase = true)
            }
        } else if (showFavoritesOnly) {
            snippets.filter { it.isFavorite }
        } else {
            snippets.filter { it.categoryId == selectedCategoryId }
        }
    }

    suspend fun handlePasteSnippet(snippet: SnippetEntity, forceAutoSend: Boolean = true, showToast: Boolean = false) {
        triggerHaptic()
        val rawContent = MacroProcessor.stripSendMacros(snippet.content)
        val processed = MacroProcessor.processMacros(rawContent, context)

        // Paste phrase exactly ONCE (clean, single insertion)
        val textToPaste = if (autoSpace) "$processed " else processed
        val shouldAutoSend = forceAutoSend || isAutoSendActive || autoEnter

        // Show toast feedback only if specifically configured
        if (showToast && showToastOnPaste) {
            Toast.makeText(
                context,
                "⚡ Enviado: ${snippet.title}",
                Toast.LENGTH_SHORT
            ).show()
        }

        if (ultraFastMode && onFastPasteAndSend != null && currentPasteSpeedDelayMs == 0L) {
            // Direct zero-delay batch injection
            onFastPasteAndSend(textToPaste, shouldAutoSend)
        } else {
            onInsertText(textToPaste)
            if (shouldAutoSend) {
                if (currentPasteSpeedDelayMs > 0L) {
                    delay(currentPasteSpeedDelayMs)
                }
                onInsertNewline()
            }
        }

        onSnippetUsed(snippet)
    }

    var holdBurstJob by remember { mutableStateOf<Job?>(null) }

    fun startHoldBurst(snippet: SnippetEntity) {
        holdBurstJob?.cancel()
        holdBurstJob = coroutineScope.launch {
            handlePasteSnippet(snippet, forceAutoSend = true, showToast = false)
            delay(currentBurstDelayMs)
            while (isActive) {
                handlePasteSnippet(snippet, forceAutoSend = true, showToast = false)
                delay(currentBurstDelayMs)
            }
        }
    }

    fun stopHoldBurst() {
        holdBurstJob?.cancel()
        holdBurstJob = null
    }

    fun stopContinuousBurst() {
        burstJob?.cancel()
        burstJob = null
        activeBurstSnippetId = null
        isCustomBatchSending = false
        customBatchProgress = 0
        customBatchTotal = 0
    }

    fun startCustomBatchSend(text: String, count: Int) {
        if (text.isBlank() || count <= 0) return
        stopContinuousBurst()

        isCustomBatchSending = true
        customBatchProgress = 0
        customBatchTotal = count

        Toast.makeText(context, "🚀 Enviando $count mensajes a ${currentBurstDelayMs}ms...", Toast.LENGTH_SHORT).show()

        burstJob = coroutineScope.launch {
            try {
                val processedText = MacroProcessor.processMacros(text, context)
                val fullText = if (autoSpace) "$processedText " else processedText
                val commitDelay = if (currentBurstDelayMs <= 40L) 3L else 10L
                for (i in 1..count) {
                    if (!isActive) break
                    customBatchProgress = i
                    if (ultraFastMode && onFastPasteAndSend != null && currentBurstDelayMs <= 30L) {
                        onFastPasteAndSend(fullText, true)
                    } else {
                        onInsertText(fullText)
                        if (commitDelay > 0L) delay(commitDelay)
                        onInsertNewline()
                    }
                    val gapDelay = currentBurstDelayMs.coerceAtLeast(15L)
                    delay(gapDelay)
                }
                if (isActive) {
                    Toast.makeText(context, "✅ ¡Envío exacto de $count mensajes completado!", Toast.LENGTH_SHORT).show()
                }
            } finally {
                isCustomBatchSending = false
                customBatchProgress = 0
                customBatchTotal = 0
            }
        }
    }

    fun toggleBurstForSnippet(snippet: SnippetEntity) {
        if (activeBurstSnippetId == snippet.id && burstJob?.isActive == true) {
            stopContinuousBurst()
        } else {
            stopContinuousBurst()
            activeBurstSnippetId = snippet.id
            val modeLabel = if (isTikTokDetected) "TikTok Live Ultra" else "Ráfaga Rápida"
            Toast.makeText(context, "🔥 $modeLabel (${currentBurstDelayMs}ms)", Toast.LENGTH_SHORT).show()
            burstJob = coroutineScope.launch {
                while (isActive) {
                    handlePasteSnippet(snippet, forceAutoSend = true, showToast = false)
                    delay(currentBurstDelayMs)
                }
            }
        }
    }

    fun onSnippetKeyClick(snippet: SnippetEntity) {
        if (activeBurstSnippetId == snippet.id) {
            stopContinuousBurst()
            return
        }
        val now = android.os.SystemClock.uptimeMillis()
        // Strict Debounce: Avoid double-tap duplicate paste (no pegar doble)
        if (now - lastPasteTimestamp < 280L) {
            return
        }
        lastPasteTimestamp = now
        lastPastedSnippetId = snippet.id

        singlePasteJob?.cancel()
        singlePasteJob = coroutineScope.launch {
            handlePasteSnippet(snippet, forceAutoSend = isAutoSendActive, showToast = false)
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp, bottom = 20.dp)
        ) {
            // Active Burst / Multi-Send Stop Pill Button (ultra-compacto & discreto)
            if (activeBurstSnippetId != null || isCustomBatchSending) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFDC2626),
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .clickable {
                                triggerHaptic()
                                stopContinuousBurst()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = if (isCustomBatchSending) "🛑 Parar ($customBatchProgress/$customBatchTotal)" else "🛑 Parar",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Search Bar input if active
            AnimatedVisibility(visible = isSearchActive) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    androidx.compose.foundation.text.BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Buscar snippet...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                            innerTextField()
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        Text(
                            text = "✕",
                            modifier = Modifier
                                .clickable { searchQuery = "" }
                                .padding(horizontal = 4.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 2. Categories Scroll Bar
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search toggle chip
                item {
                    FilterChip(
                        selected = isSearchActive,
                        onClick = {
                            triggerHaptic()
                            isSearchActive = !isSearchActive
                        },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Buscar", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    )
                }

                // Starred / Favorites filter chip
                item {
                    FilterChip(
                        selected = showFavoritesOnly,
                        onClick = {
                            triggerHaptic()
                            showFavoritesOnly = !showFavoritesOnly
                            isSearchActive = false
                        },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (showFavoritesOnly) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Favoritos", style = MaterialTheme.typography.labelMedium)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF4A4458),
                            selectedLabelColor = Color(0xFFFFD700)
                        )
                    )
                }

                items(categories, key = { it.id }) { category ->
                    val isSelected = !showFavoritesOnly && !isSearchActive && selectedCategoryId == category.id
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            triggerHaptic()
                            showFavoritesOnly = false
                            isSearchActive = false
                            selectedCategoryId = category.id
                        },
                        label = {
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    )
                }

                // Open App Settings chip
                item {
                    FilterChip(
                        selected = false,
                        onClick = {
                            triggerHaptic()
                            onOpenSettings()
                        },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Ajustes", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    )
                }

                // Reverse / Flip Button side chip
                item {
                    FilterChip(
                        selected = isButtonsReversed,
                        onClick = {
                            triggerHaptic()
                            isButtonsReversed = !isButtonsReversed
                            onUpdateButtonsReversed(isButtonsReversed)
                            Toast.makeText(context, if (isButtonsReversed) "Botones movidos al lado izquierdo" else "Botones en posición normal", Toast.LENGTH_SHORT).show()
                        },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isButtonsReversed) "↔️ Lado Izq." else "↔️ Cambiar Lado",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    )
                }

                // Speed Mode Quick Toggle Chip
                item {
                    val speedLabel = when (currentBurstDelayMs) {
                        in 0..30 -> "⚡ 25ms Turbo"
                        in 31..60 -> "⚡ 50ms Ultra"
                        in 61..120 -> "🚀 100ms"
                        in 121..200 -> "⏱️ 180ms"
                        else -> "🛡️ ${currentBurstDelayMs}ms"
                    }
                    FilterChip(
                        selected = true,
                        onClick = {
                            triggerHaptic()
                            val nextDelay = when (currentBurstDelayMs) {
                                25L -> 50L
                                50L -> 100L
                                100L -> 180L
                                180L -> 300L
                                else -> 25L
                            }
                            currentBurstDelayMs = nextDelay
                            onUpdateBurstDelay(nextDelay)
                            Toast.makeText(context, "Velocidad de ráfaga: ${nextDelay}ms", Toast.LENGTH_SHORT).show()
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFE2C55).copy(alpha = 0.25f),
                            selectedLabelColor = Color(0xFFFE2C55)
                        ),
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FlashOn,
                                    contentDescription = null,
                                    tint = Color(0xFFFE2C55),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = speedLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    )
                }
            }

            // 3. Snippets Grid Dashboard Area
            val calculatedGridHeight = remember(keyHeightScale, isLandscape) {
                if (isLandscape) 140.dp else (210.dp * keyHeightScale)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(calculatedGridHeight)
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f))
                    .padding(6.dp)
            ) {
                if (showCustomBatchDialog) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                            .padding(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🚀 Envío Múltiple (Varios)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            TextButton(onClick = { showCustomBatchDialog = false }) {
                                Text("✕ Cerrar")
                            }
                        }

                        // Selector de Frases Guardadas en Varios
                        val variosCat = categories.find { it.name.contains("Varios", ignoreCase = true) }
                        val savedPhrases = remember(snippets, variosCat) {
                            val fromVarios = if (variosCat != null) snippets.filter { it.categoryId == variosCat.id } else emptyList()
                            if (fromVarios.isNotEmpty()) fromVarios else snippets
                        }

                        if (savedPhrases.isNotEmpty()) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(savedPhrases) { saved ->
                                    val isSelected = customBatchText == saved.content
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) Color(0xFF8B5CF6)
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            .clickable {
                                                triggerHaptic()
                                                customBatchText = saved.content
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = saved.title.ifBlank { saved.content },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = customBatchText,
                            onValueChange = { customBatchText = it },
                            label = { Text("Texto a enviar") },
                            placeholder = { Text("Selecciona o escribe la frase...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            maxLines = 2,
                            textStyle = MaterialTheme.typography.bodyMedium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = customBatchCountText,
                                onValueChange = { customBatchCountText = it.filter { c -> c.isDigit() } },
                                label = { Text("Cantidad") },
                                placeholder = { Text("10") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(95.dp),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium
                            )

                            Button(
                                onClick = {
                                    val count = customBatchCountText.toIntOrNull() ?: 10
                                    if (customBatchText.isNotBlank() && count > 0) {
                                        showCustomBatchDialog = false
                                        startCustomBatchSend(customBatchText, count)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text("🚀 Enviar $customBatchCountText Msg", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else if (filteredSnippets.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isSearchActive) "No se encontraron snippets para '$searchQuery'"
                                else if (showFavoritesOnly) "No tienes snippets marcados como favoritos"
                                else "Esta categoría no tiene snippets aún",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else if (isGridMode) {
                    // Grid Dashboard View mimicking custom keyboard layout
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(gridColumnCount),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredSnippets, key = { it.id }) { snippet ->
                            SnippetGridKeyCap(
                                snippet = snippet,
                                multiplier = repeatMultiplier,
                                isBurstActive = (activeBurstSnippetId == snippet.id),
                                onClick = { onSnippetKeyClick(snippet) },
                                onAutoSendClick = { toggleBurstForSnippet(snippet) },
                                onMultiSendClick = {
                                    customBatchText = snippet.content
                                    showCustomBatchDialog = true
                                }
                            )
                        }
                    }
                } else {
                    // Flow layout view
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            filteredSnippets.forEach { snippet ->
                                SnippetKeyButton(
                                    snippet = snippet,
                                    multiplier = repeatMultiplier,
                                    isBurstActive = (activeBurstSnippetId == snippet.id),
                                    onClick = { onSnippetKeyClick(snippet) },
                                    onAutoSendClick = { toggleBurstForSnippet(snippet) },
                                    onMultiSendClick = {
                                        customBatchText = snippet.content
                                        showCustomBatchDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 4. Standard Bottom Keys Row with Auto-Send Macro Buttons
            val bottomKeyOrder = remember(isButtonsReversed) {
                if (isButtonsReversed) {
                    listOf("enter", "backspace", "space", "hora", "fecha", "saludo")
                } else {
                    listOf("saludo", "fecha", "hora", "space", "backspace", "enter")
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                bottomKeyOrder.forEach { keyType ->
                    when (keyType) {
                        "saludo" -> KeyButton(
                            text = "⚡ Saludo",
                            isAccent = false,
                            modifier = Modifier.weight(0.9f),
                            onClick = {
                                triggerHaptic()
                                val processed = MacroProcessor.processMacros("{saludo}", context)
                                val text = processed + if (autoSpace) " " else ""
                                if (ultraFastMode && onFastPasteAndSend != null && currentPasteSpeedDelayMs == 0L) {
                                    onFastPasteAndSend(text, true)
                                } else {
                                    onInsertText(text)
                                    onInsertNewline()
                                }
                            }
                        )
                        "fecha" -> KeyButton(
                            text = "⚡ Fecha",
                            modifier = Modifier.weight(0.9f),
                            onClick = {
                                triggerHaptic()
                                val processed = MacroProcessor.processMacros("{fecha}", context)
                                val text = processed + if (autoSpace) " " else ""
                                if (ultraFastMode && onFastPasteAndSend != null && currentPasteSpeedDelayMs == 0L) {
                                    onFastPasteAndSend(text, true)
                                } else {
                                    onInsertText(text)
                                    onInsertNewline()
                                }
                            }
                        )
                        "hora" -> KeyButton(
                            text = "⚡ Hora",
                            modifier = Modifier.weight(0.8f),
                            onClick = {
                                triggerHaptic()
                                val processed = MacroProcessor.processMacros("{hora}", context)
                                val text = processed + if (autoSpace) " " else ""
                                if (ultraFastMode && onFastPasteAndSend != null && currentPasteSpeedDelayMs == 0L) {
                                    onFastPasteAndSend(text, true)
                                } else {
                                    onInsertText(text)
                                    onInsertNewline()
                                }
                            }
                        )
                        "space" -> KeyButton(
                            icon = Icons.Default.SpaceBar,
                            text = "Espacio",
                            isPrimary = true,
                            modifier = Modifier.weight(1.8f),
                            onClick = {
                                triggerHaptic()
                                onInsertText(" ")
                            }
                        )
                        "backspace" -> KeyButton(
                            icon = Icons.Default.Backspace,
                            modifier = Modifier.weight(0.9f),
                            onClick = {
                                triggerHaptic()
                                onDeleteLastChar()
                            }
                        )
                        "enter" -> KeyButton(
                            icon = Icons.Default.KeyboardReturn,
                            isAccent = true,
                            modifier = Modifier.weight(0.9f),
                            onClick = {
                                triggerHaptic()
                                onInsertNewline()
                            }
                        )
                    }
                }
            }
        }


    }
}

@Composable
private fun SnippetGridKeyCap(
    snippet: SnippetEntity,
    multiplier: Int,
    isBurstActive: Boolean = false,
    onClick: () -> Unit,
    onAutoSendClick: () -> Unit,
    onMultiSendClick: () -> Unit = {}
) {
    val bgColor by animateColorAsState(
        when {
            isBurstActive -> Color(0xFFEF4444)
            else -> MaterialTheme.colorScheme.surfaceVariant
        }
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .border(
                width = if (isBurstActive) 2.dp else 1.dp,
                color = when {
                    isBurstActive -> Color.White
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                },
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        tonalElevation = 4.dp,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (snippet.isFavorite && !isBurstActive) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Text(
                        text = if (isBurstActive) "🔥 RÁFAGA: ${snippet.title}" else snippet.title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isBurstActive) Color.White else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = if (isBurstActive) "🔥 OPRIMIDO (Toca para detener)" else snippet.content.replace("\n", " "),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = if (isBurstActive) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {

                // Custom Multi-Send 🚀 Button
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF8B5CF6).copy(alpha = 0.25f))
                        .clickable { onMultiSendClick() }
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🚀", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp))
                }

                // Dedicated Auto-Send ⚡ / Ráfaga Toggle Button
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (isBurstActive) Color.White
                            else Color(0xFF10B981).copy(alpha = 0.2f)
                        )
                        .clickable { onAutoSendClick() }
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = "Ráfaga Auto-Envío",
                        tint = if (isBurstActive) Color(0xFFEF4444) else Color(0xFF10B981),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SnippetKeyButton(
    snippet: SnippetEntity,
    multiplier: Int,
    isBurstActive: Boolean = false,
    onClick: () -> Unit,
    onAutoSendClick: () -> Unit,
    onMultiSendClick: () -> Unit = {}
) {
    val bgColor by animateColorAsState(
        when {
            isBurstActive -> Color(0xFFEF4444)
            else -> MaterialTheme.colorScheme.surfaceVariant
        }
    )

    Surface(
        modifier = Modifier
            .clickable { onClick() }
            .border(
                width = if (isBurstActive) 2.dp else 1.dp,
                color = when {
                    isBurstActive -> Color.White
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                },
                shape = RoundedCornerShape(10.dp)
            ),
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        tonalElevation = 2.dp,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.weight(1f, fill = false),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (snippet.isFavorite && !isBurstActive) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(12.dp)
                    )
                }

                Column {
                    Text(
                        text = if (isBurstActive) "🔥 RÁFAGA: ${snippet.title}" else snippet.title,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isBurstActive) Color.White else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isBurstActive) "🔥 OPRIMIDO (Toca para detener)" else snippet.content.replace("\n", " "),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = if (isBurstActive) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (multiplier > 1 && !isBurstActive) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text("x$multiplier", style = MaterialTheme.typography.labelSmall)
                }
            }

            // Custom Multi-Send 🚀 Button
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF8B5CF6).copy(alpha = 0.25f))
                    .clickable { onMultiSendClick() }
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("🚀", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp))
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (isBurstActive) Color.White
                        else Color(0xFF10B981).copy(alpha = 0.2f)
                    )
                    .clickable { onAutoSendClick() }
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = "Macro Auto-Envío",
                    tint = if (isBurstActive) Color(0xFFEF4444) else Color(0xFF10B981),
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

@Composable
private fun KeyButton(
    modifier: Modifier = Modifier,
    text: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    isPrimary: Boolean = false,
    isAccent: Boolean = false,
    onClick: () -> Unit
) {
    val bgColor = when {
        isAccent -> MaterialTheme.colorScheme.primary
        isPrimary -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }

    val contentColor = when {
        isAccent -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        shadowElevation = 2.dp,
        tonalElevation = 4.dp
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = text,
                        tint = contentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                if (icon != null && text != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                }
                if (text != null) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

