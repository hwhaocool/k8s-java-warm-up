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

    private int port;                           //端口
    private String method;                      //方法
    private String uri;                         //uri， 其它查询参数，需自行组装好

    private String headers;                      // headers

    private List<String> paramList;             //参数值列表，比如 xxId列表

    private String queryParamName;              //查询参数名称，比如为 id，那么 最后的参数为 uri&id={paramList[i]}

    private Date createTime;                    // 创建时间

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

    public List<String> getParamList() {
        return paramList;
    }

    public void setParamList(List<String> paramList) {
        this.paramList = paramList;
    }

    public String getQueryParamName() {
        return queryParamName;
    }

    public void setQueryParamName(String queryParamName) {
        this.queryParamName = queryParamName;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "RequestDocument{" +
                "_id='" + _id + '\'' +
                ", podName='" + podName + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", method='" + method + '\'' +
                ", uri='" + uri + '\'' +
                ", headers='" + headers + '\'' +
                ", paramList=" + paramList +
                ", queryParamName='" + queryParamName + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
