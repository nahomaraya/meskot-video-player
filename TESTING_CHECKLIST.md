# Integration Testing Checklist

## Pre-Testing Setup

### Backend Setup
- [ ] PostgreSQL database is running
- [ ] Database credentials are set in environment variables
- [ ] Backend compiles successfully: `mvn clean compile`
- [ ] Backend starts without errors: `./mvnw spring-boot:run`
- [ ] Swagger UI is accessible: http://localhost:8080/swagger-ui.html
- [ ] Can access health endpoint (if available)

### UI Setup
- [ ] VLC Media Player is installed on the system
- [ ] UI compiles successfully: `mvn compile`
- [ ] UI starts without errors: `mvn exec:java -Dexec.mainClass="..."`
- [ ] Main window appears

## Authentication Testing

### Registration Flow
- [ ] **Test Case 1.1: Successful Registration**
  - [ ] Click "Create an account"
  - [ ] Enter username: `testuser1`
  - [ ] Enter email: `test@example.com`
  - [ ] Enter password: `password123`
  - [ ] Click "Create account"
  - [ ] Expected: Success message, redirect to login
  - [ ] Verify: Check database for new user record

- [ ] **Test Case 1.2: Duplicate Username**
  - [ ] Try to register with same username again
  - [ ] Expected: Error message "Username already exists"

- [ ] **Test Case 1.3: Invalid Email**
  - [ ] Enter username: `testuser2`
  - [ ] Enter email: `invalidemail`
  - [ ] Enter password: `password123`
  - [ ] Expected: Validation error for email format (if implemented)

- [ ] **Test Case 1.4: Empty Fields**
  - [ ] Leave fields empty
  - [ ] Click "Create account"
  - [ ] Expected: Validation errors

### Login Flow
- [ ] **Test Case 2.1: Successful Login**
  - [ ] Enter username: `testuser1`
  - [ ] Enter password: `password123`
  - [ ] Click "Sign in"
  - [ ] Expected: Login successful, shows main movie browser
  - [ ] Verify: Profile button shows username initial

- [ ] **Test Case 2.2: Wrong Password**
  - [ ] Enter username: `testuser1`
  - [ ] Enter password: `wrongpass`
  - [ ] Click "Sign in"
  - [ ] Expected: Error "Invalid username or password"

- [ ] **Test Case 2.3: Non-existent User**
  - [ ] Enter username: `nonexistent`
  - [ ] Enter password: `password123`
  - [ ] Expected: Error "Invalid username or password"

- [ ] **Test Case 2.4: Empty Fields**
  - [ ] Leave fields empty
  - [ ] Click "Sign in"
  - [ ] Expected: Error "Please enter both username and password"

### Guest Mode
- [ ] **Test Case 3.1: Continue as Guest**
  - [ ] Click "Continue as guest"
  - [ ] Expected: Bypass login, go to movie browser
  - [ ] Verify: No profile button shown

### Logout
- [ ] **Test Case 4.1: Logout**
  - [ ] After logging in, click profile button
  - [ ] Click "Sign out"
  - [ ] Expected: Logged out, returned to login screen
  - [ ] Verify: currentUser is cleared

## Movie Browsing Testing

### Initial Load
- [ ] **Test Case 5.1: Load Local Movies**
  - [ ] After login, default source is "Local Storage"
  - [ ] Expected: Movies from LOCAL source displayed in grid
  - [ ] Verify: Each card shows title, thumbnail

- [ ] **Test Case 5.2: Empty Movie List**
  - [ ] If no movies in database
  - [ ] Expected: Shows empty state or message

### Data Source Switching
- [ ] **Test Case 6.1: Switch to Supabase**
  - [ ] Select "Supabase" from dropdown
  - [ ] Expected: Grid refreshes with Supabase movies
  - [ ] Verify: sourceType of movies is "SUPABASE"

- [ ] **Test Case 6.2: Switch to Internet Archive**
  - [ ] Select "Internet Archive" from dropdown
  - [ ] Expected: Grid refreshes with IA movies
  - [ ] Verify: sourceType is "INTERNET_ARCHIVE"

- [ ] **Test Case 6.3: Switch Back to Local**
  - [ ] Select "Local Storage" from dropdown
  - [ ] Expected: Grid refreshes with local movies

### Search Functionality
- [ ] **Test Case 7.1: Search with Results**
  - [ ] Type "movie" in search field
  - [ ] Press Enter
  - [ ] Expected: Grid shows only matching movies
  - [ ] Verify: All displayed movies contain "movie" in title

