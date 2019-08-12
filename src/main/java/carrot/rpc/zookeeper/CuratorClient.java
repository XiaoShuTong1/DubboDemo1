package carrot.rpc.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
/**
 * Zookeeper注册中心
 * @author Administrator
 *客户端负责人
 */
public class CuratorClient {
    private static CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", new RetryOneTime(100));
    //使用静态工程的方法，创建一个新的客户端
    static {
        client.start();//开启客户端
    }

    public static CuratorFramework getClient(){

        return client;//返回客户端
    }

}
