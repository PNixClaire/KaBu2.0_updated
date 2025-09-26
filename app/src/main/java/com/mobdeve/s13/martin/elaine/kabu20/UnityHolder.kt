package com.mobdeve.s13.martin.elaine.kabu20

import android.content.Context
import android.view.ViewGroup
import com.unity3d.player.UnityPlayer

object UnityHolder {
    var unityPlayer: UnityPlayer? = null

    fun getOrCreatePlayer(context: Context): UnityPlayer {
        if (unityPlayer == null) {
            unityPlayer = UnityPlayer(context)
        }
        return unityPlayer!!
    }

    fun releasePlayer() {
        unityPlayer?.let {
            (it.view.parent as? ViewGroup)?.removeView(it.view)
            it.quit()
        }
        unityPlayer = null
    }
}