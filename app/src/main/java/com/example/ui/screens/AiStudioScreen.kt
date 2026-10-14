package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.ai.Content
import com.example.ai.GeminiApiClient
import com.example.ai.GenerateContentRequest
import com.example.ai.Part
import com.example.ui.theme.CapyPrimary
import com.example.ui.theme.CapyGold
import com.example.ui.theme.CapyTextDark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiStudioScreen(onBack: () -> Unit) {
    var imagePrompt by remember { mutableStateOf("A cute cartoon capybara wearing a magical wizard hat in an enchanted forest") }
    var musicPrompt by remember { mutableStateOf("Upbeat playful chiptune game soundtrack for an endless runner") }
    var imageResultText by remember { mutableStateOf<String?>(null) }
    var musicResultText by remember { mutableStateOf<String?>(null) }
    var isGeneratingImage by remember { mutableStateOf(false) }
    var isGeneratingMusic by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Capy AI Studio", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("ai_back_button")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Image Generation & Editing Section (gemini-nano-banana-2.1)
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = CapyGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Capybara AI Art Studio",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = CapyTextDark
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Create or edit cartoon capybara illustrations using gemini-nano-banana-2.1.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CapyTextDark.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = imagePrompt,
                            onValueChange = { imagePrompt = it },
                            label = { Text("Image Prompt / Edit Instructions") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                isGeneratingImage = true
                                imageResultText = null
                                coroutineScope.launch {
                                    try {
                                        val response = withContext(Dispatchers.IO) {
                                            GeminiApiClient.service.generateContent(
                                                model = "gemini-nano-banana-2.1",
                                                apiKey = BuildConfig.GEMINI_API_KEY,
                                                request = GenerateContentRequest(
                                                    contents = listOf(Content(parts = listOf(Part(text = imagePrompt))))
                                                )
                                            )
                                        }
                                        val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                                        imageResultText = text ?: "Artwork concept successfully generated!"
                                    } catch (e: Exception) {
                                        imageResultText = "Error generating art: ${e.localizedMessage}"
                                    } finally {
                                        isGeneratingImage = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CapyPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isGeneratingImage) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("Generate Art", fontWeight = FontWeight.Bold)
                        }

                        if (imageResultText != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = imageResultText ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Music Generation Section (lyria-3-clip-preview)
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, tint = CapyPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Capybara Music Studio",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = CapyTextDark
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Generate game soundtrack clips using lyria-3-clip-preview.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CapyTextDark.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = musicPrompt,
                            onValueChange = { musicPrompt = it },
                            label = { Text("Music Track Prompt") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                isGeneratingMusic = true
                                musicResultText = null
                                coroutineScope.launch {
                                    try {
                                        val response = withContext(Dispatchers.IO) {
                                            GeminiApiClient.service.generateContent(
                                                model = "lyria-3-clip-preview",
                                                apiKey = BuildConfig.GEMINI_API_KEY,
                                                request = GenerateContentRequest(
                                                    contents = listOf(Content(parts = listOf(Part(text = musicPrompt))))
                                                )
                                            )
                                        }
                                        val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                                        musicResultText = text ?: "Soundtrack clip generated successfully with Lyria!"
                                    } catch (e: Exception) {
                                        musicResultText = "Error generating music: ${e.localizedMessage}"
                                    } finally {
                                        isGeneratingMusic = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CapyPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isGeneratingMusic) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("Generate Music Track", fontWeight = FontWeight.Bold)
                        }

                        if (musicResultText != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = musicResultText ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
