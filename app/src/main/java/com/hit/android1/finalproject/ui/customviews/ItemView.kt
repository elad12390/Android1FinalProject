package com.hit.android1.finalproject.ui.customviews

import android.content.Context
import android.util.AttributeSet
import com.hit.android1.finalproject.R
import com.hit.android1.finalproject.databinding.CustomViewItemBinding
import android.view.LayoutInflater
import com.hit.android1.finalproject.app.CustomView
import android.content.ClipData
import android.content.ClipDescription
import android.graphics.Canvas
import android.graphics.Point
import android.os.Build

import android.view.View
import com.hit.android1.finalproject.app.Extensions.logDebug
import com.hit.android1.finalproject.dao.entities.InventoryItem
import com.hit.android1.finalproject.dao.entities.InventoryItem.Companion.drawableResourceId
import com.hit.android1.finalproject.dao.entities.InventoryItem.Companion.name


class ItemView @JvmOverloads constructor(
        context: Context?,
        attrs: AttributeSet? = null,
        defStyle: Int = 0
    ) : CustomView<CustomViewItemBinding>(context, attrs, defStyle) {
    constructor(context: Context?, item: InventoryItem) : this(context) {
        this.item = item
    }

    var draggable: Boolean = true
    var attachedToDrag: Boolean = false
    var longPressTimeout: Long = 500

    override fun inflate() = CustomViewItemBinding.inflate(LayoutInflater.from(context), this, true)
    var title: String = "Item Title"
    set(t: String) {
        binding.itemTitle.text = t
        field = t
    }

    var src: Int = 0
    set(s: Int) {
        binding.itemImage.setImageResource(s)
        field = s
    }

    var item: InventoryItem? = null
    set(i: InventoryItem?) {
        i?.let {
            title = i.name()
            src = i.drawableResourceId(context)
            field = i
            attachOnDrag()
        }
    }

    override fun initView(context: Context, attrs: AttributeSet?, defStyle: Int?) {
        setAttributes(attrs, context)
    }

    private fun setAttributes(attrs: AttributeSet?, context: Context) {
        attrs?.let {
            context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.ItemView,
                0,
                0
            ).apply {
                try {
                    title = getString(R.styleable.ItemView_title) ?: "Item Title"
                    src = getResourceId(R.styleable.ItemView_src, 0)
                } finally {
                    recycle()
                }
            }
        }
    }

    private fun attachOnDrag() {
        item?.let {
            if (!draggable || attachedToDrag) return
            binding.customViewItemLayout.setOnLongClickListener { view: View ->
                item?.let {
                    val dataToDrag = ClipData(
                        it.id,
                        arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                        ClipData.Item(it.id)
                    )

                    val itemShadow = DragShadow(view)

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                        //support pre-Nougat versions
                        @Suppress("DEPRECATION")
                        view.startDrag(dataToDrag, itemShadow, view, 0)
                    } else {
                        //supports Nougat and beyond
                        view.startDragAndDrop(dataToDrag, itemShadow, view, 0)
                    }
                }

                true
            }
            attachedToDrag = true
        }
    }

    private class DragShadow(var itemView: View) : DragShadowBuilder(itemView) {
        override fun onDrawShadow(canvas: Canvas?) {
            itemView.draw(canvas)
        }

        override fun onProvideShadowMetrics(shadowSize: Point, shadowTouchPoint: Point) {
            val v = view
            val height = v.height
            val width = v.width
            shadowSize.set(width, height)
            shadowTouchPoint.set(width / 2, height / 2)
        }
    }


}