- [ ] **Test Case 7.2: Search with No Results**
  - [ ] Type "xyznonexistent" in search field
  - [ ] Press Enter
  - [ ] Expected: Empty grid or "No results" message

- [ ] **Test Case 7.3: Clear Search**
  - [ ] Clear search field
  - [ ] Press Enter
  - [ ] Expected: All movies displayed again

- [ ] **Test Case 7.4: Search Across Sources**
  - [ ] Search in Local Storage
  - [ ] Switch to Supabase
  - [ ] Expected: Search is cleared or re-applied to new source

### Movie Cards
- [ ] **Test Case 8.1: Card Display**
  - [ ] Verify each card shows:
    - [ ] Movie title
    - [ ] Thumbnail (if available)
    - [ ] Duration or other metadata
  - [ ] Hover over card
  - [ ] Expected: Visual feedback (highlight, shadow, etc.)

- [ ] **Test Case 8.2: Click Movie Card**
  - [ ] Click on a movie card
  - [ ] Expected: Transitions to player view
  - [ ] Video loads and is ready to play

## Video Playback Testing

### Streaming
- [ ] **Test Case 9.1: Play Movie**
  - [ ] Click on a movie
  - [ ] Player opens
  - [ ] Click play button
  - [ ] Expected: Video starts playing smoothly
  - [ ] Verify: Audio and video are in sync

- [ ] **Test Case 9.2: Pause/Resume**
  - [ ] While playing, click pause
  - [ ] Expected: Video pauses
  - [ ] Click play again
  - [ ] Expected: Video resumes from same position

- [ ] **Test Case 9.3: Seek Forward**
  - [ ] Drag seek bar forward (e.g., to 50%)
  - [ ] Expected: Video jumps to that position
  - [ ] Playback continues from new position

- [ ] **Test Case 9.4: Seek Backward**
  - [ ] Drag seek bar backward
  - [ ] Expected: Video jumps back
  - [ ] Playback continues

- [ ] **Test Case 9.5: Volume Control**
  - [ ] Adjust volume slider
  - [ ] Expected: Audio volume changes accordingly
  - [ ] Mute button works

- [ ] **Test Case 9.6: Fullscreen**
  - [ ] Click fullscreen button (if available)
  - [ ] Expected: Video goes fullscreen
  - [ ] Press Esc to exit
  - [ ] Expected: Returns to normal view

### Multiple Movies
- [ ] **Test Case 10.1: Switch Movies**
  - [ ] Play one movie
  - [ ] Go back to library
  - [ ] Select different movie
  - [ ] Expected: New movie loads and plays
  - [ ] Previous movie is stopped

## Upload Testing

### File Selection
- [ ] **Test Case 11.1: Select Valid Video**
  - [ ] Click Upload button
  - [ ] Select a valid .mp4 file (under 500MB)
  - [ ] Expected: File name appears in UI

- [ ] **Test Case 11.2: Select Invalid File**
  - [ ] Try to select .txt file
  - [ ] Expected: File filter rejects or error message

- [ ] **Test Case 11.3: Select Large File**
  - [ ] Try to select file > 500MB
  - [ ] Expected: Error message about size limit

### Upload Process
- [ ] **Test Case 12.1: Successful Upload**
  - [ ] Select file: `test_video.mp4`
  - [ ] Enter title: `Test Upload`
  - [ ] Select resolution: `720p`
  - [ ] Select source: `LOCAL`
  - [ ] Click "Upload"
  - [ ] Expected:
    - [ ] Upload starts
    - [ ] Progress bar shows 0%
    - [ ] Progress increases
    - [ ] Job status updates: PENDING → PROCESSING → COMPLETED
    - [ ] Success message appears
    - [ ] New movie appears in library

- [ ] **Test Case 12.2: Upload Progress Tracking**
  - [ ] During upload, observe progress bar
  - [ ] Expected: Progress increases smoothly
  - [ ] Percentage shown (0-100%)

- [ ] **Test Case 12.3: Cancel Upload**
  - [ ] Start upload
  - [ ] Click Cancel button (if available)
  - [ ] Expected: Upload stops
  - [ ] Partial file removed or flagged

- [ ] **Test Case 12.4: Upload to Supabase**
  - [ ] Select source: `SUPABASE`
  - [ ] Upload movie
  - [ ] Expected: Movie uploaded to Supabase
  - [ ] Appears in Supabase movie list

