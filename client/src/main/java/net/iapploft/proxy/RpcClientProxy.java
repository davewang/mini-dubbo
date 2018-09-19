package net.iapploft.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import net.iapploft.bean.RpcRequest;
import net.iapploft.registry.ZKServiceDiscovery;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by dave on 2018/9/18.
 */
public class RpcClientProxy {

    private ZKServiceDiscovery zkServiceDiscovery;
    public RpcClientProxy(ZKServiceDiscovery zkServiceDiscovery) {
        this.zkServiceDiscovery = zkServiceDiscovery;
    }

    public <T> T create(final Class<T> interfaceClazz){

        return (T) Proxy.newProxyInstance(interfaceClazz.getClassLoader(), new Class<?>[]{interfaceClazz}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {


                RpcRequest request = new RpcRequest();
                request.setClassName(method.getDeclaringClass().getName());
                request.setMethodName(method.getName());
                request.setTypes(method.getParameterTypes());
                request.setArgs(args);

                //netty 数据交互
                String serviceName = interfaceClazz.getName();
                String serviceAddress = zkServiceDiscovery.discovery(serviceName);

                int port = Integer.parseInt(serviceAddress.split(":")[1]);
                String host =  serviceAddress.split(":")[0];
                System.out.println("serviceAddress:"+serviceAddress +" host:"+host+" port:"+port);
                final RpcProxyHandler rpcProxyHandler = new RpcProxyHandler();

                //监听服务
                EventLoopGroup workerGroup = new NioEventLoopGroup();
                try {
                    Bootstrap b = new Bootstrap(); // (2)
                    b.group( workerGroup)
                            .channel(NioSocketChannel.class) // (3)
                            .handler(new ChannelInitializer<SocketChannel>() { // (4)
                                @Override
                                public void initChannel(SocketChannel ch) throws Exception {
                                    // ch.pipeline().addLast("frameDecoder",new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4));
                                    // ch.pipeline().addLast("frameEncoder",new LengthFieldPrepender(4));
                                    // ch.pipeline().addLast("encoder",new ObjectEncoder());
                                    // ch.pipeline().addLast("decoder",new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
                                    ch.pipeline().addLast(new ObjectEncoder());
                                    ch.pipeline().addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                                    ch.pipeline().addLast(rpcProxyHandler);
                                }
                            });
                           // .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                           // .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

                    // Bind and start to accept incoming connections.

                    ChannelFuture f = b.connect(host,port).sync(); // (7)

                    // Wait until the server socket is closed.
                    // In this example, this does not happen, but you can do that to gracefully
                    // shut down your server.
                    f.channel().writeAndFlush(request);

                    System.out.println("客户发送完成，等待返回！");
                    f.channel().closeFuture().sync();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    workerGroup.shutdownGracefully();
                }
                return rpcProxyHandler.getRespObject();
            }
        });
    }
}
