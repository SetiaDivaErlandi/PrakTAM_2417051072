package com.example.praktam_2417051072

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.praktam_2417051072.model.BeautySource
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.praktam_2417051072.ui.theme.PrakTAM_2417051072Theme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PrakTAM_2417051072Theme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {

    val listBeauty = BeautySource.dummyBeauty

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {
            Text(
                text = "Halo, Saya Setia Diva Erlandi dengan NPM 2417051072 siap belajar Compose!"
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(text = "Beauty Spending Analyzer")

            val total = listBeauty.sumOf { it.harga }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Total Pengeluaran: Rp $total")
        }

        items(listBeauty) { item ->

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {

                    Image(
                        painter = painterResource(id = item.imageRes),
                        contentDescription = item.nama,
                        modifier = Modifier.size(70.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "Nama: ${item.nama}")
                    Text(text = "Kategori: ${item.kategori}")
                    Text(text = "Harga: Rp ${item.harga}")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMain() {
    PrakTAM_2417051072Theme {
        MainScreen()
    }
}