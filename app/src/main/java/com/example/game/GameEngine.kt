package com.example.game

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

class GameEngine(
    val screenWidth: Float,
    val screenHeight: Float
) {
    // Ground level relative to screen height
    val groundY: Float = screenHeight * 0.76f

    // Capybara physics & dimensions (positioned on left)
    val capyX: Float = screenWidth * 0.14f
    val capyWidth: Float = 72f
    val capyHeight: Float = 48f
    var capyY: Float = groundY - capyHeight

    // Physics
    var yVelocity: Float = 0f
    val gravity: Float = 2200f
    val jumpForce: Float = -780f
    var isGrounded: Boolean = true
    var isJumping: Boolean = false

    // Magical flight power-up
    var isFlying: Boolean = false
    var flightTimeRemaining: Float = 0f
    val maxFlightDuration: Float = 5.0f

    // Landing protection after flight: prevents unfair game-over when falling
    var isLandingGrace: Boolean = false
    var landingGraceTimer: Float = 0f

    // Speed & scoring (Tuned for kid-friendly endless fun: gentle, gradual scaling over a long time)
    val baseSpeed: Float = 320f
    var currentSpeed: Float = 320f
    val maxSpeed: Float = 540f
    var currentScore: Int = 0
    var distanceTraveled: Float = 0f
    var speedScalingDistance: Float = 0f // Freezes during flight!
    var eggsCollectedCount: Int = 0
    var survivalDuration: Long = 0L

    // Game state
    var state: GamePlayState = GamePlayState.READY

    // Spawning timers (spacious, forgiving intervals for kids)
    private var obstacleSpawnTimer: Float = 0f
    private var nextObstacleSpawnInterval: Float = 2.4f
    private var eggSpawnTimer: Float = 0f
    private var nextEggSpawnInterval: Float = 7.5f

    val obstacles = mutableListOf<Obstacle>()
    val eggs = mutableListOf<MagicalEgg>()
    val particles = mutableListOf<Particle>()

    private var nextId: Long = 0L

    fun reset(startInReady: Boolean = false) {
        capyY = groundY - capyHeight
        yVelocity = 0f
        isGrounded = true
        isJumping = false
        isFlying = false
        flightTimeRemaining = 0f
        isLandingGrace = false
        landingGraceTimer = 0f
        currentScore = 0
        distanceTraveled = 0f
        speedScalingDistance = 0f
        currentSpeed = baseSpeed
        eggsCollectedCount = 0
        survivalDuration = 0L
        obstacleSpawnTimer = 0f
        nextObstacleSpawnInterval = 2.4f
        eggSpawnTimer = 0f
        nextEggSpawnInterval = 7.5f
        obstacles.clear()
        eggs.clear()
        particles.clear()
        state = if (startInReady) GamePlayState.READY else GamePlayState.PLAYING
    }

    /**
     * Handles tap-to-jump.
     * Returns true if a jump or flutter was initiated (to trigger sound/haptic).
     */
    fun onUserTap(): Boolean {
        when (state) {
            GamePlayState.READY -> {
                state = GamePlayState.PLAYING
                // Jump immediately on first tap
                yVelocity = jumpForce
                isGrounded = false
                isJumping = true
                return true
            }
            GamePlayState.PLAYING -> {
                if (isFlying) {
                    yVelocity = -320f
                    return true
                } else if (isGrounded) {
                    yVelocity = jumpForce
                    isGrounded = false
                    isJumping = true
                    return true
                }
                return false // in the air, cannot double jump
            }
            GamePlayState.PAUSED, GamePlayState.GAME_OVER -> {
                return false // ignore gameplay taps during pause or game over
            }
        }
    }

    /**
     * Updates physics, movements, spawning, and collisions.
     * Returns true if game is still running, false if collision occurred this frame.
     */
    fun update(rawDt: Float): Boolean {
        if (state != GamePlayState.PLAYING) {
            return true
        }

        val dt = rawDt.coerceIn(0.001f, 0.05f)
        survivalDuration += (dt * 1000).toLong()

        // Progressive speed scaling:
        // Freezes incrementing speed while flying, and resumes when flying ends!
        // Scales very gently over a long period so kids can enjoy running and jumping without sudden difficulty spikes.
        if (!isFlying) {
            speedScalingDistance += currentSpeed * dt
            currentSpeed = minOf(maxSpeed, baseSpeed + (speedScalingDistance / 2400f) * 6f)
        }
        distanceTraveled += currentSpeed * dt
        currentScore = (distanceTraveled / 12f).toInt() + (eggsCollectedCount * 60)

        // Handle flight mode countdown
        if (isFlying) {
            flightTimeRemaining -= dt
            if (flightTimeRemaining <= 0f) {
                isFlying = false
                flightTimeRemaining = 0f
                // Activate landing grace immunity and clear landing zone obstacles
                isLandingGrace = true
                landingGraceTimer = 2.5f
                obstacles.removeAll { it.x in (capyX - 60f)..(capyX + 450f) }
            }
        }

        // Handle landing grace when falling to ground from flight
        if (isLandingGrace) {
            landingGraceTimer -= dt
            // Keep runway clear until safely landed
            obstacles.removeAll { it.x in (capyX - 60f)..(capyX + 350f) }
            if (landingGraceTimer <= 0f && isGrounded) {
                isLandingGrace = false
            }
        }

        // Capybara physics
        if (isFlying) {
            // Smoothly hover at comfortable flying height
            val targetY = groundY - 180f
            capyY += (targetY - capyY) * 7f * dt
            yVelocity = 0f
            isGrounded = false
        } else {
            yVelocity += gravity * dt
            capyY += yVelocity * dt

            // Ground landing detection
            if (capyY >= groundY - capyHeight) {
                capyY = groundY - capyHeight
                yVelocity = 0f
                if (isJumping) {
                    isJumping = false
                    emitLandingDust()
                }
                isGrounded = true
            } else {
                isGrounded = false
            }
        }

        // Move obstacles from right to left
        val obsIter = obstacles.iterator()
        while (obsIter.hasNext()) {
            val obs = obsIter.next()
            obs.x -= currentSpeed * dt
            // Recycle when fully off-screen on the left
            if (obs.x + obs.width < -100f) {
                obsIter.remove()
            }
        }

        // Move eggs from right to left
        val eggIter = eggs.iterator()
        while (eggIter.hasNext()) {
            val egg = eggIter.next()
            egg.x -= currentSpeed * dt
            if (egg.x + egg.size < -100f || egg.collected) {
                eggIter.remove()
            }
        }

        // Obstacle spawning (only spawn when not in landing grace runway)
        obstacleSpawnTimer += dt
        val dynamicInterval = maxOf(1.5f, nextObstacleSpawnInterval - (currentScore / 2500f) * 0.35f)
        if (obstacleSpawnTimer >= dynamicInterval) {
            // Ensure fair minimum distance from last spawned obstacle
            val minSpacing = 360f + (currentSpeed * 0.35f)
            val hasOverlap = obstacles.any { it.x > screenWidth - minSpacing }
            if (!hasOverlap && !isLandingGrace) {
                obstacleSpawnTimer = 0f
                spawnObstacle()
                nextObstacleSpawnInterval = Random.nextFloat() * 1.2f + 2.0f
            }
        }

        // Egg spawning
        eggSpawnTimer += dt
        if (eggSpawnTimer >= nextEggSpawnInterval) {
            eggSpawnTimer = 0f
            spawnEgg()
            nextEggSpawnInterval = Random.nextFloat() * 5.0f + 7.0f
        }

        // Particle update
        val pIter = particles.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.alpha -= dt * 2.2f
            if (p.alpha <= 0f) {
                pIter.remove()
            }
        }

        // Running dust / flight sparkles
        if (isGrounded && Math.random() < 0.35) {
            particles.add(
                Particle(
                    x = capyX + 12f,
                    y = capyY + capyHeight - 2f,
                    vx = -currentSpeed * 0.4f,
                    vy = (Random.nextFloat() - 0.7f) * 60f,
                    alpha = 0.7f,
                    color = Color(0xFFC68B59),
                    radius = 3.5f
                )
            )
        } else if (isFlying && Math.random() < 0.6) {
            particles.add(
                Particle(
                    x = capyX + 8f,
                    y = capyY + capyHeight * 0.5f + (Random.nextFloat() - 0.5f) * 16f,
                    vx = -currentSpeed * 0.5f,
                    vy = (Random.nextFloat() - 0.5f) * 40f,
                    alpha = 0.9f,
                    color = Color(0xFFE8A838),
                    radius = 4f
                )
            )
        }

        // Egg collection collision detection
        for (egg in eggs) {
            if (!egg.collected) {
                val dx = (capyX + capyWidth * 0.5f) - egg.x
                val dy = (capyY + capyHeight * 0.5f) - egg.y
                val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                if (dist < (capyWidth * 0.5f + egg.size)) {
                    egg.collected = true
                    eggsCollectedCount++
                    isFlying = true
                    isLandingGrace = false
                    flightTimeRemaining = maxFlightDuration
                    emitEggBurst(egg.x, egg.y)
                }
            }
        }

        // Obstacle collision detection
        val cLeft = capyX + 10f
        val cRight = capyX + capyWidth - 10f
        val cTop = capyY + 8f
        val cBottom = capyY + capyHeight - 2f

        for (obs in obstacles) {
            // While flying OR during landing descent grace, capybara is 100% immune from game over!
            if (isFlying || isLandingGrace) {
                continue
            }

            val oLeft = obs.x + 6f
            val oRight = obs.x + obs.width - 6f
            val oTop = obs.y + 6f
            val oBottom = obs.y + obs.height

            if (cRight > oLeft && cLeft < oRight && cBottom > oTop && cTop < oBottom) {
                // Collision!
                state = GamePlayState.GAME_OVER
                emitCrashSparks(obs.x + obs.width * 0.5f, obs.y + obs.height * 0.5f)
                return false
            }
        }

        return true
    }

    private fun spawnObstacle() {
        val type = when (Random.nextInt(7)) {
            0 -> ObstacleType.ROCK
            1 -> ObstacleType.LOG
            2 -> ObstacleType.TREE_STUMP
            3 -> ObstacleType.BUSH
            4 -> ObstacleType.PRICKLY_BUSH
            5 -> ObstacleType.PUDDLE
            else -> ObstacleType.FLOATING_ROCK
        }

        val width = when (type) {
            ObstacleType.ROCK -> 44f
            ObstacleType.LOG -> 56f
            ObstacleType.TREE_STUMP -> 46f
            ObstacleType.BUSH -> 50f
            ObstacleType.PRICKLY_BUSH -> 52f
            ObstacleType.PUDDLE -> 54f
            ObstacleType.FLOATING_ROCK -> 46f
        }

        val height = when (type) {
            ObstacleType.ROCK -> 34f
            ObstacleType.LOG -> 28f
            ObstacleType.TREE_STUMP -> 40f
            ObstacleType.BUSH -> 42f
            ObstacleType.PRICKLY_BUSH -> 44f
            ObstacleType.PUDDLE -> 14f
            ObstacleType.FLOATING_ROCK -> 32f
        }

        val y = when (type) {
            ObstacleType.PUDDLE -> groundY - height + 8f
            ObstacleType.FLOATING_ROCK -> groundY - 135f // elevated so running capybara passes under
            else -> groundY - height
        }

        obstacles.add(
            Obstacle(
                id = nextId++,
                x = screenWidth + 50f,
                y = y,
                width = width,
                height = height,
                type = type
            )
        )
    }

    private fun spawnEgg() {
        val y = groundY - 170f - Random.nextFloat() * 60f
        eggs.add(
            MagicalEgg(
                id = nextId++,
                x = screenWidth + 60f,
                y = y,
                size = 22f
            )
        )
    }

    private fun emitLandingDust() {
        for (i in 0..4) {
            particles.add(
                Particle(
                    x = capyX + 15f + i * 10f,
                    y = groundY - 4f,
                    vx = (Random.nextFloat() - 0.5f) * 120f,
                    vy = -Random.nextFloat() * 40f,
                    alpha = 0.6f,
                    color = Color(0xFFC68B59),
                    radius = 3f
                )
            )
        }
    }

    private fun emitEggBurst(x: Float, y: Float) {
        for (i in 0..12) {
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = (Random.nextFloat() - 0.5f) * 260f,
                    vy = (Random.nextFloat() - 0.5f) * 260f,
                    alpha = 1.0f,
                    color = Color(0xFFE8A838),
                    radius = 5f
                )
            )
        }
    }

    private fun emitCrashSparks(x: Float, y: Float) {
        for (i in 0..16) {
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = (Random.nextFloat() - 0.5f) * 320f,
                    vy = (Random.nextFloat() - 0.8f) * 280f,
                    alpha = 1.0f,
                    color = if (i % 2 == 0) Color(0xFFE53935) else Color(0xFFC68B59),
                    radius = 4.5f
                )
            )
        }
    }
}
