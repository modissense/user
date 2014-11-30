package feedcontroller;

import java.util.TimerTask;

public abstract class Feeder extends TimerTask{

	protected String since;
	protected String until;
	
	public abstract void getFeed();
	
}
