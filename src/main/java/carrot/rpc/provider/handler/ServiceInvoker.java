package carrot.rpc.provider.handler;

import carrot.rpc.common.Invoker;
import carrot.rpc.common.dto.RpcResponse;
import carrot.rpc.common.dto.RpcRequest;
import carrot.rpc.zookeeper.CuratorClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static carrot.rpc.common.Constants.REGISTRY_PREFIX;
/**
 * 
 * @author Administrator
 *ServiceInvoker的两个功能：
 *1、exportService  注册Zookeeper
 *2、动态代理 调用传回来的对象的方法
 */
public class ServiceInvoker implements Invoker {

    private final static Logger logger = LoggerFactory.getLogger(ServiceInvoker.class);

    private CuratorFramework zkClient = CuratorClient.getClient();//客户端的创建

    Map<String, ServiceBean> serviceBeanMap=new ConcurrentHashMap<String, ServiceBean>();//维护一个hashMap，里面存对应的类名和类
    	/**
    	 * -我觉得是注册服务
    	 * @param service
    	 */
    public void exportService(ServiceBean service){
        serviceBeanMap.put(service.getClazz().getName(),service);



        try {
            Stat stat = zkClient.checkExists().forPath(REGISTRY_PREFIX + "/"
                    + service.getClazz().getName());//-确认对用地址的客户端是否开启
            if(stat==null){
                zkClient.create()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(REGISTRY_PREFIX+"/"
                                +service.getClazz().getName());//-若不存在，就创建客户端
            }
            InetAddress addr = InetAddress.getLocalHost();//-获取本地ip

            zkClient.create()//-创建节点 注册
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(REGISTRY_PREFIX+"/"
                            +service.getClazz().getName()+"/"
                            +addr.getHostAddress()+":"+ 8668,  //后面需要做成可配置的
                            "empty".getBytes());

        } catch (Exception e) {
            logger.error("export service fail ",e);
        }
    }
    	/**
    	 * 动态代理调用
    	 */
    public RpcResponse invoke(RpcRequest request){
        RpcResponse rpcResponse = new RpcResponse(request.requestId);//处理完返回的请求对象
        ServiceBean serviceBean = serviceBeanMap.get(request.getServiceName());//从hashMap中拿到服务对象的类
        Method m = null;
        try {
            m = serviceBean.getService().getClass().getMethod(request.getMethodName(),request.getParameterTypes());
            Object result = m.invoke(serviceBean.getService(), request.getParameters());
            rpcResponse.setResult(result);
        } catch (NoSuchMethodException e) {
            logger.info("invoke error",e);
        } catch (IllegalAccessException e) {
            logger.info("invoke error",e);
        } catch (InvocationTargetException e) {
            logger.info("invoke error",e);
        }
        return rpcResponse;
    }
}
