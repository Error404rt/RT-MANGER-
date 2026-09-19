package com.rt.manger.export
import android.content.Context
import android.net.Uri
import com.rt.manger.data.ProjectStore
import com.rt.manger.model.IconLayer
import com.rt.manger.model.IconPack
import com.rt.manger.model.InstalledApp
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
object ZipExporter{
 fun writePack(context:Context,destination:Uri,apps:List<InstalledApp>,pack:IconPack?,store:ProjectStore){
  context.contentResolver.openOutputStream(destination)?.use{raw->ZipOutputStream(raw).use{zip->
   apps.filter{store.hasProject(it.packageName)}.forEach{app->
    val rendered=IconRenderer.render(pack?.iconFor(app)?:app.icon,store.load(app.packageName))
    zip.putNextEntry(ZipEntry(app.packageName+".png"));rendered.compress(android.graphics.Bitmap.CompressFormat.PNG,100,zip);zip.closeEntry()
   }
  }}
 }
 fun writeSingleIcon(context:Context,destination:Uri,app:InstalledApp,layers:List<IconLayer>){
  val bitmap=IconRenderer.render(app.icon,layers)
  context.contentResolver.openOutputStream(destination)?.use{raw->ZipOutputStream(raw).use{zip->zip.putNextEntry(ZipEntry(app.packageName+".png"));bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,zip);zip.closeEntry()}}
 }
}
