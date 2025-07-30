# **Face Recognition Android App**

This is an Android application that uses both **TensorFlow Lite** and **ML Kit** for real-time face detection.

---

## 🚀 **Features**

- ✅ **Dual Face Detection**: Supports both TensorFlow Lite and ML Kit  
- 🎥 **Real-time Face Detection**: Detects faces from live video feed using the camera  
- 😊 **Face Analysis**: Smile probability, eye openness, and head rotation angles  
- 🤳 **Front Camera Support**: Utilizes the selfie camera  
- 🔒 **Permission Handling**: Proper camera permission management  
- 🔄 **Easy Switching**: Toggle between TensorFlow Lite and ML Kit with a single button  

---

## 📹 Demo Video

[![Watch the demo](https://img.youtube.com/vi/SmWNt5jdyT8/0.jpg)](https://www.youtube.com/watch?v=SmWNt5jdyT8)

---

## 🛠️ **Setup Instructions**

### ✅ **Prerequisites**

- Android Studio **Arctic Fox** or newer  
- Android SDK **version 24 or higher**  
- **Physical Android device** (Camera doesn’t work on emulator)

---

### 📥 **Installation Steps**

1. **Clone or download** the project  
2. Open the project in **Android Studio**  
3. **Sync dependencies**:  
   - `File → Sync Project with Gradle Files`  
   - or press the **Gradle sync button**  
4. **Run the app** on your device:  
   - `Run → Run 'app'`  
   - or click the green ▶️ play button  

---

### 🔓 **First-Time Setup**

When the app runs for the first time:

1. App will **ask for camera permission** → Tap **“Allow”**  
2. Tap **“Start Face Detection”**  
3. You will see the **front camera preview**  
4. Face analysis results will appear at the **top-left** of the screen  

---

## 📲 **How to Use**

1. **Launch the app** and grant camera permission  
2. The **front camera feed** will be shown  
3. **Hold your face** in front of the camera  
4. The app will display:  
   - 😄 **Smile Probability (%)**  
   - 👁️ **Left Eye Openness (%)**  
   - 👁️ **Right Eye Openness (%)**  
   - 🔄 **Head Rotation Angles (Y and Z axes)**  

---

## ⚙️ **Technical Details**

### 🧩 **Dependencies Used**

- **ML Kit Face Detection**: Google’s official face detection library  
- **CameraX**: Modern Android camera API  
- **Kotlin**: Programming language  
- **AndroidX**: Modern Android libraries  

### 📂 **Key Components**

- `MainActivity.kt` – Contains face detection logic  
- `activity_main.xml` – UI layout with camera preview  
- `AndroidManifest.xml` – Declares permissions  
- `build.gradle.kts` – Manages dependencies  

### 🤖 **Face Detection Features**

- **Smile Detection** – Calculates smile probability  
- **Eye Tracking** – Detects openness of each eye  
- **Head Pose Estimation** – Tracks head rotation angles (Y, Z)  
- **Real-time Processing** – Analyzes camera feed live  

---

## 🧪 **Troubleshooting**

### 🛑 **Common Issues**

1. **Camera Permission Denied**  
   - Go to: `Settings → Apps → [Your App] → Permissions → Camera → Allow`

2. **App Crashes on Launch**  
   - Make sure the device has a **front camera**  
   - Ensure Android version is **7.0 (API 24)** or higher  

3. **Face Detection Not Working**  
   - Ensure **good lighting**  
   - Make sure face is clearly visible  
   - Ensure the camera is **not blocked**  

4. **Build Errors**  
   - **Clean and rebuild** the project  
   - Sync Gradle files  
   - Check **internet connection** for dependency downloads  

---

### 💡 **Performance Tips**

- 💡 Ensure **good lighting** for better accuracy  
- 🤳 Keep your device **steady**  
- 👀 Ensure your **face is visible and not covered**  
- 📏 Maintain a **reasonable distance** from the camera  

---

## 🔮 **Future Enhancements**

- Face recognition (identify specific people)  
- Emotion detection  
- Age and gender estimation  
- Face filters and effects  
- Multi-face detection  
- Face landmark visualization  

---

## 🆘 **Support**

If you face any issues:

1. Check **Logcat** in Android Studio  
2. Verify **device compatibility**  
3. Confirm all **permissions are granted**  
4. Try **restarting the app**

---

> **Note**: This app uses **ML Kit**, Google’s official machine learning library. An **internet connection** may be required during the first-time setup.

---
