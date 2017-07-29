package com.ninjarific.radiomesh.ui.clusters;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.ninjarific.radiomesh.MainApplication;
import com.ninjarific.radiomesh.R;
import com.ninjarific.radiomesh.database.RadioPointDatabase;


public class ForceDirectedActivity extends AppCompatActivity {

    public static final String BUNDLE_INDEX = "positioned_data_index";

    private RadioPointDatabase.RadioPointsUpdateListener listener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clusters);
        ForceDirectedView view = (ForceDirectedView) findViewById(R.id.clusters_view);
        final int index = getIntent().getExtras().getInt(BUNDLE_INDEX);
        listener = newDataset -> view.setData(newDataset.get(index));
    }

    @Override
    protected void onStart() {
        super.onStart();
        MainApplication.getDatabase().registerGroupedRadioPointsListener(listener);
    }

    @Override
    protected void onStop() {
        MainApplication.getDatabase().unregisterGroupedRadioPointsListener(listener);
        super.onStop();
    }
}
