package com.example.service

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AudioOutputDeviceType {
    BLUETOOTH,
    WIRED_HEADSET,
    PHONE_SPEAKER,
    UNKNOWN
}

data class BluetoothDeviceInfo(
    val isBluetoothConnected: Boolean = false,
    val deviceName: String = "Phone Speaker",
    val deviceType: AudioOutputDeviceType = AudioOutputDeviceType.PHONE_SPEAKER,
    val isAutoResumeEnabled: Boolean = true,
    val isPauseOnDisconnectEnabled: Boolean = true
)

class BluetoothDeviceManager(
    private val context: Context,
    private val onPauseRequested: () -> Unit,
    private val onResumeRequested: () -> Unit
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val handler = Handler(Looper.getMainLooper())

    private val _deviceInfo = MutableStateFlow(BluetoothDeviceInfo())
    val deviceInfo: StateFlow<BluetoothDeviceInfo> = _deviceInfo.asStateFlow()

    private var isAutoResumeEnabled = true
    private var isPauseOnDisconnectEnabled = true

    private val audioDeviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
            val hadBluetooth = _deviceInfo.value.isBluetoothConnected
            refreshAudioDeviceState()
            val nowHasBluetooth = _deviceInfo.value.isBluetoothConnected

            // Auto-resume playback if a Bluetooth audio device is connected
            if (!hadBluetooth && nowHasBluetooth && isAutoResumeEnabled) {
                handler.postDelayed({
                    onResumeRequested()
                }, 400L)
            }
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
            val hadBluetooth = _deviceInfo.value.isBluetoothConnected
            refreshAudioDeviceState()
            val nowHasBluetooth = _deviceInfo.value.isBluetoothConnected

            // If Bluetooth disconnected, ensure playback paused to avoid blasting phone speaker
            if (hadBluetooth && !nowHasBluetooth && isPauseOnDisconnectEnabled) {
                onPauseRequested()
            }
        }
    }

    private val audioBecomingNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                if (isPauseOnDisconnectEnabled) {
                    onPauseRequested()
                }
                refreshAudioDeviceState()
            }
        }
    }

    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            when (intent?.action) {
                "android.bluetooth.device.action.ACL_CONNECTED",
                "android.bluetooth.device.action.ACL_DISCONNECTED",
                "android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED" -> {
                    handler.postDelayed({
                        refreshAudioDeviceState()
                    }, 300L)
                }
            }
        }
    }

    fun startListening() {
        refreshAudioDeviceState()
        try {
            audioManager?.registerAudioDeviceCallback(audioDeviceCallback, handler)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val noisyFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(audioBecomingNoisyReceiver, noisyFilter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                context.registerReceiver(audioBecomingNoisyReceiver, noisyFilter)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val btFilter = IntentFilter().apply {
                addAction("android.bluetooth.device.action.ACL_CONNECTED")
                addAction("android.bluetooth.device.action.ACL_DISCONNECTED")
                addAction("android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED")
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(bluetoothStateReceiver, btFilter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                context.registerReceiver(bluetoothStateReceiver, btFilter)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopListening() {
        try {
            audioManager?.unregisterAudioDeviceCallback(audioDeviceCallback)
        } catch (_: Exception) {}

        try {
            context.unregisterReceiver(audioBecomingNoisyReceiver)
        } catch (_: Exception) {}

        try {
            context.unregisterReceiver(bluetoothStateReceiver)
        } catch (_: Exception) {}
    }

    fun refreshAudioDeviceState() {
        val am = audioManager ?: return
        val devices = am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)

        var bluetoothDeviceName: String? = null
        var isBluetooth = false
        var isWired = false
        var wiredName: String? = null

        val hasBtPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        for (device in devices) {
            when (device.type) {
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                AudioDeviceInfo.TYPE_BLE_HEADSET,
                AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> {
                    isBluetooth = true
                    val rawName = if (hasBtPermission) {
                        try {
                            device.productName?.toString()?.takeIf { it.isNotBlank() }
                        } catch (_: Exception) { null }
                    } else null
                    bluetoothDeviceName = rawName ?: "Bluetooth Audio Device"
                    break
                }
                AudioDeviceInfo.TYPE_WIRED_HEADSET,
                AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                AudioDeviceInfo.TYPE_USB_HEADSET -> {
                    isWired = true
                    wiredName = device.productName?.toString()?.takeIf { it.isNotBlank() } ?: "Wired Headphones"
                }
            }
        }

        val (type, name) = when {
            isBluetooth -> AudioOutputDeviceType.BLUETOOTH to (bluetoothDeviceName ?: "Bluetooth Device")
            isWired -> AudioOutputDeviceType.WIRED_HEADSET to (wiredName ?: "Wired Headphones")
            else -> AudioOutputDeviceType.PHONE_SPEAKER to "Phone Speaker"
        }

        _deviceInfo.value = BluetoothDeviceInfo(
            isBluetoothConnected = isBluetooth,
            deviceName = name,
            deviceType = type,
            isAutoResumeEnabled = isAutoResumeEnabled,
            isPauseOnDisconnectEnabled = isPauseOnDisconnectEnabled
        )
    }

    fun setAutoResumeEnabled(enabled: Boolean) {
        isAutoResumeEnabled = enabled
        _deviceInfo.value = _deviceInfo.value.copy(isAutoResumeEnabled = enabled)
    }

    fun setPauseOnDisconnectEnabled(enabled: Boolean) {
        isPauseOnDisconnectEnabled = enabled
        _deviceInfo.value = _deviceInfo.value.copy(isPauseOnDisconnectEnabled = enabled)
    }

    fun openBluetoothSettings() {
        try {
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val fallbackIntent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
