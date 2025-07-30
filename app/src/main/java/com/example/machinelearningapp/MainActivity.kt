package com.example.machinelearningapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.machinelearningapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Set up button listeners
        setupButtonListeners()
    }
    
    private fun setupButtonListeners() {
        // ML Kit button
        binding.mlKitButton.setOnClickListener {
            startFaceDetection("ML Kit")
        }
        
        // TensorFlow button
        binding.tensorFlowButton.setOnClickListener {
            startFaceDetection("TensorFlow")
        }
        
        // About button
        binding.aboutButton.setOnClickListener {
            showAboutDialog()
        }
    }
    
    private fun startFaceDetection(method: String) {
        val intent = Intent(this, FaceDetectionActivity::class.java).apply {
            putExtra(FaceDetectionActivity.EXTRA_DETECTION_METHOD, method)
        }
        startActivity(intent)
    }
    
    private fun showAboutDialog() {
        val message = """
            Face Detection App
            Features:
            • ML Kit Face Detection
            • TensorFlow Lite Detection
            • Emotion Detection
            • Face Landmarks
            • Performance Monitoring
            
            Created with ❤️ using Android & Kotlin
        """.trimIndent()
        
        android.app.AlertDialog.Builder(this)
            .setTitle("About App")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}