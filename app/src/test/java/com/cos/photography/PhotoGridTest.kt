package com.cos.photography

import org.junit.Assert.assertEquals
import org.junit.Test

class PhotoGridTest {

    @Test
    fun gridCellIndexMapsToCoordinates() {
        val cases = listOf(
            0 to (0 to 0),
            5 to (2 * MainActivity.PhotoGridConfig.CELL_SIZE to MainActivity.PhotoGridConfig.CELL_SIZE),
            8 to (
                2 * MainActivity.PhotoGridConfig.CELL_SIZE to
                    2 * MainActivity.PhotoGridConfig.CELL_SIZE
            )
        )

        cases.forEach { (index, expected) ->
            val expectedX = (index % MainActivity.PhotoGridConfig.GRID_SIZE) *
                MainActivity.PhotoGridConfig.CELL_SIZE
            val expectedY = (index / MainActivity.PhotoGridConfig.GRID_SIZE) *
                MainActivity.PhotoGridConfig.CELL_SIZE
            assertEquals(expected.first, expectedX)
            assertEquals(expected.second, expectedY)
        }
    }
}
