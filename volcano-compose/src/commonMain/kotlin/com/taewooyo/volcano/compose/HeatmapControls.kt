/*
 * Copyright (C) 2026 taewooyo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.taewooyo.volcano.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp

/** A small platform-neutral navigation control for heatmap sample chrome. */
@Composable
public fun HeatmapBackButton(
  onClick: () -> Unit,
  enabled: Boolean,
  modifier: Modifier = Modifier,
  label: String = "Back",
) {
  val shape = RoundedCornerShape(10.dp)
  Text(
    text = label,
    modifier = modifier
      .clip(shape)
      .background(if (enabled) Color(0xFFF1F5F9) else Color(0xFFE5E7EB))
      .border(1.dp, if (enabled) Color(0xFFD5DCE5) else Color.Transparent, shape)
      .semantics { stateDescription = if (enabled) "Enabled" else "Disabled" }
      .clickable(
        enabled = enabled,
        onClickLabel = label,
        role = Role.Button,
        onClick = onClick,
      )
      .padding(horizontal = 14.dp, vertical = 8.dp),
    color = if (enabled) Color(0xFF1F2937) else Color(0xFF94A3B8),
  )
}
