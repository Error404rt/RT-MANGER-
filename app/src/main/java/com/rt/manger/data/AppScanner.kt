package com.rt.manger.data
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.Color
import com.rt.manger.model.InstalledApp
import kotlin.math.max
import kotlin.math.min
class AppScanner(context:Context){
 private val pm=context.packageManager
 fun scanLauncherApps():List<InstalledApp>{
  val intent=Intent(Intent.ACTION_MAIN).apply{addCategory(Intent.CATEGORY_LAUNCHER)}
  return pm.queryIntentActivities(intent,0).asSequence().map{info->
   val ai=info.activityInfo.applicationInfo
   val component=info.activityInfo.packageName+"/"+info.activityInfo.name
   val bitmap=ai.loadIcon(pm).toBitmap()
   InstalledApp(ai.packageName,ai.loadLabel(pm).toString(),component,bitmap,estimatePrimaryColor(bitmap))
  }.distinctBy{it.packageName}.sortedBy{it.label.lowercase()}.toList()
 }
 private fun Drawable.toBitmap():Bitmap{val b=Bitmap.createBitmap(max(1,intrinsicWidth),max(1,intrinsicHeight),Bitmap.Config.ARGB_8888);Canvas(b).also{setBounds(0,0,it.width,it.height);draw(it)};return b}
 private fun estimatePrimaryColor(b:Bitmap):Color?{val sx=max(1,b.width/24);val sy=max(1,b.height/24);var r=0L;var g=0L;var bl=0L;var n=0L;for(y in 0 until b.height step sy)for(x in 0 until b.width step sx){val p=b.getPixel(x,y);if(android.graphics.Color.alpha(p)<48)continue;r+=android.graphics.Color.red(p);g+=android.graphics.Color.green(p);bl+=android.graphics.Color.blue(p);n++};if(n==0L)return null;return Color(min(255,(r/n).toInt()),min(255,(g/n).toInt()),min(255,(bl/n).toInt()))}
}
