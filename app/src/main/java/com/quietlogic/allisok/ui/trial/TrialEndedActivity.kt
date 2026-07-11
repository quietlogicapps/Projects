package com.quietlogic.allisok.ui.trial

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.quietlogic.allisok.BuildConfig
import com.quietlogic.allisok.PermissionSetupActivity
import com.quietlogic.allisok.R
import com.quietlogic.allisok.billing.store.BillingCoordinator
import com.quietlogic.allisok.billing.store.StoreBillingListener
import com.quietlogic.allisok.billing.store.StoreModule
import com.quietlogic.allisok.databinding.ActivityTrialEndedBinding
import com.quietlogic.allisok.security.TrialManager

class TrialEndedActivity : AppCompatActivity(), StoreBillingListener {

    private lateinit var binding: ActivityTrialEndedBinding
    private lateinit var billingCoordinator: BillingCoordinator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTrialEndedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Block back button
            }
        })

        billingCoordinator = StoreModule.createBillingCoordinator(this, this)
        billingCoordinator.connect()

        binding.btnBuy.setOnClickListener {
            billingCoordinator.purchase(this)
        }

        binding.btnRestore.setOnClickListener {
            billingCoordinator.restore()
        }
    }

    override fun onDestroy() {
        billingCoordinator.disconnect()
        super.onDestroy()
    }

    override fun onStoreReady() {
        billingCoordinator.queryProduct()
    }

    override fun onStoreDisconnected() {
        Toast.makeText(this, getString(R.string.trial_restore_soon), Toast.LENGTH_SHORT).show()
    }

    override fun onProductReady() {
        // Product loaded — ready to purchase
    }

    override fun onPurchaseSuccess() {
        if (BuildConfig.DEBUG) {
            return
        }
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

    override fun onRestoreResult(hasPurchase: Boolean) {
        if (hasPurchase) {
            if (BuildConfig.DEBUG) {
                return
            }
            TrialManager.setPurchased(this)
            val intent = Intent(this, PermissionSetupActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, getString(R.string.trial_restore_soon), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStoreError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
