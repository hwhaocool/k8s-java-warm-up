package com.yellow.k8s.warmup.model;

/**
 * @author YellowTail
 * @since 2020-10-07
 */
public class WarmUpInfo {

    private boolean ready;

    private long startTime;

    private boolean del;

    public WarmUpInfo() {
        ready = false;
    }

    public WarmUpInfo(final long startTime) {
        ready = false;
        this.startTime = startTime;
    }

    public WarmUpInfo(final boolean ready, final long startTime) {
        this.ready = ready;
        this.startTime = startTime;
    }

    public final boolean isReady() {
        return ready;
    }

    public final void setReady(final boolean ready) {
        this.ready = ready;
    }

    public final long getStartTime() {
        return startTime;
    }

    public final void setStartTime(final long startTime) {
        this.startTime = startTime;
    }

    public final boolean isDel() {
        return del;
    }

    public final void setDel(final boolean del) {
        this.del = del;
    }

    @Override
    public String toString() {
        return "WarmUpInfo{" +
                "ready=" + ready +
                ", startTime=" + startTime +
                ", del=" + del +
                '}';
    }
}
