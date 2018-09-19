package net.iapploft.registry;

import net.iapploft.bean.ZKConfig;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dave on 2018/9/18.
 */
public class ZKServiceDiscovery implements ServiceDiscovery {
    private CuratorFramework curatorFramework ;
    public ZKServiceDiscovery() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        curatorFramework = CuratorFrameworkFactory.builder().connectString(ZKConfig.ZKURL).sessionTimeoutMs(4000).retryPolicy(retryPolicy).build();
        //client = CuratorFrameworkFactory.newClient(ZKConfig.ZKURL, retryPolicy);
        curatorFramework.start();
    }

    @Override
    public String discovery(String serviceName) {

        String servicePath = ZKConfig.ZKRegistryPath+"/"+serviceName;
        List<String> children = new ArrayList<String>();
        try {
            children = curatorFramework.getChildren().forPath(servicePath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return children.get(0);
    }
}
