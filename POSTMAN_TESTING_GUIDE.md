# 📚 Postman Testing Guide for AgroSpeak API

## 🚀 Prerequisites

1. **Start the application**: Make sure your Spring Boot app is running on `http://localhost:8080`
2. **Have test data**: You'll need a registered user to test the profile endpoints

## 📋 Endpoint Testing Steps

---

## **STEP 1: Register a User (Optional - if no user exists)**

### 🔗 **POST** `http://localhost:8080/api/v1/auth/register`

**Headers:**
```
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
  "fullName": "John Doe",
  "email": "john.doe@example.com",
  "password": "password123",
  "phone": "+1234567890",
  "role": "farmer"
}
```

**✅ Expected Response (201 Created):**
```json
{
  "message": "User registered successfully",
  "user": {
    "id": 1,
    "email": "john.doe@example.com",
    "fullName": "John Doe",
    "phone": "+1234567890",
    "role": "farmer",
    "createdAt": "2025-09-30T21:00:00.123"
  }
}
```

---

## **STEP 2: Login to Get JWT Token**

### 🔗 **POST** `http://localhost:8080/api/v1/auth/login`

**Headers:**
```
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
  "email": "john.doe@example.com",
  "password": "password123"
}
```

**✅ Expected Response (200 OK):**
```json
{
  "message": "Login successful",
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTY5ODc2...",
  "user": {
    "id": 1,
    "email": "john.doe@example.com",
    "fullName": "John Doe",
    "role": "farmer"
  }
}
```

**🔑 Important:** Copy the `token` value - you'll need it for the next requests!

---

## **STEP 3: Get User Profile**

### 🔗 **GET** `http://localhost:8080/api/v1/user/profile`

**Headers:**
```
Authorization: Bearer YOUR_JWT_TOKEN_HERE
Content-Type: application/json
```

**Body:** None (GET request)

**✅ Expected Response (200 OK):**
```json
**Profile Response:**
```json
{
  "id": 1,
  "email": "user@example.com",
  "fullName": "John Doe",
  "phone": "+1234567890",
  "avatarUrl": "/uploads/avatars/avatar_1_uuid.jpg",
  "createdAt": "2025-09-30T21:00:00.123",
  "updatedAt": "2025-09-30T21:05:30.456"
}
```

**❌ Expected Error (401 Unauthorized) if no token:**
```json
{
  "error": "Unauthorized or invalid token"
}
```

---

## **STEP 4: Update User Profile**

### 🔗 **PUT** `http://localhost:8080/api/v1/user/profile/update`

**Headers:**
```
Authorization: Bearer YOUR_JWT_TOKEN_HERE
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
  "fullName": "Jane Smith",
  "phone": "+0987654321"
}
```

**✅ Expected Response (200 OK):**
```json
{
  "message": "Profile updated successfully",
  "data": {
    "id": 1,
    "email": "john.doe@example.com",
    "fullName": "Jane Smith",
    "phone": "+0987654321",
    "avatarUrl": null,
    "createdAt": "2025-09-30T21:00:00.123",
    "updatedAt": "2025-09-30T21:05:30.456"
  },
  "timestamp": 1759259730456
}
```

### **Update Password Example:**

**Body (raw JSON):**
```json
{
  "password": "newSecurePassword123"
}
```

**✅ Expected Response (200 OK):**
```json
{
  "message": "Profile updated successfully",
  "data": {
    "id": 1,
    "email": "john.doe@example.com",
    "fullName": "Jane Smith",
    "phone": "+0987654321",
    "avatarUrl": null,
    "createdAt": "2025-09-30T21:00:00.123",
    "updatedAt": "2025-09-30T21:06:15.789"
  },
  "timestamp": 1759259775789
}
```

---

## **STEP 5: Upload Avatar**

### 🔗 **POST** `http://localhost:8080/api/v1/user/avatar`

**Headers:**
```
Authorization: Bearer YOUR_JWT_TOKEN_HERE
```

**Body:**
- Select `form-data` in Postman
- Add a field with:
  - **Key:** `avatar` (set type to `File`)
  - **Value:** Choose an image file from your computer (JPG, PNG, GIF)

