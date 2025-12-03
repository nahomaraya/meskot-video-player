# JDBC Authentication Guide

## Overview

This project now supports **TWO authentication methods**:

1. **JPA-based authentication** (Original) - Uses Spring Data JPA with entities
2. **JDBC-based authentication** (New) - Direct SQL connection to Supabase PostgreSQL

## 🗂️ Files Added

### Core Files

1. **DatabaseConnection.java**
   - Location: `src/main/java/com/neu/finalproject/meskot/util/DatabaseConnection.java`
   - Purpose: Direct JDBC connection utility
   - Features:
     - Hardcoded Supabase credentials
     - Connection pooling ready
     - Test connection methods
     - User authentication helpers

2. **JdbcAuthenticationService.java**
   - Location: `src/main/java/com/neu/finalproject/meskot/service/JdbcAuthenticationService.java`
   - Purpose: Business logic for JDBC authentication
   - Features:
     - Login with username/password
     - User registration
     - Check username availability
     - Get user by ID
     - Promote user to admin

3. **JdbcAuthController.java**
   - Location: `src/main/java/com/neu/finalproject/meskot/controller/JdbcAuthController.java`
   - Purpose: REST API endpoints for JDBC authentication
   - Base path: `/api/jdbc-auth/*`

4. **DatabaseConnectionTest.java**
   - Location: `src/main/java/com/neu/finalproject/meskot/DatabaseConnectionTest.java`
   - Purpose: Standalone test to verify JDBC connection
   - Run directly from IntelliJ

---

## 🔌 Connection Details

### Hardcoded Supabase Credentials (in DatabaseConnection.java)

```java
URL: jdbc:postgresql://aws-0-us-west-2.pooler.supabase.com:5432/postgres
USERNAME: postgres.sykcyulhobvhsrssxldd
PASSWORD: YqTRixflrMN9HeM1
```

**⚠️ Security Note:** These credentials are hardcoded for convenience. In production:
- Use environment variables
- Use Spring's `@Value` annotation
- Store in application.properties with encryption

---

## 📡 New API Endpoints

All JDBC authentication endpoints are under `/api/jdbc-auth/`

### 1. Test Connection
```http
GET /api/jdbc-auth/test
```

**Response:**
```json
{
  "message": "JDBC connection test successful",
  "status": "connected"
}
```

### 2. Login with JDBC
```http
POST /api/jdbc-auth/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "password123"
}
```

**Response (Success):**
```json
{
  "message": "Login successful",
  "authMethod": "JDBC",
  "user": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "isAdmin": false,
    "createdAt": "2024-11-20T10:30:00Z"
  }
}
```

**Response (Failure):**
```json
{
  "error": "Invalid username or password"
}
```

### 3. Register with JDBC
```http
POST /api/jdbc-auth/register
Content-Type: application/json

{
  "username": "new_user",
  "email": "new@example.com",
  "password": "securepass123"
}
```

**Response:**
```json
{
  "message": "Registration successful",
  "authMethod": "JDBC",
  "userId": 5,
  "username": "new_user"
}
```

### 4. Get User by ID
```http
GET /api/jdbc-auth/user/1
```

**Response:**
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "isAdmin": false,
  "createdAt": "2024-11-20T10:30:00Z"
}
```

### 5. Make User Admin
```http
POST /api/jdbc-auth/admin/1
```

**Response:**
```json
{
  "message": "User promoted to admin",
  "userId": 1
}
```

---

## 🧪 Testing the JDBC Connection

### Option 1: Standalone Test

Run `DatabaseConnectionTest.java` directly:

```bash
# In IntelliJ: Right-click DatabaseConnectionTest.java → Run 'DatabaseConnectionTest.main()'
```

**Expected Output:**
```
============================================================
🧪 JDBC Connection Test to Supabase PostgreSQL
============================================================

📍 Test 1: Basic Connection
🔗 Attempting PostgreSQL database connection...
✅ PostgreSQL database connection established
✅ PostgreSQL connection test: PASSED
📊 PostgreSQL Version: PostgreSQL 15.x ...

📍 Test 2: Database Information
📋 Database Information:
   Database: PostgreSQL
   Version: 15.x
   URL: jdbc:postgresql://aws-0-us-west-2.pooler.supabase.com:5432/postgres
   User: postgres.sykcyulhobvhsrssxldd

📍 Test 3: User Authentication Test
✅ User found: testuser
Authentication result: ✅ SUCCESS

============================================================
✅ JDBC Connection Test Complete
============================================================
```

### Option 2: API Test

After starting the backend:

```bash
# Test connection
curl http://localhost:8080/api/jdbc-auth/test

# Register new user
curl -X POST http://localhost:8080/api/jdbc-auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "test123"
  }'

# Login
curl -X POST http://localhost:8080/api/jdbc-auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "test123"
  }'
