/*
 * Copyright (C) 2026 taewooyo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.taewooyo.volcano.compose

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.taewooyo.volcano.heatmap.HeatmapColorScale
import com.taewooyo.volcano.heatmap.HeatmapNode
import com.taewooyo.volcano.heatmap.SignedMetricColorScale
import com.taewooyo.volcano.squarified.SquarifiedMeasurer
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/**
 * Rules that preserve readable content in small heatmap cells.
 *
 * With [adaptiveContent] enabled (the default), measured text dimensions decide whether a label
 * and metric fit. The size thresholds remain deterministic fallback rules when it is disabled.
 */
public data class HeatmapDisplayPolicy(
  val hideContentBelow: Dp = 16.dp,
  val showMetricAbove: Dp = 40.dp,
  val showLabelAbove: Dp = 48.dp,
  /** Maximum inner padding. Small cells automatically use a smaller value. */
  val cellContentPadding: Dp = 6.dp,
  /** Uses measured text dimensions instead of only fixed cell-size thresholds. */
  val adaptiveContent: Boolean = true,
  val metricFormatter: HeatmapMetricFormatter = SignedMetricFormatter,
)

/** Optional content rendered above the default label and metric for a sufficiently large leaf. */
public typealias HeatmapLogoContent = @Composable (HeatmapNode, Dp) -> Unit

/** Optional replacement for the built-in tooltip body. The callback dismisses the tooltip. */
public typealias HeatmapTooltipContent = @Composable (HeatmapNode, () -> Unit) -> Unit

private enum class TooltipTrigger {
  LongPress,
  Hover,
}

/**
 * Renders the visible level in [state] using the size supplied by [modifier]'s parent.
 *
 * The composable deliberately does not apply a size modifier. Consumers can place it in a card,
 * pane, or full screen layout without the library overriding their constraints.
 */
