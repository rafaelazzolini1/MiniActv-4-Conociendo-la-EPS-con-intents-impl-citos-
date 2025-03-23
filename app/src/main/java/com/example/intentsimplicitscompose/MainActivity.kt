package com.example.intentsimplicitscompose

import android.Manifest
import android.app.SearchManager
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.example.intentsimplicitscompose.ui.theme.IntentsImplicitsComposeTheme
import java.io.File
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

class MainActivity : ComponentActivity() {
    private var callPhonePerm = mutableStateOf(false)
    private var cameraPerm = mutableStateOf(false)
    private val permissionCallPhone = Manifest.permission.CALL_PHONE
    private val permissionCamera = Manifest.permission.CAMERA

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IntentsImplicitsComposeTheme {
                MyApp(modifier = Modifier)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        callPhonePerm.value = checkSelfPermission(permissionCallPhone) == PackageManager.PERMISSION_GRANTED
        cameraPerm.value = checkSelfPermission(permissionCamera) == PackageManager.PERMISSION_GRANTED
    }

    @Composable
    fun MyApp(modifier: Modifier = Modifier) {
        val context = LocalContext.current
        val selectedImageUri = remember { mutableStateOf<Uri?>(null) }
        val photoUriState = remember { mutableStateOf<Uri?>(null) }

        val launchCallPhonePerm = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            callPhonePerm.value = isGranted
            Toast.makeText(context, if (isGranted) "Permiso de llamada concedido" else "Permiso de llamada denegado", Toast.LENGTH_SHORT).show()
        }

