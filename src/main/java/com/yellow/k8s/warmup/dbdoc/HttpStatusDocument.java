package com.yellow.k8s.warmup.dbdoc;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * @author YellowTail
 * @since 2020-09-22
 */
@Document(collection = "http-status")
public class HttpStatusDocument {

    @Id
    private String _id;

    @Indexed
    private String requestId;                   // 请求的 id

    private ObjectId requestObjectId;           // 冗余字段，便于联表

    private int cost;                           // 响应时间

    private Date createTime;                    // 创建时间


    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public ObjectId getRequestObjectId() {
        return requestObjectId;
    }

    public void setRequestObjectId(ObjectId requestObjectId) {
        this.requestObjectId = requestObjectId;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "HttpStatusDocument{" +
                "_id='" + _id + '\'' +
                ", requestId='" + requestId + '\'' +
                ", cost=" + cost +
                ", createTime=" + createTime +
                '}';
    }
}


