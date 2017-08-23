package com.panda.hook.javahook;

import android.util.Log;

import java.lang.reflect.Member;

/**
 * Created by panda on 17/8/21.
 */

public class HookParam {
    public Member method;
    /** The <code>this</code> reference for an instance method, or null for static methods */
    public Object thisObject;
    /** Arguments to the method call */
    public Object[] args;

    private Object result = null;
    private Throwable throwable = null;
    /* package */ boolean returnEarly = false;
    public HookParam(){
        Log.d("panda","in HookParam");
    }

    /** Returns the result of the method call */
    public Object getResult() {
        return result;
    }

    /**
     * Modify the result of the method call. In a "before-method-call"
     * hook, prevents the call to the original method.
     * You still need to "return" from the hook handler if required.
     */
    public void setResult(Object result) {
        this.result = result;
        this.throwable = null;
        this.returnEarly = true;
    }

    /** Returns the <code>Throwable</code> thrown by the method, or null */
    public Throwable getThrowable() {
        return throwable;
    }

    /** Returns true if an exception was thrown by the method */
    public boolean hasThrowable() {
        return throwable != null;
    }

    /**
     * Modify the exception thrown of the method call. In a "before-method-call"
     * hook, prevents the call to the original method.
     * You still need to "return" from the hook handler if required.
     */
    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
        this.result = null;
        this.returnEarly = true;
    }

    /** Returns the result of the method call, or throws the Throwable caused by it */
    public Object getResultOrThrowable() throws Throwable {
        if (throwable != null)
            throw throwable;
        return result;
    }
}
