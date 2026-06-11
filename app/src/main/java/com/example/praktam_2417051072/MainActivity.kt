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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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

// Data model untuk mensimulasikan penyimpanan akun pendaftaran
data class UserAccount(val email: String, val nama: String, val password: String)

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
    
    // State Global (Hoisted)
    val registeredUsers = remember { mutableStateListOf<UserAccount>() }
    val orderHistory = remember { mutableStateListOf<BeautyItem>() }
    val favoriteItems = remember { mutableStateListOf<BeautyItem>() }
    val scope = rememberCoroutineScope()

    fun refreshData() {
        scope.launch {
            try {
                isLoading = true
                errorMessage = null
                delay(2000)
                val result = repository.getBeautyItems()
                if (result.isEmpty()) errorMessage = "Data Kosong" else beautyItems = result
            } catch (e: Exception) {
                errorMessage = "Gagal Memuat Data."
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { refreshData() }

    if (isLoading && beautyItems.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFFFF51B5))
        }
    } else if (errorMessage != null && beautyItems.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Gagal Memuat Data", style = MaterialTheme.typography.titleLarge, color = Color.Red)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { refreshData() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF51B5))) { Text("Coba Lagi") }
            }
        }
    } else {
        NavHost(navController = navController, startDestination = "login") {
            composable("login") { LoginScreen(navController, registeredUsers) }
            composable("register") { RegisterScreen(navController, registeredUsers) }
            composable("forgot_password") { ForgotPasswordScreen(navController, registeredUsers) }
            composable("dashboard/{userEmail}") { backStackEntry ->
                DashboardScreen(navController, backStackEntry.arguments?.getString("userEmail") ?: "", beautyItems, isLoading, errorMessage, { refreshData() })
            }
            composable("profile/{userEmail}") { backStackEntry -> 
                ProfileScreen(navController, backStackEntry.arguments?.getString("userEmail") ?: "", registeredUsers) 
            }
            composable("detail/{itemName}") { backStackEntry ->
                val item = beautyItems.find { it.nama == backStackEntry.arguments?.getString("itemName") }
                if (item != null) BeautyDetailScreen(navController, item, orderHistory, favoriteItems)
            }
            composable("riwayat") { RiwayatScreen(navController, orderHistory) }
            composable("favorit") { FavoriteScreen(navController, favoriteItems) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, registeredUsers: List<UserAccount>) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Beauty Store", style = MaterialTheme.typography.headlineLarge, color = Color(0xFFFF51B5), fontWeight = FontWeight.Bold)
            Text("Silakan login untuk melanjutkan", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = null)
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                Text(
                    "Lupa Password?",
                    color = Color(0xFFFF51B5),
                    modifier = Modifier
                        .clickable { navController.navigate("forgot_password") }
                        .padding(vertical = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (email.isEmpty()) {
                        Toast.makeText(context, "Email wajib diisi!", Toast.LENGTH_SHORT).show()
                    } else if (!email.contains("@")) {
                        Toast.makeText(context, "Format email tidak valid (harus ada @)!", Toast.LENGTH_SHORT).show()
                    } else {
                        val user = registeredUsers.find { it.email == email && it.password == password }
                        if (user != null) {
                            navController.navigate("dashboard/${user.email}") { popUpTo("login") { inclusive = true } }
                        } else {
                            Toast.makeText(context, "Email belum terdaftar atau password salah!", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF51B5))
            ) {
                Text("Login", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Text("Belum punya akun? ")
                Text(
                    "Daftar Sekarang",
                    color = Color(0xFFFF51B5),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { navController.navigate("register") }
                )
            }
        }
    }
}

@Composable
fun RegisterScreen(navController: NavController, registeredUsers: MutableList<UserAccount>) {
    var nama by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp).verticalScroll(scrollState), 
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Daftar Akun Baru", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = nama, 
                onValueChange = { nama = it }, 
                label = { Text("Nama Lengkap") }, 
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email, 
                onValueChange = { email = it }, 
                label = { Text("Email") }, 
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password, 
                onValueChange = { password = it }, 
                label = { Text("Password") }, 
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(), 
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (nama.isEmpty() || email.isEmpty() || password.isEmpty()) {
                        Toast.makeText(context, "Semua kolom wajib diisi!", Toast.LENGTH_SHORT).show()
                    } else if (!email.contains("@")) {
                        Toast.makeText(context, "Format email tidak valid!", Toast.LENGTH_SHORT).show()
                    } else {
                        registeredUsers.add(UserAccount(email, nama, password))
                        Toast.makeText(context, "Pendaftaran Berhasil!", Toast.LENGTH_LONG).show()
                        navController.popBackStack()
                    }
                }, 
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF51B5))
            ) {
                Text("Daftar", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Sudah punya akun? Login", 
                color = Color(0xFFFF51B5),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun ForgotPasswordScreen(navController: NavController, registeredUsers: List<UserAccount>) {
    var email by remember { mutableStateOf("") }
    val context = LocalContext.current
    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp), 
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Reset Password", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email, 
                onValueChange = { email = it }, 
                label = { Text("Email") }, 
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (email.isEmpty()) {
                        Toast.makeText(context, "Masukkan email Anda!", Toast.LENGTH_SHORT).show()
                    } else if (!email.contains("@")) {
                        Toast.makeText(context, "Format email tidak valid!", Toast.LENGTH_SHORT).show()
                    } else if (registeredUsers.any { it.email == email }) { 
                        Toast.makeText(context, "Link reset dikirim ke email Anda!", Toast.LENGTH_LONG).show()
                        navController.popBackStack() 
                    } else {
                        Toast.makeText(context, "Email tidak terdaftar!", Toast.LENGTH_SHORT).show()
                    }
                }, 
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF51B5))
            ) {
                Text("Kirim Link Reset", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Kembali ke Login", 
                color = Color(0xFFFF51B5),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController, userEmail: String, beautyItems: List<BeautyItem>, isLoading: Boolean, errorMessage: String?, onRetry: () -> Unit) {
    var selectedCategory by remember { mutableStateOf("Semua") }
    val categories = listOf("Semua", "Makeup", "Skincare", "Haircare", "Treatment")
    val filteredItems = if (selectedCategory == "Semua") beautyItems else beautyItems.filter { it.kategori.equals(selectedCategory, ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Beauty Store", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { navController.navigate("favorit") }) { Icon(Icons.Default.Favorite, null, tint = Color.Red) }
                    IconButton(onClick = { navController.navigate("riwayat") }) { Icon(Icons.Default.History, null) }
                    IconButton(onClick = { navController.navigate("profile/$userEmail") }) { Icon(Icons.Default.Person, null) }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            item {
                Text("Rekomendasi Populer", style = MaterialTheme.typography.titleLarge, color = Color(0xFFFF51B5), fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                    items(beautyItems.take(3)) { item ->
                        Card(modifier = Modifier.width(160.dp).clickable { navController.navigate("detail/${item.nama}") }) {
                            Column {
                                AsyncImage(model = item.imageUrl, contentDescription = null, modifier = Modifier.height(100.dp).fillMaxWidth(), contentScale = ContentScale.Crop)
                                Column(Modifier.padding(8.dp)) {
                                    Text(item.nama, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                                    Text(item.kategori, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
                Text("Kategori Produk", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                    items(categories) { category ->
                        FilterChip(selected = selectedCategory == category, onClick = { selectedCategory = category }, label = { Text(category) })
                    }
                }
            }
            items(filteredItems) { item ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { navController.navigate("detail/${item.nama}") }) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(model = item.imageUrl, contentDescription = null, modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(item.nama, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(item.kategori, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Rp ${item.harga}", color = Color(0xFFFF51B5), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeautyDetailScreen(navController: NavController, item: BeautyItem, orderHistory: MutableList<BeautyItem>, favoriteItems: MutableList<BeautyItem>) {
    val isFavorite = favoriteItems.any { it.nama == item.nama }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text("Detail Produk") }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } })
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Box {
                AsyncImage(model = item.imageUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(16.dp)), contentScale = ContentScale.Crop)
                IconButton(onClick = { if (isFavorite) favoriteItems.removeAll { it.nama == item.nama } else favoriteItems.add(item) }, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                    Icon(imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, contentDescription = null, tint = if (isFavorite) Color.Red else Color.White)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(item.nama, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(item.kategori, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Rp ${item.harga}", style = MaterialTheme.typography.titleLarge, color = Color(0xFFFF51B5), fontWeight = FontWeight.Bold)
            Text(item.deskripsi, modifier = Modifier.padding(vertical = 16.dp))
            Button(
                onClick = { scope.launch { orderHistory.add(item); snackbarHostState.showSnackbar("Berhasil dipesan!") } }, 
                modifier = Modifier.fillMaxWidth().height(50.dp), 
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF51B5))
            ) {
                Text("Pesan Sekarang", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiwayatScreen(navController: NavController, orderHistory: List<BeautyItem>) {
    Scaffold(topBar = { TopAppBar(title = { Text("Riwayat") }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } }) }) { padding ->
        if (orderHistory.isEmpty()) Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Belum ada pesanan.") }
        else LazyColumn(Modifier.padding(padding).padding(16.dp)) {
            items(orderHistory) { item ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(Modifier.padding(12.dp)) {
                        AsyncImage(item.imageUrl, null, Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                        Column(Modifier.padding(start = 16.dp)) { 
                            Text(item.nama, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(item.kategori, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Text("Rp ${item.harga}", color = Color(0xFFFF51B5))
                            Text("Pesanan Sukses ✨", color = Color(0xFF2ECC71), style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(navController: NavController, favoriteItems: List<BeautyItem>) {
    Scaffold(topBar = { TopAppBar(title = { Text("Favorit") }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } }) }) { padding ->
        if (favoriteItems.isEmpty()) Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Belum ada favorit.") }
        else LazyColumn(Modifier.padding(padding).padding(16.dp)) {
            items(favoriteItems) { item ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { navController.navigate("detail/${item.nama}") }) {
                    Row(Modifier.padding(12.dp)) {
                        AsyncImage(item.imageUrl, null, Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                        Column(Modifier.padding(start = 16.dp)) { 
                            Text(item.nama, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(item.kategori, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Text("Rp ${item.harga}", color = Color(0xFFFF51B5))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, userEmail: String, registeredUsers: List<UserAccount>) {
    val user = registeredUsers.find { it.email == userEmail }
    Scaffold(topBar = { TopAppBar(title = { Text("Profil") }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Card(shape = CircleShape, modifier = Modifier.size(100.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFAD7A0))) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, Modifier.size(60.dp), tint = Color(0xFFFF51B5)) }
            }
            Text(user?.nama ?: "User", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 16.dp), fontWeight = FontWeight.Bold)
            Text(userEmail, color = Color.Gray)
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = { navController.navigate("riwayat") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF51B5)) // Tambahkan ini
            ) {
                Text("Riwayat Pemesanan", color = Color.White)
            }
            OutlinedButton(
                onClick = { navController.navigate("login") { popUpTo(0) { inclusive = true } } },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF51B5)) // Mengubah warna teks & border menjadi Pink
            ) {
                Text("Logout", fontWeight = FontWeight.Bold)
            }
        }
    }
}
