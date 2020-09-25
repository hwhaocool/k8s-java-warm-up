package com.yellow.k8s.warmup.model;

/**
 * https://kubernetes.cn/docs/reference/generated/kubernetes-api/v1.19/#podcondition-v1-core
 * @author YellowTail
 * @since 2020-09-23
 */
public class PodCondition {

    private String type;                            // https://kubernetes.cn/docs/reference/generated/kubernetes-api/v1.19/#podcondition-v1-core
    private String status;                          // Can be True, False, Unknown
    private String reason;                          // 上一次状态转变的原因， 精简、一个单词
    private String message;                         // 对人类友好的可阅读的具体状态转变信息
    private String lastTransitionTime;              // 上一次状态转变的时间
    private String lastProbeTime;                   //

    public final String getType() {
        return type;
    }

    public final void setType(final String type) {
        this.type = type;
    }

    public final String getStatus() {
        return status;
    }

    public final void setStatus(final String status) {
        this.status = status;
    }

    public final String getReason() {
        return reason;
    }

    public final void setReason(final String reason) {
        this.reason = reason;
    }

    public final String getMessage() {
        return message;
    }

    public final void setMessage(final String message) {
        this.message = message;
    }

    public final String getLastTransitionTime() {
        return lastTransitionTime;
    }

    public final void setLastTransitionTime(final String lastTransitionTime) {
        this.lastTransitionTime = lastTransitionTime;
    }

    public final String getLastProbeTime() {
        return lastProbeTime;
    }

    public final void setLastProbeTime(final String lastProbeTime) {
        this.lastProbeTime = lastProbeTime;
    }

    @Override
    public String toString() {
        return "PodCondition{" +
                "type='" + type + '\'' +
                ", status='" + status + '\'' +
                ", reason='" + reason + '\'' +
                ", message='" + message + '\'' +
                ", lastTransitionTime='" + lastTransitionTime + '\'' +
                ", lastProbeTime='" + lastProbeTime + '\'' +
                '}';
    }


}
