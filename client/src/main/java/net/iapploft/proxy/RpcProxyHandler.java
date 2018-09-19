package net.iapploft.proxy;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.iapploft.bean.RpcRequest;

/**
 * Created by dave on 2018/9/18.
 */
public class RpcProxyHandler extends ChannelInboundHandlerAdapter {

    public Object getRespObject() {
        return respObject;
    }



    private Object respObject ;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

          respObject = msg;
    }
//    @Override
//    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        System.out.println("client channelActive is ok!");
//    }
}
