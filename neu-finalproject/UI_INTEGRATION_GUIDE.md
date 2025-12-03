# Meskot Movie Streaming Platform - UI Integration Guide

## Overview
This document provides a complete guide to the Movie Streaming Platform's UI integration with the backend REST API. The application uses Java Swing for the frontend and Spring Boot for the backend.

## Architecture

### Backend (Spring Boot)
- **Base URL**: `http://localhost:8080/api`
- **Framework**: Spring Boot with PostgreSQL database
- **Authentication**: Session-based authentication
- **File Uploads**: Supports up to 500MB files
- **Storage**: Three sources - Local Storage, Supabase, Internet Archive

### Frontend (Java Swing)
- **Main Window**: `VideoPlayerUI.java` - The primary application window
- **API Services**: 
  - `AuthApiService.java` - Handles authentication
  - `MovieApiService.java` - Handles movie operations
- **Components**:
  - `LoginPanel.java` - User login
  - `RegistrationPanel.java` - User registration
  - `MovieGridPanel.java` - Movie browsing grid
  - `MovieCardPanel.java` - Individual movie cards
  - `PlayerPresenter.java` - Video playback controls

## API Endpoints Used by UI

### 1. Authentication (`AuthApiService`)

#### Login
```
POST /api/auth/login
Body: {"username": "user", "password": "pass"}
Response: {"message": "Login successful", "user": {...}}
```

#### Register
```
POST /api/auth/register
Body: {"username": "user", "email": "email@example.com", "password": "pass"}
Response: {"message": "Registered successfully", "user": {...}}
```

#### Logout
```
POST /api/auth/logout
Response: "Logged out"
```

### 2. Movie Listing (`MovieApiService`)

#### Get All Movies (by source)
```
GET /api/movies/local          # Local storage movies
GET /api/movies/supabase        # Supabase movies
GET /api/movies/internet-archive # Internet Archive movies
GET /api/movies                 # All movies from all sources
```

#### Search Movies (by source)
```
GET /api/movies/local/search?q=query
GET /api/movies/supabase/search?q=query
GET /api/movies/internet-archive/search?q=query
GET /api/movies/search?q=query  # Search all sources
```

#### Get Movie by ID
```
GET /api/movies/{id}
Response: MovieDto object
```

### 3. Streaming

#### Stream Movie
```
GET /api/movies/{id}/stream
Headers: Range (optional for partial content requests)
Response: Video stream with range support
```

### 4. Download

#### Download Movie
```
GET /api/movies/{id}/download?resolution=Original
Resolution options: Original, 1080p, 720p, 480p, 360p
Response: Movie file
```

### 5. Upload

#### Upload Movie
```
POST /api/movies/upload
Parameters:
  - file: MultipartFile
  - title: String
  - resolution: String (default: "720p")
  - sourceType: String (default: "LOCAL")
Response: {"jobId": "uuid", "status": "PENDING", "message": "Upload started"}
```

#### Check Upload Status
```
GET /api/jobs/{jobId}
Response: {
  "jobId": "uuid",
  "status": "PENDING|PROCESSING|COMPLETED|FAILED",
  "progress": 0-100,
  "errorMessage": null,
  "resultingMovieId": movieId (when completed)
}
```

### 6. Movie Management

#### Delete Movie
```
DELETE /api/movies/{id}
Response: {"deleted": id}
```

#### Update Movie Status
```
PATCH /api/movies/{id}/status?status=ACTIVE
Response: Updated MovieDto
```

#### Get Movies by Genre
```
GET /api/movies/genre/{genre}
Response: List of MovieDto
```

#### Get Movies by Year
```
GET /api/movies/year/{year}
Response: List of MovieDto
```

### 7. Internet Archive Import

#### Import Single Movie
```
POST /api/import/internet-archive?itemIdentifier=identifier&uploaderId=1
Response: MovieDto
```

#### Import Collection
```
POST /api/import/internet-archive/collection
Parameters:
  - collectionId: String
  - uploaderId: Long (default: 1)
  - limit: Integer (default: 10)
Response: {"imported": count, "movies": [MovieDto...]}
```

## Data Models

### MovieDto
```java
{
  "id": Long,
  "title": String,
  "filePath": String,
  "uploadedDate": LocalDateTime,
  "resolution": String,
  "sizeInBytes": Long,
  "format": String,
  "sourceType": "LOCAL|SUPABASE|INTERNET_ARCHIVE",
  "description": String,
  "genre": String,
  "releaseYear": Integer,
  "durationMinutes": Integer,
  "thumbnailUrl": String
}
```

### UserInfo
```java
{
  "id": Long,
  "username": String,
  "email": String
}
```

## Running the Application

### 1. Start the Backend
```bash
# Make sure PostgreSQL is running
# Set environment variables:
# DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
# SUPABASE_URL, SUPABASE_ACCESS_KEY, SUPABASE_SECRET_KEY, SUPABASE_BUCKET
# INTERNET_ARCHIVE_ACCESS_KEY, INTERNET_ARCHIVE_SECRET_KEY, INTERNET_ARCHIVE_IDENTIFIER

# Run the Spring Boot application
./mvnw spring-boot:run
```

