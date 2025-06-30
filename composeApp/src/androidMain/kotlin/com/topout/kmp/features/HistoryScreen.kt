package com.topout.kmp.features

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.topout.kmp.features.sessions.SessionsState
import com.topout.kmp.features.sessions.SessionsViewModel
import com.topout.kmp.models.Session
import org.koin.androidx.compose.koinViewModel

@Composable
fun HistoryScreen(
    viewModel: SessionsViewModel = koinViewModel(),
    onSessionClick: (Session) -> Unit
) {
  val uiState =viewModel.uiState.collectAsState().value

  when(uiState) {
    is SessionsState.Error -> ErrorContent(message = uiState.errorMessage)
    is SessionsState.Loaded -> SessionsGridContent(
      uiState.sessions,
      onSessionClicked = onSessionClick
    )
    SessionsState.Loading -> LoadingContent()
  }
}











@Composable
fun ErrorContent(message: String) {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = TextStyle(
                fontSize = 28.sp,
                textAlign = TextAlign.Center
            )
        )
    }
}

@Composable
fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.width(64.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            trackColor = MaterialTheme.colorScheme.secondary
        )
    }
}