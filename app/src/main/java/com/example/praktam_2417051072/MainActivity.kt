package com.example.praktam_2417051072

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.praktam_2417051072.data.model.BeautyItem
import com.example.praktam_2417051072.data.repository.BeautyRepository
import com.example.praktam_2417051072.ui.theme.PrakTAM_2417051072Theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PrakTAM_2417051072Theme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val repository = remember { BeautyRepository() }
    
    var beautyItems by remember { mutableStateOf<List<BeautyItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var refreshTrigger by remember { mutableLongStateOf(0L) }

    LaunchedEffect(refreshTrigger) {
        try {
            isLoading = true
            errorMessage = null
            val result = repository.getBeautyItems()
            if (result.isEmpty()) {
                errorMessage = "Data tidak ditemukan."
            } else {
                beautyItems = result
            }
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
            // Menampilkan pesan yang lebih ramah pengguna
            errorMessage = if (e is IOException) {
                "Periksa koneksi internet Anda"
            } else {
                "Gagal memuat data. Silakan coba lagi."
            }
        }
    }

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            DaftarBeautyScreen(
                navController = navController,
                items = beautyItems,
                isLoading = isLoading,
                error = errorMessage,
                onRefresh = { refreshTrigger = System.currentTimeMillis() }
            )
        }
        composable("detail/{itemName}") { backStackEntry ->
            val itemName = backStackEntry.arguments?.getString("itemName")
            val item = beautyItems.find { it.nama == itemName }
            item?.let {
                BeautyDetailScreen(navController = navController, item = it)
            }
        }
    }
}

@Composable
fun DaftarBeautyScreen(
    navController: NavController,
    items: List<BeautyItem>,
    isLoading: Boolean,
    error: String?,
    onRefresh: () -> Unit
) {
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (error != null) {
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Gagal Memuat Data", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.Red)
                Text(text = error, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onRefresh) { Text("Coba Lagi") }
            }
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    text = "Rekomendasi Populer",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items.take(3)) { item ->
                        BeautyRekomendasiCard(item = item, onClick = { 
                            navController.navigate("detail/${item.nama}") 
                        })
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Daftar Menu Lengkap",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            items(items) { item ->
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    BeautyItemRow(item = item, onClick = { 
                        navController.navigate("detail/${item.nama}") 
                    })
                }
            }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun BeautyRekomendasiCard(item: BeautyItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(160.dp).clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = null,
                placeholder = painterResource(id = R.drawable.skincare),
                error = painterResource(id = R.drawable.haircare),
                modifier = Modifier.fillMaxWidth().height(100.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(text = item.nama, style = MaterialTheme.typography.titleSmall, maxLines = 1, fontWeight = FontWeight.Bold)
                Text(text = "Rp ${item.harga}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun BeautyItemRow(item: BeautyItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.treatment)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = item.nama, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = item.deskripsi, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1)
                Text(text = "Rp ${item.harga}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeautyDetailScreen(navController: NavController, item: BeautyItem) {
    var isFavorite by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Produk") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Box {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.makeup)
                )
                IconButton(
                    onClick = { isFavorite = !isFavorite },
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isFavorite) Color.Red else Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(24.dp)) {
                Text(text = item.nama, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(text = item.deskripsi, style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Rp ${item.harga}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "Deskripsi Lengkap", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "Ini adalah produk berkualitas tinggi untuk perawatan kecantikan Anda.", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isProcessing = true
                            delay(1000)
                            snackbarHostState.showSnackbar("Berhasil dipesan!")
                            isProcessing = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = !isProcessing
                ) {
                    if (isProcessing) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else Text("Pesan Sekarang")
                }
            }
        }
    }
}
