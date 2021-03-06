package com.hit.android1.finalproject.ui.customviews

import android.animation.Animator
import android.animation.ObjectAnimator
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
import com.hit.android1.finalproject.app.Globals
import com.hit.android1.finalproject.app.Globals.sfxPlayer
import com.hit.android1.finalproject.app.SFXPlayer
import com.hit.android1.finalproject.dao.entities.InventoryItem
import com.hit.android1.finalproject.dao.entities.InventoryItem.Companion.drawableResourceId
import com.hit.android1.finalproject.dao.entities.InventoryItem.Companion.name
import com.hit.android1.finalproject.models.DropItemEventData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class ItemView @JvmOverloads constructor(
        context: Context?,
        attrs: AttributeSet? = null,
        defStyle: Int = 0
    ) : CustomView<CustomViewItemBinding>(context, attrs, defStyle) {
    constructor(context: Context?, item: InventoryItem, isDragAndDrop: Boolean = false) : this(context) {
        this.isDragAndDrop = isDragAndDrop
        this.item = item
    }
    var isDragAndDrop: Boolean = true
    private var attachedToDrag: Boolean = false

    override fun inflate() = CustomViewItemBinding.inflate(LayoutInflater.from(context), this, true)
    val draggableLayout
        get() = binding.customViewItemLayout

    private var title: String = "Item Title"
    set(t) {
        binding.itemTitle.text = t
        field = t
    }

    private var src: Int = 0
    set(s) {
        binding.itemImage.setImageResource(s)
        field = s
    }

    private var unlocked: Boolean = false
        set(unlocked) {
            if (unlocked) {
                binding.lockImg.visibility = GONE
                binding.itemImage.visibility = VISIBLE
            } else {
                binding.lockImg.visibility = VISIBLE
                binding.itemImage.visibility = GONE
            }
            field = unlocked
        }

    var item: InventoryItem? = null
    set(i) {
        i?.let {
            title = i.name(context)
            src = i.drawableResourceId(context)
            unlocked = i.unlocked
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
            if (!isDragAndDrop || attachedToDrag) return
            binding.customViewItemLayout.setOnLongClickListener { view: View ->
                item?.let {
                    val data = Json.encodeToString(DropItemEventData(it, isDragAndDrop))

                    val dataToDrag = ClipData(
                        data,
                        arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                        ClipData.Item(data)
                    )

                    val itemShadow = DragShadow(view)

                    sfxPlayer?.play(R.raw.lift_sound_pitch_enhanced)
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
        companion object {
            const val DRAG_SHADOW_SCALE = 1.5f
        }

        override fun onDrawShadow(canvas: Canvas?) {
            canvas?.scale(DRAG_SHADOW_SCALE, DRAG_SHADOW_SCALE)
            itemView.draw(canvas)
        }

        override fun onProvideShadowMetrics(shadowSize: Point, shadowTouchPoint: Point) {
            var v = view
            val height = (v.height*DRAG_SHADOW_SCALE).toInt()
            val width = (v.width*DRAG_SHADOW_SCALE).toInt()
            shadowSize.set(width, height)
            shadowTouchPoint.set(width / 2, height / 2)
        }
    }


}