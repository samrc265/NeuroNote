package com.example.neuronote

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.edit // KTX extension for SharedPreferences
import androidx.fragment.app.FragmentActivity // CRITICAL IMPORT for the fix
import java.util.concurrent.Executor // Added explicit import for Executor

// NOTE: For this AppLockManager to work, your MainActivity MUST extend FragmentActivity.

object AppLockManager {
    private const val PREFS_NAME = "AppLockPrefs"
    private const val KEY_LOCK_ENABLED = "lock_enabled"
    // NEW PERSISTENCE KEY: Tracks if the user has authenticated in the current session
    private const val KEY_HAS_AUTHENTICATED = "has_authenticated"

    // State to track if the app is currently in the locked (auth required) state
    private val _isLocked = mutableStateOf(false)
    val isLocked: State<Boolean> get() = _isLocked

    // State to track if the App Lock feature is enabled in settings
    private val _isLockEnabled = mutableStateOf(false)
    val isLockEnabled: State<Boolean> get() = _isLockEnabled

    private lateinit var applicationContext: Context
    private lateinit var prefs: SharedPreferences
    private lateinit var authenticationExecutor: Executor

    // CRITICAL FIX: Store the Activity reference as FragmentActivity, which is required by BiometricPrompt
    private lateinit var mainActivity: FragmentActivity

    // BiometricPrompt is now nullable and initialized inside 'authenticate'
    private var biometricPrompt: BiometricPrompt? = null


    fun init(activity: ComponentActivity) {
        if (!this::applicationContext.isInitialized) {
            this.applicationContext = activity.applicationContext
            this.prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

            // CRITICAL FIX: Ensure the activity is castable and store it
            if (activity is FragmentActivity) {
                this.mainActivity = activity
            } else {
                // Should never happen if the main activity extends FragmentActivity
                throw IllegalStateException("MainActivity must extend FragmentActivity for AppLockManager to function.")
            }

            this.authenticationExecutor = ContextCompat.getMainExecutor(activity)
            _isLockEnabled.value = isAppLockEnabled(activity)

            // NEW: On initial launch, if lock is enabled, assume locked until proven otherwise.
            if (_isLockEnabled.value) {
                // If the app was launched (or re-launched from a kill), it must be locked.
                if (!prefs.getBoolean(KEY_HAS_AUTHENTICATED, false)) {
                    _isLocked.value = true
                }
            }
        }
    }

    /** Checks if the app lock feature is enabled in settings. */
    fun isAppLockEnabled(context: Context): Boolean {
        if (!this::prefs.isInitialized) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
        return prefs.getBoolean(KEY_LOCK_ENABLED, false)
    }

    /** Updates the lock setting and state. */
    private fun setLockEnabled(context: Context, enabled: Boolean) {
        if (!this::prefs.isInitialized) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
        // Use KTX extension function for edit()
        prefs.edit {
            putBoolean(KEY_LOCK_ENABLED, enabled)
            // When disabling the lock, clear the authenticated state just in case
            if (!enabled) {
                remove(KEY_HAS_AUTHENTICATED)
            }
        }
        _isLockEnabled.value = enabled
        Toast.makeText(context, if (enabled) "App Lock Enabled" else "App Lock Disabled", Toast.LENGTH_SHORT).show()
    }

    // --- State Control ---

    /** Sets the app to the locked state. Called when the app is backgrounded. */
    fun lockApp() {
        // Use applicationContext, but the lock must only happen if the feature is enabled.
        if (isAppLockEnabled(applicationContext)) {
            // NEW: Set has_authenticated to false when locking (app is backgrounded)
            prefs.edit { putBoolean(KEY_HAS_AUTHENTICATED, false) }
            _isLocked.value = true
        }
    }

    /** Sets the app to the unlocked state. Called after successful authentication. */
    fun unlockApp() {
        // NEW: Set has_authenticated to true when unlocking (for the current session)
        prefs.edit { putBoolean(KEY_HAS_AUTHENTICATED, true) }
        _isLocked.value = false
    }

    /** * Checks if the app lock is enabled and forces the locked state if authentication
     * has not yet occurred in this session.
     */
    fun checkIfAuthenticationIsNeeded() {
        if (isAppLockEnabled(applicationContext)) {
            if (!prefs.getBoolean(KEY_HAS_AUTHENTICATED, false)) {
                _isLocked.value = true
            }
        }
    }

    // --- Biometric/PIN/Pattern Logic ---

    /** Checks what authentication types are available. */
    private fun checkBiometrics(context: Context): Int {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(
            // FIX: Only check for DEVICE_CREDENTIAL (Pattern/PIN/Password)
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
    }

    /** Shows the Biometric/PIN prompt for unlocking the app. */
    fun authenticate(onSuccess: () -> Unit) {
        if (!this::mainActivity.isInitialized) {
            Toast.makeText(applicationContext, "Error: AppLockManager not fully initialized.", Toast.LENGTH_LONG).show()
            return
        }

        val authResult = checkBiometrics(applicationContext)

        // Use a detailed switch to check the result, not just BIOMETRIC_SUCCESS.
        if (authResult != BiometricManager.BIOMETRIC_SUCCESS) {
            val message = when (authResult) {
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "Security check failed: Biometric hardware not available."
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Security check failed: Biometric hardware is unavailable."
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "Security check failed: PIN/Pattern is not set up on device."
                else -> "Security check failed."
            }
            Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
            return
        }

        // We use the globally stored mainActivity which is guaranteed to be a FragmentActivity
        val activity = mainActivity

        biometricPrompt = BiometricPrompt(
            activity, // FragmentActivity reference
            authenticationExecutor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess() // Directly call the dynamically supplied success lambda
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Only show a Toast if it wasn't canceled by the user
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_CANCELED) {
                        Toast.makeText(applicationContext, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock NeuroNote")
            // FIX: Subtitle updated to reflect Pattern/PIN-only
            .setSubtitle("Use your device Pattern, PIN, or Password to continue.")
            // FIX: Only allow DEVICE_CREDENTIAL (Pattern/PIN/Password)
            .setAllowedAuthenticators(BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        // FINAL FIX: This call is now guaranteed to work using the initialized prompt object
        biometricPrompt?.authenticate(promptInfo)
    }

    /** Shows the prompt used to initially enable the app lock feature in settings. */
    fun showSetupBiometricPrompt(context: Context) {
        authenticate(onSuccess = {
            setLockEnabled(context, true)
            // Do NOT call lockApp() here; it will cause the screen to immediately reappear.
        })
    }

    /** Disables the app lock feature. */
    fun disableAppLock(context: Context) {
        setLockEnabled(context, false)
        unlockApp()
    }
}

/** Composable for the lock screen (must be shown when AppLockManager.isLocked is true) */
@Composable
fun AppLockScreen(darkColor: Color, lightColor: Color, textColor: Color, onUnlockSuccess: () -> Unit) {
    // We only need context for Toast in the Composable
    val isDark by AppThemeManager.isDarkTheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF121212) else lightColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.LockOpen,
                contentDescription = "App Locked",
                tint = darkColor,
                modifier = Modifier.height(72.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "NeuroNote is Locked",
                color = darkColor,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Your data is protected. Authenticate to continue.",
                color = textColor.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    AppLockManager.authenticate(onSuccess = onUnlockSuccess)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = darkColor,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text("Unlock App", fontSize = 16.sp)
            }
        }
    }

    // Automatically trigger the prompt when the screen appears
    LaunchedEffect(Unit) {
        AppLockManager.authenticate(onSuccess = onUnlockSuccess)
    }
}