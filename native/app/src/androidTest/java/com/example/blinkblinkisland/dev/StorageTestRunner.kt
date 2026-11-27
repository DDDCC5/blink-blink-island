package com.example.blinkblinkisland.dev

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

/** SQLite tests need a real Android context, not a native Spatial render session. */
class StorageTestRunner:AndroidJUnitRunner(){
 override fun newApplication(cl:ClassLoader,className:String,context:Context):Application =
  super.newApplication(cl,Application::class.java.name,context)
}
