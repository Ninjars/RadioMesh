package com.ninjarific.radiomesh.ui.visualisation;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.ninjarific.radiomesh.MainApplication;
import com.ninjarific.radiomesh.R;
import com.ninjarific.radiomesh.database.realm.RadioPointDatabase;
import com.ninjarific.radiomesh.ui.clusters.ForceDirectedActivity;


public class VisualisationActivity extends AppCompatActivity {

    private VisualsAdapter adapter;
    private RadioPointDatabase.RadioPointsUpdateListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_visualisation);

        RecyclerView visualsListView = (RecyclerView) findViewById(R.id.visuals_list_view);
        visualsListView.setLayoutManager(new LinearLayoutManager(visualsListView.getContext()));
        adapter = new VisualsAdapter(position -> {
            Intent intent = new Intent(this, ForceDirectedActivity.class);
            Bundle extras = new Bundle();
            extras.putInt(ForceDirectedActivity.BUNDLE_INDEX, position);
            intent.putExtras(extras);
            this.startActivity(intent);
        });
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
