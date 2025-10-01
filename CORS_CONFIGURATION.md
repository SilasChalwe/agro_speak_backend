# Cross-Site Configuration Setup

This document explains the Cross-Origin Resource Sharing (CORS) and Cross-Site Request Forgery (CSRF) configurations added to your AgroSpeak backend application.

## 🔒 Security Configurations Added

### 1. CORS Configuration

**Files Modified/Added:**
- `SecurityConfig.java` - Main security configuration with simplified CORS setup
- `CorsProperties.java` - Configuration properties for CORS settings (optional)
- `application.properties` - CORS-related properties
- ~~`CorsConfig.java` - Removed (was redundant)~~

**Current Allowed Origins:**
- `http://localhost:3000` (React development server)
- `http://localhost:4200` (Angular development server)
- `http://localhost:8080` (Vue.js development server)
- `https://yourdomain.com` (Replace with your production domain)

### 2. CSRF Configuration

**Files Added:**
- `CsrfConfig.java` - CSRF token repository configuration (currently disabled for API)
- `SecurityController.java` - Endpoints for CSRF tokens and CORS testing

**Current Status:** CSRF is disabled for API endpoints since you're using JWT tokens for authentication.

## 🛠️ Configuration Options

### Updating Allowed Origins

**Method 1: Update application.properties**
```properties
cors.allowed-origins=http://localhost:3000,http://your-frontend-domain.com
```

**Method 2: Update SecurityConfig.java**
```java
configuration.setAllowedOriginPatterns(Arrays.asList(
    "http://localhost:3000",
    "https://your-production-domain.com"
));
```

### Enabling CSRF Protection (if needed)

If you need CSRF protection for form-based authentication, update `SecurityConfig.java`:

```java
http.csrf(csrf -> csrf
    .csrfTokenRepository(csrfTokenRepository())
    .ignoringRequestMatchers("/api/v1/auth/**") // Ignore for API endpoints
)
```

## 🧪 Testing CORS Configuration

### Test Endpoints

1. **Health Check:**
   ```
   GET /api/v1/security/health
   ```

2. **CSRF Token (if enabled):**
   ```
   GET /api/v1/security/csrf-token
   ```

### Frontend Integration

**JavaScript/React Example:**
```javascript
// Test CORS configuration
fetch('http://localhost:8080/api/v1/security/health', {
  method: 'GET',
  credentials: 'include', // Include cookies/credentials
  headers: {
    'Content-Type': 'application/json',
  },
})
.then(response => response.json())
.then(data => console.log('CORS working:', data));
```

**Angular Example:**
```typescript
// In your service
import { HttpClient } from '@angular/common/http';

constructor(private http: HttpClient) {}

testCors() {
  return this.http.get('http://localhost:8080/api/v1/security/health', {
    withCredentials: true
  });
}
```

## 🚀 Production Deployment

### 1. Update Production Origins

Replace placeholder domains in the following files:

**SecurityConfig.java:**
```java
configuration.setAllowedOriginPatterns(Arrays.asList(
    "https://your-production-domain.com",
    "https://api.your-domain.com"
));
```

**application.properties:**
```properties
cors.allowed-origins=https://your-production-domain.com,https://api.your-domain.com
```

### 2. Environment-Specific Configuration

Create environment-specific property files:

- `application-dev.properties` - Development settings
- `application-prod.properties` - Production settings

**application-prod.properties:**
```properties
cors.allowed-origins=https://your-production-domain.com
cors.allow-credentials=true
server.port=8080
```

## 🔍 Troubleshooting

### Common CORS Issues

1. **Preflight Request Failing:**
   - Ensure OPTIONS method is allowed
   - Check if all required headers are in `allowedHeaders`

2. **Credentials Not Working:**
   - Set `allowCredentials=true`
   - Use `allowedOriginPatterns` instead of `allowedOrigins` when using credentials

3. **Production Domain Issues:**
   - Update all origin configurations
   - Ensure HTTPS is used in production
   - Check domain spelling and protocol

### Debug Headers

Add these headers to responses for debugging:
```java
response.setHeader("Access-Control-Allow-Origin", origin);
response.setHeader("Access-Control-Allow-Credentials", "true");
response.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
```

## 📝 Additional Security Considerations

1. **JWT Token Security:**
   - Store JWT tokens securely (HttpOnly cookies recommended)
   - Set proper token expiration times
   - Use HTTPS in production

2. **CORS Security:**
   - Never use `*` for origins in production
   - Regularly review and update allowed origins
   - Use specific origins, not wildcard patterns when possible

3. **HTTPS Enforcement:**
   - Enable HTTPS in production
   - Use secure cookies
   - Set proper Content Security Policy headers

## 🔧 Configuration Files Summary

| File | Purpose |
|------|---------|
| `SecurityConfig.java` | Main security and CORS configuration |
| `CorsConfig.java` | Additional Spring MVC CORS settings |
| `CorsProperties.java` | Externalized CORS configuration properties |
| `CsrfConfig.java` | CSRF token configuration (optional) |
| `SecurityController.java` | Testing and utility endpoints |
| `application.properties` | Application-level CORS settings |

Your CORS configuration is now complete and ready for both development and production use!