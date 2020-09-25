package com.yellow.k8s.warmup.model;

/**
 * https://kubernetes.cn/docs/reference/generated/kubernetes-api/v1.19/#pod-v1-core
 * @author YellowTail
 * @since 2020-09-23
 */
public class PodInfo {

    private PodStatus status;

    public final PodStatus getStatus() {
        return status;
    }

    public final void setStatus(final PodStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "PodInfo{" +
                "status=" + status +
                '}';
    }
}
