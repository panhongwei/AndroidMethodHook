package com.panda.hook.andhook;

import android.util.Log;

/**
 * Created by panda on 17/8/16.
 */

public class Test {
    int aa;
    int bb;
    public Test(int a,int b){
        this.aa=a;
        this.bb=b;
        log();
    }
    private void log(){
        Log.d("panda", "aa");
        Log.d("panda", "bb");
    }
}
