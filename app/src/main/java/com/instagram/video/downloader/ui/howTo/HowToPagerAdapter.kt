package com.instagram.video.downloader.ui.howTo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.instagram.video.downloader.R
import com.instagram.video.downloader.databinding.HowToItemBinding

class HowToPagerAdapter(private val items: List<HowToItem>) : PagerAdapter() {


    override fun getCount() = items.size

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val binding =
            HowToItemBinding.inflate(container.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
        val currentItem = items[position]

        if (currentItem.isMethodsDivider) {
            binding.methodsDivider.visibility = View.VISIBLE
            binding.methodNumContainer.visibility = View.GONE
            binding.descriptionContainer.visibility = View.GONE
            binding.image.visibility = View.GONE

            binding.methodsDivider.text = container.context.getString(currentItem.methodDescription)

        } else {
            binding.methodsDivider.visibility = View.GONE
            binding.methodNumContainer.visibility = View.VISIBLE
            binding.descriptionContainer.visibility = View.VISIBLE
            binding.image.isVisible = !currentItem.isLastItem

            Glide.with(container.context)
                .load(currentItem.stepImageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.image)
            binding.methodNum.text =
                container.context.resources.getString(R.string.method_n, currentItem.methodNum)
            binding.stepDescription.text =
                container.context.resources.getString(currentItem.methodDescription)
            binding.stepIcon.setImageResource(currentItem.methodDrawable)
        }

        container.addView(binding.root)
        return binding.root
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}