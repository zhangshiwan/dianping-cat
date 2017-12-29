package com.dianping.cat.dubbo;

import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.site.helper.ParametersHelper;


/**
 * Created by zhangsw on 2016/12/16.
 */
@Activate(
        group = {"provider"}
)
public class CatDubboFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String name =  invocation.getInvoker().getInterface().getName() +"." + invocation.getMethodName();
        Transaction t = Cat.getProducer().newTransaction("DubboService",  name);
        try {
            t.setStatus(Transaction.SUCCESS);
            Result result =  invoker.invoke(invocation);

            if (result.hasException() && GenericService.class != invoker.getInterface()) {
                    Throwable exception = result.getException();

                    Cat.getProducer().logError("Got exception which called by " + RpcContext.getContext().getRemoteHost()
                            + ". service: " + invoker.getInterface().getName() + ", method: " + invocation.getMethodName()
                            + ", exception: " + exception.getClass().getName()+ ", " + exception.getMessage()
                            + ", args: " + ParametersHelper.getParameters( invocation.getArguments())  , exception);
                    t.setStatus(new RuntimeException(StringUtils.toString(exception)));
            }
            return result;
        }
        catch (Throwable e) {
            //用log4j记录系统异常，以便在Logview中看到此信息
            Cat.getProducer().logError(e);
            t.setStatus(e);
            return new RpcResult(new RuntimeException(StringUtils.toString(e)));
		    /*
		    (CAT所有的API都可以单独使用，也可以组合使用，比如Transaction中嵌套Event或者Metric。)
			(注意如果这里希望异常继续向上抛，需要继续向上抛出，往往需要抛出异常，让上层应用知道。)
			(如果认为这个异常在这边可以被吃掉，则不需要在抛出异常。)
			*/
        } finally {
            t.complete();
        }
    }


}
