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
import com.yellow.k8s.warmup.echarts.EchartsLine;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
        
        String podName = requestDocument.getPodName();

        Option option = genEmptyOption();
        option.title(String.format("%s %s", podName, "Http-Cost"));
        
        //x 横轴：时间
        List<String> dateStrList = getSortedDateList(list).stream()
            .map(Domain2VoUtils::date2String)
            .distinct()
            .collect(Collectors.toList());
        
        CategoryAxis axis = new CategoryAxis();
        axis.data().addAll(dateStrList);
        option.xAxis(axis);
        
        // y 轴 名称列表
        List<String> collect = list.stream()
                .map(HttpStatusDocument::getDesc)
                .distinct()
                .collect(Collectors.toList());

        option.legend().data(collect);

        List<Date> dateList = getSortedDateList(list);

        option.series(docList2SeriesList(podName, list, dateList));
        
        return option;
    }
    
    
    @SuppressWarnings("rawtypes")
    private static List<Series> docList2SeriesList(String podName, List<HttpStatusDocument> list, List<Date> dateList) {

        // y 轴

        // 根据 desc 分组
        // 使用LinkedHashMap ， 维持顺序
        LinkedHashMap<String, List<HttpStatusDocument>> map = getSortedStream(list)
                .collect(Collectors.groupingBy(HttpStatusDocument::getDesc, LinkedHashMap::new, Collectors.toList()));

        List<Series> collect = map.entrySet()
                .stream()
                .map(k -> doc2Line(k.getKey(), k.getValue(), dateList))
                .collect(Collectors.toList());

        return collect;
    }
    
    /**
     * <br> 每个 实际的 pod 记录列表 转成 line
     *
     * @param desc 名称
     * @param list  pod 记录列表
     * @param dateList
     * @return
     * @author YellowTail
     * @since 2019-11-19
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Series doc2Line(String desc, List<HttpStatusDocument> list, final List<Date> dateList) {
        EchartsLine line = new EchartsLine();
        
        line.name(desc);

        //生成固定长度的 data 数组
        // 按照 x 轴 dateList 来生成数据， 没有的话，就设置为 null

        //生成固定长度的 data 数组, 因为有些 时间点是没有数据的，部分数据是缺失的
        List<Integer> dataList = new ArrayList<>(dateList.size());

        //构造一个map
        Map<Date, Integer> dateValueMap = list.stream()
                .collect(Collectors.toMap(HttpStatusDocument::getCreateTime, HttpStatusDocument::getCost));

        //遍历x 轴，按照x轴的时间逐个添加 data， 不存在的时候，添加 null
        dateList.forEach(k -> dataList.add(dateValueMap.getOrDefault(k, null)));

        line.data().addAll(dataList);
        
        line.smooth(true);

        line.setConnectNulls(true);
        
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
        
        return dateTime.toString("MM-dd HH:mm:ss:SSS");
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
