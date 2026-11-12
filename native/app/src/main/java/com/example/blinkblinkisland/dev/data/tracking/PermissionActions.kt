package com.example.blinkblinkisland.dev.data.tracking
import android.Manifest
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.blinkblinkisland.dev.R
import com.example.blinkblinkisland.dev.platform.LaunchActivity
object PermissionActions{
 private val timer=Handler(Looper.getMainLooper())
 fun requestEye(activity:Activity){val granted=activity.checkSelfPermission("com.picovr.permission.EYE_TRACKING")==PackageManager.PERMISSION_GRANTED;Log.i("BlinkIsland","eye_permission_request host=${activity.javaClass.simpleName} granted=$granted finishing=${activity.isFinishing} destroyed=${activity.isDestroyed}");if(!granted)activity.requestPermissions(arrayOf("com.picovr.permission.EYE_TRACKING"),52)}
 fun request(activity:Activity){val names=arrayOf("com.picovr.permission.EYE_TRACKING",Manifest.permission.POST_NOTIFICATIONS).filter{activity.checkSelfPermission(it)!=PackageManager.PERMISSION_GRANTED};if(names.isNotEmpty())activity.requestPermissions(names.toTypedArray(),51)}
 fun testNotice(activity:Activity){if(activity.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED){Log.w("BlinkIsland","notification_permission_missing");return};val app=activity.applicationContext;val manager=app.getSystemService(NotificationManager::class.java);val channel="blink-island-route-test";manager.createNotificationChannel(NotificationChannel(channel,"Blink Island route tests",NotificationManager.IMPORTANCE_DEFAULT));timer.removeCallbacksAndMessages(null)
  timer.postDelayed({val route=Intent(app,LaunchActivity::class.java).putExtra("island_route","start").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP);val pi=PendingIntent.getActivity(app,1,route,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT);manager.notify(801,Notification.Builder(app,channel).setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle(app.getString(R.string.app_name)).setContentText("路由测试 / Route test — not a wear-time reminder").setContentIntent(pi).setAutoCancel(true).build());Log.i("BlinkIsland","test_notification_posted")},15000)
 }
}
