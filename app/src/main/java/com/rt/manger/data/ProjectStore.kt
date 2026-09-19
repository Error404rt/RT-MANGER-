package com.rt.manger.data
import android.content.Context
import com.rt.manger.model.IconLayer
import com.rt.manger.model.LayerType
import org.json.JSONArray
import org.json.JSONObject
class ProjectStore(context:Context){
 private val prefs=context.getSharedPreferences("rtmanager_projects",Context.MODE_PRIVATE)
 fun save(pkg:String,layers:List<IconLayer>){val a=JSONArray();layers.forEach{l->a.put(JSONObject().apply{put("id",l.id);put("name",l.name);put("type",l.type.name);put("visible",l.visible);put("opacity",l.opacity);put("color",l.colorArgb);put("stroke",l.strokeWidth);put("x1",l.x1);put("y1",l.y1);put("x2",l.x2);put("y2",l.y2)})};prefs.edit().putString(pkg,a.toString()).apply()}
 fun load(pkg:String):List<IconLayer>{val raw=prefs.getString(pkg,null)?:return emptyList();val a=JSONArray(raw);return buildList{for(i in 0 until a.length()){val o=a.getJSONObject(i);add(IconLayer(o.getString("id"),o.getString("name"),LayerType.valueOf(o.getString("type")),o.optBoolean("visible",true),o.optDouble("opacity",1.0).toFloat(),o.optInt("color",0xFF111111.toInt()),o.optDouble("stroke",10.0).toFloat(),o.optDouble("x1",.18).toFloat(),o.optDouble("y1",.18).toFloat(),o.optDouble("x2",.82).toFloat(),o.optDouble("y2",.82).toFloat()))}}}
 fun hasProject(pkg:String)=prefs.contains(pkg)
 fun editedPackages():Set<String>=prefs.all.keys
}
