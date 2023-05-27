package com.example.trelloclone.adapters

import android.content.Context
import android.opengl.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trelloclone.R
import com.example.trelloclone.databinding.ItemCardSelectedMemberBinding
import com.example.trelloclone.models.SelectedMembers

/**
 * Card member list items adapter is used to show the list of members assigned to a particular board.
 *
 * @property context
 * @property list
 * @constructor Create [CardMemberListItemsAdapter]
 */
open class CardMemberListItemsAdapter(
    private val context: Context,
    private val list: ArrayList<SelectedMembers>,
    //checks if we want to assign members or not. For plus icon
    private val assignMembers: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    class MyViewHolder(binding: ItemCardSelectedMemberBinding) : RecyclerView.ViewHolder(binding.root){
        val ivSelectedMemberImage = binding.ivSelectedMemberImage
        val ivAddMember = binding.ivAddMember
    }

    private var onClickListener : OnClickListener? = null

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(ItemCardSelectedMemberBinding.inflate(LayoutInflater.from(parent.context),
        parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]
        if(holder is MyViewHolder){
            if(position == list.size -1 && assignMembers){
                holder.ivAddMember.visibility = View.VISIBLE
                holder.ivSelectedMemberImage.visibility = View.GONE
            }else{
                holder.ivAddMember.visibility = View.GONE
                holder.ivSelectedMemberImage.visibility = View.VISIBLE
                Glide
                    .with(context)
                    .load(model.image)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(holder.ivSelectedMemberImage)
            }

            holder.itemView.setOnClickListener {
                if(onClickListener != null) {
                    onClickListener!!.onClick()
                }
            }
        }
    }

}