**✅ Expected Response (200 OK):**
```json
{
  "message": "Avatar uploaded successfully",
  "avatarUrl": "/uploads/avatars/avatar_1_550e8400-e29b-41d4-a716-446655440000.jpg",
  "user": {
    "id": 1,
    "email": "john.doe@example.com",
    "fullName": "Jane Smith",
    "phone": "+0987654321",
    "avatarUrl": "/uploads/avatars/avatar_1_550e8400-e29b-41d4-a716-446655440000.jpg",
    "createdAt": "2025-09-30T21:00:00.123",
    "updatedAt": "2025-09-30T21:07:45.123"
  }
}
```

**❌ Expected Error (400 Bad Request) for invalid file:**
```json
{
  "error": "Only image files are allowed",
  "timestamp": 1759259865123
}
```

**❌ Expected Error (400 Bad Request) for large file:**
```json
{
  "error": "File size must be less than 5MB",
  "timestamp": 1759259865123
}
```

---

## **STEP 6: Test CORS (Optional)**

### 🔗 **GET** `http://localhost:8080/api/v1/security/health`

**Headers:**
```
Origin: http://localhost:3000
Content-Type: application/json
```

**Body:** None

**✅ Expected Response (200 OK):**
```json
{
  "status": "UP",
  "message": "CORS configuration is working",
  "timestamp": 1759259415
}
```

---

## 🔧 **Postman Setup Tips**

### **1. Environment Variables**
Create a Postman environment with:
- `baseUrl`: `http://localhost:8080`
- `token`: (will be set after login)

### **2. Pre-request Script for Login**
Add this to your login request's "Pre-request Script":
```javascript
// This will automatically save the token after login
pm.test("Save token", function () {
    var jsonData = pm.response.json();
    if (jsonData.token) {
        pm.environment.set("token", jsonData.token);
    }
});
```

### **3. Authorization Setup**
For profile endpoints, use:
- **Type:** Bearer Token
- **Token:** `{{token}}` (if using environment variables)

---

## 🔍 **Expected Server Logs**

When you make requests, you should see logs like:

```
=== PROFILE ENDPOINT ACCESSED ===
Method: GET | Endpoint: /api/v1/user/profile | IP: 127.0.0.1 | User-Agent: PostmanRuntime/7.48.0
Fetching profile for authenticated user: john.doe@example.com
Profile successfully retrieved for user: john.doe@example.com (ID: 1)
```

```
=== UPDATE PROFILE ENDPOINT ACCESSED ===
Method: PUT | Endpoint: /api/v1/user/profile/update | IP: 127.0.0.1 | User-Agent: PostmanRuntime/7.48.0
Updating profile for authenticated user: john.doe@example.com
Updating fullName from 'John Doe' to 'Jane Smith' for user: john.doe@example.com
Profile successfully updated for user: john.doe@example.com (ID: 1)
```

```
=== UPLOAD AVATAR ENDPOINT ACCESSED ===
Method: POST | Endpoint: /api/v1/user/avatar | IP: 127.0.0.1 | User-Agent: PostmanRuntime/7.48.0
Uploading avatar for authenticated user: john.doe@example.com
File details - Name: profile.jpg, Size: 245760 bytes, Content-Type: image/jpeg
Avatar successfully uploaded for user: john.doe@example.com (ID: 1) - File: avatar_1_uuid.jpg
```

---

## 🚨 **Common Issues & Solutions**

### **Issue 1: 401 Unauthorized**
- **Cause:** Missing or invalid JWT token
- **Solution:** Make sure you're including the `Authorization: Bearer TOKEN` header

### **Issue 2: Token Expired**
- **Cause:** JWT token has expired (24 hours by default)
- **Solution:** Login again to get a new token

### **Issue 3: File Upload Fails**
- **Cause:** Wrong content type or file too large
- **Solution:** Use `form-data`, select an image file < 5MB

### **Issue 4: CORS Issues**
- **Cause:** Frontend origin not allowed
- **Solution:** Add your frontend URL to the CORS configuration

---

## 🎯 **Testing Checklist**

- [ ] Register a new user
- [ ] Login and save JWT token  
- [ ] Get user profile with valid token
- [ ] Try to get profile without token (should fail)
- [ ] Update profile information
- [ ] Update password
- [ ] Upload avatar image
- [ ] Try to upload non-image file (should fail)
- [ ] Test CORS health endpoint

Your endpoints are fully functional and ready for testing! 🚀