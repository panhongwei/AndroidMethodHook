# AndroidMethodHook
结合(https://yq.aliyun.com/articles/74598?t=t1#)阿里Sophix热修复方案，使用dexmaker动态生成dex文件，通过反射获取动态生成的类的Method替换想要hook的Method。<br>
使用方法：
```Java
HookManager.findAndHookMethod(MainActivity.class, "onCreate", Bundle.class, new MethodCallback() {
    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        super.beforeHookedMethod(param);
        Log.d("panda", "onCreate:"+param.thisObject.getClass().getName());
    }

    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        super.afterHookedMethod(param);
        Log.d("panda", "i'm in method " +param.method.getName()+" afterHookedMethod");
    }
});
HookManager.startHooks(base);
```
主要思路为使用dexmaker生成一个一摸一样的方法，将this和传入参数封装成Object[] args传给MethodUtil类的invoke函数，然后回调MethodCallback实现类似于xposed mehtod hook。<br>
```Java
public class com_panda_hook_andhook_MainActivity {
  public com_panda_hook_andhook_MainActivity() {
      super();
  }

  public double test(Object arg12, int arg13, int arg14, char arg15) {
      return MethodUtil.invoke("com_panda_hook_andhook_MainActivity_testLIICD", this, new Object[]{
              arg12, Integer.valueOf(arg13), Integer.valueOf(arg14), Character.valueOf(arg15)}).doubleValue();
  }
}
```
