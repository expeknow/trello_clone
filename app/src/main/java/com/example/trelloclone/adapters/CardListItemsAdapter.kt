package com.example.trelloclone.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trelloclone.activities.TaskListActivity
import com.example.trelloclone.databinding.ItemCardBinding
import com.example.trelloclone.models.Card
import com.example.trelloclone.models.SelectedMembers

class CardListItemsAdapter(private val context: Context, private val list: ArrayList<Card>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>(){


    private var onClickListener: OnClickListener? = null

    class MyViewHolder(binding: ItemCardBinding) : RecyclerView.ViewHolder(binding.root){
        val tvCardName = binding.tvCardName
        val viewLabelColor = binding.viewLabelColor
        val rvSelectedMembers = binding.rvCardSelectedMembersList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(ItemCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val model = list[position]
        if(holder is MyViewHolder){
            //set the color of card
            if(model.labelColor.isNotEmpty()){
                holder.viewLabelColor.visibility = View.VISIBLE
                holder.viewLabelColor.setBackgroundColor(Color.parseColor(model.labelColor))
            }else{
                holder.viewLabelColor.visibility = View.GONE
            }
            holder.tvCardName.text = model.name

            //If members assigned to a particular board is at least 1
            if((context as TaskListActivity).mAssignedMemberDetailList.size > 0){
                //create a list of members who are selected as a member for a card also
                val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()
                //in all the members assigned to a board
                for(i in context.mAssignedMemberDetailList.indices){
                    //in all the members assigned to a card
                    for(j in model.assignedTo){
                        //if board assignee is same as card assignee
                        if(context.mAssignedMemberDetailList[i].id == j){
                            //create a selected member object
                            val selectedMembers = SelectedMembers(
                                context.mAssignedMemberDetailList[i].id!!,
                                context.mAssignedMemberDetailList[i].image!!
                            )
                            //add that in the list of selectedMember we created above
                            selectedMembersList.add(selectedMembers)
                        }
                    }
                }

                //If list of members assigned to a card is at lease 1
                if(selectedMembersList.size > 0){
                    //if card assignee is only 1 and he is the person who created the card
                    if(selectedMembersList.size == 1 && selectedMembersList[0].id == model.createdBy){
                        //remove recycler View  because it is not important
                        holder.rvSelectedMembers.visibility = View.GONE
                    //if card assignee is more than 1
                    }else{
                        //setup the recyclerView to show the members assigned to a card on card itself.
                        holder.rvSelectedMembers.visibility = View.VISIBLE
                        holder.rvSelectedMembers.layoutManager = GridLayoutManager(
                            context, 4)
                        val adapter = CardMemberListItemsAdapter(context, selectedMembersList, false)
                        holder.rvSelectedMembers.adapter = adapter
                        adapter.setOnClickListener(object : CardMemberListItemsAdapter.OnClickListener{
                            override fun onClick() {
                                if(onClickListener != null){
                                    onClickListener!!.onClick(position)
                                }
                            }

                        })

                    }
                //if members assigned to a card is 0
                }else{
                    holder.rvSelectedMembers.visibility = View.GONE
                }
            }
            holder.itemView.setOnClickListener {
                if(onClickListener != null){
                    onClickListener!!.onClick(position)
                }
            }
        }
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener{
        fun onClick(position: Int)
    }
}