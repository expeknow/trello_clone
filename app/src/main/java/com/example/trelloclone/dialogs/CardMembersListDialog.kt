package com.example.trelloclone.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trelloclone.adapters.MembersListItemAdapter
import com.example.trelloclone.databinding.DialogListBinding
import com.example.trelloclone.models.User

abstract class CardMembersListDialog(
    context: Context,
    private var list: ArrayList<User>,
    private val title: String = ""
) : Dialog(context){

    private var adapter : MembersListItemAdapter? = null
    private var binding : DialogListBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DialogListBinding.inflate(layoutInflater)
        setContentView(binding?.root!!)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        setRecyclerView(binding!!)

    }

    private fun setRecyclerView(binding: DialogListBinding) {

        binding.tvTitle.text = title

        if(list.size > 0) {
            binding.rvList.layoutManager = LinearLayoutManager(context)
            adapter = MembersListItemAdapter(context, list)
            binding.rvList.adapter = adapter

            adapter!!.setOnClickListener(object:
            MembersListItemAdapter.OnClickListener {
                override fun onClick(position: Int, user: User, action: String) {
                    dismiss()
                    onItemSelected(user, action)
                }

            })
        }


    }

    protected abstract fun onItemSelected(user: User, action: String)

}