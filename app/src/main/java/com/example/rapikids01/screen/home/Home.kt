package com.example.rapikids01.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rapikids01.R

@Composable
fun Home(
    modifier: Modifier = Modifier,
    onPadreClick: () -> Unit,
    onGuarderiaClick: () -> Unit,
    onAdminClick: () -> Unit = {}
) {
    // Contador de toques secretos en el título
    var tapCount by remember { mutableStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.weight(0.6f))

        // ── Título — toca 5 veces para acceso admin ────────────────────
        Text(
            text = "¿Quién eres?",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures {
                    tapCount++
                    if (tapCount >= 5) {
                        tapCount = 0
                        onAdminClick()
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ── Botón Padre ────────────────────────────────────────────────
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Image(
                painter            = painterResource(id = R.drawable.familia),
                contentDescription = "Familia",
                modifier           = Modifier.size(110.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onPadreClick,
                shape   = RoundedCornerShape(50),
                colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A4DBA)),
                modifier = Modifier.width(260.dp).height(46.dp)
            ) {
                Text(
                    text      = "Soy un padre, madre o acudiente",
                    fontSize  = 13.sp,
                    color     = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // ── Botón Guardería ────────────────────────────────────────────
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Image(
                painter            = painterResource(id = R.drawable.guarderia),
                contentDescription = "Guardería",
                modifier           = Modifier.size(110.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick  = onGuarderiaClick,
                shape    = RoundedCornerShape(50),
                colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A4DBA)),
                modifier = Modifier.width(260.dp).height(46.dp)
            ) {
                Text(
                    text      = "Soy una guardería infantil",
                    fontSize  = 13.sp,
                    color     = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.6f))
    }
}
