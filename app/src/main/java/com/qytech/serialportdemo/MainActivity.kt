package com.qytech.serialportdemo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.qytech.serialportdemo.ui.theme.SerialPortDemoTheme
import java.io.File


class MainActivity : ComponentActivity() {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    private val permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val denied = permissions.any {
            ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_DENIED
        }
        if (denied) {
            ActivityCompat.requestPermissions(this, permissions, 0x01)
        }
        copyFirmware()


        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (!isGranted) {
                    ActivityCompat.requestPermissions(this, permissions, 0x01)
                }
            }
        setContent {
            SerialPortDemoTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    SerialPortWrite()
                }
            }
        }
    }

    private fun copyFirmware() {
        val firmware = File(filesDir.path, "ledMatrixApp.bin")
        if (!firmware.exists()) {
            firmware.createNewFile()
        }
        assets.open("ledMatrixApp.bin").use {
            firmware.writeBytes(it.readBytes())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        requestPermissionLauncher.unregister()
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SerialPortDemoTheme {
        Greeting("Android")
    }
}