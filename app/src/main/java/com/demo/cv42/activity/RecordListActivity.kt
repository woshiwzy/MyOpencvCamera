package com.demo.cv42.activity

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.demo.cv42.App
import com.demo.cv42.App.Companion.app
import com.demo.cv42.R
import com.wangzy.face.DbController
import com.wangzy.face.People
import kotlinx.android.synthetic.main.activity_record_list.*

class RecordListActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_list)

        val allPeople = DbController.getInstance(app).session.peopleDao.loadAll()
        Log.e(App.tag, "allcount:" + allPeople.size)
        val peopleAdapter = PeopleAdapter(R.layout.item_people, allPeople)
        recylerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recylerView.adapter = peopleAdapter

    }

    internal inner class PeopleAdapter(layoutResId: Int, data: MutableList<People?>?) : BaseQuickAdapter<People?, BaseViewHolder>(layoutResId, data) {

        override fun convert(baseViewHolder: BaseViewHolder, people: People?) {
            baseViewHolder.setText(R.id.textViewName, "" + people?.name)
        }
    }
}