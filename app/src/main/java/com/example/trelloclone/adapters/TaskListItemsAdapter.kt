package com.example.trelloclone.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.icu.text.CaseMap.Title
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trelloclone.activities.TaskListActivity
import com.example.trelloclone.databinding.ItemBoardBinding
import com.example.trelloclone.databinding.ItemTaskBinding
import com.example.trelloclone.models.Task
import com.google.api.Distribution.BucketOptions.Linear
import java.util.*
import kotlin.collections.ArrayList


open class TaskListItemsAdapter(private val context: Context,
                                private var list : ArrayList<Task>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private var mPositionDraggedFrom = -1
    private var mPositionDraggedTo = -1

    class MyViewHolder(binding: ItemTaskBinding): RecyclerView.ViewHolder(binding.root){
        //here you can save all the views in a variable
        val tvAddTaskList = binding.tvAddTaskList
        val llTaskItem = binding.llTaskItem
        val tvTaskListTitle = binding.tvTaskListTitle
        val cvAddTaskListName = binding.cvAddTaskListName
        val ibCloseListName = binding.ibCloseListName
        val ibDoneListName = binding.ibDoneListName
        val etTaskListNmae = binding.etTaskListName
        val ibEditListName = binding.ibEditListName
        val ibDeleteList = binding.ibDeleteList
        val etEditTasklistName = binding.etEditTaskListName
        val llTitleView = binding.llTitleView
        val cvEditTaskListName = binding.cvEditTaskListName
        val ibCloseEditableView = binding.ibCloseEditableView
        val ibDoneEditListName = binding.ibDoneEditListName
        val tvAddCard = binding.tvAddCard
        val cvAddCard = binding.cvAddCard
        val ibCloseCardName = binding.ibCloseCardName
        val ibDoneCardName = binding.ibDoneCardName
        val etCardName = binding.etCardName
        val rvCardList = binding.rvCardList
    }

    /**
     * Here we are setting our viewholder's margin and width before sending it to create an itemview
     */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context),
            parent,
            false)
        //defining the size for our viewholder. Not he itemview but ItemView Holder
        val layoutParams = LinearLayout.LayoutParams(
            (parent.width * 0.7).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(
            (15.toDp()).toPx(), 0, (40.toDp()).toPx(), 0)
        binding.root.layoutParams = layoutParams
        return MyViewHolder(binding)

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val model = list[position]
        if(holder is MyViewHolder){
            //If there is not entry in our task List, we show "add list" button else show list
            if(position == list.size-1){
                holder.tvAddTaskList.visibility = View.VISIBLE
                holder.llTaskItem.visibility = View.GONE
            }else{
                holder.tvAddTaskList.visibility = View.GONE
                holder.llTaskItem.visibility = View.VISIBLE
            }

            holder. tvTaskListTitle.text = model.title
            holder.tvAddTaskList.setOnClickListener {
                holder.tvAddTaskList.visibility = View.GONE
                holder.cvAddTaskListName.visibility = View.VISIBLE
            }

            holder.ibCloseListName.setOnClickListener {
                holder.tvAddTaskList.visibility = View.VISIBLE
                holder.cvAddTaskListName.visibility = View.GONE
            }
            holder.ibDoneListName.setOnClickListener {
                val listName = holder.etTaskListNmae.text.toString()

                if(listName.isNotEmpty()){
                    if(context is TaskListActivity){
                        context.createTaskList(listName)
                    }
                }else{
                    Toast.makeText(context, "Please enter a list name", Toast.LENGTH_LONG).show()
                }
            }
            holder.ibEditListName.setOnClickListener {
                holder.etEditTasklistName.setText(model.title)
                holder.llTitleView.visibility = View.GONE
                holder.cvEditTaskListName.visibility = View.VISIBLE
            }

            holder.ibCloseEditableView.setOnClickListener {
                holder.llTitleView.visibility = View.VISIBLE
                holder.cvEditTaskListName.visibility = View.GONE
            }

            holder.ibDoneEditListName.setOnClickListener{
                val listName = holder.etEditTasklistName.text.toString()
                if(listName.isNotEmpty()){
                    if(context is TaskListActivity){
                        context.updateTaskList(position, listName, model)
                    }
                }else{
                    Toast.makeText(context, "Please enter a list name", Toast.LENGTH_LONG).show()
                }
            }
            holder.ibDeleteList.setOnClickListener {
                alertDialogForDeleteList(position, model.title)
            }

            holder.tvAddCard.setOnClickListener {
                holder.cvAddCard.visibility = View.VISIBLE
                holder.tvAddCard.visibility = View.GONE
            }

            holder.ibCloseCardName.setOnClickListener {
                holder.cvAddCard.visibility = View.GONE
                holder.tvAddCard.visibility = View.VISIBLE
            }

            holder.ibDoneCardName.setOnClickListener{
                val cardName = holder.etCardName.text.toString()
                if(cardName.isNotEmpty()){
                    if(context is TaskListActivity){
                        context.addCardToTaskList(position, cardName)
                    }
                }else{
                    Toast.makeText(context, "Please enter a card name", Toast.LENGTH_LONG).show()
                }
            }

            holder.rvCardList.layoutManager =
                LinearLayoutManager(context)
            holder.rvCardList.setHasFixedSize(true)
            val adapter = CardListItemsAdapter(context, model.cards)
            holder.rvCardList.adapter = adapter

            adapter.setOnClickListener(
                object : CardListItemsAdapter.OnClickListener {
                    override fun onClick(cardPosition: Int) {
                        if(context is TaskListActivity){
                            //here position is given by this adapter and cardPosition is given by other adapter
                            context.cardDetails(position, cardPosition)
                        }
                    }

                }
            )
            //TODO Understand the code for drag and drop Step 1
            /**
             * Creates a divider {@link RecyclerView.ItemDecoration} that can be used with a
             * {@link LinearLayoutManager}.
             */
            val dividerItemDecoration = DividerItemDecoration(context,
                DividerItemDecoration.VERTICAL)
            holder.rvCardList.addItemDecoration(dividerItemDecoration)

            //  Creates an ItemTouchHelper that will work with the given Callback.
            val helper = ItemTouchHelper(
                object: ItemTouchHelper.SimpleCallback(
                    ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
                ){

                    /**Called when ItemTouchHelper wants to move the dragged item from its old position to
                    the new position.*/
                    override fun onMove(
                        recyclerView: RecyclerView,
                        dragged: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder
                    ): Boolean {
                        val draggedPosition = dragged.adapterPosition
                        val targetPosition = target.adapterPosition
                        if(mPositionDraggedFrom == -1){
                            mPositionDraggedFrom = draggedPosition
                        }
                        mPositionDraggedTo = targetPosition

                        /**
                         * Swaps the elements at the specified positions in the specified list.
                         */
                        Collections.swap(list[position].cards, draggedPosition, targetPosition)
                        // move item in `draggedPosition` to `targetPosition` in adapter.
                        adapter.notifyItemMoved(draggedPosition, targetPosition)
                        return false // true if moved, false otherwise
                    }

                    // Called when a ViewHolder is swiped by the user.
                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                    }

                    /**Called by the ItemTouchHelper when the user interaction with an element is over and it
                    also completed its animation.*/
                    override fun clearView(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder
                    ) {
                        super.clearView(recyclerView, viewHolder)

                        if(mPositionDraggedFrom != -1 && mPositionDraggedTo != -1 &&
                                mPositionDraggedFrom != mPositionDraggedTo){
                            (context as TaskListActivity).updateCardsInTaskList(
                                position,
                                list[position].cards
                            )
                        }
                        // Reset the global variables
                        mPositionDraggedFrom = -1
                        mPositionDraggedTo = -1
                    }

                }
            )
            /**
             * Attaches the ItemTouchHelper to the provided RecyclerView. If TouchHelper is already
            attached to a RecyclerView, it will first detach from the previous one.
             */
            helper.attachToRecyclerView(holder.rvCardList)

        }
    }

    /**
     * Confirms from user if they really want to delete the list or not
     */
    private fun alertDialogForDeleteList(position: Int, title: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Alert")
        builder.setMessage("Are you sure you want to delete $title")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Yes") {
            dialog, which ->
            dialog.dismiss()
            if(context is TaskListActivity){
                context.deleteTaskList(position)
            }
        }.setNegativeButton("No") {
            dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog : AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    override fun getItemCount(): Int {
        return list.size
    }

    //this time we want to make our itemView take limited space on screen i.e. 70% of screen
    //So we need a function for that
    /**
     * Gets the density of the screen in integer
     */
    private fun Int.toDp() : Int =
        (this/Resources.getSystem().displayMetrics.density).toInt()

    /**
     * Gets the pixel of the screen from density pixel (DP)
     */
    private fun Int.toPx() : Int =
        (this*Resources.getSystem().displayMetrics.density).toInt()


}