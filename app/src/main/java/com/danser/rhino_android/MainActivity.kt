package com.danser.rhino_android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import rx.android.schedulers.AndroidSchedulers

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        JsExecutor(this).execute(JsCode(""""1" + "1"""", emptyMap()))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
    }
}
