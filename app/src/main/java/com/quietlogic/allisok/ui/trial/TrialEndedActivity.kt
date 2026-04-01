package com.quietlogic.allisok.ui.trial

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.ProductDetails
import com.quietlogic.allisok.BillingManager
import com.quietlogic.allisok.PermissionSetupActivity
import com.quietlogic.allisok.R
import com.quietlogic.allisok.databinding.ActivityTrialEndedBinding
import com.quietlogic.allisok.security.TrialManager
import com.quietlogic.allisok.ui.home.Button3D

class TrialEndedActivity : AppCompatActivity(), BillingManager.Listener {

    private lateinit var binding: ActivityTrialEndedBinding
    private lateinit var billingManager: BillingManager

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("app_settings", MODE_PRIVATE)
        val languageCode = prefs.getString("app_language", "en") ?: "en"
        val locale = if (languageCode.contains("-")) {
            val parts = languageCode.split("-")
            java.util.Locale(parts[0], parts[1])
        } else {
            java.util.Locale(languageCode)
        }
        java.util.Locale.setDefault(locale)
        val configuration = android.content.res.Configuration(newBase.resources.configuration)
        configuration.setLocale(locale)
        val context = newBase.createConfigurationContext(configuration)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTrialEndedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Block back button
            }
        })

        Button3D.apply(binding.btnBuy, cornerDp = 16f, depthDp = 6f)
        Button3D.apply(binding.btnRestore, cornerDp = 16f, depthDp = 6f)

        billingManager = BillingManager(this, this)
        billingManager.startConnection()

        binding.btnBuy.setOnClickListener {
            billingManager.launchPurchase(this)
        }

        binding.btnRestore.setOnClickListener {
            billingManager.restorePurchases()
        }
    }

    override fun onDestroy() {
        billingManager.endConnection()
        super.onDestroy()
    }

    override fun onBillingReady() {
        billingManager.queryProductDetails()
    }

    override fun onBillingDisconnected() {
        Toast.makeText(this, getString(R.string.trial_restore_soon), Toast.LENGTH_SHORT).show()
    }

    override fun onProductLoaded(productDetails: ProductDetails) {
        // Product loaded — ready to purchase
    }

    override fun onPurchaseSuccess() {
        TrialManager.setPurchased(this)
        val intent = Intent(this, PermissionSetupActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onPurchaseCancelled() {
        Toast.makeText(this, getString(R.string.trial_purchase_soon), Toast.LENGTH_SHORT).show()
    }

    override fun onPurchasePending() {
        Toast.makeText(this, getString(R.string.trial_purchase_soon), Toast.LENGTH_SHORT).show()
    }

    override fun onPurchaseRestored(hasPurchase: Boolean) {
        if (hasPurchase) {
            TrialManager.setPurchased(this)
            val intent = Intent(this, PermissionSetupActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, getString(R.string.trial_restore_soon), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBillingError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}