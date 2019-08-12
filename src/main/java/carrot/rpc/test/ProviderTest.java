package carrot.rpc.test;

import carrot.rpc.provider.Provider;
import carrot.rpc.provider.handler.ServiceBean;
/**
 * 
 * @author Administrator
 *ProviderTest 中new 一个服务，初始化参数，注册服务
 */
public class ProviderTest {
    public static void main(String[] args) throws Exception {
        Provider provider = new Provider();
        provider.setPort(8668);//设置端口
        provider.afterPropertiesSet();//配置参数，开启服务
        
        TestService testService = new TestServiceImpl();//服务的接口
        ServiceBean serviceBean = new ServiceBean();//服务的bean类
        serviceBean.setName("testService");//将服务接口闯入服务的bean类中，初始化服务  服务名称
        serviceBean.setClazz(TestService.class);//初始化服务  服务类
        serviceBean.setService(testService);//初始化服务  服务功能
        
        provider.export(serviceBean);//在Zookeeper上注册上面初始化过的服务
    }
}
