package carrot.rpc.provider.handler;

import carrot.rpc.common.dto.RpcResponse;
import carrot.rpc.common.dto.RpcRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class InvocationHandler extends SimpleChannelInboundHandler {

    private ServiceInvoker invoker;

    public InvocationHandler(ServiceInvoker invoker) {
        this.invoker = invoker;
    }

    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        RpcRequest request = (RpcRequest) o ;
        System.out.println(request);
        RpcResponse future = invoker.invoke(request);
        channelHandlerContext.writeAndFlush(future);
    }
}
