package com.huehome.features.ar

import android.content.Context
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.exceptions.UnavailableApkTooOldException
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException
import com.huehome.core.domain.ar.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ARCore implementation of ArSession interface
 * Provides mobile AR functionality with plane detection, light estimation, and depth
 */
@Singleton
class ArCoreSession @Inject constructor() : ArSession {
    
    private var session: Session? = null
    private var config: Config? = null
    
    override fun initialize(context: Context, arConfig: ArConfig) {
        try {
            // Check ARCore availability
            when (ArCoreApk.getInstance().requestInstall(context as android.app.Activity, true)) {
                ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                    // ARCore installation requested, will resume when installed
                    return
                }
                ArCoreApk.InstallStatus.INSTALLED -> {
                    // ARCore is installed, continue
                }
            }
            
            // Create ARCore session
            session = Session(context)
            
            // Configure session
            config = Config(session).apply {
                // Plane detection
                if (arConfig.enablePlaneDetection) {
                    planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                } else {
                    planeFindingMode = Config.PlaneFindingMode.DISABLED
                }
                
                // Depth
                if (arConfig.enableDepth) {
                    depthMode = Config.DepthMode.AUTOMATIC
                }
                
                // Light estimation
                if (arConfig.enableLightEstimation) {
                    lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                }
                
                // Focus mode
                focusMode = when (arConfig.focusMode) {
                    FocusMode.AUTO -> Config.FocusMode.AUTO
                    FocusMode.FIXED -> Config.FocusMode.FIXED
                }
            }
            
            session?.configure(config)
            
        } catch (e: UnavailableArcoreNotInstalledException) {
            throw ArSessionException("ARCore is not installed", e)
        } catch (e: UnavailableApkTooOldException) {
            throw ArSessionException("ARCore APK is too old", e)
        } catch (e: UnavailableSdkTooOldException) {
            throw ArSessionException("ARCore SDK is too old", e)
        } catch (e: UnavailableDeviceNotCompatibleException) {
            throw ArSessionException("Device is not compatible with ARCore", e)
        } catch (e: Exception) {
            throw ArSessionException("Failed to initialize ARCore session", e)
        }
    }
    
    override fun update(): ArFrame? {
        return try {
            val frame = session?.update() ?: return null
            
            // Convert ARCore frame to domain model
            val cameraPose = frame.camera.pose
            ArFrame(
                timestamp = frame.timestamp,
                cameraPose = Pose(
                    tx = cameraPose.tx(),
                    ty = cameraPose.ty(),
                    tz = cameraPose.tz(),
                    qx = cameraPose.qx(),
                    qy = cameraPose.qy(),
                    qz = cameraPose.qz(),
                    qw = cameraPose.qw()
                )
            )
        } catch (e: Exception) {
            null
        }
    }
    
    override fun detectPlanes(): List<ArPlane> {
        val session = session ?: return emptyList()
        
        return try {
            session.getAllTrackables(com.google.ar.core.Plane::class.java)
                .filter { it.trackingState == com.google.ar.core.TrackingState.TRACKING }
                .map { plane ->
                    val pose = plane.centerPose
                    ArPlane(
                        id = plane.hashCode().toString(),
                        type = when (plane.type) {
                            com.google.ar.core.Plane.Type.HORIZONTAL_UPWARD_FACING -> 
                                PlaneType.HORIZONTAL_UPWARD
                            com.google.ar.core.Plane.Type.HORIZONTAL_DOWNWARD_FACING -> 
                                PlaneType.HORIZONTAL_DOWNWARD
                            com.google.ar.core.Plane.Type.VERTICAL -> 
                                PlaneType.VERTICAL
                        },
                        centerPose = Pose(
                            tx = pose.tx(),
                            ty = pose.ty(),
                            tz = pose.tz(),
                            qx = pose.qx(),
                            qy = pose.qy(),
                            qz = pose.qz(),
                            qw = pose.qw()
                        ),
                        extentX = plane.extentX,
                        extentZ = plane.extentZ
                    )
                }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override fun estimateLight(): LightEstimate {
        val session = session ?: return LightEstimate(0.5f)
        
        return try {
            val frame = session.update()
            val lightEstimate = frame.lightEstimate
            
            LightEstimate(
                pixelIntensity = lightEstimate.pixelIntensity
            )
        } catch (e: Exception) {
            LightEstimate(0.5f)
        }
    }
    
    override fun getDepthData(): DepthData? {
        val session = session ?: return null
        
        return try {
            val frame = session.update()
            val depthImage = frame.acquireDepthImage16Bits()
            
            val width = depthImage.width
            val height = depthImage.height
            val buffer = depthImage.planes[0].buffer
            
            val data = FloatArray(width * height)
            for (i in 0 until width * height) {
                data[i] = buffer.getShort(i * 2).toFloat() / 1000f // Convert mm to meters
            }
            
            depthImage.close()
            
            DepthData(width, height, data)
        } catch (e: Exception) {
            null
        }
    }
    
    override fun performRaycast(x: Float, y: Float): ArHitResult? {
        val session = session ?: return null
        
        return try {
            val frame = session.update()
            val hits = frame.hitTest(x, y)
            
            hits.firstOrNull()?.let { hit ->
                val pose = hit.hitPose
                val trackable = hit.trackable
                
                ArHitResult(
                    hitPose = Pose(
                        tx = pose.tx(),
                        ty = pose.ty(),
                        tz = pose.tz(),
                        qx = pose.qx(),
                        qy = pose.qy(),
                        qz = pose.qz(),
                        qw = pose.qw()
                    ),
                    distance = hit.distance,
                    plane = if (trackable is com.google.ar.core.Plane) {
                        ArPlane(
                            id = trackable.hashCode().toString(),
                            type = when (trackable.type) {
                                com.google.ar.core.Plane.Type.HORIZONTAL_UPWARD_FACING -> 
                                    PlaneType.HORIZONTAL_UPWARD
                                com.google.ar.core.Plane.Type.HORIZONTAL_DOWNWARD_FACING -> 
                                    PlaneType.HORIZONTAL_DOWNWARD
                                com.google.ar.core.Plane.Type.VERTICAL -> 
                                    PlaneType.VERTICAL
                            },
                            centerPose = Pose(
                                tx = trackable.centerPose.tx(),
                                ty = trackable.centerPose.ty(),
                                tz = trackable.centerPose.tz(),
                                qx = trackable.centerPose.qx(),
                                qy = trackable.centerPose.qy(),
                                qz = trackable.centerPose.qz(),
                                qw = trackable.centerPose.qw()
                            ),
                            extentX = trackable.extentX,
                            extentZ = trackable.extentZ
                        )
                    } else null
                )
            }
        } catch (e: Exception) {
            null
        }
    }
    
    override fun pause() {
        session?.pause()
    }
    
    override fun resume() {
        // Session resume is handled in initialize
    }
    
    override fun release() {
        session?.close()
        session = null
        config = null
    }
}

/**
 * Exception thrown when ARCore session initialization or operation fails
 */
class ArSessionException(message: String, cause: Throwable? = null) : Exception(message, cause)
