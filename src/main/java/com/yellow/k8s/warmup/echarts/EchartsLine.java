package com.yellow.k8s.warmup.echarts;

import com.github.abel533.echarts.series.Line;

/**
 * @author YellowTail
 * @since 2020-09-30
 */
public class EchartsLine extends Line {

    private boolean connectNulls;       //是否把 null 给链接起来

    public final boolean isConnectNulls() {
        return connectNulls;
    }

    public final void setConnectNulls(final boolean connectNulls) {
        this.connectNulls = connectNulls;
    }

    @Override
    public String toString() {
        return "EchartsLine{" +
                "connectNulls=" + connectNulls +
                "} " + super.toString();
    }
}
