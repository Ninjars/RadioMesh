package com.ninjarific.radiomesh.ui.clusters;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.ninjarific.radiomesh.MainApplication;
import com.ninjarific.radiomesh.R;
import com.ninjarific.radiomesh.database.room.DatabaseHelper;
import com.ninjarific.radiomesh.database.room.entities.Connection;
import com.ninjarific.radiomesh.database.room.entities.Node;
import com.ninjarific.radiomesh.utils.listutils.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class ForceDirectedActivity extends AppCompatActivity {

    public static final String BUNDLE_GRAPH_ID = "graphId";

    private ForceDirectedView view;
    private long graphIndex;
    private Random random;
    private Disposable disposable;
    private View loadingSpinner;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clusters);
        view = findViewById(R.id.clusters_view);
        loadingSpinner = findViewById(R.id.loading_spinner);
        random = new Random(0);
        graphIndex = getIntent().getExtras().getLong(BUNDLE_GRAPH_ID, -1);
    }

    @Override
    protected void onStart() {
        super.onStart();
        DatabaseHelper dbHelper = MainApplication.getDatabaseHelper();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        if (graphIndex < 0) {
            loadingSpinner.setVisibility(View.GONE);
            view.setData(DebugDataProvider.getDebugData((int) graphIndex));

        } else {
            disposable = dbHelper.getNodesForGraphObs(graphIndex)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(Schedulers.computation())
                    .map(dataset -> {
                        List<Long> nodeIds = ListUtils.map(dataset, Node::getId);
                        List<ForceConnectedNode> connectedNodes = new ArrayList<>();
                        for (int i = 0; i < dataset.size(); i++) {
                            Node node = dataset.get(i);
                            List<Long> neighbourNodeIds = getConnectedNodes(dbHelper, node.getId());
                            List<Integer> neighbourIndexes = ListUtils.map(neighbourNodeIds, nodeIds::indexOf);
                            ForceConnectedNode connectedNode = new ForceConnectedNode(i, neighbourIndexes, random.nextFloat() * 100, random.nextFloat() * 100);
                            connectedNodes.add(connectedNode);
                        }
                        return connectedNodes;
                    })
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext(nodes -> loadingSpinner.setVisibility(View.GONE))
                    .subscribe(connectedNodes -> view.setData(connectedNodes),
                            Throwable::printStackTrace);
        }
    }

    private static List<Long> getConnectedNodes(DatabaseHelper dbHelper, long nodeId) {
        // TODO: avoid iterative database lookups with a single query to get connected nodes directly via Connection object
        List<Connection> connections = dbHelper.getConnectedNodes(nodeId);
        return ListUtils.map(connections, connection -> dbHelper.getNode(connection.getToNodeId()).getId());
    }

    @Override
    protected void onStop() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        super.onStop();
    }
}
