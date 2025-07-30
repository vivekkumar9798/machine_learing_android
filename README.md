# Face Recognition Android App

यह एक Android app है जो **TensorFlow Lite** और **ML Kit** दोनों का उपयोग करके real-time face detection करता है।

## Features

- **Dual Face Detection**: TensorFlow Lite और ML Kit दोनों का support
- **Real-time Face Detection**: Camera से live video feed पर face detection
- **Face Analysis**: Smile probability, eye openness, head rotation angles
- **Front Camera Support**: Selfie camera का उपयोग
- **Permission Handling**: Camera permissions का proper management
- **Easy Switching**: Button से TensorFlow और ML Kit के बीच switch करें

## Setup Instructions

### Prerequisites
- Android Studio Arctic Fox या उससे ऊपर का version
- Android SDK 24 या उससे ऊपर
- Physical Android device (emulator में camera नहीं काम करेगा)

### Installation Steps

1. **Project को clone करें या download करें**

2. **Android Studio में project open करें**

3. **Dependencies sync करें**
   - File → Sync Project with Gradle Files
   - या Gradle sync button press करें

4. **App को device पर run करें**
   - Run → Run 'app'
   - या green play button press करें

### First Time Setup

जब app पहली बार run होगी:

1. **Camera Permission**: App camera permission मांगेगी, "Allow" करें
2. **Face Detection Start**: "Start Face Detection" button press करें
3. **Camera Preview**: Front camera का live preview दिखेगा
4. **Face Analysis**: Screen के top-left में face analysis information दिखेगा

## How to Use

1. **App Launch**: App launch करने के बाद camera permission grant करें
2. **Camera Preview**: Front camera का live preview दिखेगा
3. **Face Detection**: Camera के सामने face रखें
4. **Analysis Results**: Screen पर real-time face analysis दिखेगा:
   - Smile Probability (%)
   - Left Eye Openness (%)
   - Right Eye Openness (%)
   - Head Rotation Angles

## Technical Details

### Dependencies Used
- **ML Kit Face Detection**: Google का official face detection library
- **CameraX**: Modern camera API for Android
- **Kotlin**: Programming language
- **AndroidX**: Modern Android libraries

### Key Components
- `MainActivity.kt`: Main activity with face detection logic
- `activity_main.xml`: UI layout with camera preview
- `AndroidManifest.xml`: Camera permissions
- `build.gradle.kts`: Dependencies configuration

### Face Detection Features
- **Smile Detection**: Smile probability calculation
- **Eye Tracking**: Left and right eye openness detection
- **Head Pose**: Head rotation angles (Y and Z axis)
- **Real-time Processing**: Live video analysis

## Troubleshooting

### Common Issues

1. **Camera Permission Denied**
   - Settings → Apps → Your App → Permissions → Camera → Allow

2. **App Crashes on Launch**
   - Check if device has front camera
   - Ensure Android version is 7.0 (API 24) or higher

3. **Face Detection Not Working**
   - Ensure good lighting
   - Face should be clearly visible in camera
   - Check if camera is not blocked

4. **Build Errors**
   - Clean and rebuild project
   - Sync project with Gradle files
   - Check internet connection for dependencies

### Performance Tips

- **Good Lighting**: Better lighting = better face detection
- **Stable Position**: Keep device steady for better results
- **Clear Face**: Ensure face is not covered or obscured
- **Close Distance**: Keep face within reasonable distance from camera

## Future Enhancements

- Face recognition (identifying specific people)
- Emotion detection
- Age and gender estimation
- Face filters and effects
- Multiple face detection
- Face landmark visualization

## Support

अगर कोई issue है तो:
1. Logs check करें (Android Studio में Logcat)
2. Device compatibility verify करें
3. Permissions check करें
4. App को restart करें

---

**Note**: यह app ML Kit का उपयोग करती है जो Google का official machine learning library है। Internet connection required हो सकता है first time setup के लिए। 