# User Profile Endpoints Documentation

## 🚀 New Endpoints Created

### 1. **GET /api/v1/user/profile**
- **Purpose**: Get current authenticated user's profile
- **Authentication**: Required (JWT token)
- **Response**: User profile information (excluding password)

**Example Response:**
```json
{
  "id": 1,
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890",
  "avatarUrl": "/uploads/avatars/avatar_1_abc123.jpg",
  "createdAt": "2025-09-30T21:00:00",
  "updatedAt": "2025-09-30T21:30:00"
}
```

### 2. **PUT /api/v1/user/profile/update**
- **Purpose**: Update user profile information
- **Authentication**: Required (JWT token)
- **Body**: JSON with fields to update

**Request Body Example:**
```json
{
  "firstName": "Jane",
  "lastName": "Smith",
  "phone": "+0987654321",
  "password": "newPassword123"
}
```

**Response:**
```json
{
  "message": "Profile updated successfully",
  "data": { /* updated user profile */ },
  "timestamp": 1759259415
}
```

### 3. **POST /api/v1/user/avatar**
- **Purpose**: Upload user avatar image
- **Authentication**: Required (JWT token)
- **Content-Type**: multipart/form-data
- **Field**: `avatar` (file)
- **Constraints**: 
  - Image files only
  - Max size: 5MB
  - Allowed formats: jpg, png, gif, etc.

**Response:**
```json
{
  "message": "Avatar uploaded successfully",
  "avatarUrl": "/uploads/avatars/avatar_1_unique_id.jpg",
  "user": { /* updated user profile */ }
}
```

## 📋 Updated User Entity

The User entity has been enhanced with:
- `firstName` and `lastName` (replacing fullName)
- `phone` - User's phone number
- `avatarUrl` - Path to uploaded avatar image
- `createdAt` - Automatic timestamp when user is created
- `updatedAt` - Automatic timestamp when user is updated
- `getFullName()` - Computed property for backward compatibility

## 🔍 Comprehensive Logging

All endpoints include detailed logging that shows:

### Request Logging:
```
=== PROFILE ENDPOINT ACCESSED ===
Method: GET | Endpoint: /api/v1/user/profile | IP: 192.168.1.100 | User-Agent: Mozilla/5.0...
Fetching profile for authenticated user: user@example.com
```

### Response Logging:
```
Profile successfully retrieved for user: user@example.com (ID: 1)
```

### Error Logging:
```
Error updating user profile from IP 192.168.1.100: Validation failed
```

## 🧪 Testing the Endpoints

### 1. Test Profile Retrieval
```bash
curl -X GET http://localhost:8080/api/v1/user/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```

### 2. Test Profile Update
```bash
curl -X PUT http://localhost:8080/api/v1/user/profile/update \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Updated",
    "lastName": "Name",
    "phone": "+1111111111"
  }'
```

### 3. Test Avatar Upload
```bash
curl -X POST http://localhost:8080/api/v1/user/avatar \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "avatar=@/path/to/image.jpg"
```

## 🔧 Configuration Added

### Application Properties:
```properties
# File Upload Configuration
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=5MB
spring.servlet.multipart.location=uploads/temp
```

### Static Resource Serving:
- Uploaded files are served at `/uploads/**`
- Files cached for 1 hour
- Upload directories created automatically

## 🛡️ Security Features

1. **Authentication Required**: All endpoints require valid JWT token
2. **File Validation**: 
   - Only image files allowed for avatars
   - File size limit (5MB)
   - Secure file naming with UUIDs
3. **Data Sanitization**: Input trimming and validation
4. **Error Handling**: Comprehensive error responses
5. **Logging**: Complete audit trail of all operations

## 📁 File Structure

```
uploads/
├── avatars/
│   ├── avatar_1_uuid1.jpg
│   ├── avatar_2_uuid2.png
│   └── ...
└── temp/
    └── (temporary upload files)
```

Your user profile endpoints are now fully functional with comprehensive logging! 🎉