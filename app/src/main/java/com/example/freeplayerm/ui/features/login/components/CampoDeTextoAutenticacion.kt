// Asegúrate de que la ruta del paquete coincida con la carpeta donde guardaste el archivo.
package com.example.freeplayerm.ui.features.login.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeplayerm.ui.theme.AppColors

@Composable
fun CampoDeTextoAutenticacion(
    valor: String,
    enCambioDeValor: (String) -> Unit,
    etiqueta: String,
    esCampoDeContrasena: Boolean = false
) {
    TextField(
        value = valor,
        onValueChange = enCambioDeValor,
        textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium),
        label = { Text(etiqueta, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        singleLine = true,
        visualTransformation = if (esCampoDeContrasena) PasswordVisualTransformation() else VisualTransformation.None,
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            focusedContainerColor = AppColors.PurpuraMedio,
            unfocusedContainerColor = AppColors.PurpuraClaro,
            disabledContainerColor = Color.LightGray,
            errorContainerColor = AppColors.PurpuraClaro,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            disabledTextColor = Color.DarkGray,
            errorTextColor = Color.Red,
            unfocusedLabelColor = Color.Black,
            focusedLabelColor = Color.Black,
            disabledLabelColor = Color.DarkGray,
            errorLabelColor = Color.Black,
            cursorColor = Color.Black,
            errorCursorColor = Color.Red,
        ),
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .border(0.5.dp, AppColors.PurpuraProfundo, shape = RoundedCornerShape(15.dp))
    )
}