package com.example.audiocutter.functions.contactscreen.select

import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.mystudioscreen.DeleteState

data class SelectItemStatus(var playerState: PlayerState = PlayerState.IDLE, var duration: Int = -1, var currPos: Int = -1)
