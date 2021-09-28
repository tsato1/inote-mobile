package com.tsato.mobile.inote.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tsato.mobile.inote.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    //todo in manifest: remove usesCleartextTraffic=true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}