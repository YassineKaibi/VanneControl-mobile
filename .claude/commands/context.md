# IoT Piston Control System - Project Context

**Last Updated**: November 27, 2024
**Developer**: Yassine
**Repositories**: 
- Backend: `ZizyP/VanneControl`
- Mobile: `ChanazJms/MyApplicationV10`

---

## 🏗️ System Architecture

```
Mobile App (Android) ←→ Nginx ←→ Ktor Backend ←→ PostgreSQL
                                      ↓
                                Mosquitto MQTT
                                      ↓
                                ESP32 Devices
```

---

## 📱 MOBILE APP - Current State

**Repository**: `ChanazJms/MyApplicationV10`
**Branch for Development**: `MultiThreading`
**Language**: Kotlin
**Package**: `com.example.myapplicationv10`

### Existing Activities
- `MainActivity` - Entry point
- `LoginActivity` - Login screen (UI only, no API)
- `RegisterActivity` - Registration screen (UI only)
- `DashboardActivity` - Device list (UI only, no data)
- `ValveManagementActivity` - Control 8 pistons (UI only)
- `ProfileActivity` - User profile settings
- `HistoryActivity` - Action logs
- `StatisticsActivity` - Usage analytics

### What's Missing (PRIORITY TASKS)
1. **Network Layer** - NO API integration exists
   - Need: Retrofit setup
   - Need: API service interfaces
   - Need: Repository pattern
   - Need: Coroutines for async operations

2. **Authentication Flow**
   - Need: JWT token management
   - Need: Secure token storage
   - Need: Token refresh logic

3. **Real-time Updates**
   - Need: WebSocket client
   - Need: Real-time piston state updates
   - Need: Device status notifications

4. **State Management**
   - Need: ViewModels with StateFlow
   - Need: Proper lifecycle handling
   - Need: Error handling

---

## 🖥️ BACKEND - API Structure

**Repository**: `ZizyP/VanneControl`
**Framework**: Ktor 2.3 (Kotlin)
**Database**: PostgreSQL 15
**Port**: 8080
**Base URL**: `http://your-server.com:8080`

### Authentication Endpoints

**Register**
```http
POST /auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}

Response: 200 OK
{
  "token": "eyJhbGci...",
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "role": "user"
  }
}
```

**Login**
```http
POST /auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}

Response: 200 OK
{
  "token": "eyJhbGci...",
  "user": {
    "id": "uuid",
    "email": "user@example.com"
  }
}
```

### Device Endpoints

**Get All Devices**
```http
GET /devices
Authorization: Bearer {jwt_token}

Response: 200 OK
{
  "devices": [
    {
      "id": "uuid",
      "name": "Production Line A",
      "device_id": "ESP-A1-001",
      "status": "online",
      "last_seen": "2024-11-27T10:30:00Z",
      "pistons": [
        {
          "id": "uuid",
          "piston_number": 1,
          "state": "inactive",
          "last_triggered": null
        }
        // ... 8 pistons total
      ]
    }
  ]
}
```

**Get Single Device**
```http
GET /devices/{deviceId}
Authorization: Bearer {jwt_token}

Response: 200 OK
{
  "device": {
    "id": "uuid",
    "name": "Production Line A",
    "status": "online",
    "pistons": [...]
  }
}
```

**Control Piston**
```http
POST /devices/{deviceId}/pistons/{pistonNumber}
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "action": "activate"  // or "deactivate"
}

Response: 200 OK
{
  "message": "Piston activated",
  "piston": {
    "id": "uuid",
    "piston_number": 3,
    "state": "active",
    "last_triggered": "2024-11-27T10:30:00Z"
  }
}
```

### WebSocket Endpoint

**Connect**
```
ws://your-server.com:8080/ws
```

**Message Types Received**:
```json
// Piston Update
{
  "type": "piston_update",
  "device_id": "device-uuid",
  "piston_number": 3,
  "state": "active",
  "timestamp": "2024-11-27T10:30:00Z"
}

// Device Status
{
  "type": "device_status",
  "device_id": "device-uuid",
  "status": "online",
  "timestamp": "2024-11-27T10:30:00Z"
}
```

### Health Check
```http
GET /health

Response: 200 OK
{
  "status": "healthy",
  "timestamp": 1701087000000
}
```

---

## 🔐 Authentication Details

### JWT Token
- **Algorithm**: HS256
- **Issuer**: piston-control
- **Audience**: piston-control-api
- **Expiration**: 30 days
- **Claim**: Contains `userId`

### Usage
All authenticated endpoints require:
```
Authorization: Bearer {jwt_token}
```

### Storage (Mobile)
- Use Android KeyStore for secure storage
- Never store in SharedPreferences unencrypted
- Clear on logout

---

## 📦 Required Dependencies (build.gradle)

```gradle
dependencies {
    // Networking
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'
    
    // Kotlin Coroutines
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3'
    
    // Lifecycle + ViewModel
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2'
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.6.2'
    implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.6.2'
    
    // JSON
    implementation 'com.google.code.gson:gson:2.10.1'
    
    // Security
    implementation 'androidx.security:security-crypto:1.1.0-alpha06'
}
```

