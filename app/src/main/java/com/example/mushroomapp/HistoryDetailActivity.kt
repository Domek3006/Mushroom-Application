package com.example.mushroomapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mushroomapp.adapters.HistoryAdapter
import com.example.mushroomapp.database.DBConnector
import com.example.mushroomapp.modal.HistoryEntry

class HistoryDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        val preferences = getSharedPreferences("mushroomapp", MODE_PRIVATE)
        val userId = preferences.getInt("USERID", -1)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_detail)


        var currEntryId: Int = -1
        var currEntryName: String? = ""

        val bundle: Bundle? = intent.extras
        if (bundle != null){
            currEntryId = bundle.getInt("ENTRYID")
            currEntryName = bundle.getString("ENTRYNAME")
        }

        /*fun adapterOnClickHistory(entry: HistoryEntry) {
            //val intent = Intent(this, )

        }*/


        val historyEntryName = findViewById<TextView>(R.id.detailHistorySpecies)
        val recyclerView = findViewById<RecyclerView>(R.id.detailHistoryRecycler)
        val imgCount = findViewById<TextView>(R.id.detailHistoryCount)

        historyEntryName.text = currEntryName

        val recyclerAdapter = HistoryAdapter(DBConnector.getEntryImages(DBConnector(), userId, currEntryId))
        recyclerView.adapter = recyclerAdapter

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager

        recyclerView.adapter?.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        imgCount.text = "Liczba zdjęć: " + recyclerAdapter.itemCount

    }
}