package com.example.better.better1933.Model;

public class AlarmNode extends DBStopInfoNode {
  private final int minBefore;
  public AlarmNode(String route, int bound_seq, int stop_seq, String serviceType, int minBefore) {
    super(route,bound_seq,stop_seq,serviceType);
    this.minBefore = minBefore;
  }
}
