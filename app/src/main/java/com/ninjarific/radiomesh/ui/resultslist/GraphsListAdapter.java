package com.ninjarific.radiomesh.ui.resultslist;

import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ninjarific.radiomesh.R;
import com.ninjarific.radiomesh.database.room.queries.PopulatedGraph;

import java.util.Collections;
import java.util.List;

class GraphsListAdapter extends RecyclerView.Adapter<GraphsListAdapter.GraphViewHolder> {

    private List<PopulatedGraph> currentData = Collections.emptyList();

    public void setCurrentData(List<PopulatedGraph> data) {
        GraphListDiffCallback diffCallback = new GraphListDiffCallback(currentData, data);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(diffCallback);
        currentData = data;
        result.dispatchUpdatesTo(this);
    }

    @Override
    public GraphViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_graph, parent, false);
        return new GraphViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(GraphViewHolder holder, int position) {
        holder.update(currentData.get(position));
    }

    @Override
    public int getItemCount() {
        return currentData.size();
    }

    class GraphViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleView;
        private final TextView countView;
        private final String idStringFormat;
        private final String countStringFormat;

        GraphViewHolder(View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.id);
            countView = itemView.findViewById(R.id.count);
            idStringFormat = itemView.getContext().getString(R.string.id_readout);
            countStringFormat = itemView.getContext().getString(R.string.count_readout);
        }

        void update(PopulatedGraph populatedGraph) {
            titleView.setText(String.format(idStringFormat, String.valueOf(populatedGraph.getGraph().getId())));
            countView.setText(String.format(countStringFormat, String.valueOf(populatedGraph.getNodes().size())));
        }
    }
}
