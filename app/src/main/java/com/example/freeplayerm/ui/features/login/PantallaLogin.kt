package com.example.freeplayerm.ui.features.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                    style = TextStyle(
                        shadow = Shadow(
                            color = AppColors.PurpuraOscuro,
                            blurRadius = 20f
                    )),
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PurpuraProfundo,


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
                    fontSize = 35.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,

                    modifier = Modifier

                )
                var nombreUsuarioCorreo by remember { mutableStateOf("") }
                TextField(
                    isError = false,
                    value = nombreUsuarioCorreo,
                    onValueChange = {nombreUsuarioCorreo = it},
                    textStyle = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,

                    ),
                    label = {
                        Text(
                            "Nombre de usuario o Correo Electrónico",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,

                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        //Linea inferior del texto
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent,
                        // colores de fondo
                        focusedContainerColor = AppColors.PurpuraMedio,
                        unfocusedContainerColor = AppColors.PurpuraClaro,
                        disabledContainerColor = Color.LightGray,
                        errorContainerColor = AppColors.PurpuraClaro,
                        // Colores del texto
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        disabledTextColor = Color.DarkGray,
                        errorTextColor = Color.Red,
                        // Colores de las etiquetas
                        unfocusedLabelColor = Color.Black,
                        focusedLabelColor = Color.Black,
                        disabledLabelColor = Color.DarkGray,
                        errorLabelColor = Color.Black,

                        cursorColor = Color.Black,
                        errorCursorColor = Color.Red,
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .border(
                            0.5.dp,
                            AppColors.PurpuraProfundo,
                            shape = RoundedCornerShape(15.dp)
                        )


                )
                var contrasena by remember { mutableStateOf("") }
                TextField(
                    isError = false,
                    value = contrasena,
                    onValueChange = {contrasena = it},
                    textStyle = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,

                    ),
                    label = {
                        Text(
                            "Contraseña",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        //Linea inferior del texto
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent,
                        // colores de fondo
                        focusedContainerColor = AppColors.PurpuraMedio,
                        unfocusedContainerColor = AppColors.PurpuraClaro,
                        disabledContainerColor = Color.LightGray,
                        errorContainerColor = AppColors.PurpuraClaro,
                        // Colores del texto
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        disabledTextColor = Color.DarkGray,
                        errorTextColor = Color.Red,
                        // Colores de las etiquetas
                        unfocusedLabelColor = Color.Black,
                        focusedLabelColor = Color.Black,
                        disabledLabelColor = Color.DarkGray,
                        errorLabelColor = Color.Black,

                        cursorColor = Color.Black,
                        errorCursorColor = Color.Red,
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .border(
                            0.5.dp,
                            AppColors.PurpuraProfundo,
                            shape = RoundedCornerShape(15.dp)
                        )
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

                        "¿No tienes una cuenta?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier
                            .clickable {
                                println(
                                    "Ir a Registro"
                                )
                            }

                    )
                    // Botón Olvidé mi contraseña
                    Text(
                        "¿Olvidaste tu contraseña?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier
                            .clickable{
                                println(
                                    "Ir a Recuperar Contraseña"
                                )
                            }

                    )

                }

                Button(

                    onClick = { /* Acción login */ },
                    colors = ButtonColors(
                        containerColor = AppColors.PurpuraProfundo,
                        contentColor = AppColors.Negro,
                        disabledContainerColor = AppColors.GrisProfundo,
                        disabledContentColor = Color.White
                    ),
                    //border
                    border = BorderStroke(
                        1.dp,
                        AppColors.Negro
                    ),
                    shape = RoundedCornerShape(15.dp),
                    modifier = Modifier
                        .padding(0.dp)



                ) {
                    Text(
                        "Ingresar",
                        color = AppColors.Blanco,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier



                    )
                }



            }

            // 🔹 FOOTER
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
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