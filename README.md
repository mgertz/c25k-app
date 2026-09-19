# C25K – self-hosted Couch-to-5K running app

Kotlin + Jetpack Compose, Android-only, no backend. Built as a single Gradle module.

## Opening it
1. Open the `c25k-app` folder in Android Studio (File → Open). It will run Gradle sync
   automatically and fetch dependencies from Google's and Maven Central's repositories —
   no manual setup needed.
2. There's no `gradlew` wrapper checked in yet (this was generated in a sandbox with no
   network access to download the wrapper jar). Android Studio doesn't need it to sync
   and run. If you want `./gradlew` on the command line too, run `gradle wrapper` once
   you have any Gradle version installed locally, from inside `c25k-app/`.
3. Package name: `dk.michael.c25k`. minSdk 26, targetSdk/compileSdk 34.

## What's here
- `app/src/main/assets/programs.json` — the 27-session plan (index/week/day/intervals),
  each interval already tagged with which sound file it should play.
- `app/src/main/assets/sounds/README.txt` — which .mp3 files to record and drop in, and
  the fallback naming convention for the duration-specific clips you mentioned wanting
  to add later ("Run for 3 minutes" etc.) — no code changes needed when you add those.
- `data/` — Program/Interval models (kotlinx.serialization), Room database for run
  history (`RunSessionEntity`, DAO), and `RunSuggestion` (the next/same/one-before logic).
- `service/RunForegroundService.kt` — runs warmup → intervals → cooldown → complete as a
  foreground service with a partial wake lock, so the timer survives the screen locking.
  Plays sound + vibration on every step change.
- `ui/` — one package per screen (home, chooserun, activerun, postrun, history), each
  with its own ViewModel, wired together in `ui/navigation/NavGraph.kt`.

## Known gaps / next steps
- The launcher icon is a placeholder circle I generated — swap it for something real
  whenever you get to it (`res/mipmap-xxxhdpi/ic_launcher.png`).
- No automated tests yet.
- I wrote this without a compiler available in my sandbox (no Android SDK/Gradle network
  access), so it's unverified — expect the first Gradle sync in Android Studio to catch
  a few small things (an import here, a version bump there).
