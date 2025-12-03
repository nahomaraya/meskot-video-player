# API Endpoint Mapping - UI to Backend

## Complete Endpoint Reference

This document shows exactly how each UI component connects to backend endpoints.

## 1. Authentication Flow

### LoginPanel.java
```
User Action: Click "Sign in" button
    ↓
LoginPanel.attemptLogin()
    ↓
VideoPlayerUI.performLogin(username, password)
    ↓
AuthApiService.login(username, password)
    ↓
POST http://localhost:8080/api/auth/login
    Request Body: {"username":"user", "password":"pass"}
    ↓
Response: {"message":"Login successful", "user":{...}}
    ↓
VideoPlayerUI sets currentUser and shows main page
```

### RegistrationPanel.java
```
User Action: Click "Create account" button
    ↓
RegistrationPanel.attemptRegistration()
    ↓
VideoPlayerUI.performRegistration(username, email, password)
    ↓
AuthApiService.register(username, email, password)
    ↓
POST http://localhost:8080/api/auth/register
    Request Body: {"username":"user", "email":"email@example.com", "password":"pass"}
    ↓
Response: {"message":"Registered successfully", "user":{...}}
    ↓
Shows success message, redirects to login
```

### Profile Menu → Logout
```
User Action: Click "Sign out" in profile menu
    ↓
VideoPlayerUI.performLogout()
    ↓
AuthApiService.logout()
    ↓
POST http://localhost:8080/api/auth/logout
    ↓
Response: "Logged out"
    ↓
VideoPlayerUI clears user and shows login page
```

## 2. Movie Browsing

### MovieGridPanel.java - Initial Load
```
User: Selects data source from dropdown (e.g., "Local Storage")
    ↓
VideoPlayerUI.dataSourceCombo selection changed
    ↓
MovieApiService.setDataSource("Local Storage")
    ↓
MovieGridPanel.loadMovies()
    ↓
MovieApiService.getMovies()
    ↓
GET http://localhost:8080/api/movies/local
    ↓
Response: [{"id":1, "title":"Movie 1", ...}, {"id":2, ...}]
    ↓
MovieGridPanel displays movie cards
```

### Search Functionality
```
User: Types in search field and presses Enter
    ↓
VideoPlayerUI.mainSearchField ActionListener
    ↓
MovieApiService.searchMovies(query)
    ↓
GET http://localhost:8080/api/movies/local/search?q=query
    ↓
Response: [{"id":3, "title":"Matching Movie", ...}]
    ↓
MovieGridPanel updates with search results
```

### Data Source Switching
```
User: Changes dropdown from "Local Storage" to "Supabase"
    ↓
VideoPlayerUI.dataSourceCombo ActionListener
    ↓
MovieApiService.setDataSource("Supabase")
    ↓
MovieGridPanel.loadMovies()
    ↓
MovieApiService.getMovies()
    ↓
GET http://localhost:8080/api/movies/supabase
    ↓
Response: [{"id":10, "title":"Supabase Movie", "sourceType":"SUPABASE", ...}]
    ↓
MovieGridPanel displays Supabase movies
```

## 3. Video Playback

### Click Movie Card
```
User: Clicks on a movie card
    ↓
MovieCardPanel MouseListener
    ↓
VideoPlayerUI shows player page
    ↓
PlayerPresenter.playMovie(movieDto)
    ↓
Constructs streaming URL: 
    String streamUrl = movieApiService.getBaseUrl() + "/movies/" + movieId + "/stream"
    ↓
VLC MediaPlayer loads URL
    ↓
GET http://localhost:8080/api/movies/{id}/stream
    Header: Range: bytes=0-
    ↓
Response: HTTP 206 Partial Content
    Content-Type: video/mp4
    Content-Range: bytes 0-1048575/52428800
    [video data chunk]
    ↓
VLC player renders video
```

### Seeking in Video
```
User: Drags seek bar to 50% position
    ↓
VLC calculates byte position
    ↓
GET http://localhost:8080/api/movies/{id}/stream
    Header: Range: bytes=26214400-
    ↓
Response: HTTP 206 Partial Content
    Content-Range: bytes 26214400-52428800/52428800
    [video data from middle]
    ↓
VLC resumes playback from that position
```

## 4. Movie Upload

### UploadDialog.java
```
User: Clicks "Upload" button, selects file, fills form
    ↓
UploadDialog.startUpload()
    ↓
MovieApiService.startUpload(file, title, resolution, sourceType, progressCallback)
    ↓
POST http://localhost:8080/api/movies/upload
    Content-Type: multipart/form-data
    Parameters:
      - file: [binary movie file]
      - title: "My Movie"
      - resolution: "720p"
      - sourceType: "LOCAL"
    ↓
Response: {"jobId":"uuid-1234", "status":"PENDING", "message":"Upload started"}
    ↓
UploadDialog stores jobId and starts polling
```

