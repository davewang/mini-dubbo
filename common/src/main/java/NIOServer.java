import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by dave on 2018/9/18.
 */
public class NIOServer {

    private int port;
    private InetSocketAddress inetSocketAddress;
    private Selector selector;
    public NIOServer(int port){

        this.port = port;
        inetSocketAddress = new InetSocketAddress(port);
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

            serverSocketChannel.bind(inetSocketAddress);
            serverSocketChannel.configureBlocking(false);
            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("服务器启动："+this.port);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    public void listen(){
        try {
            while (true){

                int wait = this.selector.select();
                if (wait == 0){ continue;}
                Set<SelectionKey> keys = this.selector.selectedKeys();
                Iterator<SelectionKey> iter =  keys.iterator();
                while (iter.hasNext())
                {
                    SelectionKey key =  iter.next();
                    //针对每一个客户端进行操作
                    process(key);
                    iter.remove();
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void process(SelectionKey key) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        if (key.isAcceptable())
        {
            ServerSocketChannel server = (ServerSocketChannel)key.channel();
            SocketChannel client = server.accept();
            client.configureBlocking(false);

            //连接上来可读写
            client.register(selector,SelectionKey.OP_READ);

        }else if(key.isReadable()){
            SocketChannel client = (SocketChannel)key.channel();
            int len = client.read(buffer);
            if (len > 0){
                //buffer.flip();
                String content = new String(buffer.array());
                System.out.print(content);
                client.register(selector,SelectionKey.OP_WRITE);
            }
            buffer.clear();
        }else if(key.isWritable()){
            SocketChannel client = (SocketChannel)key.channel();
            client.write(buffer.wrap("hello world".getBytes()));
            client.close();
        }
        //System.out.println(key);
    }
}