---

## 📂 Recommended Package Structure

```
app/src/main/java/com/example/myapplicationv10/
├── network/
│   ├── ApiClient.kt              # Retrofit configuration
│   ├── ApiService.kt             # API endpoints interface
│   ├── NetworkResult.kt          # Sealed class for results
│   └── interceptors/
│       ├── AuthInterceptor.kt    # Adds JWT to requests
│       └── LoggingInterceptor.kt # Request/response logging
├── repository/
│   ├── AuthRepository.kt         # Authentication operations
│   ├── DeviceRepository.kt       # Device CRUD operations
│   └── PistonRepository.kt       # Piston control operations
├── viewmodel/
│   ├── LoginViewModel.kt
│   ├── RegisterViewModel.kt
│   ├── DashboardViewModel.kt
│   └── ValveManagementViewModel.kt
├── websocket/
│   ├── WebSocketManager.kt       # WebSocket client
│   └── WebSocketMessage.kt       # Message types
├── utils/
│   ├── TokenManager.kt           # Secure token storage
│   ├── NetworkUtils.kt           # Network state checking
│   └── Constants.kt              # API URLs, etc.
└── model/
    ├── User.kt
    ├── Device.kt
    ├── Piston.kt
    └── ApiResponses.kt
```

---

## 🎯 Implementation Priority

### Phase 1: Core Networking (CURRENT)
1. Create `NetworkResult.kt` sealed class
2. Create `ApiClient.kt` with Retrofit
3. Create `ApiService.kt` interface
4. Create `TokenManager.kt` for secure storage
5. Create `AuthRepository.kt`
6. Update `LoginActivity` to use real API

### Phase 2: Device Management
1. Create `DeviceRepository.kt`
2. Create `DashboardViewModel.kt`
3. Update `DashboardActivity` with ViewModel
4. Create RecyclerView adapter for devices

### Phase 3: Piston Control
1. Create `PistonRepository.kt`
2. Create `ValveManagementViewModel.kt`
3. Update `ValveManagementActivity`
4. Implement piston control buttons

### Phase 4: Real-time Updates
1. Create `WebSocketManager.kt`
2. Connect to WebSocket on app start
3. Update UI on piston state changes
4. Handle reconnection logic

### Phase 5: Polish
1. Add loading states
2. Add error handling
3. Add offline mode detection
4. Add pull-to-refresh
5. Add user feedback (toasts, snackbars)

---

## 🔥 Code Patterns to Follow

### 1. Use Sealed Class for Results
```kotlin
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String) : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>()
}
```

### 2. Use Coroutines, Not Threads
```kotlin
// Good ✅
viewModelScope.launch {
    val result = repository.getDevices()
    _state.value = result
}

// Bad ❌
Thread {
    // Don't use raw threads
}.start()
```

### 3. Use StateFlow for Reactive UI
```kotlin
// ViewModel
private val _devices = MutableStateFlow<NetworkResult<List<Device>>>(NetworkResult.Loading)
val devices: StateFlow<NetworkResult<List<Device>>> = _devices

// Activity
lifecycleScope.launch {
    viewModel.devices.collect { result ->
        when (result) {
            is NetworkResult.Success -> updateUI(result.data)
            is NetworkResult.Error -> showError(result.message)
            is NetworkResult.Loading -> showLoading()
        }
    }
}
```

### 4. Always Use withContext for Threading
```kotlin
suspend fun fetchData(): Result<Data> = withContext(Dispatchers.IO) {
    // Network call happens on IO thread
    apiService.getData()
}
```

---

## 🔧 Hardware Context (For Reference)

**Device**: ESP32-S3 with A7670E 4G modem
**SIM Card**: 1NCE (500MB for 10 years)
**Data Constraint**: ~126 MB over 10 years at 30-second heartbeat
**Protocol**: Binary MQTT (92% smaller than JSON)
**Pistons**: 8 per device
**Relay Control**: GPIO pins to 5V relay modules
**Power**: 24V solenoid valves with 2A fuse protection

---

## 🚨 Critical Notes

1. **Always use HTTPS in production** (currently HTTP for development)
2. **Never log JWT tokens** in production builds
3. **Handle 401 Unauthorized** → redirect to login
4. **Implement token refresh** before 30-day expiration
5. **Test offline mode** gracefully
6. **Use ProGuard/R8** to obfuscate release builds

---

## 📞 Testing Backend Locally

```bash
# Health check
curl http://localhost:8080/health

# Register
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"test123"}'

# Login (get token)
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"test123"}'

# Get devices (use token from login)
curl http://localhost:8080/devices \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

---

## 🎓 Learning Resources

- **Kotlin Coroutines**: https://kotlinlang.org/docs/coroutines-guide.html
- **Retrofit**: https://square.github.io/retrofit/
- **Android MVVM**: https://developer.android.com/topic/architecture
- **StateFlow**: https://developer.android.com/kotlin/flow/stateflow-and-sharedflow

---

**End of Context Document**
