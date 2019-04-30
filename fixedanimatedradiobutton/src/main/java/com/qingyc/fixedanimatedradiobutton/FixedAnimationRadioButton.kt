package com.qingyc.fixedanimatedradiobutton

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.widget.CompoundButton
import androidx.appcompat.widget.AppCompatRadioButton


/**
 *
 * 类说明: 1.点击带动画效果的RadioButton
 *        2.修复Android 4.4 radioButton设置 android:button="@null" 无效
 *
 * @author qing
 * @time 2019-04-28 15:29
 *
 *
 * qtip:RadioGroup中设置gravity 为bottom
 *
 */
class FixedAnimatedRadioButton : AppCompatRadioButton, ValueAnimator.AnimatorUpdateListener {
    /**
     * 缩放比
     */
    private var animatedScaleRate = 1f
    /**
     * 最小缩放比例
     */
    private var minScaleRate = .85f
    /**
     * 最大缩放比例
     */
    private var maxScaleRate = 1.0f

    /**
     * 是否打开了动画
     */
    private var animationOpen = true

    /**
     * 默认图标drawable的Rect位置
     */
    private var mDefaultDrawableBounds: Array<Rect?>? = null

    constructor(context: Context?) : super(context) {
        init(context, null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }


    private fun init(context: Context?, attrs: AttributeSet?) {
        val typedArray = context?.obtainStyledAttributes(attrs, R.styleable.FixedAnimatedRadioButton)
        typedArray?.apply {
            //默认打开缩放动画
            animationOpen = getBoolean(R.styleable.FixedAnimatedRadioButton_animation_open, true)
            maxScaleRate = getFloat(R.styleable.FixedAnimatedRadioButton_max_scale, 1.0f)
            minScaleRate = getFloat(R.styleable.FixedAnimatedRadioButton_min_scale, .85f)
            recycle()
        }
    }

    override fun setButtonDrawable(buttonDrawable: Drawable?) {
        // QTIP: 2019-04-28 修复低版本(android4.4)设置按钮为null时显示默认按钮
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            try {
                val clazz = CompoundButton::class.java
                val field = clazz.getDeclaredField("mButtonDrawable")
                field.isAccessible = true
                field.set(this, null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            super.setButtonDrawable(buttonDrawable)
        }
    }


    override fun setChecked(checked: Boolean) {
        super.setChecked(checked)
        if (width == 0 || height == 0) {
            return
        }
        if (checked) {
            val animator = ValueAnimator.ofFloat(minScaleRate, maxScaleRate)
            animator.addUpdateListener(this)
            animator.duration = 300
            animator.start()
        } else {
            if (animatedScaleRate != 1f) {
                val animator = ValueAnimator.ofFloat(animatedScaleRate, 1f)
                animator.addUpdateListener(this)
                animator.duration = 0
                animator.start()
            }
        }
    }


    override fun onAnimationUpdate(animation: ValueAnimator?) {
        animatedScaleRate = animation?.animatedValue as Float
        try {
            //1.保存图标默认位置
            if (mDefaultDrawableBounds == null) {
                mDefaultDrawableBounds = arrayOfNulls(4)
                compoundDrawables.forEachIndexed { index, drawable ->
                    drawable?.let {
                        val rect = Rect(drawable.bounds)
                        mDefaultDrawableBounds?.set(index, rect)
                    }
                }
            }
            var leftDrawable: Drawable? = null
            var rightDrawable: Drawable? = null
            var topDrawable: Drawable? = null
            var bottomDrawable: Drawable? = null
            //2.获取radioButton中设置的图标的drawable
            compoundDrawables.forEachIndexed { index, drawable ->
                drawable?.let {
                    mDefaultDrawableBounds?.get(index)?.let { mDefaultDrawableBounds ->
                        val copyBounds = Rect(mDefaultDrawableBounds)
                        //3.动态缩放RadioButton的图标
                        copyBounds.let {
                            copyBounds.left = mDefaultDrawableBounds.left
                            copyBounds.right =
                                mDefaultDrawableBounds.right - (mDefaultDrawableBounds.width() * (1 - animatedScaleRate)).toInt()
                            copyBounds.top =
                                mDefaultDrawableBounds.top + (mDefaultDrawableBounds.height() * (1 - animatedScaleRate)).toInt() / 2
                            copyBounds.bottom =
                                mDefaultDrawableBounds.bottom - (mDefaultDrawableBounds.height() * (1 - animatedScaleRate)).toInt() / 2
                            when (index) {
                                0 -> {
                                    leftDrawable = drawable
                                    leftDrawable?.bounds = copyBounds

                                }
                                1 -> {
                                    topDrawable = drawable
                                    topDrawable?.bounds = copyBounds
                                }
                                2 -> {
                                    rightDrawable = drawable
                                    rightDrawable?.bounds = copyBounds
                                }
                                3 -> {
                                    bottomDrawable = drawable
                                    bottomDrawable?.bounds = copyBounds
                                }
                            }
                        }
                    }
                }
            }
            //4.更新图标大小和位置
            setCompoundDrawables(leftDrawable, topDrawable, rightDrawable, bottomDrawable)

        } catch (e: Exception) {
        }
    }


}
