package com.ninjarific.radiomesh.ui.clusters;

public class ForceConnection {

    final int from;
    final int to;

    public ForceConnection(int from, int to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ForceConnection
                && (((ForceConnection) obj).from == from || ((ForceConnection) obj).from == to)
                && (((ForceConnection) obj).to == from || ((ForceConnection) obj).to == to);
    }

    @Override
    public int hashCode() {
        return 17 * (to + from);
    }
}
