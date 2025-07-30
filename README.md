#Face Recognition Android App

This is an Android application that uses both TensorFlow Lite and ML Kit for real-time face detection.

#Features

Dual Face Detection: Supports both TensorFlow Lite and ML Kit

Real-time Face Detection: Detects faces from live video feed using the camera

Face Analysis: Provides smile probability, eye openness, and head rotation angles

Front Camera Support: Utilizes the selfie camera

Permission Handling: Proper management of camera permissions

Easy Switching: Toggle between TensorFlow and ML Kit with a button

##Setup Instructions

#Prerequisites

Android Studio Arctic Fox or higher

Android SDK version 24 or above

A physical Android device (camera does not work on emulator)

#Installation Steps

Clone or download the project

Open the project in Android Studio

Sync project dependencies

Click on File → Sync Project with Gradle Files

Or press the Gradle sync button

Run the app on your device

Click on Run → Run 'app'

Or press the green play button

First-Time Setup

When you launch the app for the first time:

Camera Permission: The app will request camera permission. Tap “Allow”

Start Detection: Press the “Start Face Detection” button

Camera Preview: The front camera preview will appear

Face Analysis: Face analysis information will be displayed at the top-left corner

How to Use
Launch the App: Grant camera permission when prompted

Live Preview: The front camera feed will be shown

Face Detection: Hold your face in front of the camera

Real-time Analysis: The app will display:

Smile Probability (%)

Left Eye Openness (%)

Right Eye Openness (%)

Head Rotation Angles (Y and Z axes)

##Technical Details

#Dependencies Used

ML Kit Face Detection: Google's official face detection library

CameraX: Modern Android camera API

Kotlin: Programming language

AndroidX: Modern Android libraries

#Key Components

MainActivity.kt: Main activity containing the face detection logic

activity_main.xml: UI layout with camera preview

AndroidManifest.xml: Declares permissions

build.gradle.kts: Dependency configurations

Face Detection Features
Smile Detection: Detects and shows smile probability

Eye Tracking: Detects openness of both eyes

Head Pose Estimation: Tracks head rotation along Y and Z axes

Real-time Processing: Live analysis of video feed from the camera

#Troubleshooting

Common Issues

Camera Permission Denied

Go to: Settings → Apps → Your App → Permissions → Camera → Allow

App Crashes on Launch

Ensure your device has a front camera

Make sure Android version is 7.0 (API 24) or above

Face Detection Not Working

Ensure good lighting conditions

Make sure your face is clearly visible

Ensure the camera lens is not blocked

Build Errors

Clean and rebuild the project

Sync Gradle files

Check your internet connection for dependency resolution

Performance Tips
Good Lighting: Improves detection accuracy

Steady Device: Hold your device steady for better results

Clear View of Face: Avoid face coverings or obstructions

Optimal Distance: Maintain a reasonable distance from the camera

Future Enhancements
Face recognition (identify known individuals)

Emotion detection

Age and gender estimation

Fun filters and effects

Multi-face detection

Face landmark visualization

##Support

If you encounter any issues:

Check logs in Android Studio (Logcat)

Verify device compatibility

Ensure all permissions are granted

Restart the app

Note: This app uses ML Kit, Google’s official machine learning library. An internet connection may be required during the first-time setup.
