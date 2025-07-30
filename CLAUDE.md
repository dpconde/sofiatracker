# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SofiaTracker is an Android application built with Kotlin and Jetpack Compose. This is a standard Android project using modern Android development practices with Compose UI toolkit.

## Development Commands

### Build and Run
- `./gradlew build` - Build the entire project
- `./gradlew assembleDebug` - Build debug APK
- `./gradlew assembleRelease` - Build release APK
- `./gradlew installDebug` - Install debug build to connected device/emulator

### Testing
- `./gradlew test` - Run unit tests
- `./gradlew connectedAndroidTest` - Run instrumented tests on connected device/emulator
- `./gradlew testDebugUnitTest` - Run debug unit tests specifically

### Code Quality
- `./gradlew lint` - Run Android lint checks
- `./gradlew lintDebug` - Run lint on debug variant

### Clean
- `./gradlew clean` - Clean build artifacts

## Architecture

### Technology Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Build System**: Gradle with Kotlin DSL
- **Target SDK**: 35 (Android 14)
- **Minimum SDK**: 24 (Android 7.0)

### Project Structure
- `app/src/main/java/com/dpconde/sofiatracker/` - Main application code
  - `MainActivity.kt` - Single activity hosting Compose UI
  - `ui/theme/` - Compose theme definitions (Color.kt, Theme.kt, Type.kt)
- `app/src/test/` - Unit tests
- `app/src/androidTest/` - Instrumented tests
- `gradle/libs.versions.toml` - Centralized dependency version management

### Key Dependencies
- Jetpack Compose BOM for UI components
- AndroidX Core, Lifecycle, and Activity libraries
- Material 3 for design system
- JUnit and Espresso for testing

### Build Configuration
- Uses version catalogs (libs.versions.toml) for dependency management
- Compose compiler enabled
- ProGuard disabled for debug builds
- Edge-to-edge display support enabled

## Development Notes

The project follows standard Android Compose patterns with a single Activity architecture. All UI is built using Compose, and the project uses the latest stable versions of Android development tools and libraries.