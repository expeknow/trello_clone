package com.example.trelloclone.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.trelloclone.databinding.ItemLabelColorBinding

/**
 * This adapter is used to show the list of card colors that user can pick in a Dialog. Unlike others,
 * this Adapter's Recycler View is located in a Dialog
 *
 * @property context
 * @property list
 * @property mSelectedColor
 */
class LabelColorListItemAdapter(private val context: Context, private val list: ArrayList<String>,
private val mSelectedColor: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onItemClickListener : OnItemClickListener? = null

    class MyViewHolder(binding: ItemLabelColorBinding) : RecyclerView.ViewHolder(binding.root) {
        val viewMain = binding.viewMain
        val ivSelectedColor = binding.ivSelectedColor
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(ItemLabelColorBinding
            .inflate(LayoutInflater.from(parent.context),
            parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = list[position]
        if(holder is MyViewHolder){
            //setting color for the individual row item
            holder.viewMain.setBackgroundColor(Color.parseColor(item))
            //set selected color's tick mark on or off
            if(item == mSelectedColor){
                holder.ivSelectedColor.visibility = View.VISIBLE
            }else{
                holder.ivSelectedColor.visibility = View.GONE
            }
        }
        holder.itemView.setOnClickListener {
            if(onItemClickListener != null){
                onItemClickListener?.onClick(position, item)
            }
        }
    }

    interface OnItemClickListener {
        fun onClick(position: Int, color:String)
    }
}