package com.vishwanth.callmera.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vishwanth.callmera.R
import com.vishwanth.callmera.model.CallHistory

class HistoryAdapter(
    private val historyList: List<CallHistory>
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(itemView: View)
        : RecyclerView.ViewHolder(itemView) {

        val txtDate: TextView =
            itemView.findViewById(R.id.txtDate)

        val txtMorning: TextView =
            itemView.findViewById(R.id.txtMorning)

        val txtEvening: TextView =
            itemView.findViewById(R.id.txtEvening)

        val txtStatus: TextView =
            itemView.findViewById(R.id.txtStatus)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HistoryViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_history,
                parent,
                false
            )

        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: HistoryViewHolder,
        position: Int
    ) {

        val item = historyList[position]

        holder.txtDate.text = item.date

        holder.txtMorning.text =
            "Morning : ${if(item.morning) "✅" else "❌"}"

        holder.txtEvening.text =
            "Evening : ${if(item.evening) "✅" else "❌"}"

        holder.txtStatus.text =
            if(item.morning && item.evening)
                "Status : Counted ✅"
            else
                "Status : Not Counted ❌"
    }

    override fun getItemCount(): Int {
        return historyList.size
    }
}