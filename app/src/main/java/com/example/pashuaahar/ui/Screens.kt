package com.example.pashuaahar.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.pashuaahar.R
import com.example.pashuaahar.data.CowProfile
import com.example.pashuaahar.data.FeedIngredient
import com.example.pashuaahar.data.RecipeHistory
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: PashuViewModel,
    onAddCow: () -> Unit,
    onViewRecipe: () -> Unit
) {
    val summary by viewModel.dashboardSummary.collectAsState()
    val history by viewModel.history.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Namaste, Farmer!",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.dashboard_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = stringResource(R.string.total_cows),
                    value = summary.totalCows.toString(),
                    icon = Icons.Default.Pets,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = stringResource(R.string.daily_milk),
                    value = String.format(Locale.getDefault(), "%.1fL", summary.totalMilkYield),
                    icon = Icons.Default.WaterDrop,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(Color(0xFF2E7D32), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Savings, null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(stringResource(R.string.estimated_savings), style = MaterialTheme.typography.labelMedium)
                        Text(
                            String.format(Locale.getDefault(), "₹%.2f", summary.totalSavings),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                    }
                }
            }
        }

        if (history.size >= 2) {
            item {
                Text(stringResource(R.string.savings_trend), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                SavingsBarChart(history)
            }
        }

        item {
            Text(stringResource(R.string.quick_actions), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ActionTile(stringResource(R.string.add_cow_action), Icons.Default.Add, onAddCow, Modifier.weight(1f))
                ActionTile(stringResource(R.string.get_feed_action), Icons.Default.Restaurant, onViewRecipe, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun AddCowScreen(viewModel: PashuViewModel, onSaved: () -> Unit) {
    var currentStep by remember { mutableIntStateOf(1) }
    var name by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var yield by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }

    if (isListening) {
        AlertDialog(
            onDismissRequest = { isListening = false },
            title = { Text(stringResource(R.string.voice_listening)) },
            text = { 
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Say something like: 'Sahiwal cow 5 years 10 liters yield'")
                }
            },
            confirmButton = { TextButton(onClick = { isListening = false }) { Text("Cancel") } }
        )
        LaunchedEffect(Unit) {
            delay(3000)
            name = "Sahiwal 01"
            breed = "Sahiwal"
            age = "5"
            yield = "12"
            isListening = false
        }
    }

    Column(modifier = Modifier.padding(24.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.add_new_cattle), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            IconButton(onClick = { isListening = true }) {
                Icon(Icons.Default.Mic, contentDescription = "Voice Input", tint = MaterialTheme.colorScheme.primary)
            }
        }
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            StepIndicator(step = 1, currentStep = currentStep, label = stringResource(R.string.step_identity))
            HorizontalDivider(modifier = Modifier.width(32.dp).padding(horizontal = 4.dp))
            StepIndicator(step = 2, currentStep = currentStep, label = stringResource(R.string.step_milk_data))
        }

        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                } else {
                    (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                }
            }, label = "StepTransition"
        ) { step ->
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (step == 1) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(R.string.cow_name)) }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = breed, onValueChange = { breed = it }, label = { Text(stringResource(R.string.breed)) }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text(stringResource(R.string.age)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                } else {
                    OutlinedTextField(value = yield, onValueChange = { yield = it }, label = { Text(stringResource(R.string.daily_milk_yield)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    Text(stringResource(R.string.milk_data_hint), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            if (currentStep > 1) {
                TextButton(onClick = { currentStep-- }) { Text(stringResource(R.string.back)) }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }
            
            Button(
                onClick = {
                    if (currentStep < 2) {
                        currentStep++
                    } else {
                        viewModel.saveProfile(CowProfile(
                            name = name, 
                            breed = breed, 
                            age = age.toIntOrNull() ?: 0, 
                            weight = 0.0, 
                            milkYield = yield.toDoubleOrNull() ?: 0.0
                        ))
                        onSaved()
                    }
                },
                enabled = if (currentStep == 1) name.isNotBlank() else yield.isNotBlank()
            ) {
                Text(if (currentStep == 2) stringResource(R.string.save_profile) else stringResource(R.string.next))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedRecipeScreen(viewModel: PashuViewModel, onBack: () -> Unit) {
    val recipe by viewModel.recipe.collectAsState()
    val savings by viewModel.savings.collectAsState()
    val homeCost by viewModel.homeCost.collectAsState()
    val marketCost by viewModel.marketCost.collectAsState()
    val currentSeason by viewModel.selectedSeason.collectAsState()
    val seasonMessage by viewModel.seasonMessage.collectAsState()
    val profiles by viewModel.allProfiles.collectAsState()
    val selectedCow by viewModel.selectedCow.collectAsState()

    LaunchedEffect(profiles) {
        if (selectedCow == null && profiles.isNotEmpty()) {
            viewModel.selectCow(profiles.first())
        }
    }

    LazyColumn(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.feed_recommendation), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                IconButton(onClick = onBack) { Icon(Icons.Default.Close, null) }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    stringResource(R.string.current_season),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Season.entries.forEachIndexed { index, season ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = Season.entries.size),
                            onClick = { viewModel.setSeason(season) },
                            selected = currentSeason == season,
                            label = { Text(season.name) }
                        )
                    }
                }

                if (seasonMessage.isNotBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = seasonMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }

        if (profiles.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.select_cattle),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    ScrollableTabRow(
                        selectedTabIndex = profiles.indexOf(selectedCow).coerceAtLeast(0),
                        edgePadding = 0.dp,
                        divider = {},
                        containerColor = Color.Transparent,
                        indicator = { tabPositions ->
                            if (profiles.indexOf(selectedCow) != -1) {
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[profiles.indexOf(selectedCow)]),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    ) {
                        profiles.forEach { cow ->
                            Tab(
                                selected = selectedCow?.id == cow.id,
                                onClick = { viewModel.selectCow(cow) },
                                text = { 
                                    Text(
                                        cow.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (selectedCow?.id == cow.id) FontWeight.Bold else FontWeight.Normal
                                    ) 
                                }
                            )
                        }
                    }
                }
            }
        }

        if (recipe.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    val colors = listOf(
                        MaterialTheme.colorScheme.primary.toArgb(),
                        MaterialTheme.colorScheme.secondary.toArgb(),
                        MaterialTheme.colorScheme.tertiary.toArgb()
                    )
                    val onSurface = MaterialTheme.colorScheme.onSurface.toArgb()

                    AndroidView(
                        factory = { ctx -> 
                            PieChart(ctx).apply { 
                                description.isEnabled = false
                                setUsePercentValues(true)
                                holeRadius = 50f
                                setHoleColor(Color.Transparent.toArgb())
                                setDrawCenterText(true)
                                centerText = "Mix Ratio"
                                setCenterTextSize(16f)
                                setCenterTextColor(onSurface)
                                setEntryLabelColor(onSurface)
                                setEntryLabelTextSize(12f)
                                animateXY(800, 800)
                                
                                legend.apply {
                                    verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                                    horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                                    orientation = Legend.LegendOrientation.HORIZONTAL
                                    setDrawInside(false)
                                    textColor = onSurface
                                    textSize = 12f
                                    yOffset = 10f
                                    xOffset = 0f
                                }
                                setExtraOffsets(10f, 0f, 10f, 15f)
                            } 
                        },
                        update = { chart ->
                            val entries = recipe.map { PieEntry(it.ratio.toFloat(), it.name) }
                            val dataSet = PieDataSet(entries, "").apply {
                                this.colors = colors
                                valueTextSize = 13f
                                valueTextColor = Color.White.toArgb()
                                valueFormatter = PercentFormatter(chart)
                                sliceSpace = 4f
                            }
                            chart.data = PieData(dataSet)
                            chart.invalidate()
                        },
                        modifier = Modifier.padding(16.dp).fillMaxSize()
                    )
                }
            }

            item {
                CostComparisonCard(homeCost, marketCost, savings) {
                    viewModel.saveCurrentRecipeToHistory()
                }
            }

            items(recipe) { item ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Grain, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(item.name, fontWeight = FontWeight.SemiBold)
                        }
                        Text(String.format(Locale.getDefault(), "%.1f kg", item.ratio), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        } else {
            item {
                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.add_cow_hint))
                }
            }
        }
    }
}

