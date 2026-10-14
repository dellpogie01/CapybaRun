package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.UnlockableEntity
import com.example.ui.theme.CapyPrimary
import com.example.ui.theme.CapyTextDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WardrobeScreen(
    unlockables: List<UnlockableEntity>,
    equippedHat: String,
    totalEggs: Int,
    bestScore: Int,
    onEquip: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Capybara Wardrobe", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("wardrobe_back_button")) {
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Customize Your Capy!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Collect magical eggs and achieve high scores to unlock stylish hats.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            items(unlockables) { item ->
                val isEquipped = equippedHat == item.hatId
                val canUnlock = item.isUnlocked

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = CapyTextDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = CapyTextDark.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val reqText = if (item.hatId == "crown" || item.hatId == "wizard") {
                                "Requirement: Score ${item.unlockRequirement} (Best: $bestScore)"
                            } else if (item.hatId != "none") {
                                "Requirement: Collect ${item.unlockRequirement} total eggs (Total: $totalEggs)"
                            } else {
                                "Unlocked by default"
                            }
                            Text(
                                text = reqText,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (canUnlock) CapyPrimary else CapyTextDark.copy(alpha = 0.5f)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        if (canUnlock) {
                            Button(
                                onClick = { onEquip(item.hatId) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isEquipped) CapyPrimary else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isEquipped) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(if (isEquipped) "Equipped" else "Equip", color = if (isEquipped) MaterialTheme.colorScheme.onPrimary else CapyTextDark)
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "Locked 🔒",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
