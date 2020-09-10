package com.example.audiocutter.activities.acttest

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.audiocutter.R
import com.example.audiocutter.core.audioManager.AudioFileManagerImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TestAct : AppCompatActivity(), View.OnClickListener {
    lateinit var btGet: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_test)
        initViews()
    }

    private fun initViews() {
        btGet = findViewById(R.id.bt_getlisType)
        btGet.setOnClickListener(this)

    }

    override fun onClick(p0: View) {
        CoroutineScope(Dispatchers.Default).launch {
            AudioFileManagerImpl().getAllListByType(this@TestAct)
        }
    }
}