@Composable
public fun Heatmap(
  state: HeatmapState,
  modifier: Modifier = Modifier,
  displayPolicy: HeatmapDisplayPolicy = HeatmapDisplayPolicy(),
  style: HeatmapStyle = HeatmapStyle(),
  interaction: HeatmapInteraction = HeatmapInteraction(),
  colorScale: HeatmapColorScale = SignedMetricColorScale(),
  logoContent: HeatmapLogoContent? = null,
  onGroupClick: (HeatmapNode) -> Unit = {},
  onLeafClick: (HeatmapNode) -> Unit = {},
  onLeafLongClick: (HeatmapNode) -> Unit = {},
  /** Receives the hovered leaf on pointer platforms, or null after the pointer leaves it. */
  onLeafHover: ((HeatmapNode?) -> Unit)? = null,
  cellContent: @Composable (HeatmapNode, Modifier) -> Unit = { node, cellModifier ->
    DefaultHeatmapCell(
      node = node,
      modifier = cellModifier,
      displayPolicy = displayPolicy,
      color = node.color ?: colorScale.colorOf(node),
      selected = state.isSelected(node),
      selectedBorderColor = style.selectedBorderColor,
      contentColor = style.leafTextColor,
      logoContent = logoContent,
    )
  },
  motion: HeatmapMotion = HeatmapMotion(),
  tooltipContent: HeatmapTooltipContent? = null,
) {
  val visibleNode = state.visibleNode
  var tooltipNode by remember { mutableStateOf<HeatmapNode?>(null) }
  var tooltipTrigger by remember { mutableStateOf<TooltipTrigger?>(null) }
  var tooltipRequest by remember { mutableStateOf(0) }
  var hoveredNode by remember { mutableStateOf<HeatmapNode?>(null) }
  val hoverEnabled = interaction.showTooltipOnHover || onLeafHover != null
  val handleLeafLongClick: (HeatmapNode) -> Unit = { node ->
    onLeafLongClick(node)
    if (interaction.showTooltipOnLongClick) {
      tooltipNode = node
      tooltipTrigger = TooltipTrigger.LongPress
      tooltipRequest += 1
    }
  }
  val handleLeafHover: (HeatmapNode, Boolean) -> Unit = { node, isHovered ->
    if (isHovered) {
      hoveredNode = node
      onLeafHover?.invoke(node)
    } else if (hoveredNode === node) {
      hoveredNode = null
      onLeafHover?.invoke(null)
      if (tooltipTrigger == TooltipTrigger.Hover && tooltipNode === node) {
        tooltipNode = null
        tooltipTrigger = null
      }
    }
  }
  LaunchedEffect(tooltipRequest) {
    val shownRequest = tooltipRequest
    if (tooltipNode != null && interaction.tooltipDurationMillis > 0) {
      delay(interaction.tooltipDurationMillis.toLong().milliseconds)
      if (tooltipRequest == shownRequest) tooltipNode = null
    }
  }
  LaunchedEffect(hoveredNode, interaction.showTooltipOnHover, interaction.tooltipHoverDelayMillis) {
    if (!interaction.showTooltipOnHover) return@LaunchedEffect
    val node = hoveredNode ?: return@LaunchedEffect
    delay(interaction.tooltipHoverDelayMillis.toLong().milliseconds)
    if (hoveredNode === node) {
      tooltipNode = node
      tooltipTrigger = TooltipTrigger.Hover
    }
  }
  Box(modifier = modifier.background(style.borderColor)) {
    if (motion.enabled) {
      AnimatedContent(
        targetState = visibleNode,
        modifier = Modifier.fillMaxSize(),
        transitionSpec = {
          (fadeIn(animationSpec = tween(motion.durationMillis)) + scaleIn(
            initialScale = motion.initialScale,
            animationSpec = tween(motion.durationMillis),
          )).togetherWith(fadeOut(animationSpec = tween(motion.durationMillis)))
        },
        contentKey = HeatmapNode::id,
        label = "HeatmapDrillDown",
      ) { targetNode ->
        HeatmapLevel(
          visibleNode = targetNode,
          state = state,
          interaction = interaction,
          motion = motion,
          style = style,
          onGroupClick = onGroupClick,
          onLeafClick = onLeafClick,
          onLeafLongClick = handleLeafLongClick,
          onLeafHover = handleLeafHover,
          hoverEnabled = hoverEnabled,
          cellContent = cellContent,
        )
      }
    } else {
      HeatmapLevel(
        visibleNode = visibleNode,
        state = state,
        interaction = interaction,
        motion = motion,
        style = style,
        onGroupClick = onGroupClick,
        onLeafClick = onLeafClick,
        onLeafLongClick = handleLeafLongClick,
        onLeafHover = handleLeafHover,
        hoverEnabled = hoverEnabled,
        cellContent = cellContent,
      )
    }
    tooltipNode?.let { node ->
      Popup(onDismissRequest = { tooltipNode = null }, alignment = Alignment.Center) {
        tooltipContent?.invoke(node) { tooltipNode = null } ?: DefaultHeatmapTooltip(
          node = node,
          metricFormatter = displayPolicy.metricFormatter,
          onDismissRequest = { tooltipNode = null },
        )
      }
    }
  }
}

@Composable
private fun HeatmapLevel(
  visibleNode: HeatmapNode,
  state: HeatmapState,
  interaction: HeatmapInteraction,
  motion: HeatmapMotion,
  style: HeatmapStyle,
  onGroupClick: (HeatmapNode) -> Unit,
  onLeafClick: (HeatmapNode) -> Unit,
  onLeafLongClick: (HeatmapNode) -> Unit,
  onLeafHover: (HeatmapNode, Boolean) -> Unit,
  hoverEnabled: Boolean,
  cellContent: @Composable (HeatmapNode, Modifier) -> Unit,
) {
  val handleLeafClick: (HeatmapNode) -> Unit = { leaf ->
    if (interaction.selectLeafOnClick) state.select(leaf)
    onLeafClick(leaf)
  }
  val handleLeafLongClick: (HeatmapNode) -> Unit = { leaf -> onLeafLongClick(leaf) }
  if (visibleNode.isLeaf) {
    cellContent(
      visibleNode,
      heatmapPressModifier(
        motion = motion,
        onClick = { handleLeafClick(visibleNode) },
        onLongClick = { handleLeafLongClick(visibleNode) },
        onHoverChanged = { onLeafHover(visibleNode, it) },
        hoverEnabled = hoverEnabled,
      ).fillMaxSize(),
    )
  } else {
    HeatmapGroup(
      node = visibleNode,
      modifier = Modifier.fillMaxSize(),
      showLabel = false,
      style = style,
      onGroupClick = { group ->
        onGroupClick(group)
        if (interaction.drillDownOnGroupClick) state.drillDown(group.id)
      },
      onLeafClick = handleLeafClick,
      onLeafLongClick = handleLeafLongClick,
      onLeafHover = onLeafHover,
      hoverEnabled = hoverEnabled,
      motion = motion,
      cellContent = cellContent,
    )
  }
}

