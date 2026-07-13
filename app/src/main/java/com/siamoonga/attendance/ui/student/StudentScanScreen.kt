package com.siamoonga.attendance.ui.student

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.siamoonga.attendance.ui.theme.Present
import com.siamoonga.attendance.ui.theme.PresentBg

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentScanScreen(
    onBack: () -> Unit,
    viewModel: StudentScanViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Scaffold(
        containerColor = Color(0xFF0F1220),
        topBar = {
            TopAppBar(
                title = { Text("Scan to check in", color = Color.White, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F1220))
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val status = state.status) {
                is ScanStatus.Scanning -> {
                    if (hasCameraPermission) {
                        CameraPreview(onQrDetected = viewModel::onQrDetected)
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Point at the lecturer's QR",
                                color = Color(0xFFC7CBDD),
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        PermissionExplainer(
                            onRequest = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                        )
                    }
                }

                is ScanStatus.Processing -> CenteredStatus {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(Modifier.height(16.dp))
                    Text("Checking you in…", color = Color.White, fontSize = 15.sp)
                }

                is ScanStatus.Success -> CenteredStatus {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Present,
                        modifier = Modifier.size(84.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "You're checked in",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        status.courseCode,
                        color = Color(0xFFC7CBDD),
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(28.dp))
                    Button(
                        onClick = onBack,
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Done") }
                }

                is ScanStatus.Failed -> CenteredStatus {
                    Icon(
                        Icons.Filled.ErrorOutline,
                        contentDescription = null,
                        tint = Color(0xFFE24B4A),
                        modifier = Modifier.size(84.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Couldn't check you in",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        status.message,
                        color = Color(0xFFC7CBDD),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                    Spacer(Modifier.height(28.dp))
                    Button(
                        onClick = viewModel::scanAgain,
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Scan again") }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onBack,
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Cancel", color = Color.White) }
                }
            }
        }
    }
}

@Composable
private fun CenteredStatus(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) { content() }
}

@Composable
private fun PermissionExplainer(onRequest: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Camera access needed",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Attendly uses the camera only to scan the session QR code in class.",
            color = Color(0xFFC7CBDD),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onRequest, shape = RoundedCornerShape(12.dp)) {
            Text("Allow camera")
        }
    }
}

@Composable
private fun CameraPreview(onQrDetected: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val options = BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                    .build()
                val scanner = BarcodeScanning.getClient(options)

                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                analysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                    @androidx.camera.core.ExperimentalGetImage
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )
                        scanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                barcodes.firstOrNull()?.rawValue?.let { onQrDetected(it) }
                            }
                            .addOnCompleteListener { imageProxy.close() }
                    } else {
                        imageProxy.close()
                    }
                }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis
                )
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}