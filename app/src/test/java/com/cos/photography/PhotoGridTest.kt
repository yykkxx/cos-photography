package com.cos.photography

import org.junit.Assert.assertEquals
import org.junit.Test

class PhotoGridTest {

    @Test
    fun gridCellIndexMapsToCoordinates() {
        val gridSize = MainActivity.PhotoGridConfig.GRID_SIZE
        val cellSize = MainActivity.PhotoGridConfig.CELL_SIZE
        val cases = listOf(
            0 to (0 to 0),
            5 to (2 * cellSize to cellSize),
            8 to (2 * cellSize to 2 * cellSize)
        )

        cases.forEach { (index, expected) ->
            val expectedX = (index % gridSize) * cellSize
            val expectedY = (index / gridSize) * cellSize
            assertEquals(expected.first, expectedX)
            assertEquals(expected.second, expectedY)
        }
    }
}
