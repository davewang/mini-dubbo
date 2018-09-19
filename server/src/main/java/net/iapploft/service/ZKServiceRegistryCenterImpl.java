package net.iapploft.service;

import net.iapploft.bean.ZKConfig;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

/**
 * Created by dave on 2018/9/18.
 */
public class ZKServiceRegistryCenterImpl implements ServiceRegistryCenter {
    private CuratorFramework curatorFramework ;
    public ZKServiceRegistryCenterImpl(){
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        curatorFramework = CuratorFrameworkFactory.builder().connectString(ZKConfig.ZKURL).sessionTimeoutMs(4000).retryPolicy(retryPolicy).build();
        curatorFramework.start();
    }
    //服务注册
    public void registry(String serverName,String serverAddress){

        String servicePath = ZKConfig.ZKRegistryPath + "/" + serverName;

        try {
            if (curatorFramework.checkExists().forPath(servicePath) == null){
                  curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(servicePath,"0".getBytes());
            }
            System.out.println("serverName创建成功:"+serverName);

            String addressPath = servicePath + "/" + serverAddress;
            String addNode = curatorFramework.create().withMode(CreateMode.EPHEMERAL).forPath(addressPath,"0".getBytes());
            System.out.println("serverAddress创建成功:"+addNode);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args){
        ZKServiceRegistryCenterImpl center = new ZKServiceRegistryCenterImpl();
        center.registry("dev","12");
    }
}
