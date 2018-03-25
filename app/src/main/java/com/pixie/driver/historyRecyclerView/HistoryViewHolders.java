package com.pixie.driver.historyRecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.pixie.driver.R;

/**
 * Created by raulb on 26/11/2017.
 */

public class HistoryViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView rideId;

    public HistoryViewHolders(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        rideId = (TextView)itemView.findViewById(R.id.rideId);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(view.getContext(), HistorySingleActivity.class);
        Bundle b = new Bundle();
        b.putString("rideId", rideId.getText().toString());
        intent.putExtras(b);
        view.getContext().startActivity(intent);

    }
}
