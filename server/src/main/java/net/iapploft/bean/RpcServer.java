package net.iapploft.bean;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolver;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import net.iapploft.annotation.RpcService;
import net.iapploft.handler.RpcServiceServerHandler;
import net.iapploft.service.ServiceRegistryCenter;

import java.util.HashMap;

/**
 * Created by dave on 2018/9/18.
 */
public class RpcServer {
    ServiceRegistryCenter serviceRegistryCenter;
    String address;
    HashMap<String,Object> handlerMap = new HashMap<String, Object>();

    public RpcServer(ServiceRegistryCenter serviceRegistryCenter, String address) {
        this.serviceRegistryCenter = serviceRegistryCenter;
        this.address = address;
    }

    //服务注册 bind 服务名称-- 服务对象 进行绑定
    public void bind(Object ...services){

        for (Object service : services){
            RpcService ann = service.getClass().getAnnotation(RpcService.class);
            String serviceName = ann.value().getName();
            handlerMap.put(serviceName,service);
        }
    }
    //发布注册及监听
    public void registerAndListen(){

        //注册
        for (String serviceName : handlerMap.keySet()){
            this.serviceRegistryCenter.registry(serviceName,address);
        }
        //监听服务
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // (3)
                    .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
//                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4));
//                            ch.pipeline().addLast(new LengthFieldPrepender(4));
//                            ch.pipeline().addLast("encoder",new ObjectEncoder());
//                            ch.pipeline().addLast("decoder",new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
                            ch.pipeline().addLast(new ObjectEncoder());
                            ch.pipeline().addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                            ch.pipeline().addLast(new RpcServiceServerHandler(handlerMap));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)


            // Bind and start to accept incoming connections.
            int port = Integer.parseInt(address.split(":")[1]);
            System.out.println("端口："+port+" 启动成功，等待链接！");
            ChannelFuture f = b.bind(port).sync(); // (7)

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }


    }
}
