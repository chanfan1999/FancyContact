package com.chanfan.mycontact

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chanfan.mycontact.MyApplication.Companion.context
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import kotlin.random.Random

class ContactAdapter(private val contactList: List<Contact>) :
    RecyclerView.Adapter<ContactAdapter.ViewHolder>() {
    private val r = Random(1)
    private val avatarArray = arrayListOf<Int>(
        R.drawable.cat,
        R.drawable.cat,
        R.drawable.cat1,
        R.drawable.cat2,
        R.drawable.dog1,
        R.drawable.dog2,
        R.drawable.dog3
    )

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val contactName: TextView = view.findViewById(R.id.contactName)
        val contactPhone: TextView = view.findViewById(R.id.contactPhone)
        val call: Button = view.findViewById(R.id.call)
        val sms: Button = view.findViewById(R.id.sms)
        val card: MaterialCardView = view.findViewById(R.id.card)
        val avatar: ShapeableImageView = view.findViewById(R.id.avatar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.contact_item, parent, false)
        val holder = ViewHolder(view)
        holder.call.setOnClickListener {
            makeCall(holder.contactPhone.text.toString())
        }
        holder.sms.setOnClickListener {
            sendMSG(holder.contactPhone.text.toString())
        }
        holder.card.setCardBackgroundColor(
            Color.rgb(
                r.nextInt(0, 255),
                r.nextInt(0, 255),
                r.nextInt(0, 255)
            )
        )
        holder.avatar.setImageResource(avatarArray[r.nextInt(avatarArray.size)])
        return holder
    }

    override fun getItemCount() = contactList.size


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = contactList[position]
        holder.contactName.text = contact.name
        holder.contactPhone.text = contact.phone
    }

    private fun makeCall(phone: String) {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:$phone")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    private fun sendMSG(phone: String) {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("smsto:$phone")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

}