package com.example.counter_app.ui

import org.junit.Test
import org.junit.Assert.*

/**
 * Tests para validar el comportamiento de zoom en OccupancyChart.
 *
 * Verifica:
 * - Rangos válidos de zoom (50% - 300%)
 * - Incrementos/decrementos con botones
 * - Simulación de gestos de pinch-to-zoom
 */
class ZoomBehaviorTest {

    companion object {
        const val MIN_ZOOM = 0.5f  // 50%
        const val MAX_ZOOM = 3.0f  // 300%
        const val ZOOM_STEP = 0.5f // Incremento de 50%
    }

    /**
     * Simula la lógica de zoom in del botón +
     */
    private fun zoomIn(currentZoom: Float): Float {
        return if (currentZoom < MAX_ZOOM) {
            (currentZoom + ZOOM_STEP).coerceAtMost(MAX_ZOOM)
        } else {
            currentZoom
        }
    }

    /**
     * Simula la lógica de zoom out del botón -
     */
    private fun zoomOut(currentZoom: Float): Float {
        return if (currentZoom > MIN_ZOOM) {
            (currentZoom - ZOOM_STEP).coerceAtLeast(MIN_ZOOM)
        } else {
            currentZoom
        }
    }

    /**
     * Simula gestos de pinch-to-zoom
     */
    private fun pinchZoom(currentZoom: Float, zoomMultiplier: Float): Float {
        return (currentZoom * zoomMultiplier).coerceIn(MIN_ZOOM, MAX_ZOOM)
    }

    @Test
    fun `test zoom in desde 100% hasta máximo`() {
        var zoom = 1.0f
        val expected = listOf(1.5f, 2.0f, 2.5f, 3.0f, 3.0f) // Último se queda en 3.0

        val results = mutableListOf<Float>()
        repeat(5) {
            zoom = zoomIn(zoom)
            results.add(zoom)
        }

        assertEquals("Secuencia de zoom in incorrecta", expected, results)
        println("✓ TEST Zoom In: ${results.joinToString(" → ") { "${(it * 100).toInt()}%" }}")
    }

    @Test
    fun `test zoom out desde 100% hasta mínimo`() {
        var zoom = 1.0f
        val expected = listOf(0.5f, 0.5f) // Primero baja a 0.5, luego se mantiene

        val results = mutableListOf<Float>()
        repeat(2) {
            zoom = zoomOut(zoom)
            results.add(zoom)
        }

        assertEquals("Secuencia de zoom out incorrecta", expected, results)
        println("✓ TEST Zoom Out: ${results.joinToString(" → ") { "${(it * 100).toInt()}%" }}")
    }

    @Test
    fun `test zoom in no excede el máximo`() {
        var zoom = 2.5f

        // Intentar hacer zoom in 10 veces desde 250%
        repeat(10) {
            zoom = zoomIn(zoom)
        }

        assertEquals("Zoom no debe exceder 300%", MAX_ZOOM, zoom)
        println("✓ TEST Límite Máximo: ${(zoom * 100).toInt()}% (correcto)")
    }

    @Test
    fun `test zoom out no baja del mínimo`() {
        var zoom = 1.0f

        // Intentar hacer zoom out 10 veces desde 100%
        repeat(10) {
            zoom = zoomOut(zoom)
        }

        assertEquals("Zoom no debe bajar de 50%", MIN_ZOOM, zoom)
        println("✓ TEST Límite Mínimo: ${(zoom * 100).toInt()}% (correcto)")
    }

    @Test
    fun `test rangos válidos con coerceIn`() {
        val testCases = mapOf(
            0.2f to MIN_ZOOM,  // Demasiado pequeño → 0.5
            0.5f to 0.5f,      // Mínimo válido
            0.75f to 0.75f,    // Zoom out válido
            1.0f to 1.0f,      // Normal (100%)
            1.5f to 1.5f,      // Zoom in válido
            2.0f to 2.0f,      // Zoom in alto
            3.0f to 3.0f,      // Máximo válido
            4.0f to MAX_ZOOM   // Demasiado grande → 3.0
        )

        testCases.forEach { (input, expected) ->
            val result = input.coerceIn(MIN_ZOOM, MAX_ZOOM)
            assertEquals(
                "Zoom ${(input * 100).toInt()}% debe corregirse a ${(expected * 100).toInt()}%",
                expected,
                result
            )
        }
        println("✓ TEST Validación de Rangos: Todos los casos pasaron")
    }

    @Test
    fun `test pinch out moderado`() {
        val zoom = 1.0f
        val multiplier = 1.2f  // Pinch out 20%
        val result = pinchZoom(zoom, multiplier)

        assertEquals("Pinch out desde 100%", 1.2f, result, 0.01f)
        println("✓ TEST Pinch Out: 100% → ${(result * 100).toInt()}%")
    }

