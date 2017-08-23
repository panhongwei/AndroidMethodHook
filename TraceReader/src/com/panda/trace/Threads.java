package com.panda.trace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Threads {
	//List<MethodLog> methods=new ArrayList<>();
	public List<String> names=new ArrayList<>();
	Map<String,TraceThread> threads=new HashMap<>();
	List<MethodList> methodList=new ArrayList<>();
	public List<MethodList> getMethodList() {
		return methodList;
	}
	public void setMethodList(List<MethodList> methodList) {
		this.methodList = methodList;
	}
	public Map<String, TraceThread> getThreads() {
		return threads;
	}
	public void setThreads(Map<String, TraceThread> threads) {
		this.threads = threads;
	}
	@SuppressWarnings("unchecked")
	public Threads(Trace trace) throws Exception{
		if(trace==null){
			Exception e=new Exception("trace is null");
			throw e;
		}else if(trace.fmFile==null||trace.traceFile==null){
			Exception e=new Exception("trace file uninit!");
			throw e;
		}
		methodList=trace.fmFile.methods;
		List<MethodLog> methods=new ArrayList<>();
		List<MethodList> list=trace.fmFile.methods;
		boolean found;
		for(int i=0;i<trace.traceFile.records.size();++i){
			found=false;
			MethodLog ml=new MethodLog();
			ml.record=trace.traceFile.records.get(i);
			for(MethodList m:list){
				if(((int)m.getMethod())==TraceAction.decodeMethodValue(trace.traceFile.records.get(i).methodValue)){
					ml.FullName=m.getMethodDescriptor()+"."+m.getMethodName()+m.getMethodSig();
					ml.methodName=m.getMethodName();
					ml.source=m.getSource().split("\t")[0];
					ml.action=TraceAction.decodeAction(trace.traceFile.records.get(i).methodValue);
					found=true;
				}
			}
			if(!found){
				ml.methodName="0x"+Integer.toHexString(TraceAction.decodeMethodValue(trace.traceFile.records.get(i).methodValue));
				ml.action=TraceAction.decodeAction(trace.traceFile.records.get(i).methodValue);
				ml.source="unkown";
				ml.FullName=ml.methodName;
				//System.out.println(ml.record.threadId+" "+ml.methodName+" "+ml.action+" "+ml.record.threadClockDiff+" "+ml.record.wallClockDiff);
			}
			methods.add(ml);
		}
		for(int i=0;i<methods.size();++i){
			if(!threads.containsKey(methods.get(i).record.threadId+"")){
				TraceThread thread=new TraceThread();
				thread.threadId=methods.get(i).record.threadId;
				thread.methods.add(methods.get(i));
				thread.name=trace.fmFile.threads.get(methods.get(i).record.threadId+"");
				threads.put(methods.get(i).record.threadId+"", thread);
				names.add(methods.get(i).record.threadId+"");
			}else{
				threads.get(methods.get(i).record.threadId+"").methods.add(methods.get(i));
			}
		}
		Collections.sort(names,new Comparator<String>() {
            public int compare(String o1, String o2) {
                return Integer.parseInt(o1)-Integer.parseInt(o2);
            }
        });
		for (String key : threads.keySet()) {
			threads.get(key).sortMethods();
		}
		//Collections.sort(methods);
	}
}
