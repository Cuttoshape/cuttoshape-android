package com.example.cuttoshapenew.views.customerviews.checkout

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.rememberPaymentSheet
import com.example.cuttoshapenew.apiclients.PaymentIntentRequest
import com.stripe.android.paymentsheet.PaymentSheetResult

/**
 * @param showingCheckout a MutableState<Boolean> (like your SwiftUI Binding)
 * @param fetchCartItems a suspend lambda to refresh cart contents
 */
@Composable
fun StripeCheckoutSheet(
    paymentRequest: PaymentIntentRequest,
    showingCheckout: MutableState<Boolean>
) {
    val context = LocalContext.current
    val activity = context as Activity
    val viewModel = remember(paymentRequest) {
        CheckoutViewModel(paymentRequest)
    }
    val ui by viewModel.ui.collectAsState()

    // ✅ Compose-friendly PaymentSheet instance
    val paymentSheet = rememberPaymentSheet { result ->
        viewModel.onPaymentCompletion(context, result) {
            showingCheckout.value = false
        }
    }

    LaunchedEffect(Unit) {
        viewModel.preparePaymentSheet(context)
    }

    Column(Modifier.padding(16.dp)) {
        when {
            ui.isLoading -> { /* ... */ }
            ui.errorMessage != null -> { /* ... */ }
            ui.clientSecret != null && ui.customerConfig != null -> {
                Button(
                    onClick = {
                        val config = PaymentSheet.Configuration(
                            merchantDisplayName = ui.merchantDisplayName,
                            customer = ui.customerConfig,
                            allowsDelayedPaymentMethods = true
                        )
                        paymentSheet.presentWithPaymentIntent(
                            ui.clientSecret!!,
                            config
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Pay Now") }
            }
            else -> { /* ... */ }
        }

        ui.paymentResultMessage?.let { Text(it, Modifier.padding(top = 12.dp)) }
    }
}
