package com.panda.hook.javahook;

import java.lang.reflect.Member;


public abstract class MethodCallback  {
	public MethodCallback() {
	}

	protected void beforeHookedMethod(MethodHookParam param) throws Throwable {}
	protected void afterHookedMethod(MethodHookParam param) throws Throwable {}
}
