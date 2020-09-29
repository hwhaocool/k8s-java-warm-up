package com.yellow.k8s.warmup.vo;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author YellowTail
 * @since 2020-06-12
 */
public class WarmUpBatchRequest extends BaseRequest {

    private List<String> uriList;               // uri， 其它查询参数，需自行组装好

    public WarmUpBatchRequest() {
        super();
    }

    public final List<String> getUriList() {
        return uriList;
    }

    public final void setUriList(final List<String> uriList) {
        this.uriList = uriList;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WarmUpBatchRequest that = (WarmUpBatchRequest) o;
        return port == that.port &&
                Objects.equals(podName, that.podName) &&
                Objects.equals(ip, that.ip) &&
                Objects.equals(uriList, that.uriList) &&
                Objects.equals(method, that.method) &&
                Objects.equals(headers, that.headers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(podName, ip, port, uriList, method, headers);
    }

    @Override
    public String toString() {
        return "WarmUpBatchRequest{" +
                "podName='" + podName + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", uriList=" + uriList +
                ", method='" + method + '\'' +
                ", headers=" + headers +
                '}';
    }
}
