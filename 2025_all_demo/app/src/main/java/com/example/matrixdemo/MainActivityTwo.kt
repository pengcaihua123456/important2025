package com.example.matrixdemo

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.example.matrixdemo.live.MyLiveActivity

class MainActivityTwo : Activity() {


    var textView: TextView? = null
    var imageView: ImageView? = null
    private val TAG = "MatrixLog"

     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
    }

    private fun initView() {
        textView = findViewById<TextView>(R.id.tv_test)
        imageView = findViewById<ImageView>(R.id.tv_ani)
        textView!!.setOnClickListener {
            val intent = Intent(this@MainActivityTwo, MyLiveActivity::class.java)
//            this@MainActivityTwo.startActivity(intent)
//            imageView!!.setBackgroundResource(R.drawable.animation_list)
            val animationDrawable = imageView!!.background as AnimationDrawable
            //                animationDrawable.start();

//                testThreadAnr();

//                try {
//                    testThreadPool();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
        }
    }


}