package com.ninjarific.radiomesh.ui.resultslist;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ninjarific.radiomesh.R;
import com.ninjarific.radiomesh.database.realm.RadioPoint;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class RadioResultsListAdapter extends RealmRecyclerViewAdapter<RadioPoint, RadioResultsListAdapter
        .RadioResultViewHolder> {


    public RadioResultsListAdapter(@Nullable OrderedRealmCollection<RadioPoint> data) {
        super(data, true);
    }

    @Override
    public RadioResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RadioResultViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.radio_result_list_entry, parent, false));
    }

    @Override
    public void onBindViewHolder(RadioResultViewHolder holder, int position) {
        RadioPoint point = getData().get(position);
        holder.bindData(point);
    }

    class RadioResultViewHolder extends RecyclerView.ViewHolder {
        private final TextView bssid, ssid, connectionCount;

        RadioResultViewHolder(View itemView) {
            super(itemView);
            bssid = itemView.findViewById(R.id.bssid);
            ssid = itemView.findViewById(R.id.ssid);
            connectionCount = itemView.findViewById(R.id.count);
        }

        void bindData(RadioPoint data) {
            bssid.setText(data.getBssid());
            ssid.setText(data.getSsid());
            connectionCount.setText(String.valueOf(data.getConnectedPoints().size()));
        }
    }
}
