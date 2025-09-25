package com.instagram.video.downloader.common.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.instagram.video.downloader.R
import com.instagram.video.downloader.common.ui.extensions.layoutInflater
import com.instagram.video.downloader.databinding.PurchaseSubscriptionViewBinding

class SubscriptionOptionView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {


    private val binding =
        PurchaseSubscriptionViewBinding.inflate(context.layoutInflater, this, true)


    init {
        val a =
            context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.SubscriptionOptionView,
                0,
                0,
            )
        binding.descriptionTextView.text = context.getString(
            R.string.subscription_duration,
            a.getText(R.styleable.SubscriptionOptionView_recurringText),
        )
        setFlagText(a.getText(R.styleable.SubscriptionOptionView_flagText))
    }

    fun setPriceText(text: String) {
        binding.priceLabel.text = text
    }

    fun setFlagText(text: CharSequence?) {
        if ((text?.length ?: 0) == 0) {
            binding.flagFlap.visibility = View.GONE
            binding.flagTextview.visibility = View.GONE
        } else {
            binding.flagFlap.visibility = View.VISIBLE
            binding.flagTextview.visibility = View.VISIBLE
            binding.flagTextview.text = text
        }
    }

    fun setOnPurchaseClickListener(listener: OnClickListener) {
        this.setOnClickListener(listener)
    }

    fun setIsSelected(purchased: Boolean) {
        if (purchased) {
            binding.wrapper.setBackgroundResource(R.drawable.subscription_box_bg_selected)
            binding.subscriptionSelectedView.setBackgroundResource(R.drawable.subscription_selected)

            binding.priceLabel.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.md_theme_inverseSurface,
                )
            )

            binding.descriptionTextView.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.md_theme_inverseSurface,
                ),
            )

        } else {
            binding.wrapper.setBackgroundResource(R.drawable.subscription_type_box_bg)
            binding.subscriptionSelectedView.setBackgroundResource(R.drawable.subscription_unselected)

            binding.priceLabel.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.md_theme_outline
                )
            )
            binding.descriptionTextView.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.md_theme_outline,
                ),
            )
        }
    }

}