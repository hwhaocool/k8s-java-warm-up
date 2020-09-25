package com.yellow.k8s.warmup.model;

/**
 * https://kubernetes.cn/docs/reference/generated/kubernetes-api/v1.19/#containerstatus-v1-core
 * @author YellowTail
 * @since 2020-09-23
 */
public class ContainerStatus {

    private String name;                                // pod name， 不带随机数

    private boolean ready;                              // 当前容器是否通过了 就绪检查

    public final String getName() {
        return name;
    }

    public final void setName(final String name) {
        this.name = name;
    }

    public final boolean isReady() {
        return ready;
    }

    public final void setReady(final boolean ready) {
        this.ready = ready;
    }

    @Override
    public String toString() {
        return "ContainerStatus{" +
                "name='" + name + '\'' +
                ", ready=" + ready +
                '}';
    }
}
