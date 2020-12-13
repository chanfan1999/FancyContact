package com.chanfan.mycontact

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import ccy.focuslayoutmanager.FocusLayoutManager
import ccy.focuslayoutmanager.FocusLayoutManager.dp2px
import com.chanfan.mycontact.MyApplication.Companion.context
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private val viewModel by lazy { ViewModelProvider(this)[MainViewModel::class.java] }
    lateinit var adapter: ContactAdapter
    private val maxLayerCount = 4
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        adapter = ContactAdapter(viewModel.contactList)
        initList()
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions()
        } else {
            loadContactList()
        }


        val focusLayoutManager = FocusLayoutManager.Builder()
            .layerPadding(dp2px(this, 30f))
            .normalViewGap(dp2px(this, 10f))
            .focusOrientation(FocusLayoutManager.FOCUS_TOP)
            .isAutoSelect(false)
            .maxLayerCount(maxLayerCount)
            .setOnFocusChangeListener { focusdPosition, lastFocusdPosition -> }
            .build()

        recyclerView.adapter = adapter
        recyclerView.layoutManager = focusLayoutManager

        val itemTouchHelper = ItemTouchHelper(SwipeToDelete(adapter))
        itemTouchHelper.attachToRecyclerView(recyclerView)

        searchText.addTextChangedListener {
            val content = it.toString()
            if (content.isNotEmpty()) {
                viewModel.search(content)
            } else {
                viewModel.contactList.apply {
                    clear()
                    addAll(viewModel.originList)
                    adapter.notifyDataSetChanged()
                }
            }
        }

        viewModel.contactLiveData.observe(this, Observer {
            if (it.size > 0) {
                viewModel.contactList.clear()
                initList()
                viewModel.contactList.addAll(it)
                adapter.notifyDataSetChanged()
            } else {
                viewModel.contactList.clear()
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "没有结果", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun initList() {
        // 填充假数据，避免真实数据成为padding被吞
        for (i in 0 until maxLayerCount - 1) {
            viewModel.contactList.add(Contact("", ""))
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                var allGranted = true
                for (result in grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allGranted = false
                    }
                }
                if (allGranted) {
                    loadContactList()
                } else {
                    AlertDialog.Builder(this).apply {
                        setMessage("不给通讯录权限怎么看你的通讯录呢?\n不给电话权限怎么打call呢?")
                        setCancelable(false)
                        setPositiveButton("确定") { _, _ ->
                            requestPermissions()
                        }
                    }.show()
                }
            }
        }
    }

    private fun loadContactList() {
        contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null
            , null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        )?.apply {
            while (moveToNext()) {
                val name =
                    getString(getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val phone = getString(getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                val contact = Contact(name, phone)
                viewModel.contactList.add(contact)
                //数据量不够，重复添加一手
                viewModel.contactList.add(contact)
            }
            viewModel.originList.addAll(viewModel.contactList)
            close()
            adapter.notifyDataSetChanged()
        }


    }

    private fun requestPermissions() =
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE), 1
        )

    inner class SwipeToDelete(val mAdapter: ContactAdapter) : ItemTouchHelper.SimpleCallback(
        0,
        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            TODO("Not yet implemented")
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            viewModel.contactList.removeAt(position)
            adapter.notifyItemRemoved(position)
            if (viewModel.contactList.size <= maxLayerCount - 1) {
                viewModel.contactList.clear()
                adapter.notifyDataSetChanged()
            }
        }
    }
}