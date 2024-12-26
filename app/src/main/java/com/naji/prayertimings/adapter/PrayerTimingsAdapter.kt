package com.naji.prayertimings.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.naji.prayertimings.R
import com.naji.prayertimings.databinding.ItemPrayerTimingBinding
import com.naji.prayertimings.model.Timing
import com.naji.prayertimings.model.api.aladhan.Timings

class PrayerTimingsAdapter(
    private val timings: ArrayList<Timing>
) : RecyclerView.Adapter<PrayerTimingsAdapter.PrayerTimingViewHolder>() {

    inner class PrayerTimingViewHolder(val binding: ItemPrayerTimingBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrayerTimingViewHolder =
        PrayerTimingViewHolder(
            ItemPrayerTimingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun getItemCount(): Int = timings.size

    override fun onBindViewHolder(holder: PrayerTimingViewHolder, position: Int) {
        holder.binding.apply {
            val timing = timings[position]
            tvPrayerName.text = timing.name
            val hour = timing.time.substring(0, timing.time.indexOf(':')).toInt()
            val minutes = timing.time.substring(timing.time.indexOf(':') + 1).toInt()
            if(hour <= 12)
                tvPrayerTiming.text = timing.time + root.context.getString(R.string.am)
            else {
                val hourStr = if(hour - 12 < 10)
                    "0${hour-12}"
                else
                    (hour - 12).toString()
                val minutesStr = if(minutes < 10)
                    "0$minutes"
                else
                    minutes.toString()
                tvPrayerTiming.text = "$hourStr:$minutesStr" + root.context.getString(R.string.pm)
            }
        }
    }
}