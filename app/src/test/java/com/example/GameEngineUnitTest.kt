package com.example

import com.example.game.GameEngine
import com.example.game.GamePlayState
import com.example.game.Obstacle
import com.example.game.ObstacleType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GameEngineUnitTest {

    private lateinit var engine: GameEngine

    @Before
    fun setUp() {
        // Standard horizontal resolution
        engine = GameEngine(screenWidth = 800f, screenHeight = 400f)
        engine.reset(startInReady = false)
    }

    @Test
    fun testInitialGroundedState() {
        assertTrue("Capybara must be grounded at start", engine.isGrounded)
        assertFalse("Capybara must not be jumping at start", engine.isJumping)
        assertEquals(0, engine.currentScore)
        assertEquals(engine.groundY - engine.capyHeight, engine.capyY, 0.01f)
    }

    @Test
    fun testJumpPhysicsAndAirborneState() {
        val didJump = engine.onUserTap()
        assertTrue("First tap on ground must initiate jump", didJump)
        assertFalse("Capybara must no longer be grounded", engine.isGrounded)
        assertTrue("Capybara must be marked as jumping", engine.isJumping)
        assertEquals(engine.jumpForce, engine.yVelocity, 0.01f)

        // Tapping again while in the air must NOT allow double jump
        val doubleJump = engine.onUserTap()
        assertFalse("Second tap in air must be rejected", doubleJump)
    }

    @Test
    fun testGravityAndLanding() {
        engine.onUserTap()
        // Simulate forward frames
        for (i in 0 until 60) {
            engine.update(0.016f)
        }
        // Eventually capybara must return to ground
        for (i in 0 until 100) {
            engine.update(0.016f)
            if (engine.isGrounded) break
        }
        assertTrue("Capybara must land back on the ground", engine.isGrounded)
        assertFalse("isJumping must be reset after landing", engine.isJumping)
        assertEquals(engine.groundY - engine.capyHeight, engine.capyY, 0.01f)
    }

    @Test
    fun testObstacleMovesRightToLeft() {
        val initialX = 700f
        val obs = Obstacle(
            id = 1L,
            x = initialX,
            y = engine.groundY - 30f,
            width = 40f,
            height = 30f,
            type = ObstacleType.ROCK
        )
        engine.obstacles.add(obs)

        val dt = 0.05f
        engine.update(dt)

        assertTrue(
            "Obstacle must move to the left (x must decrease)",
            obs.x < initialX
        )
    }

    @Test
    fun testCollisionTriggersGameOver() {
        // Place obstacle directly overlapping capybara
        val obs = Obstacle(
            id = 1L,
            x = engine.capyX + 10f,
            y = engine.capyY + 5f,
            width = 40f,
            height = 30f,
            type = ObstacleType.ROCK
        )
        engine.obstacles.add(obs)

        val running = engine.update(0.016f)
        assertFalse("Collision must return false from update()", running)
        assertEquals("State must be GAME_OVER", GamePlayState.GAME_OVER, engine.state)
    }

    @Test
    fun testResetRestoresCleanState() {
        engine.currentScore = 550
        engine.distanceTraveled = 3000f
        engine.state = GamePlayState.GAME_OVER
        engine.obstacles.add(
            Obstacle(1L, 200f, engine.groundY - 30f, 40f, 30f, ObstacleType.ROCK)
        )

        engine.reset(startInReady = false)

        assertEquals(0, engine.currentScore)
        assertEquals(0f, engine.distanceTraveled, 0.01f)
        assertEquals(0, engine.obstacles.size)
        assertEquals(GamePlayState.PLAYING, engine.state)
        assertTrue(engine.isGrounded)
    }

    @Test
    fun testScoreIncreasesWithSurvival() {
        val initialScore = engine.currentScore
        for (i in 0 until 50) {
            engine.update(0.016f)
        }
        assertTrue("Score must increase as capybara travels", engine.currentScore > initialScore)
    }

    @Test
    fun testFlightStopsSpeedIncrementingAndResumesWhenFlightEnds() {
        // Run some frames on ground: speed should increment
        for (i in 0 until 60) {
            engine.update(0.016f)
        }
        val speedBeforeFlight = engine.currentSpeed
        val scalingDistBeforeFlight = engine.speedScalingDistance
        assertTrue("Speed should have started scaling", speedBeforeFlight >= engine.baseSpeed)

        // Enter flight mode
        engine.isFlying = true
        engine.flightTimeRemaining = 3.0f

        // Run frames during flight: speed and speedScalingDistance must NOT increase
        for (i in 0 until 60) {
            engine.update(0.016f)
        }
        assertEquals(
            "Speed must remain frozen while flying",
            speedBeforeFlight,
            engine.currentSpeed,
            0.001f
        )
        assertEquals(
            "Speed scaling distance must remain frozen while flying",
            scalingDistBeforeFlight,
            engine.speedScalingDistance,
            0.001f
        )

        // End flight mode
        engine.isFlying = false
        engine.flightTimeRemaining = 0f

        // Run frames after flight: speed incrementing must resume
        for (i in 0 until 60) {
            engine.update(0.016f)
        }
        assertTrue(
            "Speed should resume incrementing after flight ends",
            engine.currentSpeed > speedBeforeFlight
        )
        assertTrue(
            "Speed scaling distance should resume incrementing after flight ends",
            engine.speedScalingDistance > scalingDistBeforeFlight
        )
    }

    @Test
    fun testLandingGraceProtectsFallingCapybaraAndClearsObstacles() {
        // Setup flight ending into landing grace
        engine.isFlying = true
        engine.flightTimeRemaining = 0.01f
        // Update so flight ends and triggers isLandingGrace
        engine.update(0.02f)

        assertTrue("Landing grace should be activated when flight ends", engine.isLandingGrace)
        assertTrue("Landing grace timer should be positive", engine.landingGraceTimer > 0f)

        // Place obstacle right on capybara runway
        val obstacleNearLanding = Obstacle(
            id = 99L,
            x = engine.capyX + 50f,
            y = engine.groundY - 30f,
            width = 40f,
            height = 30f,
            type = ObstacleType.ROCK
        )
        engine.obstacles.add(obstacleNearLanding)

        // Run update while capybara is falling
        val running = engine.update(0.016f)

        assertTrue("Capybara must be protected from game over during landing grace", running)
        assertNotEquals("State must NOT be GAME_OVER during landing grace", GamePlayState.GAME_OVER, engine.state)
        assertFalse("Obstacles in runway must be cleared during landing grace", engine.obstacles.contains(obstacleNearLanding))
    }

    @Test
    fun testKidFriendlyGradualSpeedIncrementation() {
        // After ~10 seconds of running (600 frames at 0.016s)
        for (i in 0 until 600) {
            engine.update(0.016f)
        }
        // Speed after 10s should increase gently (under 30px/s gain), avoiding sudden spikes
        assertTrue(
            "Speed after 10s should remain very close to base speed for kids (gentle ramp)",
            engine.currentSpeed < engine.baseSpeed + 30f
        )
        assertTrue(
            "Speed must still be progressive",
            engine.currentSpeed >= engine.baseSpeed
        )
        assertTrue("Speed must not exceed maxSpeed cap", engine.currentSpeed <= engine.maxSpeed)
    }
}
