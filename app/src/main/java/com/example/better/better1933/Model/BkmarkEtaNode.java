package com.example.better.better1933.Model;

import com.example.better.better1933.Infrastructure.EtaJsonReader;

public class BkmarkEtaNode extends DBStopInfoNode {
    public boolean isShowMore = false;
    public EtaJsonReader etaJsonReader = null;
    public String[] time = new String[]{"--"},rawTime = new String[]{"--:--"};
    public BkmarkEtaNode(String route, int bound_seq, int stop_seq, String serviceType) {
        super(route, bound_seq, stop_seq, serviceType);
    }
    public BkmarkEtaNode(DBStopInfoNode node) {
        this(node.route, node.bound_seq, node.stop_seq, node.serviceType);
        this.stop = node.stop;
        this.bound = node.bound;
        this.fare =  node.fare;
    }
    public void toggleShowMore() {
        this.isShowMore = !this.isShowMore;
    }


}
