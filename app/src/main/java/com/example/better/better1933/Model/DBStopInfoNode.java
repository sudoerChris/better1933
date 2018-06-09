package com.example.better.better1933.Model;


/**
 * Created by Chris on 3/30/2018.
 */
public class DBStopInfoNode {
    public final String route, serviceType ;
    public String  stop = "", bound = "",serviceTypeDesc, fare = "0.0";
    public final int bound_seq, stop_seq;
    public int alarmMin = -1, index=-1;
    public boolean notify = false;

    public DBStopInfoNode(String route, int bound_seq, int stop_seq, String serviceType) {
        this.route = route;
        this.bound_seq = bound_seq;
        this.stop_seq = stop_seq;
        this.serviceType = serviceType;
    }

    @Override
    public String toString() {
        return route+","+Integer.toString(bound_seq)+","+Integer.toString(stop_seq)+","+bound+","+stop;
    }
}
