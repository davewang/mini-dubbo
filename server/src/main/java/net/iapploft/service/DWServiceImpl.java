package net.iapploft.service;

import net.iapploft.annotation.RpcService;

/**
 * Created by dave on 2018/9/18.
 */
@RpcService(DWService.class)
public class DWServiceImpl implements DWService {
    @Override
    public String hello(String name) {
        return "Hello,HHH," + name;
    }
}
