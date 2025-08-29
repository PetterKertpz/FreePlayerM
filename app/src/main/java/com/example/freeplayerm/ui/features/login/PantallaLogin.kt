package com.example.freeplayerm.ui.features.login

import android.graphics.drawable.Icon
import androidx.annotation.ColorRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.SemanticsActions.OnClick
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeplayerm.R
import com.example.freeplayerm.ui.theme.AppColors

/**
 * Esta es nuestra pantalla principal. Por ahora, es solo un 'Box'.
 * Un Box es el contenedor más simple, como un lienzo en blanco.
 * Ocupa toda la pantalla (Modifier.fillMaxSize()) y tiene el color de fondo de nuestro tema.
 */
@Composable
fun PantallaLogin() {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) { innerPadding ->

        Column(
            verticalArrangement = Arrangement.SpaceBetween, // 👈 divide en arriba, centro y abajo
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)

        ) {

            // 🔹 HEADER
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()



            ) {
                Text(
                    text = "FreePlayer",
                    fontSize = 50.sp,
                    fontFamily = FontFamily.Default,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red


                )
            }

            // 🔹 CONTENIDO CENTRAL
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp)
                    .border(1.dp, Color.Transparent, RoundedCornerShape(10.dp))
            ) {
                Text(
                    "Iniciar Sesión",
                    fontSize = 30.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,

                    modifier = Modifier

                )

                TextField(
                    value = "",
                    onValueChange = {},
                    label = {
                        Text(
                            "Nombre de usuario o Correo Electrónico",
                            fontSize = 15.sp,


                        )
                    },
                    shape = RoundedCornerShape(15.dp),
                    colors = TextFieldDefaults.colors(
                        //Linea inferior del texto
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Red,
                        // colores de fondo
                        focusedContainerColor = AppColors.PurpuraMedio,
                        unfocusedContainerColor = AppColors.PurpuraClaro,
                        disabledContainerColor = Color.LightGray,
                        errorContainerColor = Color.Red,
                        // Colores del texto
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        disabledTextColor = Color.DarkGray,
                        errorTextColor = Color.White,
                        // Colores de las etiquetas
                        unfocusedLabelColor = Color.Black,
                        focusedLabelColor = Color.Black,
                        disabledLabelColor = Color.DarkGray,
                        errorLabelColor = Color.White,

                        unfocusedPlaceholderColor = Color.Black,
                        focusedPlaceholderColor = Color.Black,
                        focusedSupportingTextColor = Color.Black,
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .border(0.5.dp, AppColors.PurpuraProfundo, shape = RoundedCornerShape(15.dp))


                )
                TextField(
                    value = "",
                    onValueChange = {},
                    label = { Text("Contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(10.dp),
                    colors = TextFieldDefaults.colors(
                        //Linea inferior del texto
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Red,
                        // colores de fondo
                        focusedContainerColor = AppColors.PurpuraMedio,
                        unfocusedContainerColor = AppColors.PurpuraClaro,
                        disabledContainerColor = Color.LightGray,
                        errorContainerColor = Color.Red,
                        // Colores del texto
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        disabledTextColor = Color.DarkGray,
                        errorTextColor = Color.White,
                        // Colores de las etiquetas
                        unfocusedLabelColor = Color.Black,
                        focusedLabelColor = Color.Black,
                        disabledLabelColor = Color.DarkGray,
                        errorLabelColor = Color.White,

                        unfocusedPlaceholderColor = Color.Black,
                        focusedPlaceholderColor = Color.Black,
                        focusedSupportingTextColor = Color.Black,
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .border(0.5.dp, AppColors.PurpuraProfundo, shape = RoundedCornerShape(15.dp))
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    // separación de 5dp entre botones
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    // Botón Registro
                    Text(
                        "¿No tienes una cuenta? Regístrate",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier

                    )
                    // Botón Olvidé mi contraseña
                    Text(
                        "¿Olvidaste tu contraseña?",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier

                    )

                }

                Button(
                    shape = AbsoluteRoundedCornerShape(10.dp),
                    onClick = { /* Acción login */ },
                    colors = ButtonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White,
                        disabledContainerColor = Color.Gray,
                        disabledContentColor = Color.LightGray
                    ),
                    modifier = Modifier
                        .padding(0.dp)


                ) {
                    Text(
                        "Iniciar Sesión",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold


                    )
                }



            }

            // 🔹 FOOTER
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.15f)
                    .background(Color.Transparent)


            ) {
                Text(
                    "Iniciar Sesión Con Google",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

//                Icon(
//                    painter = painterResource(id = R.drawable.google_wordmark),
//                    contentDescription = "Google Logo",
//                    tint = Color.Unspecified,
//                    modifier = Modifier
//                        .size(100.dp)
//
//                )

            }
        }
    }
}


/**
 * Esta es la vista previa para nuestro lienzo en blanco.
 * Nos permite ver la pantalla vacía en la ventana de diseño de Android Studio.
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun VistaPreviaPantallaLogin() {
    PantallaLogin()
}