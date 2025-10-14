package com.mobdeve.s13.martin.elaine.kabu20.live2d


import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.util.AttributeSet

class Live2DView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GLSurfaceView(context, attrs) {

    private val renderer = Live2DRenderer(context)

    init {
        setEGLContextClientVersion(2)

        //holder.setFormat(PixelFormat.TRANSLUCENT)

        setRenderer(renderer)
        preserveEGLContextOnPause = true

        post {
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        }
    }

    fun startIdleAnimation(){
        renderer.playMotion("Idle")
    }

    fun startTalkingAnimation(){
        renderer.playMotion("TalkNeutral")
    }

}