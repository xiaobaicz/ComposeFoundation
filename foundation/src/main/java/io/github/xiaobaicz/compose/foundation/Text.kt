package io.github.xiaobaicz.compose.foundation

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.modifiers.TextAutoSizeLayoutScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp
import io.github.xiaobaicz.compose.foundation.theme.LocalContentColor
import io.github.xiaobaicz.compose.foundation.theme.LocalTextStyle
import kotlin.math.min

@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle = FontStyle.Normal,
    fontWeight: FontWeight = FontWeight.Normal,
    lineHeightFactor: Float = 1.5f,
    lineHeight: TextUnit = if (fontSize.isUnspecified) TextUnit.Unspecified else fontSize * lineHeightFactor,
    lineHeightStyle: LineHeightStyle = defaultLineHeightStyle,
    textAlign: TextAlign = TextAlign.Unspecified,
    shadow: Shadow? = null,
    style: TextStyle = LocalTextStyle.current,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    maxLines: Int = 1,
    minLines: Int = 1,
    softWrap: Boolean = maxLines > 1,
) {
    require(lineHeightFactor >= 1f) { "lineHeightFactor should be greater than or equal to 1f" }
    val mergeStyle = style.merge(
        color = color.takeOrElse { LocalContentColor.current },
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        lineHeight = lineHeight,
        lineHeightStyle = lineHeightStyle,
        textAlign = textAlign,
        shadow = shadow,
    )
    val autoSize = remember(mergeStyle.fontSize, mergeStyle.lineHeight, lineHeightFactor) {
        TextAutoSizeByLineHeight(mergeStyle.fontSize, mergeStyle.lineHeight, lineHeightFactor)
    }
    BasicText(
        text = text,
        modifier = modifier,
        style = mergeStyle,
        onTextLayout = onTextLayout,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        color = null,
        autoSize = autoSize
    )
}

@Composable
fun Text(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle = FontStyle.Normal,
    fontWeight: FontWeight = FontWeight.Normal,
    lineHeightFactor: Float = 1.5f,
    lineHeight: TextUnit = if (fontSize.isUnspecified) TextUnit.Unspecified else fontSize * lineHeightFactor,
    lineHeightStyle: LineHeightStyle = defaultLineHeightStyle,
    textAlign: TextAlign = TextAlign.Unspecified,
    shadow: Shadow? = null,
    style: TextStyle = LocalTextStyle.current,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    maxLines: Int = 1,
    minLines: Int = 1,
    softWrap: Boolean = maxLines > 1,
    inlineContent: Map<String, InlineTextContent> = mapOf(),
) {
    require(lineHeightFactor >= 1f) { "lineHeightFactor should be greater than or equal to 1f" }
    val mergeStyle = style.merge(
        color = color.takeOrElse { LocalContentColor.current },
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        lineHeight = lineHeight,
        lineHeightStyle = lineHeightStyle,
        textAlign = textAlign,
        shadow = shadow,
    )
    val autoSize = remember(mergeStyle.fontSize, mergeStyle.lineHeight, lineHeightFactor) {
        TextAutoSizeByLineHeight(mergeStyle.fontSize, mergeStyle.lineHeight, lineHeightFactor)
    }
    BasicText(
        text = text,
        modifier = modifier,
        style = mergeStyle,
        onTextLayout = onTextLayout,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        inlineContent = inlineContent,
        color = null,
        autoSize = autoSize
    )
}

@Composable
fun TextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    inputTransformation: InputTransformation? = null,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle = FontStyle.Normal,
    fontWeight: FontWeight = FontWeight.Normal,
    lineHeightFactor: Float = 1.5f,
    lineHeight: TextUnit = if (fontSize.isUnspecified) TextUnit.Unspecified else fontSize * lineHeightFactor,
    lineHeightStyle: LineHeightStyle = defaultLineHeightStyle,
    textAlign: TextAlign = TextAlign.Unspecified,
    shadow: Shadow? = null,
    textStyle: TextStyle = LocalTextStyle.current,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.Default,
    onTextLayout: (Density.(getResult: () -> TextLayoutResult?) -> Unit)? = null,
    interactionSource: MutableInteractionSource? = null,
    cursorBrush: Brush = SolidColor(Color.Black),
    outputTransformation: OutputTransformation? = null,
    decorator: TextFieldDecorator? = null,
    scrollState: ScrollState = rememberScrollState(),
) {
    require(lineHeightFactor >= 1f) { "lineHeightFactor should be greater than or equal to 1f" }
    val mergeTextStyle = textStyle.merge(
        color = color.takeOrElse { LocalContentColor.current },
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        lineHeight = lineHeight,
        lineHeightStyle = lineHeightStyle,
        textAlign = textAlign,
        shadow = shadow,
    )
    BasicTextField(
        state = state,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        inputTransformation = inputTransformation,
        textStyle = mergeTextStyle,
        keyboardOptions = keyboardOptions,
        onKeyboardAction = onKeyboardAction,
        lineLimits = lineLimits,
        onTextLayout = onTextLayout,
        interactionSource = interactionSource,
        cursorBrush = cursorBrush,
        outputTransformation = outputTransformation,
        decorator = decorator,
        scrollState = scrollState,
    )
}

val defaultLineHeightStyle = LineHeightStyle(
    LineHeightStyle.Alignment.Center,
    LineHeightStyle.Trim.None
)

@Immutable
private data class TextAutoSizeByLineHeight(
    @Stable val fontSize: TextUnit,
    @Stable val lineHeight: TextUnit,
    @Stable val lineHeightFactor: Float,
) : TextAutoSize {
    override fun TextAutoSizeLayoutScope.getFontSize(
        constraints: Constraints,
        text: AnnotatedString
    ): TextUnit {
        val useFontSize = fontSize.isSpecified && fontSize >= FONT_SIZE_MIN
        val useLineHeight = lineHeight.isSpecified && lineHeight >= FONT_SIZE_MIN

        val fontSize = if (useFontSize) fontSize else FONT_SIZE_DEFAULT
        val lineHeight = if (useLineHeight) lineHeight else fontSize * lineHeightFactor

        val fontSizePx = fontSize.toPx()
        val maxFontSizePx =
            min(lineHeight.roundToPx(), constraints.maxHeight) / lineHeightFactor.coerceAtLeast(1f)

        val autoSizePx = fontSizePx.coerceIn(FONT_SIZE_MIN.toPx(), maxFontSizePx)
        return autoSizePx.toSp()
    }

    companion object {
        private val FONT_SIZE_MIN = 0.sp
        private val FONT_SIZE_DEFAULT = 14.sp
    }
}