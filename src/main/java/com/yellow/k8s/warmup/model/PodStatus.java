package com.yellow.k8s.warmup.model;

import java.util.List;

/**
 * https://kubernetes.cn/docs/reference/generated/kubernetes-api/v1.19/#podstatus-v1-core
 * @author YellowTail
 * @since 2020-09-23
 */
public class PodStatus {

    private String phase;
    private String hostIP;
    private String podIP;
    private String startTime;
    private String reason;

    private List<PodCondition> conditions;

    private List<ContainerStatus> containerStatuses;

    public final String getPhase() {
        return phase;
    }

    public final void setPhase(final String phase) {
        this.phase = phase;
    }

    public final String getHostIP() {
        return hostIP;
    }

    public final void setHostIP(final String hostIP) {
        this.hostIP = hostIP;
    }

    public final String getPodIP() {
        return podIP;
    }

    public final void setPodIP(final String podIP) {
        this.podIP = podIP;
    }

    public final String getStartTime() {
        return startTime;
    }

    public final void setStartTime(final String startTime) {
        this.startTime = startTime;
    }

    public final String getReason() {
        return reason;
    }

    public final void setReason(final String reason) {
        this.reason = reason;
    }

    public final List<PodCondition> getConditions() {
        return conditions;
    }

    public final void setConditions(final List<PodCondition> conditions) {
        this.conditions = conditions;
    }

    public final List<ContainerStatus> getContainerStatuses() {
        return containerStatuses;
    }

    public final void setContainerStatuses(final List<ContainerStatus> containerStatuses) {
        this.containerStatuses = containerStatuses;
    }
}
