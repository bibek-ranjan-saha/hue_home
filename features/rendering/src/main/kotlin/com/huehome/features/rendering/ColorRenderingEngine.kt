package com.huehome.features.rendering

import android.graphics.Bitmap
import android.opengl.GLES30
import com.huehome.core.domain.model.LabColor
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OpenGL ES rendering engine for applying colors to AR objects
 * Preserves texture, shadows, and adapts to lighting conditions
 */
@Singleton
class ColorRenderingEngine @Inject constructor() {
    
    private var shaderProgram: Int = 0
    private var vertexShader: Int = 0
    private var fragmentShader: Int = 0
    
    // Shader attribute/uniform locations
    private var positionHandle: Int = 0
    private var texCoordHandle: Int = 0
    private var textureHandle: Int = 0
    private var maskHandle: Int = 0
    private var originalColorHandle: Int = 0
    private var targetColorHandle: Int = 0
    private var lightIntensityHandle: Int = 0
    private var blendFactorHandle: Int = 0
    
    /**
     * Initialize OpenGL shaders
     */
    fun initialize() {
        // Compile vertex shader
        vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, VERTEX_SHADER_CODE)
        
        // Compile fragment shader
        fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE)
        
        // Create shader program
        shaderProgram = GLES30.glCreateProgram().also {
            GLES30.glAttachShader(it, vertexShader)
            GLES30.glAttachShader(it, fragmentShader)
            GLES30.glLinkProgram(it)
            
            // Check for linking errors
            val linkStatus = IntArray(1)
            GLES30.glGetProgramiv(it, GLES30.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] == 0) {
                val error = GLES30.glGetProgramInfoLog(it)
                GLES30.glDeleteProgram(it)
                throw RenderingException("Failed to link shader program: $error")
            }
        }
        
        // Get attribute/uniform locations
        positionHandle = GLES30.glGetAttribLocation(shaderProgram, "aPosition")
        texCoordHandle = GLES30.glGetAttribLocation(shaderProgram, "aTexCoord")
        textureHandle = GLES30.glGetUniformLocation(shaderProgram, "uTexture")
        maskHandle = GLES30.glGetUniformLocation(shaderProgram, "uMask")
        originalColorHandle = GLES30.glGetUniformLocation(shaderProgram, "uOriginalColor")
        targetColorHandle = GLES30.glGetUniformLocation(shaderProgram, "uTargetColor")
        lightIntensityHandle = GLES30.glGetUniformLocation(shaderProgram, "uLightIntensity")
        blendFactorHandle = GLES30.glGetUniformLocation(shaderProgram, "uBlendFactor")
    }
    
    /**
     * Render colored object
     */
    fun renderColoredObject(
        texture: Int,
        mask: Int,
        originalColor: LabColor,
        targetColor: LabColor,
        lightIntensity: Float,
        blendFactor: Float = 1.0f
    ) {
        // Use shader program
        GLES30.glUseProgram(shaderProgram)
        
        // Set up vertex data (full screen quad)
        val vertices = floatArrayOf(
            -1f, -1f, 0f,  // Bottom left
             1f, -1f, 0f,  // Bottom right
            -1f,  1f, 0f,  // Top left
             1f,  1f, 0f   // Top right
        )
        
        val texCoords = floatArrayOf(
            0f, 1f,  // Bottom left
            1f, 1f,  // Bottom right
            0f, 0f,  // Top left
            1f, 0f   // Top right
        )
        
        val vertexBuffer = createFloatBuffer(vertices)
        val texCoordBuffer = createFloatBuffer(texCoords)
        
        // Set vertex positions
        GLES30.glEnableVertexAttribArray(positionHandle)
        GLES30.glVertexAttribPointer(positionHandle, 3, GLES30.GL_FLOAT, false, 0, vertexBuffer)
        
        // Set texture coordinates
        GLES30.glEnableVertexAttribArray(texCoordHandle)
        GLES30.glVertexAttribPointer(texCoordHandle, 2, GLES30.GL_FLOAT, false, 0, texCoordBuffer)
        
        // Bind textures
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture)
        GLES30.glUniform1i(textureHandle, 0)
        
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mask)
        GLES30.glUniform1i(maskHandle, 1)
        
        // Set color uniforms (convert LAB to RGB for shader)
        val originalRgb = labToRgbFloat(originalColor)
        val targetRgb = labToRgbFloat(targetColor)
        
        GLES30.glUniform3fv(originalColorHandle, 1, originalRgb, 0)
        GLES30.glUniform3fv(targetColorHandle, 1, targetRgb, 0)
        
        // Set lighting and blend factor
        GLES30.glUniform1f(lightIntensityHandle, lightIntensity)
        GLES30.glUniform1f(blendFactorHandle, blendFactor)
        
        // Draw quad
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
        
        // Cleanup
        GLES30.glDisableVertexAttribArray(positionHandle)
        GLES30.glDisableVertexAttribArray(texCoordHandle)
    }
    
    /**
     * Load and compile shader
     */
    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES30.glCreateShader(type).also { shader ->
            GLES30.glShaderSource(shader, shaderCode)
            GLES30.glCompileShader(shader)
            
            // Check for compilation errors
            val compileStatus = IntArray(1)
            GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compileStatus, 0)
            if (compileStatus[0] == 0) {
                val error = GLES30.glGetShaderInfoLog(shader)
                GLES30.glDeleteShader(shader)
                throw RenderingException("Failed to compile shader: $error")
            }
        }
    }
    
    /**
     * Convert LAB color to RGB float array
     */
    private fun labToRgbFloat(lab: LabColor): FloatArray {
        val rgb = lab.toRgb()
        return floatArrayOf(
            ((rgb shr 16) and 0xFF) / 255f,
            ((rgb shr 8) and 0xFF) / 255f,
            (rgb and 0xFF) / 255f
        )
    }
    
    /**
     * Create float buffer from array
     */
    private fun createFloatBuffer(data: FloatArray): FloatBuffer {
        return ByteBuffer.allocateDirect(data.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(data)
                position(0)
            }
        }
    }
    
    /**
     * Release resources
     */
    fun release() {
        if (shaderProgram != 0) {
            GLES30.glDeleteProgram(shaderProgram)
            shaderProgram = 0
        }
        if (vertexShader != 0) {
            GLES30.glDeleteShader(vertexShader)
            vertexShader = 0
        }
        if (fragmentShader != 0) {
            GLES30.glDeleteShader(fragmentShader)
            fragmentShader = 0
        }
    }
    
    companion object {
        /**
         * Vertex shader - simple pass-through
         */
        private const val VERTEX_SHADER_CODE = """
            #version 300 es
            
            in vec3 aPosition;
            in vec2 aTexCoord;
            
            out vec2 vTexCoord;
            
            void main() {
                gl_Position = vec4(aPosition, 1.0);
                vTexCoord = aTexCoord;
            }
        """
        
        /**
         * Fragment shader - color replacement with texture/shadow preservation
         */
        private const val FRAGMENT_SHADER_CODE = """
            #version 300 es
            precision highp float;
            
            in vec2 vTexCoord;
            out vec4 fragColor;
            
            uniform sampler2D uTexture;
            uniform sampler2D uMask;
            uniform vec3 uOriginalColor;
            uniform vec3 uTargetColor;
            uniform float uLightIntensity;
            uniform float uBlendFactor;
            
            // Convert RGB to LAB color space
            vec3 rgbToLab(vec3 rgb) {
                // First convert to XYZ
                float r = rgb.r > 0.04045 ? pow((rgb.r + 0.055) / 1.055, 2.4) : rgb.r / 12.92;
                float g = rgb.g > 0.04045 ? pow((rgb.g + 0.055) / 1.055, 2.4) : rgb.g / 12.92;
                float b = rgb.b > 0.04045 ? pow((rgb.b + 0.055) / 1.055, 2.4) : rgb.b / 12.92;
                
                float x = r * 0.4124 + g * 0.3576 + b * 0.1805;
                float y = r * 0.2126 + g * 0.7152 + b * 0.0722;
                float z = r * 0.0193 + g * 0.1192 + b * 0.9505;
                
                // Then XYZ to LAB
                x /= 0.95047;
                z /= 1.08883;
                
                x = x > 0.008856 ? pow(x, 1.0/3.0) : (7.787 * x + 16.0/116.0);
                y = y > 0.008856 ? pow(y, 1.0/3.0) : (7.787 * y + 16.0/116.0);
                z = z > 0.008856 ? pow(z, 1.0/3.0) : (7.787 * z + 16.0/116.0);
                
                float l = (116.0 * y) - 16.0;
                float a = 500.0 * (x - y);
                float bVal = 200.0 * (y - z);
                
                return vec3(l / 100.0, (a + 128.0) / 255.0, (bVal + 128.0) / 255.0);
            }
            
            // Convert LAB to RGB color space
            vec3 labToRgb(vec3 lab) {
                float l = lab.x * 100.0;
                float a = lab.y * 255.0 - 128.0;
                float bVal = lab.z * 255.0 - 128.0;
                
                float y = (l + 16.0) / 116.0;
                float x = a / 500.0 + y;
                float z = y - bVal / 200.0;
                
                x = x * x * x > 0.008856 ? x * x * x : (x - 16.0/116.0) / 7.787;
                y = y * y * y > 0.008856 ? y * y * y : (y - 16.0/116.0) / 7.787;
                z = z * z * z > 0.008856 ? z * z * z : (z - 16.0/116.0) / 7.787;
                
                x *= 0.95047;
                z *= 1.08883;
                
                float r = x *  3.2406 + y * -1.5372 + z * -0.4986;
                float g = x * -0.9689 + y *  1.8758 + z *  0.0415;
                float bRgb = x *  0.0557 + y * -0.2040 + z *  1.0570;
                
                r = r > 0.0031308 ? 1.055 * pow(r, 1.0/2.4) - 0.055 : 12.92 * r;
                g = g > 0.0031308 ? 1.055 * pow(g, 1.0/2.4) - 0.055 : 12.92 * g;
                bRgb = bRgb > 0.0031308 ? 1.055 * pow(bRgb, 1.0/2.4) - 0.055 : 12.92 * bRgb;
                
                return clamp(vec3(r, g, bRgb), 0.0, 1.0);
            }
            
            void main() {
                // Sample textures
                vec4 texColor = texture(uTexture, vTexCoord);
                float maskValue = texture(uMask, vTexCoord).r;
                
                // If not in mask, use original texture
                if (maskValue < 0.5) {
                    fragColor = texColor;
                    return;
                }
                
                // Convert to LAB for perceptual color manipulation
                vec3 texLab = rgbToLab(texColor.rgb);
                vec3 originalLab = rgbToLab(uOriginalColor);
                vec3 targetLab = rgbToLab(uTargetColor);
                
                // Calculate luminance difference (preserve shadows/highlights)
                float luminanceDiff = texLab.x - originalLab.x;
                
                // Apply target color while preserving luminance variation
                vec3 newLab = targetLab;
                newLab.x += luminanceDiff;
                newLab.x = clamp(newLab.x, 0.0, 1.0);
                
                // Adapt to lighting conditions
                newLab.x *= uLightIntensity;
                
                // Convert back to RGB
                vec3 newRgb = labToRgb(newLab);
                
                // Blend with original based on blend factor
                vec3 finalRgb = mix(texColor.rgb, newRgb, uBlendFactor);
                
                fragColor = vec4(finalRgb, texColor.a);
            }
        """
    }
}

/**
 * Exception thrown when rendering fails
 */
class RenderingException(message: String, cause: Throwable? = null) : Exception(message, cause)
