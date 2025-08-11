> This is a personal side project created for educational purposes, aimed at helping me learn how to code by experimenting with AI tools like Claude Code.

# SofiaTracker 👶
SofiaTracker is a modern Android application designed to help parents track their baby's daily activities including sleeping, feeding, and diaper changes. Built with Jetpack Compose and following clean architecture principles, the app provides a seamless way to monitor your baby's patterns and maintain a digital log of important events.

## 🎯 Purpose
The app allows parents to:
- Track baby's sleep patterns and duration
- Record feeding events with bottle amounts
- Log diaper changes with notes
- View statistics and patterns over time
- Sync data across devices using Firebase
- Access data offline with local storage

## 🏗️ Technical Specifications

### Architecture
- **Architecture Pattern**: Clean Architecture with modular structure
- **UI Framework**: Jetpack Compose with Material 3 Design
- **Dependency Injection**: Hilt
- **Navigation**: Navigation Compose
- **Build System**: Gradle with Kotlin DSL and Convention Plugins

### Technology Stack
- **Target SDK**: 35 (Android 14)
- **Minimum SDK**: 26 (Android 8.0)
- **Kotlin**: 2.1.10
- **Compose BOM**: 2024.04.01
- **Room Database**: 2.7.2 (local storage)
- **Firebase**: 33.1.2 (cloud sync)
- **WorkManager**: 2.9.1 (background sync)
- **DataStore**: 1.0.0 (preferences)

### Project Structure
```
SofiaTracker/
├── app/                          # Main application module
├── core/
│   ├── data/                     # Data layer with repositories
│   ├── database/                 # Room database entities and DAOs
│   ├── datastore/                # SharedPreferences wrapper
│   ├── designsystem/             # Theme and design components
│   ├── domain/                   # Use cases and domain models
│   ├── model/                    # Data models
│   ├── network/                  # Firebase integration
│   └── ui/                       # Shared UI components
├── feature/
│   ├── home/                     # Main tracking screens
│   ├── settings/                 # App settings
│   └── statistics/               # Data visualization
└── build-logic/                  # Gradle convention plugins
```

### Key Features
- **Event Types**: Sleep, Eat (with bottle amounts), and Poop tracking
- **Offline-First**: Local Room database with Firebase sync
- **Background Sync**: WorkManager handles data synchronization
- **Conflict Resolution**: Built-in sync conflict handling
- **Modern UI**: Material 3 design with edge-to-edge display

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

Built with ❤️ for parents tracking their little ones' precious moments.