package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CardSurfaceVariant
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun EqualizerScreen(
    viewModel: MainViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bands by viewModel.controller.equalizerBands.collectAsState()
    val bassBoost by viewModel.controller.bassBoostStrength.collectAsState()
    val speed by viewModel.controller.playbackSpeed.collectAsState()
    val pitch by viewModel.controller.playbackPitch.collectAsState()

    var activePreset by remember { mutableStateOf("Flat") }
    val presets = listOf("Flat", "Bass Heavy", "Electronic", "Rock", "Vocal", "Phonk")

    val bandLabels = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")

    fun applyPreset(name: String) {
        activePreset = name
        when (name) {
            "Flat" -> {
                for (i in 0..4) viewModel.controller.setEqualizerBand(i, 0)
                viewModel.controller.setBassBoost(0)
            }
            "Bass Heavy" -> {
                viewModel.controller.setEqualizerBand(0, 8)
                viewModel.controller.setEqualizerBand(1, 5)
                viewModel.controller.setEqualizerBand(2, 0)
                viewModel.controller.setEqualizerBand(3, 2)
                viewModel.controller.setEqualizerBand(4, 4)
                viewModel.controller.setBassBoost(700)
            }
            "Electronic" -> {
                viewModel.controller.setEqualizerBand(0, 6)
                viewModel.controller.setEqualizerBand(1, 4)
                viewModel.controller.setEqualizerBand(2, -1)
                viewModel.controller.setEqualizerBand(3, 4)
                viewModel.controller.setEqualizerBand(4, 7)
                viewModel.controller.setBassBoost(500)
            }
            "Rock" -> {
                viewModel.controller.setEqualizerBand(0, 5)
                viewModel.controller.setEqualizerBand(1, 3)
                viewModel.controller.setEqualizerBand(2, -2)
                viewModel.controller.setEqualizerBand(3, 3)
                viewModel.controller.setEqualizerBand(4, 6)
                viewModel.controller.setBassBoost(300)
            }
            "Vocal" -> {
                viewModel.controller.setEqualizerBand(0, -3)
                viewModel.controller.setEqualizerBand(1, 2)
                viewModel.controller.setEqualizerBand(2, 6)
                viewModel.controller.setEqualizerBand(3, 3)
                viewModel.controller.setEqualizerBand(4, -1)
                viewModel.controller.setBassBoost(100)
            }
            "Phonk" -> {
                viewModel.controller.setEqualizerBand(0, 10)
                viewModel.controller.setEqualizerBand(1, 7)
                viewModel.controller.setEqualizerBand(2, 1)
                viewModel.controller.setEqualizerBand(3, 4)
                viewModel.controller.setEqualizerBand(4, 8)
                viewModel.controller.setBassBoost(900)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Audio Equalizer & FX",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Presets
        Text(text = "Presets", color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presets.forEach { p ->
                val selected = p == activePreset
                FilterChip(
                    selected = selected,
                    onClick = { applyPreset(p) },
                    label = { Text(p, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SpotifyGreen,
                        selectedLabelColor = Color.Black,
                        containerColor = CardSurfaceVariant,
                        labelColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5 Bands Sliders
        Text(text = "5-Band Graphic Equalizer", color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        bands.forEachIndexed { index, gain ->
            val label = bandLabels.getOrElse(index) { "Band $index" }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CardSurface)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = label, color = TextPrimary, fontSize = 13.sp)
                    Text(text = "${if (gain > 0) "+$gain" else "$gain"} dB", color = SpotifyGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Slider(
                    value = gain.toFloat(),
                    onValueChange = {
                        viewModel.controller.setEqualizerBand(index, it.toInt())
                        activePreset = "Custom"
                    },
                    valueRange = -10f..10f,
                    steps = 19,
                    colors = SliderDefaults.colors(
                        thumbColor = TextPrimary,
                        activeTrackColor = SpotifyGreen,
                        inactiveTrackColor = CardSurfaceVariant
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Bass Boost
        Text(text = "Bass Boost", color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CardSurface)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Subwoofer Depth", color = TextPrimary, fontSize = 13.sp)
                Text(text = "${bassBoost / 10}%", color = SpotifyGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Slider(
                value = bassBoost.toFloat(),
                onValueChange = { viewModel.controller.setBassBoost(it.toInt()) },
                valueRange = 0f..1000f,
                colors = SliderDefaults.colors(
                    thumbColor = TextPrimary,
                    activeTrackColor = SpotifyGreen,
                    inactiveTrackColor = CardSurfaceVariant
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Pitch & Speed Controls
        Text(text = "Pitch & Speed (Nightcore / Slowed + Reverb)", color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CardSurface)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Speed", color = TextPrimary, fontSize = 13.sp)
                Text(text = "${String.format("%.2f", speed)}x", color = SpotifyGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Slider(
                value = speed,
                onValueChange = { viewModel.controller.setSpeedAndPitch(it, pitch) },
                valueRange = 0.5f..2.0f,
                colors = SliderDefaults.colors(
                    thumbColor = TextPrimary,
                    activeTrackColor = SpotifyGreen,
                    inactiveTrackColor = CardSurfaceVariant
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Pitch", color = TextPrimary, fontSize = 13.sp)
                Text(text = "${String.format("%.2f", pitch)}x", color = SpotifyGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Slider(
                value = pitch,
                onValueChange = { viewModel.controller.setSpeedAndPitch(speed, it) },
                valueRange = 0.5f..2.0f,
                colors = SliderDefaults.colors(
                    thumbColor = TextPrimary,
                    activeTrackColor = SpotifyGreen,
                    inactiveTrackColor = CardSurfaceVariant
                )
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