@Composable
fun CostComparisonCard(homeCost: Double, marketCost: Double, savings: Double, onSave: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Cost Comparison (per 10kg)", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Homemade", style = MaterialTheme.typography.bodySmall)
                    Text("₹${String.format(Locale.getDefault(), "%.2f", homeCost)}", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Market Feed", style = MaterialTheme.typography.bodySmall)
                    Text("₹${String.format(Locale.getDefault(), "%.2f", marketCost)}", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.TrendingDown, null, tint = Color(0xFF2E7D32))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Savings: ₹${String.format(Locale.getDefault(), "%.2f", savings)}", color = Color(0xFF2E7D32), fontWeight = FontWeight.ExtraBold)
                }
                Button(onClick = onSave, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)) {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun RecipeHistoryScreen(viewModel: PashuViewModel) {
    val history by viewModel.history.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(text = stringResource(R.string.history_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }

        if (history.isEmpty()) {
            item { Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) { Text("No records yet.") } }
        }

        items(history) { record ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(record.cowName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(record.date)), style = MaterialTheme.typography.bodySmall)
                        }
                        Text("Saved ₹${String.format("%.1f", record.savings)}", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(record.recipeSummary, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun StepIndicator(step: Int, currentStep: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (currentStep >= step) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "$step", color = if (currentStep >= step) Color.White else Color.Black, fontWeight = FontWeight.Bold)
        }
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = color), shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, null, modifier = Modifier.size(24.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun ActionTile(label: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier) {
    OutlinedCard(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun SavingsBarChart(history: List<RecipeHistory>) {
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant.toArgb()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Savings Trend (Last 7 Days)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            AndroidView(
                factory = { ctx ->
                    BarChart(ctx).apply {
                        description.isEnabled = false
                        legend.isEnabled = false
                        setDrawGridBackground(false)
                        setDrawBarShadow(false)
                        setTouchEnabled(false)
                        setScaleEnabled(false)
                        setPinchZoom(false)
                        
                        xAxis.apply {
                            position = XAxis.XAxisPosition.BOTTOM
                            setDrawGridLines(false)
                            setDrawAxisLine(true)
                            axisLineColor = surfaceVariantColor
                            textColor = onSurfaceColor
                            granularity = 1f
                            yOffset = 5f
                        }
                        
                        axisLeft.apply {
                            setDrawGridLines(true)
                            gridColor = surfaceVariantColor
                            setDrawAxisLine(false)
                            textColor = onSurfaceColor
                            axisMinimum = 0f
                            xOffset = 5f
                        }
                        
                        axisRight.isEnabled = false
                        extraBottomOffset = 15f
                    }
                },
                update = { chart ->
                    val groupedHistory = history
                        .groupBy { SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(it.date)) }
                        .map { (date, records) -> date to records.sumOf { it.savings } }
                        .takeLast(7)

                    if (groupedHistory.isNotEmpty()) {
                        val entries = groupedHistory.mapIndexed { i, (_, savings) -> 
                            BarEntry(i.toFloat(), savings.toFloat()) 
                        }
                        val labels = groupedHistory.map { it.first }

                        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                        chart.xAxis.labelCount = labels.size

                        val dataSet = BarDataSet(entries, "").apply {
                            color = primaryColor
                            setDrawValues(true)
                            valueTextColor = onSurfaceColor
                            valueTextSize = 10f
                            valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                                override fun getFormattedValue(value: Float): String = "₹${value.toInt()}"
                            }
                        }
                        
                        chart.data = BarData(dataSet).apply {
                            barWidth = 0.5f
                        }
                        chart.setFitBars(true)
                        chart.animateY(1000)
                    }
                    chart.invalidate()
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun CowListScreen(viewModel: PashuViewModel, onAddCow: () -> Unit) {
    val cows by viewModel.allProfiles.collectAsState()
    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.your_herd),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onAddCow) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Add Cow",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
            
            items(cows) { cow ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = cow.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = cow.breed,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = String.format(Locale.getDefault(), "%.1fL", cow.milkYield),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { viewModel.deleteProfile(cow) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Cow",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
            
            if (cows.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize()
                            .padding(bottom = 64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No cattle added yet.\nTap + to get started.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VeterinaryTipsScreen(viewModel: PashuViewModel) {
    val tips by viewModel.tips.collectAsState()
    LazyColumn(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text(stringResource(R.string.health_tips), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
        items(tips) { tip ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(tip.category.uppercase(), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                    Text(tip.title, fontWeight = FontWeight.Bold)
                    Text(tip.content)
                }
            }
        }
    }
}
