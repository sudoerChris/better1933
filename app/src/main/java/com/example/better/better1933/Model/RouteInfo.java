package com.example.better.better1933.Model;

/**
 * Created by Chris on 3/30/2018.
 */
public class RouteInfo {
    public final String route,  serviceType;
    public String bound, serviceTypeDesc = "";
    public final int boundSeq;

    public RouteInfo(String route, int boundSeq, String serviceType) {
        this.route = route;
        this.boundSeq = boundSeq;
        this.serviceType = serviceType;
    }

}
