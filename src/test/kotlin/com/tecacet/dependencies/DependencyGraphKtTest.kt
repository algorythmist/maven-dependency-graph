package com.tecacet.dependencies

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

internal class DependencyGraphKtTest {

    @Test
    fun loadPoms() {
        val models = loadPoms(".")
        assertEquals(1, models.size)
        val model = models.values.first()
        assertEquals(7, model.dependencies.size)
    }
}
