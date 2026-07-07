package org.example.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import org.example.app.telephony.CallEndCoordinator
import org.example.app.telephony.SettingsRepository

class MainActivity : Activity() {

    private lateinit var enabledSwitch: Switch
    private lateinit var ringtoneSpinner: Spinner
    private lateinit var requestPermissionsButton: Button
    private lateinit var requestWriteSettingsButton: Button
    private lateinit var statusText: TextView

    private lateinit var settingsRepo: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        settingsRepo = SettingsRepository(this)

        setContentView(R.layout.activity_main)

        enabledSwitch = findViewById(R.id.enabledSwitch)
        ringtoneSpinner = findViewById(R.id.ringtoneSpinner)
        requestPermissionsButton = findViewById(R.id.requestPermissionsButton)
        requestWriteSettingsButton = findViewById(R.id.requestWriteSettingsButton)
        statusText = findViewById(R.id.statusText)

        setupRingtoneSpinner()
        setupSwitch()
        setupButtons()

        // Start/stop observer based on stored settings.
        CallEndCoordinator.ensureStartedOrStopped(this, settingsRepo)

        refreshUiState()
    }

    override fun onResume() {
        super.onResume()
        refreshUiState()
    }

    private fun setupSwitch() {
        enabledSwitch.isChecked = settingsRepo.isEnabled()
        enabledSwitch.setOnCheckedChangeListener { _, isChecked ->
            settingsRepo.setEnabled(isChecked)
            CallEndCoordinator.ensureStartedOrStopped(this, settingsRepo)
            refreshUiState()
        }
    }

    private fun setupRingtoneSpinner() {
        val labels = listOf(
            getString(R.string.ringtone_mode_random),
            getString(R.string.ringtone_mode_system_default_only)
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, labels)
        ringtoneSpinner.adapter = adapter

        ringtoneSpinner.setSelection(
            when (settingsRepo.ringtoneMode()) {
                SettingsRepository.RingtoneMode.RANDOM -> 0
                SettingsRepository.RingtoneMode.SYSTEM_DEFAULT_ONLY -> 1
            }
        )

        ringtoneSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val mode =
                    if (position == 0) SettingsRepository.RingtoneMode.RANDOM
                    else SettingsRepository.RingtoneMode.SYSTEM_DEFAULT_ONLY

                settingsRepo.setRingtoneMode(mode)
                refreshUiState()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // no-op
            }
        }
    }

    private fun setupButtons() {
        requestPermissionsButton.setOnClickListener {
            // Runtime permissions
            val perms = mutableListOf<String>()
            perms.add(Manifest.permission.READ_PHONE_STATE)
            perms.add(Manifest.permission.READ_CALL_LOG)

            requestPermissions(perms.toTypedArray(), REQUEST_PERMISSIONS_CODE)
        }

        requestWriteSettingsButton.setOnClickListener {
            // WRITE_SETTINGS is a special permission gated by a system UI screen.
            val intent = Intent(
                Settings.ACTION_MANAGE_WRITE_SETTINGS,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
    }

    private fun refreshUiState() {
        val enabled = settingsRepo.isEnabled()
        enabledSwitch.isChecked = enabled

        val phoneStateGranted = isPermissionGranted(Manifest.permission.READ_PHONE_STATE)
        val callLogGranted = isPermissionGranted(Manifest.permission.READ_CALL_LOG)
        val writeSettingsGranted = Settings.System.canWrite(this)

        requestPermissionsButton.visibility =
            if (phoneStateGranted && callLogGranted) View.GONE else View.VISIBLE

        requestWriteSettingsButton.visibility =
            if (writeSettingsGranted) View.GONE else View.VISIBLE

        val statusLines = mutableListOf<String>()
        statusLines.add(
            if (enabled) getString(R.string.status_enabled) else getString(R.string.status_disabled)
        )

        statusLines.add(
            getString(
                R.string.status_permissions,
                boolToState(phoneStateGranted),
                boolToState(callLogGranted),
                boolToState(writeSettingsGranted)
            )
        )

        statusLines.add(
            getString(
                R.string.status_mode,
                when (settingsRepo.ringtoneMode()) {
                    SettingsRepository.RingtoneMode.RANDOM -> getString(R.string.ringtone_mode_random)
                    SettingsRepository.RingtoneMode.SYSTEM_DEFAULT_ONLY -> getString(R.string.ringtone_mode_system_default_only)
                }
            )
        )

        // If enabled but missing requirements, call out that nothing will happen.
        if (enabled && (!phoneStateGranted || !callLogGranted || !writeSettingsGranted)) {
            statusLines.add(getString(R.string.status_missing_requirements))
        }

        statusText.text = statusLines.joinToString(separator = "\n")
    }

    private fun isPermissionGranted(permission: String): Boolean {
        return checkSelfPermission(permission) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    private fun boolToState(v: Boolean): String = if (v) "OK" else "MISSING"

    companion object {
        private const val REQUEST_PERMISSIONS_CODE = 1001
    }
}
