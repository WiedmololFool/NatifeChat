package com.max.natifechat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.max.natifechat.presentation.login.StartFragment


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container_view, StartFragment.newInstance())
            .commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(Constants.TAG,"onDestroy ${this.javaClass.simpleName}")
    }
}