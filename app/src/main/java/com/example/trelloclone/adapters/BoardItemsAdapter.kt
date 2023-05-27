package com.example.trelloclone.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PostProcessor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trelloclone.R
import com.example.trelloclone.databinding.ActivityCreateBoardBinding
import com.example.trelloclone.databinding.ItemBoardBinding
import com.example.trelloclone.models.Board


// private val with a parameter makes the variable of that parameter with the same name and this
// variable can be accessed from anywhere in this class

// Here we are also going to implement onClickListener for each RecyclerViewItem so look for the
// 5 steps to implement onClickListener

open class BoardItemsAdapter(private val context: Context,
                             private val list: ArrayList<Board>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //STEP 2: a listener variable to make recyclerView rows clickable. This is the same interface we
    //created below.
    private var onClickListener: OnClickListener? = null

    /**
     * This is used to hold the views of the item board class
     */
    class MyViewHolder(binding: ItemBoardBinding): RecyclerView.ViewHolder(binding.root) {
        val boardName = binding.tvName
        val boardImage = binding.ivBoardImage
        val createdBy = binding.tvCreatedBy
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            ItemBoardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        //This check is neccessary because we can create multiple viewholder classes like MyViewHolder
        //where each of them holds different views
        if(holder is MyViewHolder){
            Glide
            .with(context)
            .load(model.image)
            .centerCrop()
            .placeholder(R.drawable.ic_board_place_holder)
            .into(holder.boardImage)

        holder.boardName.text = model.name
        holder.createdBy.text = "Created by: ${model.createdBy}"

        //Item view is the whole row card
        //STEP 5: Here only, all entries in RV are assigned their content so we assign onClickListener
        //in here to all itemViews. where one itemView is the whole row card.
        holder.itemView.setOnClickListener {
            if(onClickListener != null){
                onClickListener!!.onClick(position, model)
                }
            }
        }
    }

    // STEP 3: Creating an On Click Listener to set the value for our onClickListener variable we
    //created above
    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    //STEP 1: create an interface for onClickListener. The parameter for onClick is what we pass
    //whenever click happens. This value is same for all basic adapters
    interface OnClickListener{
        fun onClick(position: Int, model: Board)
    }

}


