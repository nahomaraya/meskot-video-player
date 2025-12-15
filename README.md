
<img width="1880" height="1166" alt="Screenshot 2025-12-15 123320" src="https://github.com/user-attachments/assets/3b3c23d7-e36e-42ef-b056-70d9faeab06a" />

A full-stack, enterprise-grade media platform designed to manage, moderate, and distribute video content from multiple sources. The system supports both original creator uploads and public-domain content imports via the Internet Archive, with role-based access control and a production-ready architecture. The application is built using Spring Boot for the backend and a Java Swing desktop client for the user interface.

✨ Key Features & Innovations

HTTP 206 Partial Content Streaming
Enables efficient video seeking and resumable playback for large media files.

On-the-Fly Transcoding
Media processing powered by JavaCV / FFmpeg, supporting H.264 and H.265 codecs for real-time conversion and optimized downloads.

Asynchronous Job Processing
Long-running tasks such as uploads, imports, and transcoding are executed asynchronously with real-time progress tracking.

Multi-Source Content Ingestion
Supports both original creator uploads and automated public-domain imports from the Internet Archive.

Role-Based Access Control
Fine-grained permissions enforced across Admin, Content Creator, Registered User, and Guest roles.

Production-Oriented Architecture
Designed with scalability, security, and separation of concerns in mind.