### Upload Error Handling
- [ ] **Test Case 13.1: Backend Offline**
  - [ ] Stop backend server
  - [ ] Try to upload
  - [ ] Expected: Error "Cannot connect to server"

- [ ] **Test Case 13.2: Upload Failed**
  - [ ] Simulate encoding failure (if possible)
  - [ ] Expected: Job status becomes "FAILED"
  - [ ] Error message shown to user

## Download Testing

### Download Process
- [ ] **Test Case 14.1: Download Original Quality**
  - [ ] Right-click movie → Download
  - [ ] Select resolution: `Original`
  - [ ] Choose save location
  - [ ] Click "Download"
  - [ ] Expected:
    - [ ] Download starts
    - [ ] Progress bar shows progress
    - [ ] File saved to chosen location
    - [ ] Success message

- [ ] **Test Case 14.2: Download with Conversion**
  - [ ] Select resolution: `720p`
  - [ ] Download movie
  - [ ] Expected:
    - [ ] Conversion happens on-the-fly
    - [ ] Downloaded file is 720p
    - [ ] Playable in video player

- [ ] **Test Case 14.3: Download Progress**
  - [ ] Monitor progress bar during download
  - [ ] Expected: Smooth progress increase
  - [ ] Shows bytes downloaded / total size

- [ ] **Test Case 14.4: Cancel Download**
  - [ ] Start download
  - [ ] Click Cancel (if available)
  - [ ] Expected: Download stops
  - [ ] Partial file removed or saved

### Download Error Handling
- [ ] **Test Case 15.1: Disk Full**
  - [ ] Try downloading to a full disk
  - [ ] Expected: Error message about disk space

- [ ] **Test Case 15.2: Permission Denied**
  - [ ] Try saving to protected directory
  - [ ] Expected: Error message about permissions

## Movie Management Testing

### Delete Movie
- [ ] **Test Case 16.1: Delete Movie**
  - [ ] Right-click movie → Delete
  - [ ] Confirm deletion
  - [ ] Expected:
    - [ ] Movie removed from database
    - [ ] Card disappears from grid
    - [ ] Can't be played anymore

- [ ] **Test Case 16.2: Cancel Deletion**
  - [ ] Right-click movie → Delete
  - [ ] Click Cancel
  - [ ] Expected: Movie remains in grid

### Filters
- [ ] **Test Case 17.1: Filter by Genre**
  - [ ] Select "Action" genre
  - [ ] Expected: Only Action movies shown

- [ ] **Test Case 17.2: Filter by Year**
  - [ ] Select year "2020"
  - [ ] Expected: Only 2020 movies shown

- [ ] **Test Case 17.3: Combined Filters**
  - [ ] Select genre AND year
  - [ ] Expected: Movies matching both criteria

## Internet Archive Testing

### Import Single Movie
- [ ] **Test Case 18.1: Import Valid Item**
  - [ ] Menu → Import → Internet Archive
  - [ ] Enter item identifier (e.g., `prelinger_test`)
  - [ ] Click "Import"
  - [ ] Expected:
    - [ ] Movie imported from IA
    - [ ] Appears in Internet Archive list
    - [ ] Can be streamed

- [ ] **Test Case 18.2: Import Invalid Item**
  - [ ] Enter non-existent identifier
  - [ ] Click "Import"
  - [ ] Expected: Error message

### Import Collection
- [ ] **Test Case 19.1: Import Collection**
  - [ ] Menu → Import → IA Collection
  - [ ] Enter collection ID
  - [ ] Set limit: 5
  - [ ] Click "Import"
  - [ ] Expected:
    - [ ] Progress indicator
    - [ ] Success message "Imported 5 movies"
    - [ ] Movies appear in IA list

## Performance Testing

### Load Testing
- [ ] **Test Case 20.1: Many Movies**
  - [ ] Library with 100+ movies
  - [ ] Expected: Grid loads in reasonable time (<5 seconds)
  - [ ] Scrolling is smooth

- [ ] **Test Case 20.2: Large File Upload**
  - [ ] Upload 400MB file
  - [ ] Expected: Progress tracked correctly
  - [ ] Completes successfully

- [ ] **Test Case 20.3: Long Video Streaming**
  - [ ] Stream 2-hour movie
  - [ ] Seek multiple times
  - [ ] Expected: Seeking is responsive
  - [ ] No buffering issues

