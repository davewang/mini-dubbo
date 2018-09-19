package net.iapploft.handler;

import com.sun.corba.se.impl.ior.OldJIDLObjectKeyTemplate;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.iapploft.bean.RpcRequest;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dave on 2018/9/18.
 */
public class RpcServiceServerHandler extends ChannelInboundHandlerAdapter {

    private Map<String,Object> handlerMap = new HashMap<String,Object>();

    public RpcServiceServerHandler(HashMap<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //super.channelRead(ctx, msg);

        System.out.println("receive:"+msg);
        RpcRequest request = (RpcRequest) msg;

        Object result = new Object();
        //更加request进行service调用
        if(handlerMap.containsKey(request.getClassName())){

            Object clazz = handlerMap.get(request.getClassName());
            Method method = clazz.getClass().getMethod(request.getMethodName(),request.getTypes());
            result = method.invoke(clazz,request.getArgs());
        }
        ctx.write(result);
        ctx.flush();
        ctx.close();
    }

//    @Override
//    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        System.out.println("server channelActive is ok!");
//    }
}
