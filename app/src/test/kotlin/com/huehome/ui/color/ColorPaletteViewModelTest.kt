package com.huehome.ui.color

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.huehome.core.data.repository.SceneRepository
import com.huehome.core.domain.model.*
import com.huehome.features.color.RecommendationEngine
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for ColorPaletteViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ColorPaletteViewModelTest {
    
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var viewModel: ColorPaletteViewModel
    private lateinit var recommendationEngine: RecommendationEngine
    private lateinit var sceneRepository: SceneRepository
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        recommendationEngine = mockk()
        sceneRepository = mockk()
        
        viewModel = ColorPaletteViewModel(recommendationEngine, sceneRepository)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `loadRecommendations should update state to Success`() = runTest {
        val testObject = createTestObject()
        val testRecs = listOf(createTestRecommendation())
        val roomContext = RoomContext(0.5f, floatArrayOf(1f, 1f, 1f, 1f))
        
        every { 
            recommendationEngine.recommend(any(), any(), any(), any()) 
        } returns testRecs
        
        viewModel.loadRecommendations(testObject, roomContext)
        advanceUntilIdle()
        
        assertEquals(ColorPaletteUiState.Success, viewModel.uiState.value)
        assertEquals(testRecs, viewModel.recommendations.value)
    }
    
    @Test
    fun `selectColor should update selectedColor state`() {
        val testRec = createTestRecommendation()
        
        viewModel.selectColor(testRec)
        
        assertEquals(testRec, viewModel.selectedColor.value)
    }
    
    @Test
    fun `applyColor should call repository and update state`() = runTest {
        val testObject = createTestObject()
        val testRec = createTestRecommendation()
        val roomContext = RoomContext(0.5f, floatArrayOf(1f, 1f, 1f, 1f))
        
        every { recommendationEngine.recommend(any(), any(), any(), any()) } returns listOf(testRec)
        coEvery { sceneRepository.applyColor(any(), any()) } just Runs
        
        viewModel.loadRecommendations(testObject, roomContext)
        advanceUntilIdle()
        
        viewModel.selectColor(testRec)
        viewModel.applyColor()
        advanceUntilIdle()
        
        coVerify { sceneRepository.applyColor(testObject.id, testRec.color) }
        assertEquals(ColorPaletteUiState.Applied, viewModel.uiState.value)
    }
    
    @Test
    fun `undo should restore previous color`() = runTest {
        val testObject = createTestObject(appliedColor = 0xFF0000FF.toInt())
        val testRec1 = createTestRecommendation(color = 0xFF00FF00.toInt())
        val testRec2 = createTestRecommendation(color = 0xFFFF0000.toInt())
        val roomContext = RoomContext(0.5f, floatArrayOf(1f, 1f, 1f, 1f))
        
        every { recommendationEngine.recommend(any(), any(), any(), any()) } returns listOf(testRec1, testRec2)
        coEvery { sceneRepository.applyColor(any(), any()) } just Runs
        
        viewModel.loadRecommendations(testObject, roomContext)
        advanceUntilIdle()
        
        // Apply first color
        viewModel.selectColor(testRec1)
        viewModel.applyColor()
        advanceUntilIdle()
        
        // Apply second color
        viewModel.selectColor(testRec2)
        viewModel.applyColor()
        advanceUntilIdle()
        
        // Undo
        viewModel.undo()
        advanceUntilIdle()
        
        assertEquals(testRec1, viewModel.selectedColor.value)
        assertTrue(viewModel.canRedo.value)
    }
    
    @Test
    fun `togglePreview should flip isPreviewMode`() {
        assertFalse(viewModel.isPreviewMode.value)
        
        viewModel.togglePreview()
        
        assertTrue(viewModel.isPreviewMode.value)
        
        viewModel.togglePreview()
        
        assertFalse(viewModel.isPreviewMode.value)
    }
    
    private fun createTestObject(appliedColor: Int? = null) = SceneObject(
        id = "test-1",
        type = ObjectType.WALL,
        detectedColor = ColorInfo(
            rgb = 0xFFFFFFFF.toInt(),
            lab = LabColor(l = 100f, a = 0f, b = 0f),
            confidence = 0.9f
        ),
        boundingBox = floatArrayOf(0f, 0f, 100f, 100f),
        isActive = true,
        timestamp = System.currentTimeMillis(),
        userLabel = null,
        appliedColor = appliedColor
    )
    
    private fun createTestRecommendation(color: Int = 0xFF00FF00.toInt()) = ColorRecommendation(
        color = color,
        labColor = LabColor(l = 50f, a = 20f, b = 30f),
        reason = "Test recommendation",
        confidence = 0.85f,
        category = RecommendationCategory.COMPLEMENTARY
    )
}
