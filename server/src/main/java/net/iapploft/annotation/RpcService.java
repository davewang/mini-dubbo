package net.iapploft.annotation;

import java.lang.annotation.*;


/**
 * Created by dave on 2018/9/18.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcService {


    Class<?> value();
}
