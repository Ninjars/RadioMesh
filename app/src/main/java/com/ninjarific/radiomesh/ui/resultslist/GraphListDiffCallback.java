package com.ninjarific.radiomesh.ui.resultslist;

import android.support.v7.util.DiffUtil;

import com.ninjarific.radiomesh.database.room.queries.PopulatedGraph;

import java.util.List;


public class GraphListDiffCallback extends DiffUtil.Callback {

    private final List<PopulatedGraph> old;
    private final List<PopulatedGraph> current;

    public GraphListDiffCallback(List<PopulatedGraph> old, List<PopulatedGraph> current) {
        this.old = old;
        this.current = current;
    }

    @Override
    public int getOldListSize() {
        return old.size();
    }

    @Override
    public int getNewListSize() {
        return current.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        PopulatedGraph oldItem = old.get(oldItemPosition);
        PopulatedGraph newItem = current.get(newItemPosition);
        return oldItem.getGraph().getId() == newItem.getGraph().getId() ;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        PopulatedGraph oldItem = old.get(oldItemPosition);
        PopulatedGraph newItem = current.get(newItemPosition);
        return oldItem.getNodes().size() == newItem.getNodes().size();
    }
}
