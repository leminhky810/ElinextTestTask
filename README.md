# Images Test Task

A modern Android application built with Kotlin that displays a 7×10 grid of images with efficient loading, caching, and background synchronization capabilities.

## 📱 Overview

This application demonstrates modern Android development practices using Clean Architecture, MVVM pattern, and the latest Android libraries. It features a responsive grid layout, efficient image loading with Coil, background data synchronization with WorkManager, and robust error handling.

## ✨ Features

- **Grid Layout**: 7×10 responsive image grid with proper spacing and decoration
- **Image Loading**: Efficient image loading and caching using Coil library
- **Shimmer Animation**: Smooth loading animations for better user experience
- **Background Sync**: Automatic data synchronization using WorkManager
- **Offline Support**: Local database storage with Room
- **Permission Handling**: Modern permission management for Android 13+
- **Edge-to-Edge**: Modern edge-to-edge display support
- **Splash Screen**: Integrated splash screen with initialization coordination
- **Error Handling**: Comprehensive error handling and retry mechanisms
- **Responsive Design**: Adapts to different screen sizes and orientations

## 🏗️ Architecture

The application follows Clean Architecture principles with clear separation of concerns:

### Layers

- **Presentation Layer**: Activities, Fragments, ViewModels, and Adapters
- **Domain Layer**: Use Cases, Models, and Repository interfaces
- **Data Layer**: Repository implementations, Database, and Network services
- **Core Layer**: Utilities, Constants, and shared components

### Key Components

- **MVVM Pattern**: ViewModels manage UI state and business logic
- **Repository Pattern**: Centralized data access with local and remote sources
- **Dependency Injection**: Hilt for dependency management
- **Reactive Programming**: Kotlin Flow for reactive data streams
- **Background Processing**: WorkManager for reliable background tasks

## 🛠️ Technology Stack

- **Language**: Kotlin
- **Architecture**: Clean Architecture + MVVM
- **Dependency Injection**: Hilt
- **Database**: Room with Kotlin Flow
- **Image Loading**: Coil 3
- **Networking**: OkHttp
- **Background Work**: WorkManager
- **UI**: ViewBinding, RecyclerView, ConstraintLayout
- **Build System**: Gradle with Kotlin DSL
- **Testing**: JUnit, Mockk

## 📁 Project Structure

```
app/src/main/java/com/elinex/imagestesttask/
├── core/                           # Core utilities and constants
│   ├── di/                        # Dependency injection modules
│   └── utils/                     # Utility classes
├── data/                          # Data layer
│   ├── di/                        # Data DI modules
│   ├── mapper/                    # Data mappers
│   └── repository/                # Repository implementations
├── database/                      # Local database
│   ├── di/                        # Database DI modules
│   ├── ImageDatabase.kt           # Room database
│   ├── ImageDao.kt                # Data Access Object
│   └── ImageEntity.kt             # Database entities
├── domain/                        # Domain layer
│   ├── di/                        # Domain DI modules
│   ├── model/                     # Domain models
│   ├── repository/                # Repository interfaces
│   └── usecase/                   # Use cases
├── network/                       # Network layer
│   ├── di/                        # Network DI modules
│   ├── RedirectService.kt         # URL redirect handling
│   └── ForceCacheStrategy.kt      # Caching strategies
├── presentation/                  # Presentation layer
│   ├── adapter/                   # RecyclerView adapters
│   ├── base/                      # Base classes
│   ├── exts/                      # Extension functions
│   ├── helper/                    # UI helper classes
│   ├── home/                      # Home screen
│   ├── imageDetail/               # Image detail screen
│   ├── MainActivity.kt            # Main activity
│   └── MainActivityViewModel.kt   # Main activity ViewModel
├── preferences/                   # Shared preferences
│   └── di/                        # Preferences DI modules
├── worker/                        # Background workers
│   ├── di/                        # Worker DI modules
│   ├── ImageSyncInitializer.kt    # Work initialization
│   └── ImageSyncWorker.kt         # Background sync worker
└── ImagesApplication.kt           # Application class
```

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- Android SDK 34 (API level 34)
- Kotlin 2.1.10 or later
- JDK 17 or later

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd ElinextTestTask
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the project directory and select it

3. **Sync the project**
   - Wait for Gradle sync to complete
   - Ensure all dependencies are downloaded

4. **Configure API URL**
   - The `API_URL` in `secrets.properties` has already for testing

5. **Build and run**
   - Connect an Android device or start an emulator
   - Click the "Run" button in Android Studio
   - The app will install and launch automatically

### Configuration

The application uses several configuration files:

- **`local.defaults.properties`**: Format configuration values
- **`local.properties`**: Local configuration (gitignored)
- **`secrets.properties`**: Sensitive configuration (gitignored)

## 📱 Usage

### Main Features

1. **Image Grid**: The main screen displays a 7×10 grid of images
2. **Pull to Refresh**: Swipe down to refresh the image data
3. **Add Image**: Use the floating action button to add new images
4. **Image Details**: Tap on any image to view details
5. **Background Sync**: Data automatically syncs in the background

### Permissions

The application requests notification permissions on Android 13+ devices to provide background sync notifications. This permission is optional and the app works without it.

## 🧪 Testing

The project includes comprehensive testing:

### Unit Tests
- ViewModels
- Use Cases
- Repository implementations
- Utility classes

### Instrumented Tests
- UI tests for main functionality
- Database operations
- Network operations

### Running Tests

```bash
# Run all unit tests
./gradlew test

# Run all instrumented tests
./gradlew connectedAndroidTest

# Run specific test class
./gradlew test --tests "com.elinex.imagestesttask.presentation.MainActivityViewModelTest"
```

## 🔧 Build Variants

- **Debug**: Development build with debugging enabled
- **Release**: Production build with optimizations

### Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Build and install debug APK
./gradlew installDebug
```

## 📊 Performance

The application is optimized for performance:

- **Efficient Image Loading**: Coil with memory and disk caching
- **Background Processing**: WorkManager for reliable background tasks
- **Database Optimization**: Room with efficient queries and indexing
- **Memory Management**: Proper lifecycle management and view binding
- **Network Optimization**: OkHttp with connection pooling and caching

## 🔒 Security

- **Network Security**: HTTPS-only network requests
- **Permission Handling**: Minimal permission requirements

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style

- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Add comprehensive comments for complex logic
- Write unit tests for new functionality
- Follow the existing architecture patterns

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Team

- **Elinext Test Task Team**
- **Version**: 1.0.0
- **Last Updated**: 2024

## 📞 Support

For support and questions:
- Create an issue in the repository
- Contact the development team
- Check the documentation in the code comments

## 🔄 Changelog

### Version 1.0.0
- Initial release
- Complete image grid functionality
- Background synchronization
- Modern Android architecture implementation
- Comprehensive testing suite

---

**Note**: This is a demo application showcasing modern Android development practices. The `secrets.properties` file is included for demonstration purposes.
