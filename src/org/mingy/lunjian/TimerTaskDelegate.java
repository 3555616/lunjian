package org.mingy.lunjian;

import java.util.TimerTask;

public abstract class TimerTaskDelegate extends TimerTask {

	private long tick;
	private long timestamp;

	public TimerTaskDelegate(long tick) {
		super();
		this.tick = tick;
	}

	@Override
	public void run() {
		if (System.currentTimeMillis() - timestamp >= tick) {
			try {
				onTimer();
			} catch (Exception e) {
				e.printStackTrace();
			}
			timestamp = System.currentTimeMillis();
		}
	}

	protected abstract void onTimer() throws Exception;

	public long getTick() {
		return tick;
	}

	public void setTick(long tick) {
		this.tick = tick;
	}
}
