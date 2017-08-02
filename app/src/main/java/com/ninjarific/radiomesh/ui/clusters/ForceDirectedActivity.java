package com.ninjarific.radiomesh.ui.clusters;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.ninjarific.radiomesh.MainApplication;
import com.ninjarific.radiomesh.R;
import com.ninjarific.radiomesh.database.RadioPoint;
import com.ninjarific.radiomesh.database.RadioPointDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class ForceDirectedActivity extends AppCompatActivity {

    public static final String BUNDLE_INDEX = "positioned_data_index";

    private RadioPointDatabase.RadioPointsUpdateListener listener;
    private ForceDirectedView view;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clusters);
        view = (ForceDirectedView) findViewById(R.id.clusters_view);
        final int index = getIntent().getExtras().getInt(BUNDLE_INDEX);
        Random random = new Random(0);
        listener = newDataset -> {
            List<RadioPoint> dataset = newDataset.get(index);
            List<ForceConnectedNode> connectedNodes = new ArrayList<>();
            for (RadioPoint point : dataset) {
                List<Integer> neighbours = new ArrayList<>();
                for (RadioPoint neighbour : point.getConnectedPoints()) {
                    neighbours.add(dataset.indexOf(neighbour));
                }
                ForceConnectedNode node = new ForceConnectedNode(neighbours, random.nextFloat(), random.nextFloat());
                connectedNodes.add(node);
            }
            view.setData(connectedNodes);
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        view.resume();
    }

    @Override
    protected void onPause() {
        view.pause();
        super.onPause();
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
