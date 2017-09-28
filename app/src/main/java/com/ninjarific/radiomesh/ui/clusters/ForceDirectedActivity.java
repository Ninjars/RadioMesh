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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;


public class ForceDirectedActivity extends AppCompatActivity {

    public static final String BUNDLE_GRAPH_ID = "graphId";

    private ForceDirectedView view;
    private long graphIndex;
    private Random random;
    private Disposable disposable;
    private View loadingSpinner;
    private DatabaseHelper dbHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clusters);
        view = findViewById(R.id.clusters_view);
        loadingSpinner = findViewById(R.id.loading_spinner);
        random = new Random(0);
        graphIndex = getIntent().getExtras().getLong(BUNDLE_GRAPH_ID, 1);
    }

    @Override
    protected void onStart() {
        super.onStart();
        dbHelper = MainApplication.getDatabaseHelper();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        disposable = dbHelper.getNodesForGraphObs(graphIndex)
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.computation())
                .map(this::groupNodes)
                .map(dataset -> {
                    List<List<Long>> nodeIds = ListUtils.map(dataset, CompoundNode::getContainedNodes);
                    List<ForceConnectedNode> connectedNodes = new ArrayList<>();
                    for (int i = 0; i < dataset.size(); i++) {
                        CompoundNode node = dataset.get(i);
                        List<Long> neighbourNodeIds = node.getNeighbourIds();
                        List<Integer> neighbourIndexes = ListUtils.map(neighbourNodeIds, targetId -> {
                            for (int j = 0; j < nodeIds.size(); j++) {
                                if (nodeIds.get(j).contains(targetId)) {
                                    return j;
                                }
                            }
                            return -1;
                        });
                        ForceConnectedNode connectedNode = new ForceConnectedNode(i, neighbourIndexes, node.getContainedNodes().size(), random.nextFloat() * 100, random.nextFloat() * 100);
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

    private List<CompoundNode> groupNodes(List<Node> nodeDataset) {
        ListUtils.foreach(nodeDataset, node -> node.setNeighbours(getConnectedNodes(dbHelper, node.getId())));
        List<Long> nodeIdsAddedToCompoundNode = new ArrayList<>(nodeDataset.size());
        List<Node> processed = new ArrayList<>(nodeDataset.size());
        List<CompoundNode> compoundNodes = new ArrayList<>(nodeDataset.size());
        // to reduce some object creation, reuse these collections in the loop
        final List<Long> compareList = new ArrayList<>();
        final Set<Long> compoundNeighboursSet = new HashSet<>();
        for (Node currentNode : nodeDataset) {
            if (nodeIdsAddedToCompoundNode.contains(currentNode.getId())) {
                continue;
            }
            final List<Long> neighbourIds = currentNode.getNeighbours();
            List<Node> neighbourNodes = ListUtils.filter(nodeDataset, node -> {
                return !nodeIdsAddedToCompoundNode.contains(node.getId()) && neighbourIds.contains(node.getId());
            });
            List<List<Long>> neighbourNeighbourIds = ListUtils.map(neighbourNodes, Node::getNeighbours);
            boolean isEngulfed = true;
            for (Node node : neighbourNodes) {
                compareList.clear();
                compareList.addAll(node.getNeighbours());
                compareList.remove(currentNode.getId());
                compareList.add(node.getId());
                if (!compareList.containsAll(neighbourIds)) {
                    isEngulfed = false;
                    break;
                }
            }
            if (isEngulfed) {
                // create compound node
                processed.removeAll(neighbourNodes);
                processed.remove(currentNode);

                compoundNeighboursSet.clear();
                ListUtils.foreach(neighbourNeighbourIds, compoundNeighboursSet::addAll);
                compoundNeighboursSet.removeAll(neighbourIds);
                compoundNeighboursSet.remove(currentNode.getId());

                neighbourIds.add(currentNode.getId());
                nodeIdsAddedToCompoundNode.addAll(neighbourIds);
                compoundNodes.add(CompoundNode.create(currentNode.getId(), neighbourIds, new ArrayList<>(compoundNeighboursSet)));
            } else {
                processed.add(currentNode);
            }
        }
        Timber.i("created group nodes: compound count " + compoundNodes.size() + " singles count " + processed.size());
        compoundNodes.addAll(ListUtils.map(processed, node -> CompoundNode.create(node.getId(), Collections.singletonList(node.getId()), node.getNeighbours())));
        return compoundNodes;
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
