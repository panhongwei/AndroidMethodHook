package com.panda.hook.javahook;

import java.lang.reflect.Member;

/**
 * Created by panda on 17/8/12.
 */

public class BackMethod {
    private Member oldMethod;

    public Member getNewMethod() {
        return newMethod;
    }

    public void setNewMethod(Member newMethod) {
        this.newMethod = newMethod;
    }

    private Member newMethod;

    public void setBackAddr(Object backAddr) {
        this.backAddr = backAddr;
    }

    public void setOldMethod(Member oldMethod) {
        this.oldMethod = oldMethod;
    }

    public Object getBackAddr() {
        return backAddr;
    }

    public Member getOldMethod() {
        return oldMethod;
    }

    private Object backAddr;

    public Object getCallback() {
        return callback;
    }

    public void setCallback(Object callback) {
        this.callback = callback;
    }

    private Object callback;


}
