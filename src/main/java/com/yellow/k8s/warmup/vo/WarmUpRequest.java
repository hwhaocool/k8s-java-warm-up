package com.yellow.k8s.warmup.vo;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author YellowTail
 * @since 2020-06-12
 */
public class WarmUpRequest extends BaseRequest {

    private String uri;                         // uri， 其它查询参数，需自行组装好

    private List<String> paramList;             // 参数值列表，比如 xxId列表

    private String queryParamName;              // 查询参数名称，比如为 id，那么 最后的参数为 uri&id={paramList[i]}

    public WarmUpRequest() {
        super();
    }

    public final String getUri() {
        return uri;
    }

    public final void setUri(final String uri) {
        this.uri = uri;
    }

    public final List<String> getParamList() {
        return paramList;
    }

    public final void setParamList(final List<String> paramList) {
        this.paramList = paramList;
    }

    public final String getQueryParamName() {
        return queryParamName;
    }

    public final void setQueryParamName(final String queryParamName) {
        this.queryParamName = queryParamName;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WarmUpRequest that = (WarmUpRequest) o;
        return port == that.port &&
                Objects.equals(podName, that.podName) &&
                Objects.equals(ip, that.ip) &&
                Objects.equals(uri, that.uri) &&
                Objects.equals(method, that.method) &&
                Objects.equals(headers, that.headers) &&
                Objects.equals(paramList, that.paramList) &&
                Objects.equals(queryParamName, that.queryParamName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(podName, ip, port, uri, method, headers, paramList, queryParamName);
    }

    @Override
    public String toString() {
        return "WarmUpRequest{" +
                "podName='" + podName + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", uri='" + uri + '\'' +
                ", method='" + method + '\'' +
                ", headers=" + headers +
                ", paramList=" + paramList +
                ", queryParamName='" + queryParamName + '\'' +
                '}';
    }
}
