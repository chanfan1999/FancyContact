package com.chanfan.mycontact

import android.provider.ContactsContract
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.chanfan.mycontact.MyApplication.Companion.context

class MainViewModel : ViewModel() {
    val contactList = ArrayList<Contact>()
    val originList = ArrayList<Contact>()
    private val searchLiveData = MutableLiveData<String>()

    val contactLiveData = Transformations.switchMap(searchLiveData) {
        searchContact(it)
    }

    fun search(s: String) {
        searchLiveData.value = s
    }

    private fun searchContact(contactName: String): LiveData<ArrayList<Contact>> {
        val l = MutableLiveData<ArrayList<Contact>>()
        l.value = ArrayList()
        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null
            ,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " LIKE ?",
            arrayOf("$contactName%"),
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        )?.apply {
            while (moveToNext()) {
                val name =
                    getString(getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val phone = getString(getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                val contact = Contact(name, phone)
                l.value?.add(contact)
            }
            close()
        }

        return l
    }
}