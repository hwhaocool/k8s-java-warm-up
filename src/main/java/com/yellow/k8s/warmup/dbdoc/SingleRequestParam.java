package com.yellow.k8s.warmup.dbdoc;

import java.util.List;

/**
 * @author YellowTail
 * @since 2020-09-29
 */
public class SingleRequestParam {

    private List<String> paramList;             //参数值列表，比如 xxId列表, type=single

    private String queryParamName;              //查询参数名称，比如为 id，那么 最后的参数为 uri&id={paramList[i]} , type=single

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
    public String toString() {
        return "SingleRequestParam{" +
                "paramList=" + paramList +
                ", queryParamName='" + queryParamName + '\'' +
                '}';
    }
}
