package com.panda.hook.javahook;

import java.lang.reflect.Member;
import java.lang.reflect.Method;

/**
 * Created by panda on 17/8/12.
 */

public class BackMethod {
    public Method getInvoker() {
        return invoker;
    }

    public void setInvoker(Method invoker) {
        this.invoker = invoker;
    }

    private Method invoker;

    private Member oldMethod;

    public Member getNewMethod() {
        return newMethod;
    }

    public void setNewMethod(Member newMethod) {
        this.newMethod = newMethod;
    }

    private Member newMethod;

    public void setBackAddr(long backAddr) {
        this.backAddr = backAddr;
    }

    public void setOldMethod(Member oldMethod) {
        this.oldMethod = oldMethod;
    }

    public long getBackAddr() {
        return backAddr;
    }

    public Member getOldMethod() {
        return oldMethod;
    }

    private long backAddr;

    public Object getCallback() {
        return callback;
    }

    public void setCallback(Object callback) {
        this.callback = callback;
    }

    private Object callback;


}
