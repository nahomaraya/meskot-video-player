# Meskot Movie Streaming Platform - Quick Start Guide

## Prerequisites

### Required Software
1. **Java Development Kit (JDK) 17 or higher**
   ```bash
   java -version  # Should show 17 or higher
   ```

2. **Maven 3.6+**
   ```bash
   mvn -version
   ```

3. **PostgreSQL 12+**
   - Running instance with a database created
   - Note your: host, port, database name, username, password

4. **VLC Media Player** (for video playback in UI)
   - Download from: https://www.videolan.org/vlc/
   - Must be installed on system PATH

### Optional (for cloud storage features)
5. **Supabase Account** (optional - for Supabase storage)
6. **Internet Archive Account** (optional - for IA uploads)

## Setup Steps

### 1. Clone and Navigate to Project
```bash
cd neu-finalproject
```

### 2. Configure Environment Variables

Create a `.env` file or set environment variables:

**Required:**
```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=meskot_db
export DB_USER=your_db_username
export DB_PASSWORD=your_db_password
```

**Optional (for cloud storage):**
```bash
export SUPABASE_URL=your_supabase_url
export SUPABASE_ACCESS_KEY=your_access_key
export SUPABASE_SECRET_KEY=your_secret_key
export SUPABASE_BUCKET=your_bucket_name

export INTERNET_ARCHIVE_ACCESS_KEY=your_ia_key
export INTERNET_ARCHIVE_SECRET_KEY=your_ia_secret
export INTERNET_ARCHIVE_IDENTIFIER=your_identifier
```

### 3. Build the Project
```bash
# Clean and compile
mvn clean compile

# Or build with tests
mvn clean package
```

### 4. Start the Backend Server
```bash
# Using Maven
./mvnw spring-boot:run

# Or if you built a JAR
java -jar target/meskot-0.0.1-SNAPSHOT.jar
```

**Backend will start on**: `http://localhost:8080`

**Verify backend is running**:
- Open browser to: http://localhost:8080/swagger-ui.html
- You should see the Swagger API documentation

### 5. Start the UI Application

**Option A: Using Maven**
```bash
mvn exec:java -Dexec.mainClass="com.neu.finalproject.meskot.ui.MovieStreamingApp"
```

**Option B: Using IDE**
1. Open project in IntelliJ IDEA / Eclipse
2. Navigate to: `src/main/java/com/neu/finalproject/meskot/ui/MovieStreamingApp.java`
3. Right-click and select "Run MovieStreamingApp.main()"

**Option C: Using compiled JAR**
```bash
java -cp target/meskot-0.0.1-SNAPSHOT.jar com.neu.finalproject.meskot.ui.MovieStreamingApp
```

## First Time Usage

### 1. Register a New Account
1. Click "Create an account" on the login screen
2. Enter username, email, and password
3. Click "Create account"
4. You'll be redirected to login

### 2. Login
1. Enter your credentials
2. Click "Sign in"
3. You'll see the main movie browser

### 3. Browse Movies
- Use the search bar to find movies
- Switch between data sources (Local Storage, Supabase, Internet Archive)
- Click on a movie card to view details and play

### 4. Upload a Movie
1. Click the "Upload" button
2. Select a video file (max 500MB)
3. Enter movie title
4. Choose resolution and source type
5. Click "Upload" and monitor progress

### 5. Play a Movie
1. Click on any movie card
2. Video player will open
3. Use controls to play/pause, seek, adjust volume

## Common Commands

### Backend

**Start backend in development mode:**
```bash
./mvnw spring-boot:run
```

**Run with specific profile:**
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

**Build JAR file:**
```bash
./mvnw clean package -DskipTests
```

### Database

**Create database:**
```sql
CREATE DATABASE meskot_db;
```

**Reset database (WARNING: Deletes all data):**
```sql
DROP DATABASE meskot_db;
CREATE DATABASE meskot_db;
```

The tables will be auto-created by Hibernate when the backend starts.

### UI

**Compile UI classes:**
```bash
mvn compile
```

**Run UI (while backend is running):**
```bash
mvn exec:java -Dexec.mainClass="com.neu.finalproject.meskot.ui.MovieStreamingApp"
```

## Default Ports

| Service | Port | URL |
|---------|------|-----|
| Backend API | 8080 | http://localhost:8080/api |
| Swagger UI | 8080 | http://localhost:8080/swagger-ui.html |
| PostgreSQL | 5432 | localhost:5432 |

## Testing the Setup

### 1. Test Backend API
```bash
# Test health (if endpoint exists)
curl http://localhost:8080/api/movies

# Test login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"testpass"}'
```

### 2. Test UI Connection
1. Start the UI application
2. Register a test account
3. Login successfully
4. Browse movies (may be empty initially)

## Troubleshooting

### Backend won't start

**Error: "Failed to configure a DataSource"**
- Check database is running: `psql -h localhost -p 5432 -U your_user`
- Verify environment variables are set correctly
- Check database credentials

**Error: "Port 8080 already in use"**
```bash
# Find process using port 8080
lsof -i :8080   # Mac/Linux
netstat -ano | findstr :8080   # Windows

# Kill the process or change port in application.properties
server.port=8081
```

### UI won't start

**Error: "VLC not found"**
- Install VLC Media Player
- Ensure it's in system PATH
- On Mac: `/Applications/VLC.app/Contents/MacOS/lib`
- On Windows: `C:\Program Files\VideoLAN\VLC`

**Error: "Cannot connect to backend"**
- Verify backend is running on port 8080
- Check firewall settings
- Verify API base URL in `MovieApiService.java` and `AuthApiService.java`

### Authentication issues

**Cannot login after registration**
- Check backend logs for errors
- Verify database contains user record
- Try different browser/clear cache (if testing web version)

### Video playback issues

**Video won't play**
- Ensure VLC is installed
- Check video format compatibility (MP4, MKV, AVI supported)
- Verify streaming endpoint returns data: 
  ```bash
  curl -I http://localhost:8080/api/movies/1/stream
  ```

## Development Tips

1. **Enable hot reload for backend:**
   ```xml
   <!-- Add to pom.xml -->
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-devtools</artifactId>
   </dependency>
   ```

2. **View backend logs:**
   ```bash
   # Console output shows all API calls and SQL queries
   # Check application.properties:
   spring.jpa.show-sql=true
   ```

3. **Debug mode:**
   - IntelliJ: Right-click → Debug 'MovieStreamingApp.main()'
   - Eclipse: Debug As → Java Application
   - Set breakpoints in API service methods

4. **Test API with Postman:**
   - Import Swagger spec from http://localhost:8080/api-docs
   - Create collections for testing endpoints

## Next Steps

1. **Add sample movies**: Upload a few test videos
2. **Explore features**: Try search, filtering, playback
3. **Configure cloud storage**: Set up Supabase or Internet Archive (optional)
4. **Customize UI**: Modify themes, colors in VideoPlayerUI.java
5. **Extend API**: Add new endpoints following the integration guide

## Support

For detailed information:
- API Documentation: See `UI_INTEGRATION_GUIDE.md`
- Backend Code: Check `src/main/java/com/neu/finalproject/meskot/controller`
- UI Code: Check `src/main/java/com/neu/finalproject/meskot/ui`

Happy streaming! 🎬
