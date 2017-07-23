package com.ninjarific.radiomesh.ui.visualisation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.ninjarific.radiomesh.MainApplication;
import com.ninjarific.radiomesh.R;
import com.ninjarific.radiomesh.database.RadioPointDatabase;

import timber.log.Timber;


public class VisualisationActivity extends AppCompatActivity {

    private VisualsAdapter adapter;
    private RadioPointDatabase.RadioPointsUpdateListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_visualisation);

        RecyclerView visualsListView = (RecyclerView) findViewById(R.id.visuals_list_view);
        visualsListView.setLayoutManager(new LinearLayoutManager(visualsListView.getContext()));
        adapter = new VisualsAdapter(position -> Timber.i("clicked on item " + position));
        visualsListView.setAdapter(adapter);

        listener = newDataset -> adapter.setData(newDataset);
        MainApplication.getDatabase().registerGroupedRadioPointsListener(listener);
    }

    @Override
    protected void onDestroy() {
        MainApplication.getDatabase().unregisterGroupedRadioPointsListener(listener);
        super.onDestroy();
    }
}