```

---

## 🔄 Comparison: JPA vs JDBC Authentication

| Feature | JPA Authentication | JDBC Authentication |
|---------|-------------------|---------------------|
| **Endpoints** | `/api/auth/*` | `/api/jdbc-auth/*` |
| **Method** | Spring Data JPA entities | Direct SQL queries |
| **Configuration** | Environment variables | Hardcoded credentials |
| **Pros** | Type-safe, ORM features | Direct control, simpler |
| **Cons** | More overhead | Manual SQL, no ORM benefits |
| **When to use** | Production, complex queries | Testing, simple operations |

---

## 🚀 How to Use in Your UI

### Update ApiService.java

You can add JDBC authentication methods to your `ApiService.java`:

```java
// Login using JDBC
public Map<String, Object> loginWithJdbc(String username, String password) throws IOException {
    String url = BASE_URL + "/jdbc-auth/login";
    
    JSONObject credentials = new JSONObject();
    credentials.put("username", username);
    credentials.put("password", password);
    
    HttpPost request = new HttpPost(url);
    request.setHeader("Content-Type", "application/json");
    request.setEntity(new StringEntity(credentials.toString()));
    
    try (CloseableHttpResponse response = httpClient.execute(request)) {
        String responseBody = EntityUtils.toString(response.getEntity());
        return objectMapper.readValue(responseBody, Map.class);
    }
}

// Register using JDBC
public Map<String, Object> registerWithJdbc(String username, String email, String password) throws IOException {
    String url = BASE_URL + "/jdbc-auth/register";
    
    JSONObject userData = new JSONObject();
    userData.put("username", username);
    userData.put("email", email);
    userData.put("password", password);
    
    HttpPost request = new HttpPost(url);
    request.setHeader("Content-Type", "application/json");
    request.setEntity(new StringEntity(userData.toString()));
    
    try (CloseableHttpResponse response = httpClient.execute(request)) {
        String responseBody = EntityUtils.toString(response.getEntity());
        return objectMapper.readValue(responseBody, Map.class);
    }
}
```

### Update LoginPanel.java

Add toggle for authentication method:

```java
private JComboBox<String> authMethodCombo;

public LoginPanel(VideoPlayerUI parent, ApiService apiService) {
    // ... existing code ...
    
    // Add authentication method selector
    authMethodCombo = new JComboBox<>(new String[]{"JPA Auth", "JDBC Auth"});
    // Add to your panel
}

private void performLogin() {
    String username = usernameField.getText().trim();
    String password = new String(passwordField.getPassword());
    String authMethod = (String) authMethodCombo.getSelectedItem();
    
    try {
        Map<String, Object> response;
        
        if ("JDBC Auth".equals(authMethod)) {
            response = apiService.loginWithJdbc(username, password);
        } else {
            response = apiService.login(username, password); // Original JPA
        }
        
        // Handle successful login...
    } catch (Exception e) {
        // Handle error...
    }
}
```

---

## ⚠️ Important Security Notes

### Current Implementation Issues

1. **Passwords are not hashed** - The code has TODOs for BCrypt
2. **Credentials are hardcoded** - Should use environment variables
3. **No SQL injection protection** - Using PreparedStatements helps, but validation needed
4. **No session management** - Just returns user info, no JWT/session

### Production Recommendations

```java
// Add BCrypt password hashing
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

// When registering
String hashedPassword = encoder.encode(plainPassword);

// When authenticating
if (encoder.matches(plainPassword, storedHash)) {
    // Success
}
```

---

## 🐛 Troubleshooting

### Connection Refused
```
❌ PostgreSQL connection test: FAILED - Connection refused
```
**Fix:** Check Supabase is accessible, verify credentials

### Driver Not Found
```
❌ PostgreSQL Driver not found: org.postgresql.Driver
```
**Fix:** PostgreSQL driver should be in `pom.xml` already (it is)

### Authentication Always Fails
**Current Issue:** Password verification is not implemented (TODO in code)
**Fix:** Add BCrypt password hashing (see security notes above)

### Table Not Found
```
ERROR: relation "users" does not exist
```
**Fix:** Make sure `users` table exists in Supabase, or run the backend with JPA first to create tables

---

## 📋 Next Steps

1. **Test the connection:**
   ```bash
   # Run DatabaseConnectionTest
   ```

2. **Test via API:**
   ```bash
   curl http://localhost:8080/api/jdbc-auth/test
   ```

3. **Register a user:**
   ```bash
   curl -X POST http://localhost:8080/api/jdbc-auth/register \
     -H "Content-Type: application/json" \
     -d '{"username":"testuser","email":"test@example.com","password":"test123"}'
   ```

4. **Login:**
   ```bash
   curl -X POST http://localhost:8080/api/jdbc-auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"testuser","password":"test123"}'
   ```

5. **Add password hashing** (see Security Notes)

6. **Update UI to use JDBC auth** (see "How to Use in Your UI")

---

## ✅ Summary

You now have **two authentication systems**:

1. **Original JPA** (`/api/auth/*`) - For production use with Spring Data JPA
2. **New JDBC** (`/api/jdbc-auth/*`) - Direct SQL access to Supabase

Both connect to the same Supabase PostgreSQL database and use the same `users` table.

Choose based on your needs:
- **Use JPA** for production, type safety, and Spring Boot integration
- **Use JDBC** for learning, debugging, or when you need direct SQL control
