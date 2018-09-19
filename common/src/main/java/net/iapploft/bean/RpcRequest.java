package net.iapploft.bean;

import java.io.Serializable;

/**
 * Created by dave on 2018/9/18.
 */
public class RpcRequest implements Serializable{


    private static final long serialVersionUID = 6236459685465723312L;
    private String className;
    private String methodName;
    private Class<?>[] types;
    private Object[] args;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getTypes() {
        return types;
    }

    public void setTypes(Class<?>[] types) {
        this.types = types;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }
}
