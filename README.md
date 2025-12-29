# Piston Control Mobile

An Android application for controlling and monitoring industrial piston/valve systems through a backend API with real-time updates via WebSocket.

## Overview

This mobile application provides a user-friendly interface for managing industrial piston control systems. It features user authentication, real-time device monitoring, valve management, and comprehensive statistics tracking.

## Features

- **User Authentication**: Secure login and registration with JWT token-based authentication
- **Real-time Monitoring**: WebSocket integration for live device status updates
- **Device Management**: Control and monitor multiple devices with 8 pistons each
- **Valve Control**: Individual piston activation/deactivation with real-time feedback
- **Statistics Dashboard**: Visual representation of usage data using MPAndroidChart
- **History Tracking**: Complete audit trail of piston operations
- **Profile Management**: User profile viewing and editing capabilities
- **Offline Support**: Graceful handling of network connectivity issues

## Technical Stack

### Core Technologies
- **Language**: Kotlin
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36
- **Build System**: Gradle with Kotlin DSL

### Architecture
- **Pattern**: MVVM (Model-View-ViewModel)
- **Async Programming**: Kotlin Coroutines
- **Networking**: Retrofit 2.9.0 + OkHttp 4.12.0
- **Dependency Injection**: Manual DI with Repository pattern
- **UI**: ViewBinding enabled

### Key Dependencies

#### Networking & Communication
- Retrofit 2.9.0 - REST API client
- OkHttp 4.12.0 - HTTP client with logging interceptor
- Gson 2.10.1 - JSON serialization/deserialization
- Custom WebSocket implementation for real-time updates

#### UI & Visualization
- Material Design Components
- MPAndroidChart v3.1.0 - Statistics graphs and charts
- SwipeRefreshLayout - Pull-to-refresh functionality

#### Architecture Components
- Lifecycle ViewModel KTX 2.6.2
- LiveData KTX 2.6.2
- Coroutines (Android 1.7.3, Core 1.7.3)

#### Security
- AndroidX Security Crypto 1.1.0-alpha06 - Encrypted SharedPreferences
- Network Security Config - HTTPS enforcement

## Project Structure

```
app/src/main/java/com/example/myapplicationv10/
├── model/
│   ├── User.kt                 # User data model
│   ├── Device.kt               # Device data model
│   ├── Piston.kt               # Piston/valve data model
│   ├── ApiRequests.kt          # API request DTOs
│   └── ApiResponses.kt         # API response DTOs
├── network/
│   ├── ApiService.kt           # Retrofit API interface
│   ├── ApiClient.kt            # Retrofit client setup
│   ├── NetworkResult.kt        # Network result wrapper
│   └── interceptors/
│       └── AuthInterceptor.kt  # JWT token injection
├── repository/
│   ├── AuthRepository.kt       # Authentication logic
│   ├── DeviceRepository.kt     # Device data operations
│   ├── UserRepository.kt       # User profile operations
│   └── PistonRepository.kt     # Piston control operations
├── viewmodel/
│   ├── LoginViewModel.kt
│   ├── RegisterViewModel.kt
│   ├── DashboardViewModel.kt
│   ├── ValveManagementViewModel.kt
│   └── ProfileViewModel.kt
├── websocket/
│   ├── WebSocketManager.kt     # WebSocket connection handler
│   └── WebSocketMessage.kt     # WebSocket message models
├── utils/
│   ├── TokenManager.kt         # Secure token storage
│   └── Constants.kt            # App-wide constants
├── MainActivity.kt             # Landing screen
├── LoginActivity.kt            # User login
├── RegisterActivity.kt         # User registration
├── DashboardActivity.kt        # Main dashboard
├── ValveManagementActivity.kt  # Valve control interface
├── StatisticsActivity.kt       # Usage statistics
├── HistoryActivity.kt          # Operation history
├── ProfileActivity.kt          # User profile view
├── EditProfileActivity.kt      # Profile editing
├── ActiveValvesAdapter.kt      # RecyclerView adapter
├── HistoryAdapter.kt           # History RecyclerView adapter
├── MqttManager.kt              # MQTT client (legacy/alternative)
├── ThreadManager.kt            # Thread management utilities
└── PistonControlApplication.kt # Application class
```

