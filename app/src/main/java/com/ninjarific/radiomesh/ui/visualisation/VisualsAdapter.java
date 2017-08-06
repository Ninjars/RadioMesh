package com.ninjarific.radiomesh.ui.visualisation;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ninjarific.radiomesh.R;
import com.ninjarific.radiomesh.database.realm.RadioPoint;

import java.util.Collections;
import java.util.List;

class VisualsAdapter extends RecyclerView.Adapter {

    private final IOnEntryClickListener listener;
    private List<List<RadioPoint>> dataset = Collections.emptyList();

    VisualsAdapter(IOnEntryClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<List<RadioPoint>> dataset) {
        this.dataset = dataset;
        notifyDataSetChanged();
    }

    private static class VisualsHolder extends RecyclerView.ViewHolder {

        private final CircularVisualisation visualsView;

        VisualsHolder(View itemView) {
            super(itemView);
            visualsView = (CircularVisualisation) itemView;
        }

        void setData(List<RadioPoint> data, View.OnClickListener listener) {
            visualsView.setDataset(data);
            itemView.setOnClickListener(listener);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VisualsHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_visualisation,
                parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((VisualsHolder) holder).setData(dataset.get(position), view -> listener.onVisualsItemClick(position));
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }
}
