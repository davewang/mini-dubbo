package net.iapploft.service;

import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Created by dave on 2018/9/19.
 */

public class SearchNearbyService {
    private TransportClient client;
    //database
    private String indexName = "people_es_search";
    //table
    private String indexType = "wechat";

    public SearchNearbyService() throws UnknownHostException {

        InetSocketAddress address = new InetSocketAddress("10.7.7.198",9300);

        Settings settings = Settings.builder().put("cluster.name", "docker-cluster").build();

       // Settings.EMPTY
        client = new PreBuiltTransportClient(settings).addTransportAddress(new TransportAddress(address));
        System.out.println("nodeName:"+client.nodeName());

    }
    //like create database
    public void reCreateIndex(){

        client.admin().indices().prepareDelete(indexName).execute().actionGet();
        System.out.println("删除成功！");
        createIndex();

    }

    private void createIndex() {
        XContentBuilder mapping = createMapping();
        //create db
        client.admin().indices().prepareCreate(indexName).execute().actionGet();
        //create table
        PutMappingRequest putMappingRequest = Requests.putMappingRequest(indexName).type(indexType).source(mapping);
        PutMappingResponse putMappingResponse = client.admin().indices().putMapping(putMappingRequest).actionGet();

        if (!putMappingResponse.isAcknowledged()){
            System.out.println("无法创建"+indexName+" and "+indexType);
        }else{
            System.out.println("创建"+indexName+" and "+indexType+" 成功");
        }
    }

    private XContentBuilder createMapping() {
        XContentBuilder mapping = null;
        try {
            mapping = XContentFactory.jsonBuilder().startObject()
                   //like table
                   .startObject(indexType).startObject("properties")
                   //id
                   .startObject("wxNo").field("type","text").endObject()
                   //nick
                   .startObject("nickName").field("type","text").endObject()
                   //sex
                   .startObject("sex").field("type","text").endObject()
                   //location
                   .startObject("location").field("type","geo_point").endObject()
                   .endObject().endObject().endObject();

        } catch (IOException e) {
            e.printStackTrace();
        }


        return mapping;
    }
    public SearchResponse search(double myLat, double myLon, int radius, int size , String sex){
       // int size = 10,radius = 50;

      //  System.out.println("开始获取距离 "+ myName + radius+"米以内的人");
       // SearchPhaseResult result = new SearchPhaseResult();
        //String unit = DistanceUnit.METERS.toString();//计量单位

//        SearchRequestBuilder srb = client.prepareSearch(indexName).setTypes(indexType);
//        srb.setFrom(0).setSize(size);//最高条数
//
//        srb.setPostFilter(QueryBuilders.rangeQuery("age"))
        AggregationBuilder geoAgg =
                AggregationBuilders
                        .geoDistance("agg", new GeoPoint(myLat,myLon))
                        .field("location")
                        .unit(DistanceUnit.METERS)
                        .addRange(0, radius);

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (!(sex == null) || "".equals(sex.trim()))
        {
            boolQueryBuilder.must(QueryBuilders.matchQuery("sex",sex));
        }

        GeoDistanceSortBuilder geoDistanceSort = SortBuilders.geoDistanceSort("location",myLat,myLon);
        geoDistanceSort.unit(DistanceUnit.METERS);
        geoDistanceSort.order(SortOrder.ASC);

        SearchResponse response = client.prepareSearch(indexName).setTypes(indexType)
                .addAggregation(geoAgg)
                .addSort(geoDistanceSort)
                .setQuery(boolQueryBuilder)
                .setFrom(0) //.setSize(size)  //忽略命中 hits  //如何优先显示 聚合的前10条呢？el暂时不支持
                .execute().actionGet();


        //高亮
        SearchHits hits = response.getHits();
        SearchHit[]  searchHits = hits.getHits();

        Float usetime = response.getTook().millis() / 1000.f;
        System.out.println("usetime " + usetime);
        System.out.println("searchHits count " + searchHits.length);

        return response;
    }

    //like insert into
    public int addDataToIndex(double myLat,double myLon,int total) {
//        List<String> peopleList = new ArrayList<String>();

        Gson gson = new Gson();
        List<IndexRequest>  requests = new ArrayList<IndexRequest>();

        for (long i=0;i<total;i++){
            People people = randomPeople(myLat,myLon);
           // System.out.println(gson.toJson(people));
            IndexRequest request = client.prepareIndex(indexName,indexType).setSource(gson.toJson(people), XContentType.JSON).request();
            requests.add(request);
            //peopleList.add(gson.toJson(people));
        }

        //批量创建索引
        BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();
        for(IndexRequest request : requests){
            bulkRequestBuilder.add(request);
        }

        BulkResponse bulkResponse = bulkRequestBuilder.execute().actionGet();
        if(bulkResponse.hasFailures()) {

            System.out.println("创建索引出错 => "+bulkResponse.buildFailureMessage());
        }
        return bulkRequestBuilder.numberOfActions();
    }

    private People randomPeople(double myLat, double myLon) {

        String uid = UUID.randomUUID().toString().replaceAll("-","");
        People people = new People();
        people.setWxNo("wx_" + uid);
        people.setNickName(uid);
        int r = new Random().nextInt(10);
        people.setSex(r % 2 == 0 ? "女" : "男");
        double rd = new Random().nextDouble();
        people.setLocation(new GeoPoint(myLat+rd,myLon+rd));
        return people;
    }
}
