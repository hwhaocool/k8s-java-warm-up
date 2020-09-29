package com.yellow.k8s.warmup.vo;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author YellowTail
 * @since 2020-06-12
 */
public abstract class BaseRequest {

    protected String podName;                     // podName, 非必填， 可以通过环境变量 HOSTNAME 切掉后面两个 - 得到

    protected String ip;                          // ip
    protected int port;                           // 端口， 不填为 8080
    protected String method;                      // 方法， 不填为 get

    protected Map<String, String> headers;        // headers


    public BaseRequest() {
        method = "GET";
        port = 8080;
    }

    public final String getPodName() {
        return podName;
    }

    public final void setPodName(final String podName) {
        this.podName = podName;
    }

    public final String getIp() {
        return ip;
    }

    public final void setIp(final String ip) {
        this.ip = ip;
    }

    public final int getPort() {
        return port;
    }

    public final void setPort(final int port) {
        this.port = port;
    }

    public final String getMethod() {
        return method;
    }

    public final void setMethod(final String method) {
        this.method = method;
    }

    public final Map<String, String> getHeaders() {
        return headers;
    }

    public final void setHeaders(final Map<String, String> headers) {
        this.headers = headers;
    }
}
