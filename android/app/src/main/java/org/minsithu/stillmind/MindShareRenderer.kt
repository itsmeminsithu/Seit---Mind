package org.minsithu.stillmind

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat

/** Produces self-contained RGB/PNG share cards, no network or face/photo required. */
object MindShareRenderer {
    fun render(context:Context,minutes:Int,mood:String,name:String,streak:Int,stars:Int,
        showName:Boolean,showStreak:Boolean,showStars:Boolean,language:String,format:String,theme:String): Bitmap {
        val w=1080;val h=if(format=="story")1920 else 1080
        val bmp=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
        val canvas=Canvas(bmp)
        val colors=when(theme){
            "sunset"->intArrayOf(0xFF55415F.toInt(),0xFFBF846E.toInt())
            "ocean"->intArrayOf(0xFF124761.toInt(),0xFF32839A.toInt())
            "cream"->intArrayOf(0xFF755F46.toInt(),0xFFC0A67D.toInt())
            else->intArrayOf(0xFF153E34.toInt(),0xFF417E60.toInt())
        }
        val paint=Paint(Paint.ANTI_ALIAS_FLAG)
        paint.shader=LinearGradient(0f,0f,w.toFloat(),h.toFloat(),colors[0],colors[1],Shader.TileMode.CLAMP)
        canvas.drawRect(0f,0f,w.toFloat(),h.toFloat(),paint)
        paint.shader=null
        paint.color=0x12FFFFFF
        canvas.drawCircle(850f,210f,235f,paint)
        canvas.drawCircle(130f,h-180f,250f,paint)
        val face=ResourcesCompat.getFont(context,R.font.pyidaungsu_regular)
        val text=TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color=android.graphics.Color.WHITE;typeface=face; textAlign=Paint.Align.CENTER }
        fun one(value:String,y:Float,size:Float,bold:Boolean=false){
            text.textSize=size;text.isFakeBoldText=bold
            val originalScale=text.textScaleX
            text.textScaleX=1f
            val measured=text.measureText(value)
            if(measured>940f) text.textScaleX=940f/measured
            canvas.drawText(value,540f,y,text)
            text.textScaleX=originalScale
        }
        fun multiline(value:String,y:Float,size:Float):Float {
            val blockPaint=TextPaint(text).apply { textSize=size; textAlign=Paint.Align.LEFT;isFakeBoldText=false;textScaleX=1f }
            val layout=StaticLayout.Builder.obtain(value,0,value.length,blockPaint,880)
                .setAlignment(Layout.Alignment.ALIGN_CENTER).setIncludePad(false).setMaxLines(if(format=="story")5 else 3).build()
            canvas.save();canvas.translate(100f,y);layout.draw(canvas);canvas.restore()
            return y+layout.height+26f
        }
        val story=format=="story"
        one("စိတ် - Mind",if(story)185f else 90f,57f,true)
        one("Your space for peace, focus & better sleep.",if(story)250f else 148f,27f)
        one("TODAY I MEDITATED",if(story)330f else 240f,37f)
        one("$minutes",if(story)530f else 415f,if(story)176f else 160f,true)
        one("MINUTES",if(story)595f else 473f,44f)
        one(mood,if(story)695f else 550f,31f,true)
        val quote=QuoteRepository.forMood(context,mood)
        val lines=listOfNotNull(if(language!="mm")"“${quote.en}”" else null,
            if(language!="en")"“${quote.mm}”" else null)
        val extras=listOf(showName,showStreak,showStars).count{it}
        val quoteStart=if(story)840f else 595f
        val quoteEnd=if(story)1490f else 895f-extras*43f
        fun blockHeight(value:String,size:Float):Int {
            val tp=TextPaint(text).apply { textSize=size;textAlign=Paint.Align.LEFT;isFakeBoldText=false;textScaleX=1f }
            return StaticLayout.Builder.obtain(value,0,value.length,tp,880)
                .setAlignment(Layout.Alignment.ALIGN_CENTER).setIncludePad(false).build().height
        }
        var quoteSize=if(story)38f else 31f
        while(quoteSize>20f && lines.sumOf{ blockHeight(it,quoteSize).toDouble()+15.0 } > (quoteEnd-quoteStart)) quoteSize--
        var y=quoteStart
        for(value in lines){ y=multiline(value,y,quoteSize) }
        var extraY=if(story)1630f else 960f-extras*43f
        if(showName){one(name,extraY,28f,true);extraY+=43f}
        if(showStreak){one("$streak day streak",extraY,26f);extraY+=43f}
        if(showStars){one("$stars stars",extraY,26f)}
        one("One mindful moment at a time",h-58f,27f)
        return bmp
    }
}