### Upload Progress Polling
```
Timer fires every 1 second
    ↓
UploadDialog polls status
    ↓
MovieApiService.getUploadStatus(jobId)
    ↓
GET http://localhost:8080/api/jobs/uuid-1234
    ↓
Response: {"jobId":"uuid-1234", "status":"PROCESSING", "progress":45, ...}
    ↓
UploadDialog updates progress bar to 45%
    ↓
Continue polling until status is "COMPLETED" or "FAILED"
```

### Upload Complete
```
Status changes to "COMPLETED"
    ↓
GET http://localhost:8080/api/jobs/uuid-1234
Response: {
  "jobId":"uuid-1234",
  "status":"COMPLETED",
  "progress":100,
  "resultingMovieId":42
}
    ↓
UploadDialog stops polling
    ↓
Shows success message: "Movie uploaded successfully!"
    ↓
Refreshes movie list to show new movie
```

## 5. Movie Download

### DownloadDialog.java
```
User: Selects movie, clicks "Download", chooses resolution and save location
    ↓
DownloadDialog.startDownload()
    ↓
MovieApiService.downloadMovie(movieId, outputFile, resolution, progressCallback)
    ↓
GET http://localhost:8080/api/movies/{id}/download?resolution=720p
    ↓
Response: HTTP 200 OK
    Content-Type: video/mp4
    Content-Length: 104857600
    [video file data]
    ↓
MovieApiService writes to outputFile
Calls progressCallback every 8KB written
    ↓
DownloadDialog updates progress bar
    ↓
Download complete: File saved to chosen location
```

## 6. Movie Management

### Delete Movie
```
User: Right-clicks movie card → "Delete"
    ↓
Confirmation dialog appears
    ↓
User confirms deletion
    ↓
DELETE http://localhost:8080/api/movies/{id}
    ↓
Response: {"deleted": 5}
    ↓
Remove movie card from UI
Refresh movie list
```

### Filter by Genre
```
User: Selects "Action" from genre filter
    ↓
GET http://localhost:8080/api/movies/genre/Action
    ↓
Response: [{"id":1, "genre":"Action", ...}, {"id":5, "genre":"Action", ...}]
    ↓
MovieGridPanel displays only Action movies
```

### Filter by Year
```
User: Selects "2020" from year filter
    ↓
GET http://localhost:8080/api/movies/year/2020
    ↓
Response: [{"id":3, "releaseYear":2020, ...}, {"id":7, "releaseYear":2020, ...}]
    ↓
MovieGridPanel displays only 2020 movies
```

## 7. Internet Archive Import

### Import Single Movie
```
User: Menu → Import → Internet Archive → Enters item identifier
    ↓
POST http://localhost:8080/api/import/internet-archive?itemIdentifier=prelinger_films
    ↓
Response: {"id":50, "title":"Imported Movie", "sourceType":"INTERNET_ARCHIVE", ...}
    ↓
Show success message
Refresh movie list to include new IA movie
```

### Import Collection
```
User: Menu → Import → IA Collection → Enters collection ID
    ↓
POST http://localhost:8080/api/import/internet-archive/collection
    Parameters:
      - collectionId: "prelinger"
      - uploaderId: 1
      - limit: 10
    ↓
Response: {"imported": 10, "movies": [{...}, {...}, ...]}
    ↓
Show success: "Imported 10 movies from Internet Archive"
Refresh movie list
```

## HTTP Methods Summary

| Method | Endpoint Pattern | Purpose |
|--------|-----------------|---------|
| POST | /api/auth/login | Authenticate user |
| POST | /api/auth/register | Create new user |
| POST | /api/auth/logout | End session |
| GET | /api/movies | List all movies |
| GET | /api/movies/{id} | Get single movie |
| GET | /api/movies/local | List local movies |
| GET | /api/movies/supabase | List Supabase movies |
| GET | /api/movies/internet-archive | List IA movies |
| GET | /api/movies/search?q= | Search all movies |
| GET | /api/movies/local/search?q= | Search local movies |
| GET | /api/movies/{id}/stream | Stream video |
| GET | /api/movies/{id}/download | Download video |
| POST | /api/movies/upload | Upload new movie |
| GET | /api/jobs/{jobId} | Check upload status |
| DELETE | /api/movies/{id} | Delete movie |
| PATCH | /api/movies/{id}/status | Update status |
| GET | /api/movies/genre/{genre} | Filter by genre |
| GET | /api/movies/year/{year} | Filter by year |
| POST | /api/import/internet-archive | Import IA movie |
| POST | /api/import/internet-archive/collection | Import IA collection |

