# A Today Habit 🌿

A minimalist, beautiful habit tracking app built with Jetpack Compose for Android.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Language: Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org/)
[![Android: 8.0+](https://img.shields.io/badge/Android-8.0+-green.svg)](https://www.android.com/)

## Features ✨

- **Minimalist Design** — Clean, distraction-free interface with elegant animations
- **Multi-rep Tracking** — Track daily goals with multiple reps (e.g., drink 8 glasses of water)
- **Visual Progress** — Circular progress rings and animated check-ins for motivation
- **Heatmap View** — See all habits in one unified heatmap with 5-level color intensity
- **Flexible Schedules** — Daily, weekdays, custom weekly, and custom monthly intervals
- **Rich Icons** — 20+ built-in linear icons covering fitness, learning, lifestyle, and more
- **Full Control** — Edit, rename, or delete habits anytime

## Tech Stack 🛠

| Component | Technology |
|-----------|-----------|
| **UI** | [Jetpack Compose](https://developer.android.com/jetpack/compose) |
| **Architecture** | MVVM (ViewModel + Flow + LiveData) |
| **Database** | [Room](https://developer.android.com/training/data-storage/room) |
| **Navigation** | Compose Navigation |
| **Language** | Kotlin 2.0+ |
| **Async** | Kotlin Coroutines & Flow |

## Quick Start 🚀

### Prerequisites
- Android Studio Ladybug (2024.2.1) or later
- JDK 17+
- Android SDK 26+ (API 26, Android 8.0+)

### Build
```bash
# Clone the repository
git clone https://github.com/qnmlgbd250/a-today-habit.git

# Open in Android Studio and build
# Or build via command line
./gradlew build

# Run on device/emulator
./gradlew installDebug
```

## Project Structure 📁

```
a-today-habit/
├── app/
│   ├── src/main/java/com/example/habit/
│   │   ├── ui/          # Compose screens & components
│   │   ├── viewmodel/   # MVVM ViewModels
│   │   ├── model/       # Data models & entities
│   │   └── database/    # Room database setup
│   └── build.gradle.kts
└── gradle/
```

## Contributing 🤝

Contributions are welcome! Please feel free to:
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License 📄

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

## Author

[@qnmlgbd250](https://github.com/qnmlgbd250)

---

*Built with ❤️ in Kotlin*
