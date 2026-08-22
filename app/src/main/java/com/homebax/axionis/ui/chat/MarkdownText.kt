package com.homebax.axionis.ui.chat

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    fontSize: TextUnit = 16.sp
) {
    Text(
        text = parseMarkdown(text),
        modifier = modifier,
        color = color,
        fontSize = fontSize
    )
}

fun parseMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var currentText = text
        
        // This is a very basic markdown parser for demonstration
        // Handling **bold**, *italic*, and `code`
        
        val boldRegex = Regex("\\*\\*(.*?)\\*\\*")
        val italicRegex = Regex("\\*(.*?)\\*")
        val codeRegex = Regex("`(.*?)`")
        
        // Simplified approach: split by parts and apply styles
        // In a real app, a more robust parser would be used
        
        var lastIndex = 0
        val allMatches = mutableListOf<Triple<IntRange, SpanStyle, String>>()
        
        boldRegex.findAll(text).forEach { allMatches.add(Triple(it.range, SpanStyle(fontWeight = FontWeight.Bold), it.groupValues[1])) }
        italicRegex.findAll(text).forEach { 
            // Avoid matching bold as italic
            if (!it.value.startsWith("**")) {
                allMatches.add(Triple(it.range, SpanStyle(fontStyle = FontStyle.Italic), it.groupValues[1])) 
            }
        }
        codeRegex.findAll(text).forEach { allMatches.add(Triple(it.range, SpanStyle(fontFamily = FontFamily.Monospace, background = Color.Gray.copy(alpha = 0.3f)), it.groupValues[1])) }
        
        allMatches.sortBy { it.first.first }
        
        var currentIdx = 0
        for (match in allMatches) {
            if (match.first.first > currentIdx) {
                append(text.substring(currentIdx, match.first.first))
            }
            withStyle(match.second) {
                append(match.third)
            }
            currentIdx = match.first.last + 1
        }
        if (currentIdx < text.length) {
            append(text.substring(currentIdx))
        }
        
        if (allMatches.isEmpty()) {
            append(text)
        }
    }
}