## Response Codes Handled

| Code | Meaning | UI Behavior |
|------|---------|-------------|
| 200 | Success | Display data or success message |
| 201 | Created | Show success, refresh list |
| 202 | Accepted | Start polling for job status |
| 206 | Partial Content | Continue streaming video |
| 400 | Bad Request | Show "Invalid input" error |
| 401 | Unauthorized | Show "Invalid credentials" |
| 404 | Not Found | Show "Movie not found" |
| 409 | Conflict | Show "Username already exists" |
| 500 | Server Error | Show "Server error, try again" |

## Error Handling Flow

```
Any API Call
    ↓
Try {
    HttpURLConnection.getResponseCode()
    ↓
    if (200-299) → Parse response, update UI
    else → Read error stream
} Catch (IOException) {
    ↓
    Show user-friendly error dialog
    ↓
    Log error details for debugging
}
```

## Threading Model

```
UI Thread (Event Dispatch Thread)
    ↓
User clicks button
    ↓
Create SwingWorker
    ↓
    ├── Background Thread (doInBackground)
    │   └── API call via MovieApiService/AuthApiService
    │       └── HTTP request to backend
    │           └── Wait for response
    │
    └── UI Thread (done)
        └── Update UI with results
            └── Show success/error message
```

This ensures UI remains responsive during all network operations!

## Complete Data Flow Example: Upload a Movie

```
1. USER ACTION
   User: File → Upload Movie → Select video file → Fill form → Click Upload
   
2. UI EVENT
   UploadDialog: actionPerformed(uploadButton)
   
3. SERVICE CALL
   MovieApiService.startUpload(file, "My Movie", "720p", "LOCAL", progress -> {
       SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
   })
   
4. HTTP REQUEST
   POST http://localhost:8080/api/movies/upload
   Content-Type: multipart/form-data; boundary=----WebKitFormBoundary...
   
   ------WebKitFormBoundary...
   Content-Disposition: form-data; name="file"; filename="movie.mp4"
   Content-Type: video/mp4
   
   [binary movie data - 104857600 bytes]
   ------WebKitFormBoundary...
   Content-Disposition: form-data; name="title"
   
   My Movie
   ------WebKitFormBoundary...
   Content-Disposition: form-data; name="resolution"
   
   720p
   ------WebKitFormBoundary...
   Content-Disposition: form-data; name="sourceType"
   
   LOCAL
   ------WebKitFormBoundary...--
   
5. BACKEND PROCESSING
   Controller.uploadMovie() receives MultipartFile
   → Saves temp file
   → Creates UploadJob (status=PENDING)
   → Starts async encoding
   → Returns jobId
   
6. HTTP RESPONSE
   HTTP/1.1 202 Accepted
   Content-Type: application/json
   
   {
       "jobId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
       "status": "PENDING",
       "message": "Upload started"
   }
   
7. UI UPDATE
   UploadDialog receives jobId
   → Stores jobId
   → Starts timer (poll every 1 second)
   → Shows "Uploading..." with progress bar
   
8. STATUS POLLING (every 1 second)
   GET http://localhost:8080/api/jobs/a1b2c3d4...
   
   Response (t=1s): {"status":"PENDING", "progress":0}
   Response (t=5s): {"status":"PROCESSING", "progress":25}
   Response (t=10s): {"status":"PROCESSING", "progress":50}
   Response (t=15s): {"status":"PROCESSING", "progress":75}
   Response (t=20s): {"status":"COMPLETED", "progress":100, "resultingMovieId":42}
   
9. COMPLETION
   UploadDialog detects COMPLETED status
   → Stops timer
   → Shows success dialog: "Movie uploaded successfully!"
   → Calls MovieGridPanel.refreshMovies()
   → New movie appears in grid
```

## Summary

Every user action in the UI translates to a specific backend API call. The integration is complete and follows these principles:

1. **Service Layer**: All API calls go through ApiService classes
2. **Async Operations**: Network calls don't block UI (SwingWorker)
3. **Error Handling**: Every call wrapped in try-catch with user feedback
4. **Progress Tracking**: Long operations show progress to user
5. **Clean Separation**: UI components don't directly construct HTTP requests

Your movie streaming platform is fully integrated and ready to use! 🎬
