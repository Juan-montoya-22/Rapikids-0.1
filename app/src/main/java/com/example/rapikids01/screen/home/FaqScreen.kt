package com.example.rapikids01.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

private val FaqPurple     = Color(0xFF6A4DBA)
private val FaqPurpleLight = Color(0xFF9C7EE8)

data class FaqItem(
    val pregunta: String,
    val respuesta: String
)

val FAQ_PADRE = listOf(
    FaqItem(
        pregunta  = "¿Cómo busco una guardería cercana?",
        respuesta = "En la pantalla principal toca el botón \"Usar mi ubicación\" — la app detecta tu posición y calcula la distancia a cada guardería. Luego activa el filtro \"< 3 km\" para ver solo las más cercanas. También puedes usar el mapa tocando el ícono de mapa en la barra superior."
    ),
    FaqItem(
        pregunta  = "¿Cómo califico una guardería?",
        respuesta = "Entra al perfil de la guardería tocando \"Ver perfil completo\". Desplázate hasta abajo y toca el botón \"Calificar y comentar\". Selecciona las estrellas que quieres dar (1 a 5) y escribe tu comentario. Solo puedes calificar una vez por guardería."
    ),
    FaqItem(
        pregunta  = "¿Cómo contacto a una guardería?",
        respuesta = "En la tarjeta de cada guardería encontrarás dos botones: el ícono de teléfono para llamar directamente y el ícono verde de WhatsApp para enviar un mensaje. También puedes tocar el correo electrónico para enviar un email. Recuerda que al contactar directamente, la interacción no es responsabilidad de RapiKids."
    ),
    FaqItem(
        pregunta  = "¿Cómo veo el perfil completo de una guardería?",
        respuesta = "En la lista principal toca el botón morado \"Ver perfil completo\" en la tarjeta de la guardería que te interesa. Allí encontrarás toda la información: fotos, descripción, horarios, precio mensual, dirección, teléfono, correo y las reseñas de otros padres."
    )
)

val FAQ_GUARDERIA = listOf(
    FaqItem(
        pregunta  = "¿Cómo actualizo mi perfil?",
        respuesta = "En tu pantalla principal toca el botón de edición (lápiz) en la esquina superior. Se habilitarán los campos para editar nombre, dirección, teléfono, descripción, precio mensual, horarios y jornada. Cuando termines toca \"Guardar cambios\"."
    ),
    FaqItem(
        pregunta  = "¿Cómo subo fotos?",
        respuesta = "En tu perfil hay dos tipos de fotos: la foto principal (toca el círculo con tu inicial o foto actual) y el carrusel de hasta 3 fotos adicionales que aparecen en tu perfil público. Toca cada espacio del carrusel para subir o cambiar las fotos."
    ),
    FaqItem(
        pregunta  = "¿Por qué mi guardería no aparece en el mapa?",
        respuesta = "Tu guardería solo aparece en el mapa si tiene estado \"verificada\". Si tu solicitud está en revisión (pendiente) o fue rechazada, no será visible para los padres. Además, asegúrate de haber marcado la ubicación correctamente en el mapa al registrarte."
    ),
    FaqItem(
        pregunta  = "¿Qué papeles puedo subir?",
        respuesta = "Puedes subir el registro mercantil o la licencia de funcionamiento en formato PDF o imagen (JPG, PNG). Este documento es revisado por el equipo de RapiKids para verificar que tu guardería es legítima. Si tu solicitud fue rechazada, puedes subir un nuevo documento desde tu perfil."
    ),
    FaqItem(
        pregunta  = "¿Cuánto se demora en revisar mi solicitud?",
        respuesta = "El equipo de RapiKids revisa las solicitudes en un plazo de 1 a 3 días hábiles. Recibirás una notificación cuando tu perfil sea verificado o si necesita correcciones. Puedes ver el estado actual de tu solicitud en tu pantalla de perfil."
    ),
    FaqItem(
        pregunta  = "¿Cómo sé si los padres pueden ver mi perfil?",
        respuesta = "En tu pantalla principal verás una etiqueta que indica el estado de tu cuenta: \"Verificada\" (visible para padres, etiqueta verde), \"Pendiente\" (en revisión, no visible aún) o \"Rechazada\" (debes corregir y reenviar el documento). Solo las guarderías verificadas aparecen en la búsqueda de padres."
    )
)

@Composable
fun FaqDialog(
    esPadre: Boolean,
    onDismiss: () -> Unit
) {
    val preguntas = if (esPadre) FAQ_PADRE else FAQ_GUARDERIA
    val titulo    = if (esPadre) "Ayuda para padres" else "Ayuda para guarderías"

    Dialog(
        onDismissRequest = onDismiss,
        properties       = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape    = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f),
            colors   = CardDefaults.cardColors(containerColor = Color(0xFFF5F0FF))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(listOf(FaqPurple, FaqPurpleLight))
                        )
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Column {
                        Text(
                            "Preguntas frecuentes",
                            color      = Color.White.copy(alpha = 0.85f),
                            fontSize   = 13.sp
                        )
                        Text(
                            titulo,
                            color      = Color.White,
                            fontSize   = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick  = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, null, tint = Color.White)
                    }
                }
                LazyColumn(
                    contentPadding    = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier          = Modifier.weight(1f)
                ) {
                    items(preguntas) { faq ->
                        FaqItemCard(faq = faq)
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                }
            }
        }
    }
}

@Composable
private fun FaqItemCard(faq: FaqItem) {
    var expandido by remember { mutableStateOf(false) }

    Card(
        shape   = RoundedCornerShape(14.dp),
        colors  = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandido = !expandido }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier.weight(1f)
                ) {
                    Box(
                        modifier         = Modifier
                            .size(32.dp)
                            .background(FaqPurple.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.QuestionMark,
                            null,
                            tint     = FaqPurple,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text       = faq.pregunta,
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = Color(0xFF1A1A2E),
                        modifier   = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = if (expandido) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint     = FaqPurple,
                    modifier = Modifier.size(22.dp)
                )
            }
            AnimatedVisibility(
                visible = expandido,
                enter   = expandVertically(),
                exit    = shrinkVertically()
            ) {
                Column {
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8F5FF))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Box(
                            modifier         = Modifier
                                .size(32.dp)
                                .background(FaqPurple.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Lightbulb,
                                null,
                                tint     = FaqPurple,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text       = faq.respuesta,
                            fontSize   = 13.sp,
                            color      = Color(0xFF444444),
                            lineHeight = 20.sp,
                            modifier   = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}