The backend will start on `http://localhost:8080`

### 2. Start the UI
```bash
# Compile and run the UI
mvn compile exec:java -Dexec.mainClass="com.neu.finalproject.meskot.ui.MovieStreamingApp"

# Or run directly from IDE
# Run: com.neu.finalproject.meskot.ui.MovieStreamingApp
```

## UI Components Structure

```
MovieStreamingApp (Main Entry Point)
  └── VideoPlayerUI (Main Window)
      ├── LoginPanel (Authentication)
      │   └── AuthApiService (Backend Auth Calls)
      ├── RegistrationPanel (User Registration)
      ├── MovieGridPanel (Movie Browser)
      │   ├── MovieCardPanel (Movie Cards)
      │   └── MovieApiService (Backend Movie Calls)
      ├── PlayerPresenter (Video Player)
      │   └── VLC MediaPlayer Component
      └── Dialogs
          ├── UploadDialog (Upload Movies)
          ├── DownloadDialog (Download Movies)
          └── CompressDialog (Video Compression)
```

## Key Features

### 1. Multi-Source Support
The UI allows users to switch between three data sources:
- **Local Storage**: Files stored on the server's file system
- **Supabase**: Cloud storage via Supabase
- **Internet Archive**: Public domain movies from archive.org

### 2. Video Streaming
- Uses VLC embedded player for video playback
- Supports HTTP range requests for seeking
- Stream movies directly from any source

### 3. Upload & Processing
- Asynchronous upload with progress tracking
- Video encoding/transcoding to different resolutions
- Job status polling to track progress

### 4. Download with Resolution Conversion
- Download movies in original quality
- Or convert on-the-fly to 1080p, 720p, 480p, or 360p
- Progress tracking for downloads

### 5. Search & Filter
- Real-time search across all movies
- Filter by source type
- Browse by genre or release year

## Error Handling

### Common Error Responses

#### 401 Unauthorized
```json
"Invalid username or password"
```

#### 404 Not Found
```json
{
  "error": "Movie not found"
}
```

#### 409 Conflict
```json
"Username already exists"
```

#### 500 Internal Server Error
```json
{
  "error": "Error message details"
}
```

### UI Error Handling
All API services handle errors gracefully and display user-friendly messages:
- Connection errors
- Timeout errors
- Invalid data errors
- Server errors

## Development Guide

### Adding New API Endpoints

1. **Backend**: Add endpoint in `Controller.java`
```java
@GetMapping("/movies/featured")
public ResponseEntity<List<MovieDto>> getFeaturedMovies() {
    // Implementation
}
```

2. **UI Service**: Add method in `MovieApiService.java`
```java
public List<MovieDto> getFeaturedMovies() throws Exception {
    return fetchMovieList(baseUrl + "/movies/featured");
}
```

3. **UI Component**: Call from Swing component
```java
SwingWorker<List<MovieDto>, Void> worker = new SwingWorker<>() {
    @Override
    protected List<MovieDto> doInBackground() throws Exception {
        return movieApiService.getFeaturedMovies();
    }
    
    @Override
    protected void done() {
        try {
            List<MovieDto> movies = get();
            updateUI(movies);
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }
};
worker.execute();
```

### Best Practices

1. **Always use SwingWorker for API calls**
   - Never block the Event Dispatch Thread
   - Show loading indicators
   - Handle errors properly

2. **Validate user input**
   - Check for empty fields
   - Validate email formats
   - Sanitize search queries

3. **Provide user feedback**
   - Show progress bars for long operations
   - Display success/error messages
   - Enable/disable buttons during processing

4. **Handle network errors gracefully**
   - Implement timeouts
   - Retry logic for transient failures
   - Clear error messages

## Testing

### Backend Testing
```bash
# Run all tests
./mvnw test

# Test specific endpoints with curl
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test123"}'
```

### UI Testing
1. Test authentication flow
2. Test movie browsing with different sources
3. Test video playback
4. Test upload/download with progress tracking
5. Test search functionality

## Troubleshooting

### Common Issues

**Issue**: Cannot connect to backend
- **Solution**: Ensure backend is running on port 8080
- Check firewall settings
- Verify API base URL in services

**Issue**: Video won't play
- **Solution**: Ensure VLC is installed on the system
- Check video file format compatibility
- Verify streaming endpoint is accessible

**Issue**: Upload fails
- **Solution**: Check file size (max 500MB)
- Verify upload permissions
- Check available disk space

**Issue**: Authentication fails
- **Solution**: Verify database connection
- Check user credentials
- Review backend logs for errors

## Additional Resources

- **Swagger UI**: http://localhost:8080/swagger-ui.html (when backend is running)
- **API Docs**: http://localhost:8080/api-docs
- **Backend Repository**: Check README.md in project root
- **VLC Java Bindings**: https://github.com/caprica/vlcj

## Contributors

This UI was designed to seamlessly integrate with the backend REST API, providing a complete movie streaming platform experience.

For questions or issues, please refer to the project documentation or contact the development team.
