package com.ninjarific.radiomesh.ui.visualisation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ninjarific.radiomesh.MainApplication;
import com.ninjarific.radiomesh.R;
import com.ninjarific.radiomesh.database.RadioPoint;

import io.realm.RealmResults;


public class VisualisationActivity extends AppCompatActivity {

    private RealmResults<RadioPoint> radioPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_visualisation);

        final CircularVisualisation visualisationView = (CircularVisualisation) findViewById(R.id.visualisation);

        radioPoints = MainApplication.getDatabase().getRadioPoints();
        radioPoints.addChangeListener((points, changeSet) -> visualisationView.setDataset(points));
        visualisationView.setDataset(radioPoints);
    }

    @Override
    protected void onDestroy() {
        radioPoints.removeAllChangeListeners();
        super.onDestroy();
    }
}
