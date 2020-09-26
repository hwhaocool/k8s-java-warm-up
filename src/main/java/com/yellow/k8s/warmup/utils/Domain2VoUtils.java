package com.yellow.k8s.warmup.utils;

import com.github.abel533.echarts.Option;
import com.github.abel533.echarts.axis.CategoryAxis;
import com.github.abel533.echarts.axis.ValueAxis;
import com.github.abel533.echarts.code.Trigger;
import com.github.abel533.echarts.feature.Feature;
import com.github.abel533.echarts.series.Line;
import com.github.abel533.echarts.series.Series;
import com.yellow.k8s.warmup.dbdoc.HttpStatusDocument;
import com.yellow.k8s.warmup.dbdoc.RequestDocument;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Domain2VoUtils {
    
    private static Logger LOGGER = LoggerFactory.getLogger(Domain2VoUtils.class);

    //折线图堆叠
    public static Mono<Option> onePodTopStack(Mono<RequestDocument> documentMono, Flux<HttpStatusDocument> docFlux) {

        Mono<List<HttpStatusDocument>> listMono = docFlux.collectList();

        Mono<RequestModel> requestModelMono = documentMono.zipWith(listMono, (r, l) -> {
            RequestModel requestModel = new RequestModel();
            requestModel.setDocumentList(l);
            requestModel.setRequestDocument(r);
            return requestModel;
        });

        return requestModelMono.map(Domain2VoUtils::stack);
    }
    
    @SuppressWarnings("unchecked")
    private static Option stack(RequestModel requestModel) {

        List<HttpStatusDocument> list = requestModel.getDocumentList();
        RequestDocument requestDocument = requestModel.getRequestDocument();

        LOGGER.info("Domain2VoUtils stack list size {}", list.size());
        
        Option option = genEmptyOption();
        
        option.title("Http-Cost");
        
        //x 横轴：时间
        List<String> dateStrList = getSortedDateList(list).stream()
            .map(Domain2VoUtils::date2String)
            .collect(Collectors.toList());
        
        CategoryAxis axis = new CategoryAxis();
        axis.data().addAll(dateStrList);
        option.xAxis(axis);
        
        // y 轴 名称列表
        String podName = requestDocument.getPodName();
        List<String> podNameList = List.of(podName);
        
        option.legend().data(podNameList);
        
        option.series(docList2SeriesList(podName, list));
        
        return option;
    }
    
    
    @SuppressWarnings("rawtypes")
    private static List<Series> docList2SeriesList(String podName, List<HttpStatusDocument> list) {
        //x 轴
        List<Date> dateList = getSortedDateList(list);

        // y 轴
        List<Series> collect = List.of(doc2Line(podName, list));
        
        return collect;
    }
    
    /**
     * <br> 每个 实际的 pod 记录列表 转成 line
     *
     * @param podName 名称
     * @param list  pod 记录列表
     * @return
     * @author YellowTail
     * @since 2019-11-19
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Series doc2Line(String podName, List<HttpStatusDocument> list) {
        Line line = new Line();
        
        line.name(podName);

        //生成固定长度的 data 数组,
        List<Integer> dataList = list.stream()
                .map(HttpStatusDocument::getCost)
                .collect(Collectors.toList());

        line.data().addAll(dataList);
        
        line.smooth(true);
        
        return line;
    }
    
    /**
     * <br>得到 按照时间排序 后的 流
     *
     * @param list
     * @return
     * @author YellowTail
     * @since 2019-11-19
     */
    private static Stream<HttpStatusDocument> getSortedStream(List<HttpStatusDocument> list) {
        // 排序，按照时间 从小到大
        
        return list.stream()
                .sorted(Comparator.comparing(HttpStatusDocument::getCreateTime));
    }
    
    /**
     * <br>得到 按照时间排序后的 时间列表
     *
     * @param dbList
     * @return
     * @author YellowTail
     * @since 2019-11-19
     */
    private static List<Date> getSortedDateList(List<HttpStatusDocument> dbList) {
        return getSortedStream(dbList)
        .map(HttpStatusDocument::getCreateTime)
        .distinct()                            //去重
        .collect(Collectors.toList());
    }
    
    /**
     * <br>date 转成 string
     *
     * @param date
     * @return
     * @author YellowTail
     * @since 2019-11-19
     */
    private static String date2String(Date date) {
        DateTime dateTime = new DateTime(date);
        
        return dateTime.toString("MM-dd HH:mm:ss");
    }
    
    /**
     * <br>生成一个空的 option
     *
     * @return
     * @author YellowTail
     * @since 2019-11-19
     */
    private static Option genEmptyOption() {
        Option option = new Option();
        
        option.title("");
      
        option.tooltip().trigger(Trigger.axis);
      
        option.legend();
      
        option.grid()
          .left("3%")
          .right("1%")
          .bottom("3%")
          .containLabel(true);
      
        option.toolbox()
          .feature()
          .put("saveAsImage", new Feature());
        
        //y 纵轴：固定为 value
        option.yAxis(new ValueAxis());
      
        return option;
    }

    private static class RequestModel {

        private RequestDocument requestDocument;

        private List<HttpStatusDocument> documentList;

        public RequestDocument getRequestDocument() {
            return requestDocument;
        }

        public void setRequestDocument(RequestDocument requestDocument) {
            this.requestDocument = requestDocument;
        }

        public List<HttpStatusDocument> getDocumentList() {
            return documentList;
        }

        public void setDocumentList(List<HttpStatusDocument> documentList) {
            this.documentList = documentList;
        }
    }

}