## Setup & Configuration

### Prerequisites
- Android Studio Hedgehog or later
- JDK 11 or later
- Android SDK 36
- Backend API server running (default: http://10.0.2.2:8080/)

### Configuration Steps

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd MyApplicationV10
   ```

2. **Configure Backend URL**

   Edit `app/src/main/java/com/example/myapplicationv10/utils/Constants.kt`:

   ```kotlin
   const val BASE_URL = "http://10.0.2.2:8080/"  // For Android emulator
   // OR
   const val BASE_URL = "http://YOUR_IP:8080/"   // For physical device
   // OR
   const val BASE_URL = "https://your-domain.com/" // For production
   ```

3. **Configure WebSocket URL**

   In the same `Constants.kt` file:

   ```kotlin
   const val WEBSOCKET_URL = "ws://10.0.2.2:8080/ws"  // Match your BASE_URL
   ```

4. **Network Security Configuration**

   For development with HTTP (not recommended for production):
   - The app uses `network_security_config.xml` for cleartext traffic
   - For production, ensure HTTPS is configured

5. **Build and Run**
   ```bash
   ./gradlew build
   ```

## API Integration

The app expects a backend API with the following endpoints:

### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login

### User Management
- `GET /api/users/profile` - Get user profile
- `PUT /api/users/profile` - Update user profile

### Device Management
- `GET /api/devices` - List all devices
- `GET /api/devices/{id}` - Get device details
- `GET /api/devices/{id}/pistons` - Get device pistons

### Piston Control
- `POST /api/pistons/{id}/activate` - Activate a piston
- `POST /api/pistons/{id}/deactivate` - Deactivate a piston

### WebSocket
- `WS /ws` - Real-time updates (requires authentication via token query parameter)

## Security Features

1. **JWT Authentication**: Secure token-based authentication
2. **Encrypted Storage**: Tokens stored using AndroidX Security Crypto
3. **Auth Interceptor**: Automatic token injection in API requests
4. **Network Security Config**: Enforced HTTPS (with development exceptions)
5. **Password Security**: Passwords handled securely (never logged or stored locally)

## Recent Updates

- Migrated from ExecutorService to Kotlin Coroutines for better async handling
- Implemented MVVM architecture with Repository pattern
- Added WebSocket support for real-time updates
- Enhanced security with encrypted SharedPreferences
- Improved error handling with NetworkResult wrapper
- Fixed Kotlin deprecation warnings
- Added proper state management with Idle states

## Development

### Building the App
```bash
./gradlew assembleDebug
```

### Running Tests
```bash
./gradlew test
./gradlew connectedAndroidTest
```

### Code Style
- Follow Kotlin coding conventions
- Use ViewBinding (enabled in build.gradle.kts)
- Leverage Coroutines for async operations
- Maintain MVVM separation of concerns

## Troubleshooting

### Common Issues

1. **Connection Refused (10.0.2.2:8080)**
   - Ensure backend server is running
   - For physical devices, use actual IP address
   - Check firewall settings

2. **Cleartext HTTP Traffic Blocked**
   - Verify `network_security_config.xml` is properly configured
   - Use HTTPS in production

3. **WebSocket Connection Fails**
   - Ensure WebSocket URL matches backend configuration
   - Check authentication token is valid
   - Verify WebSocket endpoint is accessible

4. **Token Expiration**
   - App will redirect to login on 401 responses
   - Check token refresh mechanism if implemented

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Commit your changes (`git commit -m 'Add some feature'`)
4. Push to the branch (`git push origin feature/your-feature`)
5. Open a Pull Request

## License

This project is licensed under the Apache-2.0 License - see the [LICENSE](LICENSE) file for details.

## Contact

**Mohamed Yassine Kaibi**
- LinkedIn: [https://www.linkedin.com/in/mohamedyassinekaibi/](https://www.linkedin.com/in/mohamedyassinekaibi/)

## Acknowledgments

- HiveMQ MQTT Client (alternative to WebSocket)
- MPAndroidChart for visualization
- Android Jetpack components