/** A compact breadcrumb intended to be displayed above a [Heatmap]. */
@Composable
public fun HeatmapBreadcrumb(
  state: HeatmapState,
  modifier: Modifier = Modifier,
  separator: String = " / ",
) {
  Row(modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
    state.breadcrumbs.forEachIndexed { index, crumb ->
      Text(
        text = crumb.label,
        modifier = if (index < state.breadcrumbs.lastIndex) {
          Modifier.clickable(
            onClickLabel = "Navigate to ${crumb.label}",
            role = Role.Button,
          ) { state.navigateToBreadcrumb(index) }
        } else {
          Modifier.semantics { stateDescription = "Current group" }
        },
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      if (index < state.breadcrumbs.lastIndex) {
        Text(text = separator)
      }
    }
  }
}

@Composable
private fun HeatmapGroup(
  node: HeatmapNode,
  modifier: Modifier,
  showLabel: Boolean,
  style: HeatmapStyle,
  onGroupClick: (HeatmapNode) -> Unit,
  onLeafClick: (HeatmapNode) -> Unit,
  onLeafLongClick: (HeatmapNode) -> Unit,
  onLeafHover: (HeatmapNode, Boolean) -> Unit,
  hoverEnabled: Boolean,
  motion: HeatmapMotion,
  cellContent: @Composable (HeatmapNode, Modifier) -> Unit,
) {
  val visibleChildren = node.children.filter { it.layoutValue > 0.0 }
  if (visibleChildren.isEmpty()) {
    cellContent(node, modifier)
    return
  }
  val groupInteractionSource = remember { MutableInteractionSource() }
  val groupHovered by groupInteractionSource.collectIsHoveredAsState()
  val groupPressed by groupInteractionSource.collectIsPressedAsState()
  // SquarifiedMeasurer mutates its working area while measuring. Each recursive group needs an
  // independent instance; sharing one across nested Layout measure passes can overwrite a
  // parent's coordinates and leave only its header visible.
  val measurer = remember { SquarifiedMeasurer() }
  Layout(
    content = {
      if (showLabel) {
        Text(
          text = node.label,
          modifier = Modifier
            .fillMaxWidth()
            .background(style.groupHeaderColor)
            .hoverable(interactionSource = groupInteractionSource)
            .clickable(
              interactionSource = groupInteractionSource,
              onClickLabel = "Open ${node.label}",
              role = Role.Button,
              onClick = { onGroupClick(node) },
            )
            .padding(horizontal = 4.dp),
          color = style.groupHeaderTextColor,
          fontSize = 12.sp,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      visibleChildren.forEach { child ->
        if (child.isLeaf) {
          cellContent(
            child,
            heatmapPressModifier(
              motion = motion,
              onClick = { onLeafClick(child) },
              onLongClick = { onLeafLongClick(child) },
              onHoverChanged = { onLeafHover(child, it) },
              hoverEnabled = hoverEnabled,
            ),
          )
        } else {
          HeatmapGroup(
            node = child,
            modifier = Modifier,
            showLabel = true,
            style = style,
            onGroupClick = onGroupClick,
            onLeafClick = onLeafClick,
            onLeafLongClick = onLeafLongClick,
            onLeafHover = onLeafHover,
            hoverEnabled = hoverEnabled,
            motion = motion,
            cellContent = cellContent,
          )
        }
      }
    },
    modifier = modifier.drawWithContent {
      drawContent()
      if (showLabel && (groupHovered || groupPressed)) {
        drawRect(Color.Black.copy(alpha = 0.08f))
      }
    },
  ) { measurables, constraints ->
    // Nested groups are measured with fixed bounds. Passing that fixed minimum height to the
    // header makes Text expand to the entire group, leaving zero height for the child treemap.
    // Keep its width but measure the header at its intrinsic height.
    val header = if (showLabel) {
      measurables.first().measure(constraints.copy(minHeight = 0))
    } else {
      null
    }
    val availableHeight = (constraints.maxHeight - (header?.height ?: 0)).coerceAtLeast(0)
    val nodes = measurer.measureNodes(
      values = visibleChildren.map(HeatmapNode::layoutValue),
      width = constraints.maxWidth,
      height = availableHeight,
    )
    val childMeasurables = if (showLabel) measurables.drop(1) else measurables
    val placeables = childMeasurables.mapIndexed { index, measurable ->
      measurable.measure(Constraints.fixed(nodes[index].width, nodes[index].height))
    }
    layout(constraints.maxWidth, constraints.maxHeight) {
      header?.placeRelative(0, 0)
      placeables.forEachIndexed { index, placeable ->
        placeable.placeRelative(nodes[index].offsetX, nodes[index].offsetY + (header?.height ?: 0))
      }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun heatmapPressModifier(
  motion: HeatmapMotion,
  onClick: () -> Unit,
  onLongClick: () -> Unit,
  hoverEnabled: Boolean,
  onHoverChanged: (Boolean) -> Unit,
): Modifier {
  val interactionSource = remember { MutableInteractionSource() }
  val pressed by interactionSource.collectIsPressedAsState()
  val hovered by interactionSource.collectIsHoveredAsState()
  val scale by animateFloatAsState(
    targetValue = if (pressed) motion.pressScale else 1f,
    animationSpec = tween(motion.pressDurationMillis),
    label = "HeatmapLeafPressScale",
  )
  val alpha by animateFloatAsState(
    targetValue = if (pressed) motion.pressedAlpha else 1f,
    animationSpec = tween(motion.pressDurationMillis),
    label = "HeatmapLeafPressAlpha",
  )
  return Modifier
    .graphicsLayer {
      scaleX = scale
      scaleY = scale
      this.alpha = alpha
    }
    .drawWithContent {
      drawContent()
      if (hoverEnabled && hovered) drawRect(Color.White.copy(alpha = 0.08f))
    }
    .hoverable(interactionSource = interactionSource, enabled = hoverEnabled)
    .combinedClickable(
      interactionSource = interactionSource,
      indication = null,
      onClick = onClick,
      onLongClickLabel = "Show details",
      onLongClick = onLongClick,
    )
    .heatmapHoverModifier(enabled = hoverEnabled, onHoverChanged = onHoverChanged)
}

private fun Modifier.heatmapHoverModifier(
  enabled: Boolean,
  onHoverChanged: (Boolean) -> Unit,
): Modifier = pointerInput(enabled) {
  if (!enabled) return@pointerInput
  awaitPointerEventScope {
    while (true) {
      when (awaitPointerEvent().type) {
        PointerEventType.Enter -> onHoverChanged(true)
        PointerEventType.Exit -> onHoverChanged(false)
        else -> Unit
      }
    }
  }
}

/**
 * Default leaf renderer.
 *
 * Supply [logoContent] to render an optional node image without making the base artifact depend
 * on a particular image-loading library. For a custom overall cell, use [Heatmap]'s [cellContent].
 */
@Composable
public fun DefaultHeatmapCell(
  node: HeatmapNode,
  modifier: Modifier = Modifier,
  displayPolicy: HeatmapDisplayPolicy = HeatmapDisplayPolicy(),
  color: Long? = node.color,
  selected: Boolean = false,
  selectedBorderColor: Color = Color.Black,
  contentColor: Color = Color.White,
  logoContent: HeatmapLogoContent? = null,
) {
  val density = LocalDensity.current
  val textMeasurer = rememberTextMeasurer()
  val targetColor = color?.let { Color(it.toInt()) } ?: defaultHeatmapColor(node.effectiveMetric)
  val animatedColor by animateColorAsState(
    targetValue = targetColor,
    animationSpec = tween(240),
    label = "HeatmapMetricColor",
  )
  androidx.compose.foundation.layout.BoxWithConstraints(
    modifier = modifier
      .border(
        width = if (selected && selectedBorderColor.alpha > 0f) 2.dp else 0.5.dp,
        color = if (selected && selectedBorderColor.alpha > 0f) selectedBorderColor else Color.White,
      )
      .background(animatedColor)
      .semantics(mergeDescendants = true) {
        contentDescription = defaultHeatmapCellContentDescription(node, displayPolicy.metricFormatter)
        this.selected = selected
        stateDescription = if (selected) "Selected" else "Not selected"
      },
    contentAlignment = Alignment.Center,
  ) {
    val minDimension = minOf(constraints.maxWidth, constraints.maxHeight).toFloat()
    val hideBelow = with(density) { displayPolicy.hideContentBelow.toPx() }
    val showMetricAbove = with(density) { displayPolicy.showMetricAbove.toPx() }
    val showLabelAbove = with(density) { displayPolicy.showLabelAbove.toPx() }
    val metric = node.effectiveMetric
    if (minDimension >= hideBelow) {
      val cellPaddingPx = minOf(
        with(density) { displayPolicy.cellContentPadding.toPx() },
        minDimension * 0.12f,
      )
      val availableWidth = (constraints.maxWidth - cellPaddingPx * 2).toInt().coerceAtLeast(0)
      val availableHeight = (constraints.maxHeight - cellPaddingPx * 2).toInt().coerceAtLeast(0)
      val logoSizePx = minOf(
        minDimension * 0.32f,
        with(density) { 48.dp.toPx() },
      )
      val logoGapPx = with(density) { 4.dp.toPx() }
      val imageUrl = node.imageUrl?.takeIf(String::isNotBlank)
      val showLogo = logoContent != null && imageUrl != null &&
        minDimension >= with(density) { 72.dp.toPx() } &&
        logoSizePx + logoGapPx <= availableHeight
      val textAvailableHeight = (
        availableHeight - (if (showLogo) logoSizePx + logoGapPx else 0f)
      ).coerceAtLeast(0f).toInt()
      val labelFontSize = (minDimension * 0.14f / density.density).coerceIn(8f, 26f).sp
      val metricFontSize = (minDimension * 0.12f / density.density).coerceIn(8f, 22f).sp
      val labelSize = textMeasurer.measure(
        text = AnnotatedString(node.label),
        style = TextStyle(fontSize = labelFontSize, fontWeight = FontWeight.Bold),
      ).size
      val metricText = metric?.let(displayPolicy.metricFormatter::format)
      val metricSize = metricText?.let { value ->
        textMeasurer.measure(text = AnnotatedString(value), style = TextStyle(fontSize = metricFontSize)).size
      }
      val labelFits = labelSize.width <= availableWidth && labelSize.height <= textAvailableHeight
      val metricFits = metricSize != null && metricSize.width <= availableWidth && metricSize.height <= textAvailableHeight
      val showMetric = metricFits && (
        displayPolicy.adaptiveContent || minDimension >= showMetricAbove
      )
      val metricHeight = metricSize?.height ?: 0
      val showLabel = labelFits && if (displayPolicy.adaptiveContent) {
        labelSize.height + (if (showMetric) metricHeight else 0) <= textAvailableHeight
      } else {
        minDimension >= showLabelAbove
      }
      Column(
        modifier = Modifier.padding(with(density) { cellPaddingPx.toDp() }),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        if (showLogo) {
          logoContent(node, with(density) { logoSizePx.toDp() })
          androidx.compose.foundation.layout.Spacer(Modifier.size(4.dp))
        }
        if (showLabel) {
          Text(
            text = node.label,
            fontSize = labelFontSize,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
          )
        }
        if (showMetric) {
          Text(
            text = metricText.orEmpty(),
            fontSize = metricFontSize,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    }
  }
}

internal fun defaultHeatmapCellContentDescription(
  node: HeatmapNode,
  metricFormatter: HeatmapMetricFormatter,
): String = buildString {
  append(node.label)
  node.effectiveMetric?.let { metric ->
    append(", metric ")
    append(metricFormatter.format(metric))
  }
}

/**
 * Default long-press detail popup.
 *
 * Tapping it dismisses it. When used through [Heatmap], it also automatically dismisses after
 * [HeatmapInteraction.tooltipDurationMillis]. Supply [onLeafLongClick] for a custom presentation.
 */
@Composable
public fun DefaultHeatmapTooltip(
  node: HeatmapNode,
  metricFormatter: HeatmapMetricFormatter = SignedMetricFormatter,
  modifier: Modifier = Modifier,
  onDismissRequest: () -> Unit,
) {
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(14.dp))
      .background(Color(0xEE0F172A))
      .clickable(onClick = onDismissRequest)
      .padding(horizontal = 18.dp, vertical = 12.dp),
  ) {
    Text(text = node.label, color = Color.White, fontWeight = FontWeight.Bold)
    node.effectiveMetric?.let { metric ->
      Text(text = metricFormatter.format(metric), color = Color.White)
    }
  }
}

private fun defaultHeatmapColor(metric: Double?): Color = when {
  metric == null -> Color(0xFF6B7280)
  metric > 0.0 -> Color(0xFF1E9E63)
  metric < 0.0 -> Color(0xFFD44848)
  else -> Color(0xFF6B7280)
}
