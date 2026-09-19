package com.rt.manger.data
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Xml
import com.rt.manger.model.IconPack
import com.rt.manger.model.IconPackEntry
import org.xmlpull.v1.XmlPullParser
import java.util.zip.ZipFile
class IconPackScanner(private val context:Context){
 private val pm=context.packageManager
 fun scan():List<IconPack>{
  val candidates=linkedSetOf<String>()
  listOf("com.anddoes.launcher.THEME","org.adw.launcher.THEMES","com.novalauncher.THEME").forEach{a->pm.queryIntentActivities(Intent(a),PackageManager.MATCH_ALL).forEach{candidates+=it.activityInfo.packageName}}
  pm.getInstalledApplications(PackageManager.GET_META_DATA).filter{hasAppFilter(it.sourceDir)}.forEach{candidates+=it.packageName}
  return candidates.mapNotNull(::parse).sortedBy{it.label.lowercase()}
 }
 private fun hasAppFilter(path:String)=runCatching{ZipFile(path).use{z->z.entries().asSequence().any{e->!e.isDirectory&&e.name.endsWith("/appfilter.xml",true)}}}.getOrDefault(false)
 private fun parse(pkg:String):IconPack?=runCatching{
  val ai=pm.getApplicationInfo(pkg,0)
  val bytes=ZipFile(ai.sourceDir).use{z->val e=z.entries().asSequence().firstOrNull{!it.isDirectory&&it.name.endsWith("/appfilter.xml",true)}?:return null;z.getInputStream(e).use{it.readBytes()}}
  val entries=linkedMapOf<String,IconPackEntry>();val p=Xml.newPullParser();p.setInput(bytes.inputStream(),"UTF-8")
  while(p.next()!=XmlPullParser.END_DOCUMENT){if(p.eventType!=XmlPullParser.START_TAG||p.name!="item")continue;val c=p.getAttributeValue(null,"component")?:continue;val d=p.getAttributeValue(null,"drawable")?:continue;val n=c.removePrefix("ComponentInfo{").removeSuffix("}");val target=n.substringBefore("/");val icon=loadDrawable(pkg,d);val entry=IconPackEntry(n,target,d,icon);entries[n]=entry;entries["package:$target"]=entry}
  if(entries.isEmpty())null else IconPack(pkg,ai.loadLabel(pm).toString(),entries)
 }.getOrNull()
 private fun loadDrawable(pkg:String,name:String):Bitmap?=runCatching{val r=pm.getResourcesForApplication(pkg);val id=(r.getIdentifier(name,"drawable",pkg).takeIf{it!=0}?:r.getIdentifier(name,"mipmap",pkg));if(id==0)return null;val d=r.getDrawable(id,null);val b=Bitmap.createBitmap(d.intrinsicWidth.coerceAtLeast(1),d.intrinsicHeight.coerceAtLeast(1),Bitmap.Config.ARGB_8888);Canvas(b).also{d.setBounds(0,0,it.width,it.height);d.draw(it)};b}.getOrNull()
}
