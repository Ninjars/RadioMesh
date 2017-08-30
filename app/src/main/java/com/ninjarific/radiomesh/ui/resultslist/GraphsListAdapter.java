package com.ninjarific.radiomesh.ui.resultslist;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ninjarific.radiomesh.R;

import java.util.Collections;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

class GraphsListAdapter extends RecyclerView.Adapter<GraphsListAdapter.GraphViewHolder> {

    private List<Flowable<GraphNodes>> currentData = Collections.emptyList();

    private void setCurrentData(List<Flowable<GraphNodes>> data) {
        Timber.i("setCurrentData " + data.size());
        currentData = data;
        notifyDataSetChanged();
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

    public Disposable bind(Flowable<List<Flowable<GraphNodes>>> populatedGraphs) {
        return populatedGraphs
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setCurrentData, Throwable::printStackTrace);
    }

    class GraphViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleView;
        private final TextView countView;
        private final String idStringFormat;
        private final String countStringFormat;
        private Disposable disposable;

        GraphViewHolder(View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.id);
            countView = itemView.findViewById(R.id.count);
            idStringFormat = itemView.getContext().getString(R.string.id_readout);
            countStringFormat = itemView.getContext().getString(R.string.count_readout);
        }

        void update(Flowable<GraphNodes> populatedGraph) {
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
            }
            disposable = populatedGraph
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(graph -> {
                                Timber.i("update graphNode " + graph.getGraphId());
                                titleView.setText(String.format(idStringFormat, String.valueOf(graph.getGraphId())));
                                countView.setText(String.format(countStringFormat, String.valueOf(graph.getNodes().size())));
                            },
                            Throwable::printStackTrace);
        }
    }
}
