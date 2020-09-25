package com.yellow.k8s.warmup.model;

/**
 * @author YellowTail
 * @since 2020-09-23
 */
public class PodEvent {

    private String type;                //状态, ADDED/MODIFIED/DELETED

    private PodInfo object;             // pod 状态

    public final String getType() {
        return type;
    }

    public final void setType(final String type) {
        this.type = type;
    }

    public final PodInfo getObject() {
        return object;
    }

    public final void setObject(final PodInfo object) {
        this.object = object;
    }

    @Override
    public String toString() {
        return "PodEvent{" +
                "type='" + type + '\'' +
                ", object=" + object +
                '}';
    }
}
