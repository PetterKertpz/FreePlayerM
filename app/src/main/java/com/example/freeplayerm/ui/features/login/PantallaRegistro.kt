package com.example.freeplayerm.ui.features.login

import android.widget.Toast
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.freeplayerm.ui.features.nav.Rutas
import com.example.freeplayerm.ui.theme.AppColors
import com.example.freeplayerm.ui.theme.FreePlayerMTheme

@Composable
fun PantallaRegistro(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    //
    val estado by viewModel.estadoUi.collectAsState()
    val contexto = LocalContext.current

    LaunchedEffect(key1 = estado.registroExitoso, key2 = estado.error) {
        if (estado.registroExitoso) {
            Toast.makeText(contexto, "¡Registro exitoso!", Toast.LENGTH_SHORT).show()
            navController.navigate(Rutas.Biblioteca.ruta) {
                popUpTo(Rutas.Login.ruta) { inclusive = true }
            }
            viewModel.enEvento(LoginEvento.ConsumirEventoDeNavegacion)
        }

        estado.error?.let { mensajeError ->
            Toast.makeText(contexto, mensajeError, Toast.LENGTH_LONG).show()
        }
    }




    //
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
                    "Crear Cuenta",
                    fontSize = 35.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier

                )
                TextField(
                    isError = false,
                    value = estado.nombreUsuario,
                    onValueChange = {
                        viewModel.enEvento(LoginEvento.NombreUsuarioCambiado(it))
                    },
                    textStyle = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,

                        ),
                    label = {
                        Text(
                            "Nombre de usuario",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,

                            )
                    },
                    singleLine = true,
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

                TextField(
                    isError = false,
                    value = estado.correoOUsuario,
                    onValueChange = {
                        viewModel.enEvento(LoginEvento.CorreoOUsuarioCambiado(it))
                    },
                    textStyle = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,

                        ),
                    label = {
                        Text(
                            "Correo Electrónico",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,

                            )
                    },
                    singleLine = true,
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

                TextField(
                    isError = false,
                    value = estado.contrasena,
                    onValueChange = {
                        viewModel.enEvento(LoginEvento.ContrasenaCambiada(it))
                    },
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
                    singleLine = true,
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
                if (estado.estaCargando) {
                    CircularProgressIndicator(

                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    // separación de 5dp entre botones
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    // Botón Registro
                    Text(

                        "¿Tienes una cuenta?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier
                            .clickable {
                                println(
                                    "Ir a Iniciar Sesión"
                                )
                                navController.navigate("login") {
                                    popUpTo("registro"){
                                        inclusive = true
                                    }
                                }
                            }

                    )

                }

                Button(

                    onClick = {
                        println("Crear Cuenta Local")
                        viewModel.enEvento(LoginEvento.BotonRegistroPresionado)
                    },
                    enabled = !estado.estaCargando,
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
                        "Registrarse",
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

            }
        }
    }
}
@Preview(
    showBackground = true,
    showSystemUi = true,
)
@Composable
fun PreviewPantallaRegistro () {
    FreePlayerMTheme {
        val navControllerFalso = rememberNavController()
        PantallaRegistro(navController = navControllerFalso)
    }

}


