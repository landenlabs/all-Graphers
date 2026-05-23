# LanDen Labs - All-Graphers
<br>18-Arp-2026
<br>API 36 AndroidX Java
<br>[Home website](https://landenlabs.com/android/index.html)

<img src="screens/landenlabs.webp" width="300" alt="Logo">


Android application "experiment" using various graphing and charting libraries. 
This project serves as a playground and reference for implementing data visualization 
in Android apps.

WARNING - includes some JSON parsing code as a sandbox to test performance unrelated to the graphs. 


## 🚀 Features

- **Multiple Graphing Libraries**: Demonstrates integration with several popular Android charting libraries.
- **Modern Android Tooling**: Built with the latest Android Gradle Plugin (9.1.1) and Gradle 9.0.2.
- **Java 17 & SDK 36**: Targeted at the latest Android API levels with modern Java features.
- **Version Catalog**: Centralized dependency management using `libs.versions.toml`.
- **Custom APK Renaming**: Automatic APK naming convention: `[project]-[variant]-[version].apk`.

## 📊 Libraries Included

- **[MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)**: A powerful & easy to use chart library.
- **[AndroidPlot](https://androidplot.com/)**: A library for creating dynamic charts and graphs.
- **Custom Implementations**: Includes custom drawing logic like `GraphLineView`.
- **Mapbox Vector Tile**: Support for vector tile data processing.

## 🛠️ Project Structure

- `app/`: The main application module.
- `gradle/libs.versions.toml`: Version catalog for dependency management.
- `build.gradle`: Root build script using modern DSL.
- `settings.gradle`: Project-wide settings and repository configuration.

## 🏗️ Getting Started

### Prerequisites

- **Android Studio Ladybug (2024.2.1)** or newer.
- **JDK 17**: Required for building the project.

### Building the Project

To build the project and generate a debug APK:

```bash
./gradlew :app:assembleDebug
```

The APK will be generated in `app/build/outputs/apk/debug/` with the custom naming format.

## ⚙️ Configuration

- **Minimum SDK**: 28
- **Target SDK**: 36
- **Compile SDK**: 36
- **Java Toolchain**: 17

## 📜 License

Copyright © 2024 Landen Labs. All rights reserved.
