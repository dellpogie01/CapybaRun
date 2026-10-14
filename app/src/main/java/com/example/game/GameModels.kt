package com.example.game

import androidx.compose.ui.graphics.Color

enum class GameScreenState {
    SPLASH,
    HOME,
    PLAYING,
    PAUSED,
    GAMEOVER,
    WARDROBE,
    STATS,
    TUTORIAL,
    AI_STUDIO
}

enum class GamePlayState {
    READY,
    PLAYING,
    PAUSED,
    GAME_OVER
}

enum class ObstacleType {
    ROCK,
    LOG,
    TREE_STUMP,
    BUSH,
    PRICKLY_BUSH,
    PUDDLE,
    FLOATING_ROCK
}

data class Obstacle(
    val id: Long,
    var x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val type: ObstacleType
)

data class MagicalEgg(
    val id: Long,
    var x: Float,
    var y: Float,
    val size: Float,
    var collected: Boolean = false
)

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var alpha: Float,
    val color: Color,
    val radius: Float = 4f
)