        val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            selectedImageUri.value = uri
        }

        val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                Toast.makeText(context, "Foto tomada con éxito", Toast.LENGTH_SHORT).show()
                photoUriState.value?.let { uri ->
                    val values = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, "photo.jpg")
                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MyApp")
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }

                    val contentResolver = context.contentResolver
                    val newUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

                    newUri?.let {
                        contentResolver.openOutputStream(it)?.use { outputStream ->
                            contentResolver.openInputStream(uri)?.use { inputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                        values.clear()
                        values.put(MediaStore.Images.Media.IS_PENDING, 0)
                        contentResolver.update(it, values, null, null)
                        photoUriState.value = it
                    }
                }
            }
        }

        LaunchedEffect(Unit) {
            if (!callPhonePerm.value) {
                Log.i(TAG, "Solicitando permiso de llamada")
                launchCallPhonePerm.launch(permissionCallPhone)
            }
        }

        Surface(modifier = modifier) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(modifier = modifier.align(Alignment.CenterHorizontally).padding(vertical = 30.dp), text = stringResource(R.string.bienvenidos))
                Text(modifier = modifier.align(Alignment.CenterHorizontally).padding(vertical = 10.dp), text = stringResource(R.string.ubicando))

                ElevatedButton(
                    modifier = modifier.padding(vertical = 10.dp).align(Alignment.CenterHorizontally),
                    elevation = ButtonDefaults.elevatedButtonElevation(5.dp),
                    onClick = {
                        Toast.makeText(context, "Seleccionado Localizacion por coordenadas", Toast.LENGTH_LONG).show()
                        val geo = "geo:$lat,$lon?q=$lat,$lon"
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(geo)))
                    }
                ) { Text(text = stringResource(R.string.Boton1)) }

                ElevatedButton(
                    modifier = modifier.padding(vertical = 10.dp).align(Alignment.CenterHorizontally),
                    elevation = ButtonDefaults.elevatedButtonElevation(5.dp),
                    onClick = {
                        Toast.makeText(context, "Seleccionado Localizacion por dirección", Toast.LENGTH_LONG).show()
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$address")))
                    }
                ) { Text(text = stringResource(R.string.Boton2)) }

                Text(modifier = modifier.align(Alignment.CenterHorizontally).padding(vertical = 30.dp), text = stringResource(R.string.navegando))

                ElevatedButton(
                    modifier = modifier.align(Alignment.CenterHorizontally).padding(vertical = 3.dp),
                    elevation = ButtonDefaults.elevatedButtonElevation(5.dp),
                    onClick = {
                        Toast.makeText(context, "Accediendo a la web", Toast.LENGTH_LONG).show()
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(webEPS)))
                    }
                ) { Text(text = stringResource(R.string.Boton3)) }

                ElevatedButton(
                    modifier = modifier.align(Alignment.CenterHorizontally).padding(vertical = 10.dp),
                    elevation = ButtonDefaults.elevatedButtonElevation(5.dp),
                    onClick = {
                        Toast.makeText(context, "Buscando en Google", Toast.LENGTH_LONG).show()
                        val intent = Intent(Intent.ACTION_WEB_SEARCH).putExtra(SearchManager.QUERY, textoABuscar)
                        context.startActivity(intent)
                    }
                ) { Text(text = stringResource(R.string.Boton4)) }



                Text(modifier = modifier.align(Alignment.CenterHorizontally).padding(vertical = 30.dp), text = stringResource(R.string.contactando))
                ElevatedButton(
                    modifier = modifier.align(Alignment.CenterHorizontally).padding(vertical = 10.dp),
                    elevation = ButtonDefaults.elevatedButtonElevation(5.dp),
                    onClick = {
                        Toast.makeText(context, "Enviando SMS", Toast.LENGTH_LONG).show()
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$smsNumber"))
                        intent.putExtra("sms_body", smsText)
                        context.startActivity(intent)
                    }
                ) { Text(text = "Enviar SMS") }

                ElevatedButton(
                    modifier = modifier.align(Alignment.CenterHorizontally).padding(vertical = 10.dp),
                    elevation = ButtonDefaults.elevatedButtonElevation(5.dp),
                    onClick = {
                        Toast.makeText(context, "Abriendo Agenda", Toast.LENGTH_LONG).show()
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("content://contacts/people/"))
                        context.startActivity(intent)
                    }
                ) { Text(text = "Abrir Agenda") }

                Row(
                    modifier = modifier.align(Alignment.CenterHorizontally).padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ElevatedButton(
                        elevation = ButtonDefaults.elevatedButtonElevation(5.dp),
                        onClick = {
                            Toast.makeText(context, "Abriendo Galería", Toast.LENGTH_LONG).show()
                            galleryLauncher.launch("image/*")
                        }
                    ) { Text(text = "Abrir Galería") }

                    selectedImageUri.value?.let { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = "Imagen seleccionada",
                            modifier = Modifier.size(100.dp)
                        )
                    }
                }

                Column(
                    modifier = modifier.align(Alignment.CenterHorizontally).padding(vertical = 30.dp)
                ) {
                    Text(
                        text = "Nota: Debe conceder permiso de cámara en ajustes",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    ElevatedButton(
                        elevation = ButtonDefaults.elevatedButtonElevation(5.dp),
                        enabled = cameraPerm.value,
                        modifier = modifier.align(Alignment.CenterHorizontally),
                                onClick = {
                            Toast.makeText(context, "Abriendo Cámara", Toast.LENGTH_LONG).show()
                            val photoFile = File(externalCacheDir, "photo.jpg")
                            val photoUri = FileProvider.getUriForFile(context, "${packageName}.provider", photoFile)
                            photoUriState.value = photoUri
                            cameraLauncher.launch(photoUri)
                        }
                    ) { Text(text = "Abrir Cámara") }
                }

                ElevatedButton(
                    modifier = modifier.align(Alignment.CenterHorizontally).padding(vertical = 10.dp),
                    elevation = ButtonDefaults.elevatedButtonElevation(5.dp),
                    enabled = callPhonePerm.value,
                    onClick = {
                        Toast.makeText(context, "Marcando Tlfn. Consergeria", Toast.LENGTH_LONG).show()
                        context.startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$telef")))
                    }
                ) { Text(text = stringResource(R.string.Boton5)) }

                ElevatedButton(
                    modifier = modifier.align(Alignment.CenterHorizontally).padding(vertical = 10.dp),
                    elevation = ButtonDefaults.elevatedButtonElevation(5.dp),
                    onClick = {
                        Toast.makeText(context, "Compartiendo texto", Toast.LENGTH_LONG).show()
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Este es un texto de prueba para compartir")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Compartir vía"))
                    }
                ) { Text(text = "Compartir Texto") }
            }
        }
    }

    companion object {
        const val lat = "41.60788"
        const val lon = "0.623333"
        const val address = "Carrer de Jaume II, 69, Lleida"
        const val webEPS = "http://www.eps.udl.cat/"
        const val textoABuscar = "escola politecnica superior UdL"
        const val telef = "+34617195034"
        const val smsNumber = "+34617195034"
        const val smsText = "Hola, este es un mensaje de prueba"
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        IntentsImplicitsComposeTheme {
            MyApp()
        }
    }
}