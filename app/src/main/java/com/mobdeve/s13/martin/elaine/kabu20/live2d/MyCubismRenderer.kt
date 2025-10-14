package com.mobdeve.s13.martin.elaine.kabu20.live2d

import com.live2d.sdk.cubism.framework.rendering.android.CubismRendererAndroid

class MyCubismRenderer : CubismRendererAndroid() {
    fun bindTextureSafe(index: Int, textureId: Int) {
        // Reflection hack (since textures is private)
        try {
            val field = CubismRendererAndroid::class.java.getDeclaredField("textures")
            field.isAccessible = true
            val textures = field.get(this) as MutableMap<Int, Int>
            textures[index] = textureId
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
