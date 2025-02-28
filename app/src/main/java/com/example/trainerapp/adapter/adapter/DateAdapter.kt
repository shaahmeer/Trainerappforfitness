package com.example.trainerapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trainerapp.R
import java.text.SimpleDateFormat
import java.util.*

class DateAdapter(
    private val dates: List<Date>,
    private val selectedDate: Date,
    private val onDateSelected: (Date) -> Unit
) : RecyclerView.Adapter<DateAdapter.DateViewHolder>() {

    private var currentSelectedDate: Date = selectedDate

    class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewDate: TextView = itemView.findViewById(R.id.textViewDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_date, parent, false)
        return DateViewHolder(view)
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        val date = dates[position]
        val sdf = SimpleDateFormat("EEE\ndd/MM", Locale.getDefault())
        holder.textViewDate.text = sdf.format(date)

        holder.itemView.setOnClickListener {
            currentSelectedDate = date
            onDateSelected(date)
            notifyDataSetChanged()
        }

        if (currentSelectedDate == date) {
            holder.itemView.setBackgroundColor(holder.itemView.context.getColor(R.color.green))
        } else {
            holder.itemView.setBackgroundColor(holder.itemView.context.getColor(R.color.white))
        }
    }

    override fun getItemCount() = dates.size
}
