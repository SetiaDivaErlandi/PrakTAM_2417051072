package com.example.praktam_2417051072

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import java.text.SimpleDateFormat
import java.util.*

// Model Riwayat Pemesanan
data class Order(
    val item: BeautyItem,
    val orderTime: String
)

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
    
    // State Global
    var beautyItems by remember { mutableStateOf<List<BeautyItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // State Riwayat Pemesanan (MutableStateList agar reaktif)
    val orderHistory = remember { mutableStateListOf<Order>() }

    // Memuat data awal dari API Gist
    LaunchedEffect(Unit) {
        try {
            isLoading = true
            val result = repository.getBeautyItems()
            beautyItems = result
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
            errorMessage = "Gagal memuat data produk."
        }
    }

    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("forgot_password") { ForgotPasswordScreen(navController) }
        composable("home") {
            DaftarBeautyScreen(
                navController = navController,
                items = beautyItems,
                isLoading = isLoading,
                error = errorMessage
            )
        }
        composable("detail/{itemName}") { backStackEntry ->
            val itemName = backStackEntry.arguments?.getString("itemName")
            val item = beautyItems.find { it.nama == itemName }
            item?.let {
                BeautyDetailScreen(
                    navController = navController, 
                    item = it,
                    onOrderSuccess = { orderedItem ->
                        val currentTime = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
                        orderHistory.add(Order(orderedItem, currentTime))
                    }
                )
            }
        }
        composable("history") {
            HistoryScreen(navController, orderHistory)
        }
    }
}

// --- 1. SISTEM AUTENTIKASI ---

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Beauty Store", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("Silakan login untuk melanjutkan", color = Color.Gray)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = "Toggle Visibility"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        
        TextButton(
            onClick = { navController.navigate("forgot_password") },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Lupa Password?")
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { navController.navigate("home") },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Login")
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Belum punya akun?")
            TextButton(onClick = { navController.navigate("register") }) {
                Text("Daftar Sekarang")
            }
        }
    }
}

@Composable
fun RegisterScreen(navController: NavController) {
    var nama by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Daftar Akun Baru", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = nama,
            onValueChange = { nama = it },
            label = { Text("Nama Lengkap") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Daftar")
        }
        TextButton(onClick = { navController.popBackStack() }) {
            Text("Sudah punya akun? Login")
        }
    }
}

@Composable
fun ForgotPasswordScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Reset Password", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Masukkan email Anda untuk menerima link reset.", textAlign = TextAlign.Center, color = Color.Gray)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
        )

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { 
                Toast.makeText(context, "Link reset password telah dikirim ke email Anda!", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Kirim Link Reset")
        }
        TextButton(onClick = { navController.popBackStack() }) {
            Text("Kembali ke Login")
        }
    }
}

// --- 2. HALAMAN UTAMA & FILTER KATEGORI ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaftarBeautyScreen(
    navController: NavController,
    items: List<BeautyItem>,
    isLoading: Boolean,
    error: String?
) {
    var selectedCategory by remember { mutableStateOf("Semua") }
    val categories = listOf("Semua", "Makeup", "Skincare", "Haircare", "Treatment")

    // Filter berdasarkan kategori dari JSON
    val filteredItems = if (selectedCategory == "Semua") {
        items
    } else {
        items.filter { it.kategori.equals(selectedCategory, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Beauty Store", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { navController.navigate("history") }) {
                        Icon(Icons.Default.History, contentDescription = "Riwayat")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(error, color = Color.Red)
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                // Rekomendasi Populer
                item {
                    Text(
                        text = "Rekomendasi Populer",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items.take(4)) { item ->
                            BeautyRekomendasiCard(item = item, onClick = {
                                navController.navigate("detail/${item.nama}")
                            })
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Baris Filter Kategori
                item {
                    Text(
                        text = "Kategori Produk",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category },
                                label = { Text(category) }
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "Daftar Menu Lengkap",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // List Produk terfilter
                items(filteredItems) { item ->
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
    var isFavorite by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.nama, fontWeight = FontWeight.Bold)
                Text(text = item.kategori, fontSize = 12.sp, color = Color.Gray)
                Text(text = "Rp ${item.harga}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = { isFavorite = !isFavorite }) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    tint = if (isFavorite) Color.Red else Color.Gray
                )
            }
        }
    }
}

// --- 3. HALAMAN DETAIL ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeautyDetailScreen(
    navController: NavController, 
    item: BeautyItem,
    onOrderSuccess: (BeautyItem) -> Unit
) {
    var isProcessing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Detail Produk") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(250.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(24.dp)) {
                Text(text = item.nama, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(text = item.kategori, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Rp ${item.harga}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Deskripsi", fontWeight = FontWeight.Bold)
                Text(text = item.deskripsi)

                Spacer(modifier = Modifier.weight(1f))
                
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isProcessing = true
                            delay(1200) // Simulasi loading
                            onOrderSuccess(item)
                            isProcessing = false
                            snackbarHostState.showSnackbar("Berhasil dipesan!")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !isProcessing
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("Pesan Sekarang")
                    }
                }
            }
        }
    }
}

// --- 4. HALAMAN RIWAYAT PEMESANAN ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController, orderHistory: List<Order>) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Pemesanan") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        if (orderHistory.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Belum ada riwayat pemesanan", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                items(orderHistory.reversed()) { order ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = order.item.nama, fontWeight = FontWeight.Bold)
                                Text(text = "Rp ${order.item.harga}", color = MaterialTheme.colorScheme.primary)
                                Text(text = "Dipesan pada: ${order.orderTime}", fontSize = 12.sp, color = Color.Gray)
                            }
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
                        }
                    }
                }
            }
        }
    }
}
