package com.cos.photography

import org.junit.Assert.assertEquals
import org.junit.Test

class PhotoGridTest {

    @Test
    fun gridCellIndexMapsToCoordinates() {
        val gridSize = 3
        val cellSize = 360
        val index = 5
        val expectedX = (index % gridSize) * cellSize
        val expectedY = (index / gridSize) * cellSize

        assertEquals(720, expectedX)
        assertEquals(360, expectedY)
    }
}
