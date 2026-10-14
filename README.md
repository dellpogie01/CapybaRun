# CapybaRun 🦫

**CapybaRun** is an adorable, fast-paced 2D endless-runner Android game featuring a cute running capybara, realistic jumping physics, multi-layered parallax scrolling scenery, moving obstacles, magical eggs granting temporary flight, unlockable cosmetic hats, and offline Room SQLite persistence.

---

## 🎮 Main Features

- **Intuitive Touch Controls**: Tap anywhere on the screen to jump over incoming hazards. Accurately simulates gravity, velocity, and ground collision with no unintended mid-air double jumps.
- **Continuously Moving World**: Seamless parallax scrolling background featuring blue sky, drifting clouds, distant rolling mountains, midground forest trees, and textured running terrain.
- **Dynamic Obstacle Spawning**: Randomly spawns rocks, fallen logs, tree stumps, green bushes, prickly thickets, puddles, and elevated floating rocks from right to left with fair spacing and progressive difficulty scaling.
- **Magical Flight Mode**: Collect floating golden eggs to unlock 5 seconds of smooth flight power, soaring over ground obstacles with golden particle trails.
- **State Management**:
  - `READY`: Tap anywhere to jump & start running.
  - `PLAYING`: Full real-time physics, obstacles moving right to left, scoring, collisions.
  - `PAUSED`: Freezes simulation with Resume, Restart, and Main Menu options.
  - `GAME_OVER`: Displays final score, personal best, "NEW BEST!" badge, instant Play Again, and Main Menu.
- **Wardrobe & Customization**: Unlock and equip stylish hats (Explorer Hat, Golden Crown, Flower Crown, Wizard Hat) earned by collecting eggs and reaching high score milestones.
- **Local Room SQLite Persistence**: Tracks personal bests, total games played, total eggs collected, cumulative distance, and survival time offline.
- **Original Two-Tone Aesthetic**: Warm Caramel Brown (`#C68B59`) paired with Soft Cream (`#FDFBF7`) and natural forest accents.

---

## 🛠️ Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Game Engine**: Custom 2D Canvas rendering engine with 60 FPS delta-time physics
- **Persistence**: Room Database with SQLite & Kotlin Coroutines Flow
- **Architecture**: MVVM with Repository pattern
- **Audio**: Lightweight low-latency `SoundManager` using `ToneGenerator`
- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 36

---

## 📁 Project Structure

```
com.example/
├── MainActivity.kt               # Dynamic orientation & top-level navigation
├── audio/
│   └── SoundManager.kt           # Jump, egg, flight, and game over sound effects
├── data/
│   ├── GameEntities.kt           # Room SQLite entities (records, stats, settings, unlocks)
│   ├── GameDao.kt                # Data Access Object for Room queries
│   ├── CapybaDatabase.kt         # Room database with initial seed data
│   └── GameRepository.kt         # Repository abstracting local database operations
├── game/
│   ├── GameModels.kt             # GamePlayState, ObstacleType, MagicalEgg, Particle
│   └── GameEngine.kt             # Physics, jump calculations, obstacle spawner, collisions
├── ui/
│   ├── screens/
│   │   ├── SplashScreen.kt       # Animated cartoon splash screen
│   │   ├── HomeScreen.kt         # Vertical portrait home screen with mascot & menu
│   │   ├── GameScreen.kt         # Horizontal 2D Canvas runner with parallax scenery
│   │   ├── GameOverScreen.kt     # Game Over dialog overlay
│   │   ├── WardrobeScreen.kt     # Unlockable hats & customization screen
│   │   ├── StatisticsScreen.kt   # Lifetime gameplay statistics screen
│   │   ├── TutorialScreen.kt     # "How to Play" tutorial screen
│   │   └── PauseDialog.kt        # Pause menu dialog
│   └── theme/
│       ├── Color.kt              # Warm Caramel Brown, Soft Cream, Espresso, Gold
│       ├── Theme.kt              # CapybarunTheme Material 3 configuration
│       └── Type.kt               # Typography specifications
└── viewmodel/
    └── GameViewModel.kt          # UI state, DB flows, audio actions, and run recorder
```

---

## 🚀 Build Instructions

### Prerequisites
- Android Studio Hedgehog (or newer)
- Android SDK (API 36, minimum API 24)
- JDK 17 or JDK 21

### Building the Project
Open the project directory in Android Studio and run:
```bash
./gradlew assembleDebug
```
To install directly to a connected Android phone or emulator:
```bash
./gradlew installDebug
```

---

## 📦 How to Generate a Signed Android App Bundle (AAB) for Google Play

1. **Create an Upload Keystore**:
   ```bash
   keytool -genkey -v -keystore my-upload-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
   ```
2. **Set Environment Variables**:
   ```bash
   export KEYSTORE_PATH="/path/to/my-upload-key.jks"
   export STORE_PASSWORD="your-store-password"
   export KEY_PASSWORD="your-key-password"
   ```
3. **Generate the Release App Bundle**:
   ```bash
   ./gradlew bundleRelease
   ```
4. **Locate the Output**:
   The generated `.aab` will be located in:
   `app/build/outputs/bundle/release/app-release.aab`
5. Upload this file to the Google Play Console under **Production** or **Testing Track**.

---

## ✅ Testing Checklist

- [x] **Touch to Jump**: Tapping the screen initiates a jump immediately with realistic gravity and ground landing.
- [x] **No Air Double Jumps**: Tapping again while in the air does not trigger additional jumps.
- [x] **Seamless Parallax Background**: Clouds, distant mountains, near hills, trees, and grass scroll continuously from right to left without seams or resets.
- [x] **Moving Obstacles**: Rocks, logs, tree stumps, and bushes move continuously from right to left across the screen and can be avoided by jumping.
- [x] **Fair Spawning**: Obstacles maintain safe minimum distances and never spawn on top of each other.
- [x] **Collision & Game Over**: Hitting an obstacle immediately freezes simulation and displays Game Over with final and best scores.
- [x] **Instant Restart**: Tapping "Play Again" resets the run in-place without orientation churn or app reload.
- [x] **Magical Flight**: Collecting golden eggs triggers 5 seconds of flight with golden particle trail and immunity to ground hazards.
- [x] **Persistent High Scores**: Personal best scores, egg counts, and lifetime statistics persist across app restarts using Room SQLite.
