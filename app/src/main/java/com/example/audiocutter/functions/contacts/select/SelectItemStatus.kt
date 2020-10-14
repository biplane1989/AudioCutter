package com.example.audiocutter.functions.contacts.select

import com.example.audiocutter.core.manager.PlayerState

data class SelectItemStatus(var playerState: PlayerState = PlayerState.IDLE, var duration: Int = -1, var currPos: Int = -1)
