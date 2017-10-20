package com.ninjarific.radiomesh.ui.clusters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import timber.log.Timber;

public class DebugDataProvider {

    public static final int SIMPLE_PAIR = -1;
    public static final int REGUALAR_GRID = -2;
    public static final int INTER_CONNECTED = -3;

    private static final int GRID_COUNT = 5;
    private static final int GRID_SPACING = 10;

    public static List<ForceConnectedNode> getDebugData(int id) {
        switch (id) {
            case SIMPLE_PAIR:
                return getSimplePair();
            case REGUALAR_GRID:
                return getRegularGrid();
            case INTER_CONNECTED:
                return getInterconnected();
            default:
                Timber.e("unknown id " + id);
                return Collections.emptyList();
        }
    }

    private static List<ForceConnectedNode> getSimplePair() {
        List<ForceConnectedNode> nodes = new ArrayList<>();
        nodes.add(new ForceConnectedNode(0, Collections.singletonList(1), 0, 5));
        nodes.add(new ForceConnectedNode(1, Collections.singletonList(0), 5, 0));
        return nodes;
    }

    private static List<ForceConnectedNode> getInterconnected() {
        List<ForceConnectedNode> nodes = new ArrayList<>(10);
        List<Integer> indexes = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        Random random = new Random(0);
        for (int index : indexes) {
            nodes.add(new ForceConnectedNode(index, indexes, random.nextFloat() * 10, random.nextFloat() * 10));
        }
        return nodes;
    }

    private static List<ForceConnectedNode> getRegularGrid() {
        List<ForceConnectedNode> nodes = new ArrayList<>();
        for (int y = 0; y < GRID_COUNT; y++) {
            for (int x = 0; x < GRID_COUNT; x++) {
                int index = nodes.size();
                nodes.add(new ForceConnectedNode(index, getGridNeighbours(index, x, y),
                        x * GRID_SPACING, y * GRID_SPACING));
            }
        }
        return nodes;
    }

    private static List<Integer> getGridNeighbours(int index, int x, int y) {
        List<Integer> neighbourIndex = new ArrayList<>();
        if (x > 0) neighbourIndex.add(index - 1);
        if (x < GRID_COUNT-1) neighbourIndex.add(index + 1);
        if (y > 0) neighbourIndex.add(index - GRID_COUNT);
        if (y < GRID_COUNT - 1) neighbourIndex.add(index + GRID_COUNT);
        return neighbourIndex;
    }
}
