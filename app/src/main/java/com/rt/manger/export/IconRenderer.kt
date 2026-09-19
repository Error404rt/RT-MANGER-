package com.rt.manger.export
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.rt.manger.model.IconLayer
import com.rt.manger.model.LayerType
object IconRenderer{
 fun render(source:Bitmap?,layers:List<IconLayer>,size:Int=432):Bitmap{
  val out=Bitmap.createBitmap(size,size,Bitmap.Config.ARGB_8888);val c=Canvas(out);c.drawColor(android.graphics.Color.TRANSPARENT)
  if(source!=null){val p=Paint(3);val side=minOf(source.width,source.height);val src=android.graphics.Rect((source.width-side)/2,(source.height-side)/2,(source.width+side)/2,(source.height+side)/2);c.drawBitmap(source,src,android.graphics.Rect(0,0,size,size),p)}
  layers.filter{it.visible&&it.type==LayerType.LINE}.forEach{l->val p=Paint(3).apply{color=l.colorArgb;alpha=(l.opacity*255).toInt().coerceIn(0,255);style=Paint.Style.STROKE;strokeWidth=l.strokeWidth;strokeCap=Paint.Cap.ROUND};c.drawLine(l.x1*size,l.y1*size,l.x2*size,l.y2*size,p)}
  return out
 }
}
