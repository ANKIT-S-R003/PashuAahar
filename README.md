# Pashu Aahar (पशु आहार)

Pashu Aahar is an Android application designed to help farmers and cattle owners manage their livestock more effectively. The app focuses on optimizing animal nutrition by calculating cost-effective feed recipes, tracking cow profiles, and providing essential veterinary tips.

## 🚀 Features

- **📊 Smart Dashboard**: Provides a quick overview of your livestock status, milk yield trends, and recent feed optimizations using intuitive charts.
- **🐄 Cow Profile Management**: Add and manage detailed profiles for each animal, including name, breed, age, weight, and daily milk yield.
- **⚖️ Feed Recipe Optimizer**: Calculate balanced feed mixtures based on available ingredients. Compare home-made feed costs with market prices to maximize savings.
- **🎤 Voice-Assisted Entry**: Quickly add cattle profiles using voice commands for a hands-free experience.
- **history 📜 Recipe History**: Automatically save and revisit previous feed calculations to track nutritional changes and financial savings over time.
- **💡 Veterinary Tips**: Access a curated list of health and safety tips to ensure the well-being of your cattle.
- **🌐 Multi-language Support**: Includes support for both English and Hindi to cater to a wider range of users.
- **🌙 Modern UI**: Built with Jetpack Compose for a smooth, responsive, and user-friendly experience.

## 🛠️ Tech Stack

- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Language**: [Kotlin](https://kotlinlang.org/)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Local Database**: [Room Persistence Library](https://developer.android.com/training/data-storage/room)
- **Navigation**: Compose Navigation
- **Charts**: [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)
- **Dependency Injection**: ViewModel Factory for managing repositories and data sources.

## 📂 Project Structure

- `ui/`: Contains all Compose screens, themes, and the shared ViewModel.
- `data/`: Handles data models, Room database setup, and the repository pattern for data abstraction.
- `MainActivity.kt`: The main entry point and navigation host for the application.

## 📸 Screenshots

*(Add your screenshots here)*

## 🛠️ Getting Started

### Prerequisites

- Android Studio Koala | 2024.1.1 or newer
- JDK 17
- Android SDK 24+

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/Pashu_Aahar.git
   ```
2. Open the project in Android Studio.
3. Sync the project with Gradle files.
4. Run the app on an emulator or a physical device.

## 🤝 Contributing

Contributions are welcome! If you have suggestions for new features or improvements, feel free to open an issue or submit a pull request.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
