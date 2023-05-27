package com.example.trelloclone.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trelloclone.adapters.LabelColorListItemAdapter
import com.example.trelloclone.databinding.DialogListBinding


/**
 * This is used to implement the color picker dialog. It is implementing a recycler View in the
 * Dialog but How? Still figuring  it out.
 */

abstract class LabelColorListDialog(
    context: Context,
    private var list: ArrayList<String>,
    private var mSelectedColor: String = "",
    private val title: String = ""

) : Dialog(context){

    private var adapter : LabelColorListItemAdapter?  = null
    var binding : DialogListBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DialogListBinding.inflate(layoutInflater)

        setContentView(binding!!.root)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setUpRecyclerView(binding!!)
    }

    private fun setUpRecyclerView(binding: DialogListBinding) {
        binding.tvTitle.text = title
        binding.rvList.layoutManager = LinearLayoutManager(context)
        adapter = LabelColorListItemAdapter(context, list, mSelectedColor)
        binding.rvList.adapter = adapter

        adapter!!.onItemClickListener = object : LabelColorListItemAdapter.OnItemClickListener{
            override fun onClick(position: Int, color: String) {
                dismiss()
                onItemSelected(color)
            }
        }
    }

    protected abstract fun onItemSelected(color: String)

}