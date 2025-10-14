package com.mobdeve.s13.martin.elaine.kabu20.live2d

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import com.live2d.sdk.cubism.framework.CubismFramework
import com.live2d.sdk.cubism.framework.CubismFramework.Option
import com.mobdeve.s13.martin.elaine.kabu20.R
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class Live2DRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private var model: MyLive2DModel? = null
    private var surfaceReady = false

    private var vw = 0
    private var vh = 0


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        val c = androidx.core.content.ContextCompat.getColor(context, R.color.KaBu_yellow)
        val r = ((c shr 16) and 0xFF) / 255f
        val g = ((c shr 8) and 0xFF) / 255f
        val b = (c and 0xFF) / 255f
        GLES20.glClearColor(r, g, b, 1f)
        model = MyLive2DModel(context)

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        vw = width
        vh = height

        GLES20.glViewport(0, 0, width, height)
        Log.d("Live2D", "Surface changed: $width x $height")
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        model?.updateModel()
        if(vw > 0 && vh > 0) model?.draw(vw, vh)
    }

    fun playMotion(name: String) {
        // Placeholder motion trigger (to be expanded when motion3.json playback is wired)
        Log.d("Live2D", "🎬 Playing motion: $name")
    }
}
