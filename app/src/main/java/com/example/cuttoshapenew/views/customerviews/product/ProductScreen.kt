package com.example.cuttoshapenew.views.customerviews.product

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.cuttoshapenew.model.Product

@Composable
fun ProductScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: ProductListView = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ProductListView::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return ProductListView(context) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    )

    when {
        viewModel.isLoading.value -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        viewModel.errorMessage.value != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = viewModel.errorMessage.value!!, color = Color.Red)
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.products.value) { product ->
                    ProductItem(product = product, navController = navController)
                }
            }
        }
    }
}