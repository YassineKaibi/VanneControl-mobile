package com.example.myapplicationv10

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // MotionLayout gère automatiquement le swipe et l'animation
        val motionLayout = findViewById<MotionLayout>(R.id.motionLayout)

        // 🎯 Récupération du TextView "Already have an account? Sign in"
        val signInText = findViewById<TextView>(R.id.haveAccount)

        // 🔹 Lorsque l'utilisateur clique dessus → ouvrir LoginActivity
        signInText.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            // Optionnel : fermer cette activité pour ne pas revenir avec le bouton retour
            finish()
        }

        // Optionnel : listener pour suivre la transition
        motionLayout.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(motionLayout: MotionLayout?, startId: Int, endId: Int) {}
            override fun onTransitionChange(motionLayout: MotionLayout?, startId: Int, endId: Int, progress: Float) {}
            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {}
            override fun onTransitionTrigger(motionLayout: MotionLayout?, triggerId: Int, positive: Boolean, progress: Float) {}
        })
    }
}
