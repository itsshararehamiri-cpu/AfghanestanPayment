package com.danesh.common

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.asComposePath
import androidx.core.graphics.PathParser

class VectorPathShape(private val pathData: String) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = PathParser.createPathFromPathData(pathData).asComposePath()
        path.transform(androidx.compose.ui.graphics.Matrix().apply {
            scale(size.width / 412f, size.height / 178f)
        })
        return Outline.Generic(path)
    }
}
