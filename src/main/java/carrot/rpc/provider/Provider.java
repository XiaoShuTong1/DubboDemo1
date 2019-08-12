package carrot.rpc.provider;

import carrot.rpc.common.codec.RpcJdkEncoder;
import carrot.rpc.common.dto.RpcRequest;
import carrot.rpc.common.codec.RpcJdkDecoder;
import carrot.rpc.common.dto.RpcResponse;
import carrot.rpc.provider.handler.InvocationHandler;
import carrot.rpc.provider.handler.ServiceInvoker;
import carrot.rpc.provider.handler.ServiceBean;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
/**
 * 
 * @author Administrator
 *Provider的作用：1、注册Zookeeper
 *2、设置端口
 *3、配置参数，开启服务
 */
public class Provider implements InitializingBean, ApplicationContextAware {

    private final static Logger logger = LoggerFactory.getLogger(Provider.class);

    private ApplicationContext applicationContext;

    private ServiceInvoker invoker;

    private int port;

    public void setPort(int port) {
        this.port = port;
    }//端口

    public void afterPropertiesSet() throws Exception {
        startServer();
    }//开启服务  初始化netty 配置（acceptor线程  work线程组 通道类型 socket参数  work线程组的handler）  启动服务blind方法  返回future对象

    public void export(ServiceBean serviceBean){
        this.invoker.exportService(serviceBean);
    }//注册Zookpeer

    private void startServer() throws InterruptedException {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);//acceptor线程
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(10);//work线程组
        ServerBootstrap server = new ServerBootstrap();//初始化netty的（配置 启动）类
        this.invoker = new ServiceInvoker();//ServiceInvoker  注册到Zookeeper  调用动态代理来调用处理完请求返回的对象的方法
        server
                .group(bossGroup,workerGroup)
                .channel(NioServerSocketChannel.class)//通道的类型，就是说这个ReflectiveChannelFactory创建的就是NioServerSocketChannel的实例。
                .childOption(ChannelOption.SO_KEEPALIVE,true)//Socket连接的参数    设置tcp协议的请求等待队列
                .childOption(ChannelOption.TCP_NODELAY,true)//Socket连接的参数
                .childHandler(new ChannelInitializer<SocketChannel>() {//设置childGroup的Handler 即work组的线程

                    protected void initChannel(SocketChannel channel) throws Exception {
                        channel.pipeline()//用于初始化Channel的pipeline(责任链)，以处理请求内容。
                                .addLast(new RpcJdkDecoder<RpcRequest>(RpcRequest.class))//编码
                                .addLast(new RpcJdkEncoder<RpcResponse>(RpcResponse.class))//解码
                                .addLast(new InvocationHandler(invoker));
                    }
                });
        ChannelFuture future = server.bind(this.port).sync();//启动
        logger.info("Provider start!");

    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
