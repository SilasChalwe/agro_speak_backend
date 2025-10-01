# 🌾 AgroSpeak Backend API

A comprehensive REST API for agricultural applications providing user management, weather data, and farming insights.

**Repository**: `git@github.com:SilasChalwe/agro_speak_backend.git`

## 📋 Table of Contents

- [Overview](#overview)
- [Quick Start](#quick-start)
- [Authentication](#authentication)
- [API Endpoints](#api-endpoints)
- [Frontend Integration Examples](#frontend-integration-examples)
- [Error Handling](#error-handling)
- [CORS Configuration](#cors-configuration)
- [Development Setup](#development-setup)
- [Contributing](#contributing)

## 🌟 Overview

AgroSpeak Backend provides:
- **User Management**: Registration, authentication, profile management
- **Weather Services**: Current weather, forecasts, soil data
- **File Upload**: Avatar management with 5MB limit
- **JWT Authentication**: Secure API access with 24-hour tokens
- **CORS Support**: Ready for web and mobile frontends
- **Agricultural Focus**: Specialized data for farming applications

**Base URL**: `http://localhost:8080`  
**Repository**: `git@github.com:SilasChalwe/agro_speak_backend.git`

## 🚀 Quick Start

### Prerequisites
- Java 17+
- MySQL 8.0+
- Maven 3.6+
- Git

### 1. Clone Repository
```bash
git clone git@github.com:SilasChalwe/agro_speak_backend.git
cd agro_speak_backend
```

### 2. Database Setup
```sql
CREATE DATABASE agro_speak_db;
CREATE USER 'agro_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON agro_speak_db.* TO 'agro_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Configure Application
Update `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/agro_speak_db
spring.datasource.username=agro_user
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

jwt.secret=your_super_secret_jwt_key_here_make_it_long_and_secure
jwt.expiration=86400000

spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=5MB

logging.level.com.nextinnomind.agro_speak_backend=INFO
logging.level.org.springframework.security=DEBUG
```

### 4. Run Application
```bash
# Using Maven wrapper
./mvnw spring-boot:run

# Or using Maven directly
mvn spring-boot:run
```

### 5. Verify Installation
```bash
# Test health endpoint
curl http://localhost:8080/api/v1/security/health

# Test weather endpoint
curl "http://localhost:8080/api/v1/weather/current?latitude=-1.286389&longitude=36.817223"
```

## 🔐 Authentication

### JWT Token Flow
1. **Register/Login** → Get JWT token
2. **Include token** in subsequent requests: `Authorization: Bearer <token>`
3. **Token expires** in 24 hours

### Public Endpoints (No Auth Required)
```
GET  /api/v1/auth/register          - User registration
POST /api/v1/auth/login             - User login
GET  /api/v1/weather/**             - All weather endpoints
GET  /api/v1/security/health        - Health check
```

### Protected Endpoints (Auth Required)
```
GET  /api/v1/user/profile           - Get user profile
PUT  /api/v1/user/profile           - Update user profile
PUT  /api/v1/user/profile/update    - Alternative update endpoint
POST /api/v1/user/avatar            - Upload user avatar
```

## 📡 API Endpoints

### 👤 Authentication

#### Register User
```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "fullName": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "phone": "+1234567890",
  "role": "farmer"
}
```

**Success Response (201)**:
```json
{
  "message": "User registered successfully",
  "user": {
    "id": 1,
    "email": "john@example.com",
    "fullName": "John Doe",
    "phone": "+1234567890",
    "role": "farmer",
    "createdAt": "2025-10-01T10:00:00.123"
  }
}
```

#### Login User
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

**Success Response (200)**:
```json
{
  "message": "Login successful",
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huQGV4YW1wbGUuY29tIiwiaWF0IjoxNjMzMDI0ODAwLCJleHAiOjE2MzMxMTEyMDB9...",
  "user": {
    "id": 1,
    "email": "john@example.com",
    "fullName": "John Doe",
    "role": "farmer"
  }
}
```

### 👨‍🌾 User Profile Management

#### Get User Profile
```http
GET /api/v1/user/profile
Authorization: Bearer <jwt_token>
```

**Success Response (200)**:
```json
{
  "id": 1,
  "email": "john@example.com",
  "fullName": "John Doe",
  "phone": "+1234567890",
  "avatarUrl": "/uploads/avatars/avatar_1_uuid.jpg",
  "createdAt": "2025-10-01T10:00:00.123",
  "updatedAt": "2025-10-01T15:30:00.456"
}
```

#### Update User Profile
```http
PUT /api/v1/user/profile
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "fullName": "Jane Doe",
  "phone": "+0987654321"
}
```

**Note**: Frontend can send either `"fullName"` or `"name"` field - both are supported.

#### Upload User Avatar
```http
POST /api/v1/user/avatar
Authorization: Bearer <jwt_token>
Content-Type: multipart/form-data

Form Data:
- avatar: [image file] (max 5MB, images only: jpg, png, gif, etc.)
```

**Success Response (200)**:
```json
{
  "message": "Avatar uploaded successfully",
  "avatarUrl": "/uploads/avatars/avatar_1_uuid.jpg",
  "user": {
    "id": 1,
    "email": "john@example.com",
    "fullName": "John Doe",
    "avatarUrl": "/uploads/avatars/avatar_1_uuid.jpg"
  }
}
```

### 🌤️ Weather Endpoints (Public Access)

#### Current Weather
```http
GET /api/v1/weather/current?latitude=-1.286389&longitude=36.817223
```

**Success Response (200)**:
```json
{
  "latitude": -1.286389,
  "longitude": 36.817223,
  "timezone": "Africa/Nairobi",
  "timezone_abbreviation": "EAT",
  "elevation": 1795.0,
  "current_weather": {
    "temperature": 22.5,
    "windspeed": 8.2,
    "winddirection": 45,
    "weathercode": 1,
    "is_day": 1,
    "time": "2025-10-01T15:00",
    "message": "Mainly clear"
  }
}
```

#### Daily Forecast
```http
GET /api/v1/weather/daily?latitude=-1.286389&longitude=36.817223&days=7
```

**Success Response (200)**:
```json
{
  "latitude": -1.286389,
  "longitude": 36.817223,
  "timezone": "Africa/Nairobi",
  "daily": {
    "time": ["2025-10-01", "2025-10-02", "2025-10-03"],
    "temperatureMax": [28.5, 29.1, 27.8],
    "temperatureMin": [18.2, 19.1, 17.5],
    "precipitationSum": [0.0, 2.1, 0.0],
    "weathercode": [1, 61, 2],
    "messages": ["Mainly clear", "Rainy", "Partly cloudy"]
  }
}
```

#### Hourly Forecast
```http
GET /api/v1/weather/hourly?latitude=-1.286389&longitude=36.817223&hours=24
```

#### Soil Data (Agricultural Focus)
```http
GET /api/v1/weather/soil?latitude=-1.286389&longitude=36.817223
```

**Success Response (200)**:
```json
{
  "latitude": -1.286389,
  "longitude": 36.817223,
  "hourly": {
    "time": ["2025-10-01T00:00", "2025-10-01T01:00"],
    "soilTemperature": [19.5, 19.3],
    "soilMoisture": [0.25, 0.24]
  }
}
```

## 💻 Frontend Integration Examples

### React/JavaScript Integration

```javascript
// AgroSpeakAPI.js
const API_BASE_URL = 'http://localhost:8080';

class AgroSpeakAPI {
  constructor() {
    this.token = localStorage.getItem('agro_jwt_token');
  }

  // Set authorization header
  getAuthHeaders() {
    return {
      'Content-Type': 'application/json',
      ...(this.token && { 'Authorization': `Bearer ${this.token}` })
    };
  }

  // Authentication Methods
  async register(userData) {
    const response = await fetch(`${API_BASE_URL}/api/v1/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(userData)
    });
    
    if (!response.ok) {
      throw new Error(`Registration failed: ${response.status}`);
    }
    
    return response.json();
  }

  async login(email, password) {
    const response = await fetch(`${API_BASE_URL}/api/v1/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });
    
    if (!response.ok) {
      throw new Error(`Login failed: ${response.status}`);
    }
    
    const data = await response.json();
    if (data.token) {
      localStorage.setItem('agro_jwt_token', data.token);
      this.token = data.token;
    }
    return data;
  }

  logout() {
    localStorage.removeItem('agro_jwt_token');
    this.token = null;
  }

  // User Profile Methods
  async getProfile() {
    const response = await fetch(`${API_BASE_URL}/api/v1/user/profile`, {
      headers: this.getAuthHeaders()
    });
    
    if (response.status === 401) {
      this.logout();
      throw new Error('Session expired. Please login again.');
    }
    
    return response.json();
  }

  async updateProfile(profileData) {
    const response = await fetch(`${API_BASE_URL}/api/v1/user/profile`, {
      method: 'PUT',
      headers: this.getAuthHeaders(),
      body: JSON.stringify(profileData)
    });
    
    if (!response.ok) {
      throw new Error(`Profile update failed: ${response.status}`);
    }
    
    return response.json();
  }

  async uploadAvatar(file) {
    const formData = new FormData();
    formData.append('avatar', file);

    const response = await fetch(`${API_BASE_URL}/api/v1/user/avatar`, {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${this.token}` },
      body: formData
    });
    
    return response.json();
  }

  // Weather Methods (No Auth Required)
  async getCurrentWeather(latitude, longitude) {
    const response = await fetch(
      `${API_BASE_URL}/api/v1/weather/current?latitude=${latitude}&longitude=${longitude}`
    );
    return response.json();
  }

  async getDailyForecast(latitude, longitude, days = 7) {
    const response = await fetch(
      `${API_BASE_URL}/api/v1/weather/daily?latitude=${latitude}&longitude=${longitude}&days=${days}`
    );
    return response.json();
  }

  async getHourlyForecast(latitude, longitude, hours = 24) {
    const response = await fetch(
      `${API_BASE_URL}/api/v1/weather/hourly?latitude=${latitude}&longitude=${longitude}&hours=${hours}`
    );
    return response.json();
  }

  async getSoilData(latitude, longitude) {
    const response = await fetch(
      `${API_BASE_URL}/api/v1/weather/soil?latitude=${latitude}&longitude=${longitude}`
    );
    return response.json();
  }
}

// Usage Examples
const api = new AgroSpeakAPI();

// Registration
api.register({
  fullName: "John Farmer",
  email: "john@farm.com",
  password: "securepass123",
  phone: "+1234567890",
  role: "farmer"
}).then(result => {
  console.log('User registered:', result);
}).catch(error => {
  console.error('Registration error:', error.message);
});

// Login
api.login("john@farm.com", "securepass123")
  .then(result => {
    console.log('Logged in:', result);
    return api.getProfile();
  })
  .then(profile => {
    console.log('User profile:', profile);
  });

// Weather data
api.getCurrentWeather(-1.286389, 36.817223)
  .then(weather => {
    console.log('Current weather:', weather.current_weather.message);
    console.log('Temperature:', weather.current_weather.temperature + '°C');
  });

export default AgroSpeakAPI;
```

### React Hook Example

```javascript
// useAgroSpeak.js - Custom React Hook
import { useState, useEffect } from 'react';
import AgroSpeakAPI from './AgroSpeakAPI';

export function useAgroSpeak() {
  const [api] = useState(() => new AgroSpeakAPI());
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Auto-load user profile if token exists
  useEffect(() => {
    if (api.token) {
      loadProfile();
    }
  }, []);

  const loadProfile = async () => {
    try {
      setLoading(true);
      const profile = await api.getProfile();
      setUser(profile);
      setError(null);
    } catch (err) {
      setError(err.message);
      setUser(null);
    } finally {
      setLoading(false);
    }
  };

  const login = async (email, password) => {
    try {
      setLoading(true);
      const result = await api.login(email, password);
      setUser(result.user);
      setError(null);
      return result;
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    api.logout();
    setUser(null);
    setError(null);
  };

  return {
    api,
    user,
    loading,
    error,
    login,
    logout,
    loadProfile,
    isAuthenticated: !!user
  };
}

// Component usage example
function WeatherDashboard() {
  const { api } = useAgroSpeak();
  const [weather, setWeather] = useState(null);

  useEffect(() => {
    // Get user's location and fetch weather
    navigator.geolocation.getCurrentPosition(async (position) => {
      const { latitude, longitude } = position.coords;
      const weatherData = await api.getCurrentWeather(latitude, longitude);
      setWeather(weatherData);
    });
  }, [api]);

  return (
    <div>
      {weather && (
        <div>
          <h2>Current Weather</h2>
          <p>Temperature: {weather.current_weather.temperature}°C</p>
          <p>Condition: {weather.current_weather.message}</p>
        </div>
      )}
    </div>
  );
}
```

### Flutter/Dart Integration

```dart
// agro_speak_api.dart
import 'dart:convert';
import 'dart:io';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';

class AgroSpeakAPI {
  static const String _baseUrl = 'http://10.0.2.2:8080'; // Android emulator
  // Use 'http://localhost:8080' for iOS simulator
  // Use your actual IP for physical devices
  
  String? _token;

  AgroSpeakAPI() {
    _loadToken();
  }

  Future<void> _loadToken() async {
    final prefs = await SharedPreferences.getInstance();
    _token = prefs.getString('agro_jwt_token');
  }

  Future<void> _saveToken(String token) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('agro_jwt_token', token);
    _token = token;
  }

  Future<void> _clearToken() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove('agro_jwt_token');
    _token = null;
  }

  Map<String, String> _getHeaders({bool includeAuth = true}) {
    final headers = <String, String>{
      'Content-Type': 'application/json',
    };
    
    if (includeAuth && _token != null) {
      headers['Authorization'] = 'Bearer $_token';
    }
    
    return headers;
  }

  // Authentication Methods
  Future<Map<String, dynamic>> register({
    required String fullName,
    required String email,
    required String password,
    String? phone,
    String role = 'farmer',
  }) async {
    final response = await http.post(
      Uri.parse('$_baseUrl/api/v1/auth/register'),
      headers: _getHeaders(includeAuth: false),
      body: json.encode({
        'fullName': fullName,
        'email': email,
        'password': password,
        'phone': phone,
        'role': role,
      }),
    );

    if (response.statusCode != 201) {
      throw Exception('Registration failed: ${response.body}');
    }

    return json.decode(response.body);
  }

  Future<Map<String, dynamic>> login(String email, String password) async {
    final response = await http.post(
      Uri.parse('$_baseUrl/api/v1/auth/login'),
      headers: _getHeaders(includeAuth: false),
      body: json.encode({
        'email': email,
        'password': password,
      }),
    );

    if (response.statusCode != 200) {
      throw Exception('Login failed: ${response.body}');
    }

    final data = json.decode(response.body);
    if (data['token'] != null) {
      await _saveToken(data['token']);
    }

    return data;
  }

  Future<void> logout() async {
    await _clearToken();
  }

  // User Profile Methods
  Future<Map<String, dynamic>> getProfile() async {
    final response = await http.get(
      Uri.parse('$_baseUrl/api/v1/user/profile'),
      headers: _getHeaders(),
    );

    if (response.statusCode == 401) {
      await _clearToken();
      throw Exception('Session expired. Please login again.');
    }

    if (response.statusCode != 200) {
      throw Exception('Failed to get profile: ${response.body}');
    }

    return json.decode(response.body);
  }

  Future<Map<String, dynamic>> updateProfile({
    String? fullName,
    String? phone,
  }) async {
    final body = <String, dynamic>{};
    if (fullName != null) body['fullName'] = fullName;
    if (phone != null) body['phone'] = phone;

    final response = await http.put(
      Uri.parse('$_baseUrl/api/v1/user/profile'),
      headers: _getHeaders(),
      body: json.encode(body),
    );

    if (response.statusCode != 200) {
      throw Exception('Failed to update profile: ${response.body}');
    }

    return json.decode(response.body);
  }

  Future<Map<String, dynamic>> uploadAvatar(File imageFile) async {
    final request = http.MultipartRequest(
      'POST',
      Uri.parse('$_baseUrl/api/v1/user/avatar'),
    );

    request.headers['Authorization'] = 'Bearer $_token';
    request.files.add(
      await http.MultipartFile.fromPath('avatar', imageFile.path),
    );

    final streamedResponse = await request.send();
    final response = await http.Response.fromStream(streamedResponse);

    if (response.statusCode != 200) {
      throw Exception('Failed to upload avatar: ${response.body}');
    }

    return json.decode(response.body);
  }

  // Weather Methods
  Future<Map<String, dynamic>> getCurrentWeather(
    double latitude,
    double longitude,
  ) async {
    final response = await http.get(
      Uri.parse('$_baseUrl/api/v1/weather/current?latitude=$latitude&longitude=$longitude'),
    );

    if (response.statusCode != 200) {
      throw Exception('Failed to get weather: ${response.body}');
    }

    return json.decode(response.body);
  }

  Future<Map<String, dynamic>> getDailyForecast(
    double latitude,
    double longitude, {
    int days = 7,
  }) async {
    final response = await http.get(
      Uri.parse('$_baseUrl/api/v1/weather/daily?latitude=$latitude&longitude=$longitude&days=$days'),
    );

    return json.decode(response.body);
  }

  Future<Map<String, dynamic>> getSoilData(
    double latitude,
    double longitude,
  ) async {
    final response = await http.get(
      Uri.parse('$_baseUrl/api/v1/weather/soil?latitude=$latitude&longitude=$longitude'),
    );

    return json.decode(response.body);
  }
}

// Usage in Flutter Widget
class WeatherScreen extends StatefulWidget {
  @override
  _WeatherScreenState createState() => _WeatherScreenState();
}

class _WeatherScreenState extends State<WeatherScreen> {
  final AgroSpeakAPI _api = AgroSpeakAPI();
  Map<String, dynamic>? _weather;
  bool _loading = true;

  @override
  void initState() {
    super.initState();
    _loadWeather();
  }

  Future<void> _loadWeather() async {
    try {
      // Example coordinates for Nairobi, Kenya
      final weather = await _api.getCurrentWeather(-1.286389, 36.817223);
      setState(() {
        _weather = weather;
        _loading = false;
      });
    } catch (e) {
      setState(() {
        _loading = false;
      });
      print('Error loading weather: $e');
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_loading) {
      return Center(child: CircularProgressIndicator());
    }

    if (_weather == null) {
      return Center(child: Text('Failed to load weather'));
    }

    final currentWeather = _weather!['current_weather'];
    
    return Column(
      children: [
        Text('Temperature: ${currentWeather['temperature']}°C'),
        Text('Condition: ${currentWeather['message']}'),
        Text('Wind Speed: ${currentWeather['windspeed']} km/h'),
      ],
    );
  }
}
```

## ⚠️ Error Handling

### HTTP Status Codes

| Status | Meaning | Common Causes |
|--------|---------|---------------|
| 200    | OK | Request successful |
| 201    | Created | User registration successful |
| 400    | Bad Request | Invalid input data, missing required fields |
| 401    | Unauthorized | Invalid JWT token, expired token, missing token |
| 404    | Not Found | User not found, endpoint doesn't exist |
| 413    | Payload Too Large | File upload exceeds 5MB limit |
| 500    | Internal Server Error | Database connection issues, external API failures |

### Error Response Format

```json
{
  "error": "Authentication failed - please login again",
  "timestamp": 1633024800000
}
```

### Common Error Scenarios

#### Authentication Errors
```javascript
// Handle authentication errors
async function makeAuthenticatedRequest(apiCall) {
  try {
    return await apiCall();
  } catch (error) {
    if (error.message.includes('Session expired') || 
        error.message.includes('Authentication failed')) {
      // Redirect to login
      localStorage.removeItem('agro_jwt_token');
      window.location.href = '/login';
    }
    throw error;
  }
}
```

#### File Upload Errors
```javascript
// Handle file upload validation
function validateAvatarFile(file) {
  // Check file size (5MB limit)
  if (file.size > 5 * 1024 * 1024) {
    throw new Error('File size must be less than 5MB');
  }
  
  // Check file type
  if (!file.type.startsWith('image/')) {
    throw new Error('Only image files are allowed');
  }
  
  return true;
}
```

#### Weather API Errors
```javascript
// Handle weather API errors with fallback
async function getWeatherWithFallback(lat, lon) {
  try {
    return await api.getCurrentWeather(lat, lon);
  } catch (error) {
    console.warn('Weather API unavailable, using cached data');
    return getCachedWeatherData(lat, lon);
  }
}
```

## 🌐 CORS Configuration

The API supports CORS for these origins by default:
- `http://localhost:3000` (React development server)
- `http://localhost:4200` (Angular development server)
- `http://localhost:8080` (Vue.js development server)

### Adding Production Domains

Update `SecurityConfig.java` for production:

```java
config.setAllowedOriginPatterns(Arrays.asList(
    "http://localhost:3000",           // Development
    "https://yourdomain.com",          // Production
    "https://*.yourdomain.com",        // Subdomains
    "https://your-app.vercel.app",     // Vercel deployment
    "https://your-app.netlify.app"     // Netlify deployment
));
```

## 🛠️ Development Setup

### Project Structure
```
agro_speak_backend/
├── src/main/java/com/nextinnomind/agro_speak_backend/
│   ├── controllers/           # REST endpoints
│   │   ├── AuthController.java
│   │   ├── UserController.java
│   │   └── WeatherController.java
│   ├── entity/               # JPA entities
│   │   └── User.java
│   ├── model/                # DTOs and response models
│   │   └── WeatherResponse.java
│   ├── service/              # Business logic
│   │   └── WeatherService.java
│   ├── security/             # JWT and security config
│   │   ├── JwtUtil.java
│   │   ├── JwtAuthenticationFilter.java
│   │   └── AuthEntryPointJwt.java
│   ├── config/               # Configuration classes
│   │   ├── SecurityConfig.java
│   │   ├── WebConfig.java
│   │   └── RestTemplateConfig.java
│   └── repository/           # Data access layer
│       └── UserRepository.java
├── src/main/resources/
│   └── application.properties
├── uploads/avatars/          # User avatar storage
├── pom.xml                   # Maven dependencies
└── README.md                 # This file
```

### Environment Variables

Create `.env` file for development:
```env
# Database Configuration
DB_URL=jdbc:mysql://localhost:3306/agro_speak_db
DB_USERNAME=agro_user
DB_PASSWORD=your_secure_password

# JWT Configuration
JWT_SECRET=your_super_secret_jwt_key_make_it_at_least_32_characters_long
JWT_EXPIRATION=86400000

# File Upload Configuration
MAX_FILE_SIZE=5MB
MAX_REQUEST_SIZE=5MB

# Logging Configuration
LOG_LEVEL=INFO
```

### Testing Endpoints

#### Using cURL
```bash
# Health check
curl -X GET http://localhost:8080/api/v1/security/health

# Register user
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test User",
    "email": "test@example.com",
    "password": "password123",
    "phone": "+1234567890",
    "role": "farmer"
  }'

# Login user
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'

# Get weather data
curl -X GET "http://localhost:8080/api/v1/weather/current?latitude=-1.286389&longitude=36.817223"

# Get user profile (replace TOKEN with actual JWT)
curl -X GET http://localhost:8080/api/v1/user/profile \
  -H "Authorization: Bearer TOKEN"
```

#### Postman Collection

Import this collection into Postman:

```json
{
  "info": {
    "name": "AgroSpeak Backend API",
    "description": "Complete API collection for AgroSpeak agricultural platform",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://localhost:8080"
    },
    {
      "key": "token",
      "value": ""
    }
  ],
  "item": [
    {
      "name": "Authentication",
      "item": [
        {
          "name": "Register User",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"fullName\": \"John Farmer\",\n  \"email\": \"john@farm.com\",\n  \"password\": \"password123\",\n  \"phone\": \"+1234567890\",\n  \"role\": \"farmer\"\n}"
            },
            "url": {
              "raw": "{{baseUrl}}/api/v1/auth/register",
              "host": ["{{baseUrl}}"],
              "path": ["api", "v1", "auth", "register"]
            }
          }
        },
        {
          "name": "Login User",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"email\": \"john@farm.com\",\n  \"password\": \"password123\"\n}"
            },
            "url": {
              "raw": "{{baseUrl}}/api/v1/auth/login",
              "host": ["{{baseUrl}}"],
              "path": ["api", "v1", "auth", "login"]
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "exec": [
                  "if (pm.response.code === 200) {",
                  "    const jsonData = pm.response.json();",
                  "    if (jsonData.token) {",
                  "        pm.collectionVariables.set('token', jsonData.token);",
                  "    }",
                  "}"
                ]
              }
            }
          ]
        }
      ]
    },
    {
      "name": "User Profile",
      "item": [
        {
          "name": "Get Profile",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/v1/user/profile",
              "host": ["{{baseUrl}}"],
              "path": ["api", "v1", "user", "profile"]
            }
          }
        }
      ]
    },
    {
      "name": "Weather",
      "item": [
        {
          "name": "Current Weather",
          "request": {
            "method": "GET",
            "url": {
              "raw": "{{baseUrl}}/api/v1/weather/current?latitude=-1.286389&longitude=36.817223",
              "host": ["{{baseUrl}}"],
              "path": ["api", "v1", "weather", "current"],
              "query": [
                {
                  "key": "latitude",
                  "value": "-1.286389"
                },
                {
                  "key": "longitude",
                  "value": "36.817223"
                }
              ]
            }
          }
        }
      ]
    }
  ]
}
```

### Sample Test Data

```json
{
  "users": [
    {
      "fullName": "John Farmer",
      "email": "john@farm.com",
      "password": "password123",
      "phone": "+1234567890",
      "role": "farmer"
    },
    {
      "fullName": "Jane Agricultural Specialist",
      "email": "jane@agro.com",
      "password": "securepass456",
      "phone": "+0987654321",
      "role": "specialist"
    }
  ],
  "locations": [
    {
      "name": "Nairobi, Kenya",
      "latitude": -1.286389,
      "longitude": 36.817223
    },
    {
      "name": "Lagos, Nigeria",
      "latitude": 6.5244,
      "longitude": 3.3792
    },
    {
      "name": "Cape Town, South Africa",
      "latitude": -33.9249,
      "longitude": 18.4241
    }
  ]
}
```

## 📈 Performance & Best Practices

### API Usage Guidelines

- **Rate Limiting**: Weather endpoints have no rate limiting, authentication endpoints are limited to 10 requests/minute per IP
- **Caching**: Weather data should be cached on frontend for 5-10 minutes
- **File Uploads**: Maximum 5MB per file, images only
- **JWT Tokens**: Expire in 24 hours, refresh tokens should be implemented in production

### Security Best Practices

1. **HTTPS in Production**: Always use HTTPS for production deployments
2. **Strong JWT Secrets**: Use at least 32 character random strings
3. **Input Validation**: Validate all user inputs on both frontend and backend
4. **File Upload Security**: Validate file types and sizes before upload
5. **CORS Configuration**: Only allow trusted domains in production

### Frontend Best Practices

```javascript
// 1. Implement proper error handling
const handleAPIError = (error) => {
  if (error.message.includes('Session expired')) {
    // Handle token expiration
    api.logout();
    redirectToLogin();
  } else if (error.message.includes('Network')) {
    // Handle network errors
    showOfflineMessage();
  } else {
    // Handle other errors
    showErrorMessage(error.message);
  }
};

// 2. Cache weather data
const weatherCache = new Map();
const CACHE_DURATION = 10 * 60 * 1000; // 10 minutes

const getCachedWeather = (lat, lon) => {
  const key = `${lat},${lon}`;
  const cached = weatherCache.get(key);
  
  if (cached && Date.now() - cached.timestamp < CACHE_DURATION) {
    return cached.data;
  }
  
  return null;
};

// 3. Implement retry logic
const apiWithRetry = async (apiCall, maxRetries = 3) => {
  for (let i = 0; i < maxRetries; i++) {
    try {
      return await apiCall();
    } catch (error) {
      if (i === maxRetries - 1) throw error;
      await new Promise(resolve => setTimeout(resolve, 1000 * (i + 1)));
    }
  }
};
```

## 🚀 Production Deployment

### Docker Deployment

Create `Dockerfile`:
```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/agro_speak_backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

Create `docker-compose.yml`:
```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: agro_speak_db
      MYSQL_USER: agro_user
      MYSQL_PASSWORD: ${DB_PASSWORD}
      MYSQL_ROOT_PASSWORD: ${DB_ROOT_PASSWORD}
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/agro_speak_db
      - SPRING_DATASOURCE_USERNAME=agro_user
      - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
    depends_on:
      - mysql

volumes:
  mysql_data:
```

### Environment Variables for Production

```bash
# Production .env file
DB_PASSWORD=your_super_secure_database_password
DB_ROOT_PASSWORD=your_super_secure_root_password
JWT_SECRET=your_super_secure_jwt_key_at_least_32_chars_long_for_production
```

### Build and Deploy Commands

```bash
# Build the application
./mvnw clean package -DskipTests

# Build Docker image
docker build -t agro-speak-backend .

# Run with Docker Compose
docker-compose up -d

# View logs
docker-compose logs -f app
```

### Health Checks

Monitor your application health:
```bash
# Health check endpoint
curl http://your-domain.com/api/v1/security/health

# Expected response
{
  "message": "CORS configuration is working",
  "status": "UP",
  "timestamp": 1633024800
}
```

## 🤝 Contributing

### Development Workflow

1. **Fork the repository**
   ```bash
   git clone git@github.com:YourUsername/agro_speak_backend.git
   cd agro_speak_backend
   ```

2. **Create feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **Make changes and test**
   ```bash
   ./mvnw test
   ./mvnw spring-boot:run
   ```

4. **Commit and push**
   ```bash
   git add .
   git commit -m "Add your feature description"
   git push origin feature/your-feature-name
   ```

5. **Create Pull Request**

### Code Style Guidelines

- Follow Java naming conventions
- Use meaningful variable and method names
- Add comprehensive logging for all endpoints
- Include proper error handling
- Write unit tests for new features
- Update documentation for API changes

### API Versioning

When making breaking changes:
- Create new version: `/api/v2/`
- Maintain backward compatibility
- Update documentation
- Notify frontend developers

## 📞 Support & Resources

### Documentation
- **API Base URL**: `http://localhost:8080`
- **Repository**: `git@github.com:SilasChalwe/agro_speak_backend.git`
- **Health Check**: `GET /api/v1/security/health`

### Getting Help

1. **Check server logs** for detailed error messages
2. **Use health endpoint** to verify API status
3. **Review this README** for integration examples
4. **Check CORS configuration** for frontend connection issues

### External APIs Used
- **Open-Meteo Weather API**: `https://api.open-meteo.com/v1/forecast`
  - Free weather data service
  - No API key required
  - Global coverage

### Common Coordinates for Testing
```json
{
  "nairobi": {"lat": -1.286389, "lon": 36.817223},
  "kampala": {"lat": 0.3476, "lon": 32.5825},
  "lagos": {"lat": 6.5244, "lon": 3.3792},
  "cape_town": {"lat": -33.9249, "lon": 18.4241},
  "addis_ababa": {"lat": 9.145, "lon": 40.4897}
}
```

---

## 🌾 **Happy Farming with AgroSpeak!**

This API is designed to empower modern agricultural applications with comprehensive user management, real-time weather data, and robust security features. Whether you're building a mobile app for farmers or a web platform for agricultural specialists, AgroSpeak Backend provides all the tools you need.

**Repository**: `git@github.com:SilasChalwe/agro_speak_backend.git`

For questions, contributions, or support, please check the repository issues or create a new one. Let's build the future of agriculture together! 🚀

---

*Last updated: October 2025*