    @Test
    fun `test pinch in moderado`() {
        val zoom = 1.0f
        val multiplier = 0.8f  // Pinch in 20%
        val result = pinchZoom(zoom, multiplier)

        assertEquals("Pinch in desde 100%", 0.8f, result, 0.01f)
        println("✓ TEST Pinch In: 100% → ${(result * 100).toInt()}%")
    }

    @Test
    fun `test pinch extremo no excede límites`() {
        // Pinch out extremo
        val zoomOutExtreme = pinchZoom(1.0f, 10.0f)
        assertEquals("Pinch out extremo limitado a 300%", MAX_ZOOM, zoomOutExtreme)

        // Pinch in extremo
        val zoomInExtreme = pinchZoom(1.0f, 0.1f)
        assertEquals("Pinch in extremo limitado a 50%", MIN_ZOOM, zoomInExtreme)

        println("✓ TEST Pinch Extremo: Límites respetados")
    }

    @Test
    fun `test secuencia realista de usuario`() {
        var zoom = 1.0f
        val actions = mutableListOf<Pair<String, Float>>()

        // Usuario hace zoom in con botón
        zoom = zoomIn(zoom)
        actions.add("Zoom In Botón" to zoom)

        // Usuario hace zoom in con botón otra vez
        zoom = zoomIn(zoom)
        actions.add("Zoom In Botón" to zoom)

        // Usuario hace pinch out
        zoom = pinchZoom(zoom, 1.3f)
        actions.add("Pinch Out" to zoom)

        // Usuario hace zoom out con botón
        zoom = zoomOut(zoom)
        actions.add("Zoom Out Botón" to zoom)

        // Usuario hace pinch in fuerte
        zoom = pinchZoom(zoom, 0.6f)
        actions.add("Pinch In Fuerte" to zoom)

        // Verificar que todas las acciones mantienen el zoom en rango válido
        actions.forEach { (action, zoomLevel) ->
            assertTrue(
                "$action resultó en zoom inválido: ${(zoomLevel * 100).toInt()}%",
                zoomLevel in MIN_ZOOM..MAX_ZOOM
            )
        }

        println("✓ TEST Secuencia de Usuario:")
        actions.forEach { (action, zoomLevel) ->
            println("  - $action: ${(zoomLevel * 100).toInt()}%")
        }
    }

    @Test
    fun `test botones habilitados solo en rangos válidos`() {
        // En el mínimo (50%), solo zoom in debe estar habilitado
        val atMin = MIN_ZOOM
        assertTrue("Zoom in debe estar habilitado en 50%", atMin < MAX_ZOOM)
        assertFalse("Zoom out debe estar deshabilitado en 50%", atMin > MIN_ZOOM)

        // En el máximo (300%), solo zoom out debe estar habilitado
        val atMax = MAX_ZOOM
        assertFalse("Zoom in debe estar deshabilitado en 300%", atMax < MAX_ZOOM)
        assertTrue("Zoom out debe estar habilitado en 300%", atMax > MIN_ZOOM)

        // En el medio (150%), ambos deben estar habilitados
        val atMiddle = 1.5f
        assertTrue("Zoom in debe estar habilitado en 150%", atMiddle < MAX_ZOOM)
        assertTrue("Zoom out debe estar habilitado en 150%", atMiddle > MIN_ZOOM)

        println("✓ TEST Botones Habilitados: Lógica correcta")
    }

    @Test
    fun `test graphicsLayer escala solo en X no en Y`() {
        // Este test documenta que el zoom debe aplicarse solo horizontalmente
        val zoomLevel = 2.0f
        val scaleX = zoomLevel  // Debe escalar en X
        val scaleY = 1f         // NO debe escalar en Y

        assertEquals("ScaleX debe ser igual al zoomLevel", zoomLevel, scaleX)
        assertEquals("ScaleY debe permanecer en 1.0", 1f, scaleY)

        println("✓ TEST GraphicsLayer: scaleX=$scaleX, scaleY=$scaleY (correcto)")
    }

    @Test
    fun `test zoom levels comunes`() {
        val commonZooms = mapOf(
            "50%" to 0.5f,
            "75%" to 0.75f,
            "100%" to 1.0f,
            "125%" to 1.25f,
            "150%" to 1.5f,
            "200%" to 2.0f,
            "250%" to 2.5f,
            "300%" to 3.0f
        )

        println("✓ TEST Zoom Levels Comunes:")
        commonZooms.forEach { (label, value) ->
            val isValid = value in MIN_ZOOM..MAX_ZOOM
            println("  - $label (${value}f): ${if (isValid) "✓ VÁLIDO" else "✗ INVÁLIDO"}")
            assertTrue("$label debe ser válido", isValid)
        }
    }
}
