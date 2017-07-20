package com.ninjarific.radiomesh.ui.visualisation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.ninjarific.radiomesh.MainApplication;
import com.ninjarific.radiomesh.R;
import com.ninjarific.radiomesh.database.RadioPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import io.realm.RealmResults;


public class VisualisationActivity extends AppCompatActivity {

    private RealmResults<RadioPoint> radioPoints;
    private VisualsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_visualisation);

        radioPoints = MainApplication.getDatabase().getRadioPoints();
        radioPoints.addChangeListener((points, changeSet) -> {});

        RecyclerView visualsListView = (RecyclerView) findViewById(R.id.visuals_list_view);
        visualsListView.setLayoutManager(new LinearLayoutManager(visualsListView.getContext()));
        adapter = new VisualsAdapter();
        adapter.setData(groupData(radioPoints));
        visualsListView.setAdapter(adapter);

        radioPoints.addChangeListener((radioPoints1, changeSet) -> adapter.setData(groupData(radioPoints1)));
    }

    private List<List<RadioPoint>> groupData(List<RadioPoint> dataset) {
        List<RadioPoint> parsedPoints = new ArrayList<>(dataset.size());
        List<List<RadioPoint>> datapoints = new ArrayList<>();

        for (RadioPoint point : dataset) {
            if (parsedPoints.contains(point)) continue;
            HashSet<RadioPoint> currentSet = new HashSet<>(dataset.size()/4);
            addConnectedNodesRecursive(point, currentSet);
            parsedPoints.addAll(currentSet);
            datapoints.add(new ArrayList<>(currentSet));
        }
        Collections.sort(datapoints, (lhs, rhs) -> Integer.compare(lhs.size(), rhs.size()));
        return datapoints;
    }

    private void addConnectedNodesRecursive(RadioPoint point, HashSet<RadioPoint> currentSet) {
        currentSet.add(point);
        for (RadioPoint connected : point.getConnectedPoints()) {
            if (!currentSet.contains(connected)) {
                addConnectedNodesRecursive(connected, currentSet);
            }
        }
    }

    @Override
    protected void onDestroy() {
        radioPoints.removeAllChangeListeners();
        super.onDestroy();
    }
}
