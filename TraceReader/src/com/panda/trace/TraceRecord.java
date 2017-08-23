package com.panda.trace;

public class TraceRecord {
	int threadId;
	public int getThreadId() {
		return threadId;
	}
	public void setThreadId(int threadId) {
		this.threadId = threadId;
	}
	public int getMethodValue() {
		return methodValue;
	}
	public void setMethodValue(int methodValue) {
		this.methodValue = methodValue;
	}
	public int getThreadClockDiff() {
		return threadClockDiff;
	}
	public void setThreadClockDiff(int threadClockDiff) {
		this.threadClockDiff = threadClockDiff;
	}
	public int getWallClockDiff() {
		return wallClockDiff;
	}
	public void setWallClockDiff(int wallClockDiff) {
		this.wallClockDiff = wallClockDiff;
	}
	int methodValue;
	int threadClockDiff;
	int wallClockDiff;
}
