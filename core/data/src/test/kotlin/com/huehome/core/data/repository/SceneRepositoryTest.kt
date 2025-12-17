package com.huehome.core.data.repository

import com.huehome.core.data.local.SceneObjectDao
import com.huehome.core.domain.model.*
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SceneRepository
 */
class SceneRepositoryTest {
    
    private lateinit var repository: SceneRepository
    private lateinit var dao: SceneObjectDao
    
    @Before
    fun setup() {
        dao = mockk()
        repository = SceneRepository(dao)
    }
    
    @Test
    fun `getActiveObjects should return flow from DAO`() = runTest {
        val testObjects = listOf(
            createTestObject(id = "1", isActive = true),
            createTestObject(id = "2", isActive = true)
        )
        
        every { dao.getActiveObjects() } returns flowOf(testObjects)
        
        val result = repository.getActiveObjects().first()
        
        assertEquals(2, result.size)
        assertTrue(result.all { it.isActive })
    }
    
    @Test
    fun `saveObject should call DAO insert`() = runTest {
        val testObject = createTestObject(id = "1")
        
        coEvery { dao.insertObject(any()) } just Runs
        
        repository.saveObject(testObject)
        
        coVerify { dao.insertObject(testObject) }
    }
    
    @Test
    fun `toggleObject should flip isActive state`() = runTest {
        val testObject = createTestObject(id = "1", isActive = true)
        
        coEvery { dao.getObjectById("1") } returns testObject
        coEvery { dao.setObjectActive("1", false) } just Runs
        
        repository.toggleObject("1")
        
        coVerify { dao.setObjectActive("1", false) }
    }
    
    @Test
    fun `applyColor should update object color`() = runTest {
        val color = 0xFF00FF00.toInt()
        
        coEvery { dao.updateAppliedColor("1", color) } just Runs
        
        repository.applyColor("1", color)
        
        coVerify { dao.updateAppliedColor("1", color) }
    }
    
    @Test
    fun `resetObject should set color to null`() = runTest {
        coEvery { dao.updateAppliedColor("1", null) } just Runs
        
        repository.resetObject("1")
        
        coVerify { dao.updateAppliedColor("1", null) }
    }
    
    @Test
    fun `clearAll should delete all objects`() = runTest {
        coEvery { dao.deleteAll() } just Runs
        
        repository.clearAll()
        
        coVerify { dao.deleteAll() }
    }
    
    private fun createTestObject(
        id: String,
        isActive: Boolean = true
    ) = SceneObject(
        id = id,
        type = ObjectType.WALL,
        detectedColor = ColorInfo(
            rgb = 0xFFFFFFFF.toInt(),
            lab = LabColor(l = 100f, a = 0f, b = 0f),
            confidence = 0.9f
        ),
        boundingBox = floatArrayOf(0f, 0f, 100f, 100f),
        isActive = isActive,
        timestamp = System.currentTimeMillis(),
        userLabel = null,
        appliedColor = null
    )
}
