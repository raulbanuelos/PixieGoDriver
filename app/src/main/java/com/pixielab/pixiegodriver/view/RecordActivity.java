package com.pixielab.pixiegodriver.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.pixielab.pixiegodriver.R;
import com.pixielab.pixiegodriver.model.HistoryAdapter;
import com.pixielab.pixiegodriver.model.HistoryObject;

import java.util.ArrayList;
import java.util.List;

public class RecordActivity extends AppCompatActivity {

    private RecyclerView mHistoryRecyclerView;
    private RecyclerView.Adapter mHistoryAdapter;
    private RecyclerView.LayoutManager mHistoryLayoutManager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        mHistoryRecyclerView = (RecyclerView) findViewById(R.id.historyRecyclerView);
        mHistoryRecyclerView.setNestedScrollingEnabled(false);
        mHistoryRecyclerView.setHasFixedSize(true);
        mHistoryLayoutManager = new LinearLayoutManager(RecordActivity.this);
        mHistoryRecyclerView.setLayoutManager(mHistoryLayoutManager);
        mHistoryAdapter = new HistoryAdapter(getDataSetHistory(),RecordActivity.this);

        mHistoryRecyclerView.setAdapter(mHistoryAdapter);


        for (int i = 0; i< 100; i++){
            HistoryObject obj = new HistoryObject(Integer.toString(i));
            resultHistory.add(obj);

        }

        mHistoryAdapter.notifyDataSetChanged();
    }

    private ArrayList resultHistory = new ArrayList<HistoryObject>();

    public ArrayList<HistoryObject> getDataSetHistory() {

        return resultHistory;
    }
}
