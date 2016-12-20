package com.dianping.cat.dubbo;

import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.utils.ReflectUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.rpc.*;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;

import java.lang.reflect.Method;

/**
 * Created by zhangsw on 2016/12/16.
 */
@Activate
public class CatDubboFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        Transaction t = Cat.getProducer().newTransaction("dubboService", invocation.getMethodName());
        try {
            t.setStatus(Transaction.SUCCESS);
            Result result =  invoker.invoke(invocation);

            if (result.hasException() && GenericService.class != invoker.getInterface()) {
                    Throwable exception = result.getException();

                    // 如果是checked异常，直接抛出
                    if (! (exception instanceof RuntimeException) && (exception instanceof Exception)) {
                        return result;
                    }
                    // 在方法签名上有声明，直接抛出
                    try {
                        Method method = invoker.getInterface().getMethod(invocation.getMethodName(), invocation.getParameterTypes());
                        Class<?>[] exceptionClassses = method.getExceptionTypes();
                        for (Class<?> exceptionClass : exceptionClassses) {
                            if (exception.getClass().equals(exceptionClass)) {
                                return result;
                            }
                        }
                    } catch (NoSuchMethodException e) {
                        return result;
                    }

                    // 未在方法签名上定义的异常，在服务器端打印ERROR日志
                    Cat.getProducer().logError("Got unchecked and undeclared exception which called by " + RpcContext.getContext().getRemoteHost()
                            + ". service: " + invoker.getInterface().getName() + ", method: " + invocation.getMethodName()
                            + ", exception: " + exception.getClass().getName() + ": " + exception.getMessage(), exception);
                    t.setStatus(new RuntimeException(StringUtils.toString(exception)));
                    // 异常类和接口类在同一jar包里，直接抛出
                    String serviceFile = ReflectUtils.getCodeBase(invoker.getInterface());
                    String exceptionFile = ReflectUtils.getCodeBase(exception.getClass());
                    if (serviceFile == null || exceptionFile == null || serviceFile.equals(exceptionFile)){
                        return result;
                    }
                    // 是JDK自带的异常，直接抛出
                    String className = exception.getClass().getName();
                    if (className.startsWith("java.") || className.startsWith("javax.")) {
                        return result;
                    }
                    // 是Dubbo本身的异常，直接抛出
                    if (exception instanceof RpcException) {
                        return result;
                    }

                    // 否则，包装成RuntimeException抛给客户端
                    return new RpcResult(new RuntimeException(StringUtils.toString(exception)));
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
