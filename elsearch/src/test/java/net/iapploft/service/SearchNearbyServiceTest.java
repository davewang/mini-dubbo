package net.iapploft.service;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.net.UnknownHostException;


/**
 * Created by dave on 2018/9/19.
 */
public class SearchNearbyServiceTest {



    double myLat = 39.1551326837;
    double myLog = 116.69367143983;
    int radius = 50;
    SearchNearbyService searchNearbyService;
    @Before
    public void initData() throws UnknownHostException {

        int total = 100000;

        searchNearbyService = new SearchNearbyService();
        //lucene  document index analyzer

        System.out.println("连接成功");
        //create database
        searchNearbyService.reCreateIndex();
        System.out.println("创建Index成功");
        //insert into
        searchNearbyService.addDataToIndex(myLat,myLog,total);
        System.out.println("插入数据成功");

        //System.out.println(searchNearbyService.toString());

    }
    @Test
    public void testSearchNearby() throws UnknownHostException {

        SearchNearbyService searchNearbyService = new SearchNearbyService();
        int size = 100;
        System.out.println("开始获得距离我 "+radius+"米以内的人");
        SearchResponse response = searchNearbyService.search(myLat,myLog,radius,size,"男");

        long total = response.getHits().getTotalHits();
        System.out.println("共找到"+total+"人，优先显示"+size+"人,查找耗时"+response.getTook().millis()+"毫秒");

        Range agg = response.getAggregations().get("agg");

        // For each entry
        for (Range.Bucket entry : agg.getBuckets()) {
            String key = entry.getKeyAsString();    // key as String
            Number from = (Number) entry.getFrom(); // bucket from value
            Number to = (Number) entry.getTo();     // bucket to value
            long docCount = entry.getDocCount();    // Doc count


            System.out.printf("key [%s], from [%s], to [%s], doc_count [%s]\n", key, from, to, docCount);
        }


        for(SearchHit hit: response.getHits().getHits()){

            String nickName = hit.getSourceAsMap().get("nickName").toString();

            String location = hit.getSourceAsMap().get("location").toString();

            String sex = hit.getSourceAsMap().get("sex").toString();


            BigDecimal geoDis = new BigDecimal((Double)hit.getSortValues()[0]);
            geoDis.setScale(0,BigDecimal.ROUND_HALF_DOWN);

            System.out.println("昵称："+nickName +",性别："+sex+",微信号码： "+ hit.getSourceAsMap().get("wxNo")+" 距离:"+ geoDis.intValue()+"米 坐标:"+location);

        }


    }
}
