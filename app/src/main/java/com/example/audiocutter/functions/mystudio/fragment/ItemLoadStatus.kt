package com.example.audiocutter.functions.mystudio.fragment

import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.mystudio.DeleteState

data class ItemLoadStatus(
    var deleteState: DeleteState = DeleteState.HIDE,
    var playerState: PlayerState = PlayerState.IDLE,
    var duration:Int = -1,
    var currPos:Int=-1
)
