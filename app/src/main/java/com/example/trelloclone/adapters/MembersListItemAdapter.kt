package com.example.trelloclone.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trelloclone.R
import com.example.trelloclone.databinding.ItemMemberBinding
import com.example.trelloclone.models.User
import com.example.trelloclone.utils.Constants

/**
 * Members list item adapter is used to show the list of members assigned to a particular Card.
 * The recycler View is based off a GridLayout in here.
 *
 * @property context
 * @property list
 * @constructor Create [MembersListItemAdapter]
 */

class MembersListItemAdapter(private val context: Context, private val list: ArrayList<User>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener : OnClickListener? = null

    class MyViewHolder (binding: ItemMemberBinding) : RecyclerView.ViewHolder(binding.root) {
        val ivMemberImage = binding.ivMemberImage
        val tvMemberName = binding.tvMemberName
        val tvMemberEmail = binding.tvMemberEmail
        val ivSelectedMember = binding.ivSelectedMember
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            ItemMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if(holder is MyViewHolder){
            holder.tvMemberName.text = model.name
            holder.tvMemberEmail.text = model.email
            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(holder.ivMemberImage)

            //Visibality for tick mark in itemView
            if(model.selected){
                holder.ivSelectedMember.visibility = View.VISIBLE
            }else{
                holder.ivSelectedMember.visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                if(onClickListener != null){
                    //If the member is selected, clicking on it makes the member unselected
                    if(model.selected){
                        onClickListener!!.onClick(position, model, Constants.UN_SELECT)
                    }else{
                        onClickListener!!.onClick(position, model, Constants.SELECT)
                    }
                }
            }
        }
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    interface OnClickListener{
        fun onClick(position: Int, user: User, action: String)
    }
}