package feedcontroller;

import java.io.FileNotFoundException;
import java.util.Timer;

/**
 * 
 * @author giagulei
 * periodic task which crawls data from social networks every 5 minutes.
 */
public class UpdateOrchestrator {

	public static void main(String[] args) {
		/*
		 *  Every five minutes the PeriodicFbCollector is activated
		 */
		Timer timer = new Timer("PeriodicdataCollector");
		CFeeder collector;
		try {
			collector = new CFeeder();
			timer.scheduleAtFixedRate(collector, 0, 5*60*1000);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
