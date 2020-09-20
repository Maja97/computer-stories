package mmatijevic.ferit.hr.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.ImageView


class ImageAdapter(private val context: Context, private val images:List<Bitmap>): BaseAdapter(){
    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val imageView: ImageView

        if (p1 == null) {
            imageView = ImageView(context)
            imageView.setPadding(1,1,1,1)
            imageView.adjustViewBounds = true
            imageView.scaleType = ImageView.ScaleType.FIT_XY
        } else {
            imageView = p1 as ImageView
        }
        imageView.setImageBitmap(images[p0])
        return imageView
    }

    override fun getItem(p0: Int): Bitmap {
        return images[p0]
    }

    override fun getItemId(p0: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return images.size
    }

}