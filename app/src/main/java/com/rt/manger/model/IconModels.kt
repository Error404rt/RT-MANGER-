package com.rt.manger.model
import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
data class InstalledApp(val packageName:String,val label:String,val componentName:String,val icon:Bitmap?,val primaryColor:Color?,val isEdited:Boolean=false)
data class IconPackEntry(val componentName:String,val packageName:String,val drawableName:String,val icon:Bitmap?)
data class IconPack(val packageName:String,val label:String,val entries:Map<String,IconPackEntry>){
 fun iconFor(app:InstalledApp):Bitmap?=entries[app.componentName]?.icon?:entries["package:\${app.packageName}"]?.icon
 fun supports(app:InstalledApp)=entries.containsKey(app.componentName)||entries.containsKey("package:\${app.packageName}")
}
enum class LayerType{RASTER,VECTOR,SHAPE,LINE,TEXT,MASK,EFFECT}
data class IconLayer(val id:String,val name:String,val type:LayerType,val visible:Boolean=true,val opacity:Float=1f,val colorArgb:Int=0xFF111111.toInt(),val strokeWidth:Float=10f,val x1:Float=.18f,val y1:Float=.18f,val x2:Float=.82f,val y2:Float=.82f)
data class IconDocument(val packageName:String,val canvasSize:Int=432,val layers:List<IconLayer> = emptyList())
