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
import androidx.compose.material.icons.filled.Favorite

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
    // 1. TAMBAHKAN STATE MANAGEMENT (Modul 5) untuk melacak status favorit tiap item
    var isFavorite by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {

            // Layout Utama horizontal
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gambar Produk
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.treatment)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Kolom informasi teks dan tombol
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.nama,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item.kategori,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "Rp ${item.harga}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { onClick() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE45F14)
                        ),
                        contentPadding = PaddingValues(vertical = 0.dp)
                    ) {
                        Text(
                            text = "Pesan Sekarang",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // 2. PERBAIKAN: Mengubah Icon biasa menjadi IconButton agar responsif saat diklik
            IconButton(
                onClick = {
                    // Mengubah status true/false saat ditekan tanpa membuka halaman detail
                    isFavorite = !isFavorite
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp) // Sedikit disesuaikan agar area klik pas di pojok
            ) {
                Icon(
                    // Logika Modul 5: Jika true pakai ikon Filled (Penuh), jika false pakai Outlined (Garis biasa)
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorit",
                    // Logika Warna: Jika true warnanya Merah, jika false warnanya Abu-abu
                    tint = if (isFavorite) Color.Red else Color.Gray,
                    modifier = Modifier.size(22.dp)
                )
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
                Text(text = item.kategori, style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Rp ${item.harga}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "Deskripsi Lengkap", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = item.deskripsi, style = MaterialTheme.typography.bodyLarge)

                // Mendorong konten tombol agar menetap rapi di bagian bawah screen
                Spacer(modifier = Modifier.weight(1f))

                // PERBAIKAN: Struktur Dua Tombol Berdampingan Sesuai Modul 8 & Gambar Acuan
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Tombol Kembali
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text("Kembali", fontWeight = FontWeight.Bold)
                    }

                    // Tombol Pesan (dengan integrasi Coroutine & Snackbar Modul 9)
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isProcessing = true
                                delay(1000) // Simulasi Asynchronous proses
                                snackbarHostState.showSnackbar("Berhasil dipesan!")
                                isProcessing = false
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        enabled = !isProcessing
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Pesan", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}