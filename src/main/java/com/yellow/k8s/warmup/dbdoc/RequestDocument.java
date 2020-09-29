package com.yellow.k8s.warmup.dbdoc;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

/**
 * @author YellowTail
 * @since 2020-09-22
 */
@Document(collection = "request")
public class RequestDocument {

    @Id
    private String _id;

    private String podName;                        // pod 的 name

    private String ip;

    private int port;                                //端口
    private String method;                              //方法

    private String type;                                // single/multi

    private String uri;                                 // uri, type=single
    private List<String> uriList;                        // uri列表， type=multi

    private String headers;                              // headers

    private SingleRequestParam singleRequestParam;       // 参数， type=single

    private Date createTime;                              // 创建时间

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getPodName() {
        return podName;
    }

    public void setPodName(String podName) {
        this.podName = podName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public final SingleRequestParam getSingleRequestParam() {
        return singleRequestParam;
    }

    public final void setSingleRequestParam(final SingleRequestParam singleRequestParam) {
        this.singleRequestParam = singleRequestParam;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public final String getType() {
        return type;
    }

    public final void setType(final String type) {
        this.type = type;
    }

    public final List<String> getUriList() {
        return uriList;
    }

    public final void setUriList(final List<String> uriList) {
        this.uriList = uriList;
    }

    @Override
    public String toString() {
        return "RequestDocument{" +
                "_id='" + _id + '\'' +
                ", podName='" + podName + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", method='" + method + '\'' +
                ", type='" + type + '\'' +
                ", uri='" + uri + '\'' +
                ", uriList=" + uriList +
                ", headers='" + headers + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
