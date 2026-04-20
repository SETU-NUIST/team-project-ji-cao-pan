package com.example.calorietracker.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calorietracker.components.InlineErrorCard
import com.example.calorietracker.components.SelectorRow
import com.example.calorietracker.components.WellnessTextField
import com.example.calorietracker.viewmodel.AuthMode
import com.example.calorietracker.viewmodel.AuthViewModel

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.secondary,
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(28.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Daily calorie tracking that feels practical.",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Sign in to manage meals, compare intake with TDEE, and review nutrition trends.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
        }

        SelectorRow(
            options = AuthMode.entries.toList(),
            selected = uiState.mode,
            labelOf = { if (it == AuthMode.LOGIN) "Login" else "Register" },
            onSelected = viewModel::switchMode
        )

        if (uiState.mode == AuthMode.REGISTER) {
            WellnessTextField(
                label = "Username",
                value = uiState.username,
                onValueChange = viewModel::updateUsername,
                placeholder = "Mia"
            )
        }

        WellnessTextField(
            label = "Email",
            value = uiState.email,
            onValueChange = viewModel::updateEmail,
            placeholder = "student@wellness.com",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        WellnessTextField(
            label = "Password",
            value = uiState.password,
            onValueChange = viewModel::updatePassword,
            placeholder = "At least 6 characters",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation()
        )

        if (uiState.mode == AuthMode.REGISTER) {
            WellnessTextField(
                label = "Confirm Password",
                value = uiState.confirmPassword,
                onValueChange = viewModel::updateConfirmPassword,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation()
            )
        }

        uiState.errorMessage?.let { InlineErrorCard(it) }

        Button(
            onClick = viewModel::submit,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSubmitting,
            contentPadding = PaddingValues(vertical = 16.dp),
            shape = RoundedCornerShape(22.dp)
        ) {
            Text(if (uiState.isSubmitting) "Please wait..." else if (uiState.mode == AuthMode.LOGIN) "Sign In" else "Create Account")
        }

    }
}