### Concurrent Operations
- [ ] **Test Case 21.1: Upload While Browsing**
  - [ ] Start an upload
  - [ ] Browse other movies
  - [ ] Expected: UI remains responsive
  - [ ] Upload continues in background

- [ ] **Test Case 21.2: Multiple Downloads**
  - [ ] Start downloading 2 movies simultaneously
  - [ ] Expected: Both progress correctly
  - [ ] No conflicts

## Error Handling Testing

### Network Errors
- [ ] **Test Case 22.1: Backend Disconnected**
  - [ ] Stop backend server
  - [ ] Try any operation
  - [ ] Expected: User-friendly error message
  - [ ] UI doesn't crash

- [ ] **Test Case 22.2: Network Timeout**
  - [ ] Simulate slow network
  - [ ] Start upload/download
  - [ ] Expected: Timeout handled gracefully
  - [ ] Error message shown

### Data Validation
- [ ] **Test Case 23.1: Invalid Movie ID**
  - [ ] Manually call API with ID 99999
  - [ ] Expected: 404 Not Found handled
  - [ ] Shows "Movie not found"

- [ ] **Test Case 23.2: Malformed Response**
  - [ ] Simulate bad JSON response (if possible)
  - [ ] Expected: Parsing error caught
  - [ ] User sees generic error message

## UI/UX Testing

### Responsiveness
- [ ] **Test Case 24.1: Window Resize**
  - [ ] Resize main window
  - [ ] Expected: Layout adjusts properly
  - [ ] No overlap or cutoff

- [ ] **Test Case 24.2: Button States**
  - [ ] During operation, buttons disabled
  - [ ] Expected: Can't click multiple times
  - [ ] Button text changes (e.g., "Uploading...")

### Visual Feedback
- [ ] **Test Case 25.1: Loading Indicators**
  - [ ] Any long operation shows spinner or progress
  - [ ] Expected: User knows something is happening

- [ ] **Test Case 25.2: Success Messages**
  - [ ] Successful operations show green/success indicator
  - [ ] Auto-dismiss after few seconds

- [ ] **Test Case 25.3: Error Messages**
  - [ ] Errors show in red/error dialog
  - [ ] Clear, actionable messages

## Security Testing

### Session Management
- [ ] **Test Case 26.1: Session Timeout**
  - [ ] Login and wait idle for long time
  - [ ] Try operation
  - [ ] Expected: Re-authentication required (if implemented)

- [ ] **Test Case 26.2: Logout Clears Session**
  - [ ] Login, browse movies, logout
  - [ ] Try to access API directly
  - [ ] Expected: Session invalidated

### Input Validation
- [ ] **Test Case 27.1: SQL Injection Attempt**
  - [ ] Try login with username: `admin'--`
  - [ ] Expected: Treated as literal string, fails login

- [ ] **Test Case 27.2: XSS Attempt**
  - [ ] Upload movie with title: `<script>alert('xss')</script>`
  - [ ] Expected: Title displayed as plain text, not executed

## Final Checklist

### Code Quality
- [ ] No compiler warnings
- [ ] No runtime exceptions in console
- [ ] Proper error logging
- [ ] Memory leaks checked (long-running test)

### Documentation
- [ ] All API endpoints documented
- [ ] README updated
- [ ] Code comments for complex logic

### Deployment Ready
- [ ] Backend builds successfully
- [ ] UI builds successfully
- [ ] Configuration documented
- [ ] Dependencies listed

## Test Results Summary

| Category | Total Tests | Passed | Failed | Notes |
|----------|-------------|--------|--------|-------|
| Authentication | 11 | | | |
| Browsing | 11 | | | |
| Playback | 7 | | | |
| Upload | 9 | | | |
| Download | 6 | | | |
| Management | 5 | | | |
| Internet Archive | 4 | | | |
| Performance | 4 | | | |
| Error Handling | 4 | | | |
| UI/UX | 6 | | | |
| Security | 4 | | | |
| **TOTAL** | **71** | | | |

## Notes Section

Use this space to record any issues found during testing:

```
Issue 1: [Description]
Severity: High/Medium/Low
Steps to reproduce:
Expected behavior:
Actual behavior:
Status: Open/Fixed/Won't Fix

Issue 2: ...
```

---

**Testing completed by:** _______________  
**Date:** _______________  
**Backend version:** _______________  
**UI version:** _______________  

**Overall Status:** ☐ Pass ☐ Pass with minor issues ☐ Fail
