package com.mobdeve.s13.martin.elaine.kabu20.live2d

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Log
import com.live2d.sdk.cubism.framework.CubismModelSettingJson
import com.live2d.sdk.cubism.framework.math.CubismMatrix44
import com.live2d.sdk.cubism.framework.model.CubismUserModel
import com.live2d.sdk.cubism.framework.rendering.CubismRenderer
import com.live2d.sdk.cubism.framework.rendering.android.CubismRendererAndroid

class MyLive2DModel(private val context: Context) : CubismUserModel() {

    private val projectionMatrix = CubismMatrix44.create()
    private var renderer: CubismRenderer? = null // 👈 use base renderer type

    init {
        try {
            val jsonBuffer = context.assets.open("Live2D/Kabu/kabu_base_model.model3.json").readBytes()
            val setting = CubismModelSettingJson(jsonBuffer)

            val mocFileName = setting.getModelFileName()
            val mocBuffer = context.assets.open("Live2D/Kabu/$mocFileName").readBytes()
            loadModel(mocBuffer)

            renderer = MyCubismRenderer()
            renderer?.initialize(model)

            val myRenderer = renderer as MyCubismRenderer
            for (i in 0 until setting.textureCount) {
                val textureName = setting.getTextureFileName(i)
                val textureId = loadTexture("Live2D/Kabu/$textureName")
                myRenderer.bindTextureSafe(i, textureId)
            }



            Log.d("Live2D", "Model loaded successfully.")
        } catch (e: Exception) {
            Log.e("Live2D", "Error loading model: ${e.message}")
        }
    }

    private fun loadTexture(path: String): Int {
        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)
        val textureId = textureIds[0]

        val bitmap = BitmapFactory.decodeStream(context.assets.open(path))
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        bitmap.recycle()
        return textureId
    }

    fun updateModel() {
        model?.update()
        model?.saveParameters()
    }

    // MyLive2DModel.kt
    fun draw(width: Int, height: Int) {
        val m = model ?: return
        val r = renderer as? CubismRendererAndroid ?: return

        // 1) Build projection in *model space* (no pixels)
        val cw = if (m.canvasWidth  > 0f) m.canvasWidth  else 1f
        val ch = if (m.canvasHeight > 0f) m.canvasHeight else 1f
        val base = maxOf(width / cw.toFloat(), height / ch.toFloat())

        val fill = base * 1.03f

        projectionMatrix.loadIdentity()

        // Map model canvas to clip space (roughly -1..1)
        // Scale so cw,ch become ~2.0 in clip space
        projectionMatrix.scale(fill, fill)

        // Correct for the view aspect so circles stay round
        val viewW = width.toFloat()
        val viewH = height.toFloat()
        if (viewW > viewH) {
            // wide device: shrink X
            projectionMatrix.scale(viewH / viewW, 1f)
        } else {
            // tall device: shrink Y
            projectionMatrix.scale(1f, viewW / viewH)
        }

        // 2) Optional framing (zoom + slight up shift)
        modelMatrix?.apply {
            loadIdentity()
            scale(2.3f, 4f)   // tweak 1.1–1.6 to taste
            translateY(-0.2f)     // tweak -0.2..0.2
        }

        // 3) Apply modelMatrix into projection BEFORE drawing
        modelMatrix?.let { projectionMatrix.multiplyByMatrix(it) }

        // 4) Draw
        r.setMvpMatrix(projectionMatrix)
        r.drawModel()
    }


}
