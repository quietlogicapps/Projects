package com.quietlogic.allisok.ui.home

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.quietlogic.allisok.R
import com.quietlogic.allisok.security.AdminSession
import com.quietlogic.allisok.security.LockGate
import com.quietlogic.allisok.security.TrialManager
import com.quietlogic.allisok.security.UserSession
import com.quietlogic.allisok.ui.info.InfoActivity
import com.quietlogic.allisok.ui.pin.PinActivity
import com.quietlogic.allisok.ui.trial.TrialEndedActivity

class HomeActivity : AppCompatActivity() {

    private var skipUserUnlockOnce = false
    private var isSettingsVisible = false

    private var homeFragment: HomeFragment? = null
    private var settingsFragment: SettingsFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val toolbar = findViewById<Toolbar>(R.id.toolbarHome)
        val originalHeight = resources.getDimensionPixelSize(R.dimen.toolbar_height)

        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { view, insets ->
            val statusBarTop = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(view.paddingLeft, statusBarTop, view.paddingRight, view.paddingBottom)
            val params = view.layoutParams
            params.height = originalHeight + statusBarTop
            view.layoutParams = params
            view.requestLayout()
            insets
        }

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        findViewById<AppCompatImageButton>(R.id.buttonOpenSettings).apply {
            setImageDrawable(
                AppCompatResources.getDrawable(
                    this@HomeActivity,
                    androidx.appcompat.R.drawable.abc_ic_menu_overflow_material
                )
            )
            imageTintList = ColorStateList.valueOf(Color.WHITE)
            setOnClickListener { showSettingsFragment() }
        }

        if (savedInstanceState != null) {
            isSettingsVisible = savedInstanceState.getBoolean(STATE_SETTINGS_VISIBLE, false)
        }

        ensureFragments()
        applyFragmentVisibility()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isSettingsVisible) {
                    showHomeFragment()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        updateAdminIndicator()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(STATE_SETTINGS_VISIBLE, isSettingsVisible)
    }

    override fun onResume() {
        super.onResume()

        if (!TrialManager.isTrialActive(this)) {
            val intent = Intent(this, TrialEndedActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        updateAdminIndicator()

        if (skipUserUnlockOnce) {
            skipUserUnlockOnce = false
            return
        }

        val homePrefs = getSharedPreferences("home_prefs", MODE_PRIVATE)
        if (homePrefs.getBoolean("returning_from_store", false)) {
            homePrefs.edit().putBoolean("returning_from_store", false).apply()
            LockGate.markUserUnlocked()
            return
        }

        LockGate.requireUserUnlock(this)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LockGate.REQUEST_USER_UNLOCK) {
            when (resultCode) {
                RESULT_OK -> {}

                PinActivity.RESULT_OPEN_EMERGENCY_INFO -> {
                    skipUserUnlockOnce = true
                    startActivity(Intent(this, InfoActivity::class.java))
                }

                else -> {
                    finish()
                }
            }
        }
    }

    // toolbar_settings_menu.xml is kept for future cleanup; not inflated or used.

    override fun onStop() {
        super.onStop()
        val homePrefs = getSharedPreferences("home_prefs", MODE_PRIVATE)
        if (!homePrefs.getBoolean("returning_from_store", false)) {
            UserSession.stop(this)
        }
    }

    fun onAdminStateChanged() {
        updateAdminIndicator()
    }

    private fun ensureFragments() {
        val fragmentManager = supportFragmentManager
        homeFragment = fragmentManager.findFragmentByTag(TAG_HOME) as? HomeFragment
        settingsFragment = fragmentManager.findFragmentByTag(TAG_SETTINGS) as? SettingsFragment

        if (homeFragment != null && settingsFragment != null) {
            return
        }

        val transaction = fragmentManager.beginTransaction().setReorderingAllowed(true)

        if (homeFragment == null) {
            homeFragment = HomeFragment()
            transaction.add(R.id.fragmentContainer, homeFragment!!, TAG_HOME)
        }

        if (settingsFragment == null) {
            settingsFragment = SettingsFragment()
            transaction.add(R.id.fragmentContainer, settingsFragment!!, TAG_SETTINGS)
            transaction.hide(settingsFragment!!)
        }

        transaction.commitNow()
    }

    private fun applyFragmentVisibility() {
        ensureFragments()

        val home = homeFragment ?: return
        val settings = settingsFragment ?: return

        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .apply {
                if (isSettingsVisible) {
                    hide(home)
                    show(settings)
                } else {
                    hide(settings)
                    show(home)
                }
            }
            .commit()
    }

    private fun showSettingsFragment() {
        isSettingsVisible = true
        applyFragmentVisibility()
    }

    private fun showHomeFragment() {
        isSettingsVisible = false
        applyFragmentVisibility()
    }

    private fun updateAdminIndicator() {
        val indicator = findViewById<View>(R.id.viewAdminIndicator)
        indicator.visibility =
            if (AdminSession.isActive()) View.VISIBLE else View.GONE
    }

    companion object {
        private const val TAG_HOME = "home_fragment"
        private const val TAG_SETTINGS = "settings_fragment"
        private const val STATE_SETTINGS_VISIBLE = "state_settings_visible"
    }
}
