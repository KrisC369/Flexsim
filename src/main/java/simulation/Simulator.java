package simulation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import time.Clock;

public class Simulator implements ISimulationContext {

	private static final int RUNNERTHREADSLEEPDURATION = 500;
	private volatile boolean runflag;
	private int duration;
	private final Clock clock;
	private final List<ISimulationComponent> components;

	public Simulator() {
		this.runflag = false;
		this.duration = 0;
		this.clock = new Clock();
		this.components = new ArrayList<ISimulationComponent>();
	}

	public boolean isRunning() {
		return this.runflag;
	}

	public void start(boolean immediateReturn) {
		setFlag(true);
		simloop();
		while (!immediateReturn && runflag) {
			sleep(RUNNERTHREADSLEEPDURATION);
		}
	}

	private void sleep(long duration) {
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			Logger.getGlobal().log(Level.WARNING,
					"Simulator control got woken from sleep by interrupt.");
		}
	}

	private void simloop() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (shouldRun()) {
					getClock().addTimeStep(1);
					tickComponents();
				}
				setFlag(false);
			}
		}).start();
	}

	private synchronized void tickComponents() {
		for (ISimulationComponent c : components) {
			c.tick();
		}
	}

	private boolean shouldRun() {
		if (!runflag) {
			return false;
		}
		if (getDuration() > 0 && getClock().getTimeCount() >= getDuration()) {
			return false;
		}
		return true;

	}

	private Clock getClock() {
		return this.clock;
	}

	public void setDuration(int i) {
		this.duration = i;

	}

	public int getDuration() {
		return this.duration;
	}

	@Override
	public void register(ISimulationComponent comp) {
		this.components.add(comp);

	}

	public Collection<ISimulationComponent> getComponents() {
		return Collections.unmodifiableCollection(components);
	}

	public void stop() {
		setFlag(false);
	}

	private void setFlag(boolean flag) {
		this.runflag = flag;
	}

}
