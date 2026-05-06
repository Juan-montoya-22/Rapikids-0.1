package com.example.rapikids01.screen.home

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.rapikids01.data.location.LatLng
import com.example.rapikids01.data.model.Guarderia
import com.example.rapikids01.viewmodel.GuarderiaConDistancia
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng as MLatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.Marker
import java.util.Locale
import com.example.rapikids01.R

private val Purple    = Color(0xFF6A4DBA)
private val Yellow    = Color(0xFFFFD23F)
private val GreenSoft = Color(0xFF4CAF50)

private fun drawableToBitmap(context: android.content.Context, drawableId: Int): android.graphics.Bitmap {
    val drawable = androidx.core.content.ContextCompat.getDrawable(context, drawableId)!!
    val bitmap = android.graphics.Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        android.graphics.Bitmap.Config.ARGB_8888
    )
    val canvas = android.graphics.Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}
@Composable
fun MapaGuarderiasScreen(
    guarderias: List<GuarderiaConDistancia>,
    ubicacionPadre: LatLng?,
    onVerPerfil: (Guarderia) -> Unit,
    onVolverLista: () -> Unit
) {
    val context = LocalContext.current
    var guarderiaSeleccionada by remember { mutableStateOf<GuarderiaConDistancia?>(null) }

    LaunchedEffect(Unit) {
        MapLibre.getInstance(context)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapLibre.getInstance(ctx)
                MapView(ctx).apply {
                    getMapAsync { map ->
                        map.setStyle(
                            org.maplibre.android.maps.Style.Builder()
                                .fromUri("https://tiles.openfreemap.org/styles/liberty")
                        ) {
                            val centro = ubicacionPadre
                                ?.let { MLatLng(it.lat, it.lng) }
                                ?: MLatLng(4.7110, -74.0721)

                            map.cameraPosition = CameraPosition.Builder()
                                .target(centro)
                                .zoom(12.0)
                                .build()

                            ubicacionPadre?.let { latLng ->
                                val iconoPadre = org.maplibre.android.annotations.IconFactory.getInstance(ctx)
                                    .fromBitmap(drawableToBitmap(ctx, R.drawable.ic_marker_padre))
                                map.addMarker(
                                    MarkerOptions()
                                        .position(MLatLng(latLng.lat, latLng.lng))
                                        .title("Tu ubicación")
                                        .icon(iconoPadre)
                                )
                            }

                            guarderias.forEach { item ->
                                val lat = item.guarderia.latitud ?: return@forEach
                                val lng = item.guarderia.longitud ?: return@forEach
                                map.addMarker(
                                    MarkerOptions()
                                        .position(MLatLng(lat, lng))
                                        .title(item.guarderia.nombreGuarderia)
                                        .snippet(
                                            item.distanciaKm?.let { dist ->
                                                if (dist < 1.0) "${(dist * 1000).toInt()} m"
                                                else String.format(Locale.getDefault(), "%.1f km", dist)
                                            } ?: item.guarderia.direccion
                                        )
                                )
                            }
                            map.setOnMarkerClickListener { marker ->
                                val item = guarderias.find {
                                    it.guarderia.nombreGuarderia == marker.title
                                }
                                guarderiaSeleccionada = item
                                true
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        Button(
            onClick  = onVolverLista,
            shape    = RoundedCornerShape(20.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = Purple),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .statusBarsPadding()
        ) {
            Icon(Icons.Default.List, null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Ver lista", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp)
                .statusBarsPadding()
                .background(Color.White.copy(alpha = 0.93f), RoundedCornerShape(12.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("${guarderias.size} guardería(s)", fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold, color = Purple)
        }
        guarderiaSeleccionada?.let { item ->
            val g = item.guarderia
            Card(
                shape     = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                colors    = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier  = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(g.nombreGuarderia, fontWeight = FontWeight.Bold,
                                fontSize = 17.sp, color = Color(0xFF1A1A2E))
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, null, tint = Purple,
                                    modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(3.dp))
                                Text(g.direccion, fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                        item.distanciaKm?.let { dist ->
                            Surface(
                                color = Purple.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (dist < 1.0) "${(dist * 1000).toInt()} m"
                                    else String.format(Locale.getDefault(), "%.1f km", dist),
                                    color = Purple, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(5) { i ->
                            Icon(Icons.Default.Star, null,
                                tint = if (i.toDouble() < g.calificacionPromedio) Yellow
                                else Color.LightGray,
                                modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (g.calificacionPromedio > 0)
                                String.format("%.1f", g.calificacionPromedio)
                            else "Nuevo",
                            fontSize = 13.sp, color = Color.Gray
                        )
                        if (g.verificada) {
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                color = GreenSoft.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("✓ Verificada", color = GreenSoft, fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick  = { guarderiaSeleccionada = null },
                            shape    = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) { Text("Cerrar") }

                        Button(
                            onClick  = { onVerPerfil(g); guarderiaSeleccionada = null },
                            shape    = RoundedCornerShape(10.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = Purple),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Ver perfil", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
