package com.dianping.cat.aop;


import com.alibaba.dubbo.common.utils.StringUtils;
import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.site.helper.ParametersHelper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;


/**
 * Created by Administrator on 2016/12/12.
 */
public class ServiceProfiler {

    public Object profile(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Class clazz = signature.getDeclaringType();
        String name =  clazz.getName() +"." + signature.getName();

        Transaction t = Cat.getProducer().newTransaction("ServiceProfiler",  name);

        try {
            t.setStatus(Transaction.SUCCESS);
            return pjp.proceed(pjp.getArgs());
        } catch (Throwable e) {
            Cat.getProducer().logError("ServiceProfiler exception "
                    + " service: " + clazz.getName() + ", method: " + signature.getName()
                    + ", args: " + ParametersHelper.getParameters(pjp.getArgs())  , e);
            t.setStatus(new RuntimeException(StringUtils.toString(e)));
            throw e;
        } finally {
            t.complete();
        }
    }




}
