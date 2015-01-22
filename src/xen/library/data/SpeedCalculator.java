package xen.library.data;

import java.util.Timer;
import java.util.TimerTask;

public class SpeedCalculator {
	Timer timer;
	
	long db = 0;
	long b = 0;
	int i = 0;
	
	double bytePerSec;
	double KbytePerSec;
	double MbytePerSec;
	double GbytePerSec;
	
	private TimerTask task;
	
	public SpeedCalculator(){
		timer = new Timer();
		task = new TimerTask() {
			@Override
			public void run() {
				bytePerSec = b-db;
				db = b;
				KbytePerSec = bytePerSec/(Math.pow(2, 10));
				MbytePerSec = bytePerSec/(Math.pow(2, 20));
				GbytePerSec = bytePerSec/(Math.pow(2, 30));
				i++;
			}
		};
		timer.schedule(task, 0, 1000);
	}
	
	public void update(long b){
		this.b = b;
	}
	public void reset(){
		b = 0;
		db = 0;
		i = 0;
	}
	
	public double getByteRate(){
		return bytePerSec;
	}
	public double getKByteRate(){
		return KbytePerSec;
	}
	public double getMByteRate(){
		return MbytePerSec;
	}
	public double getGByteRate(){
		return GbytePerSec;
	}
	
	public double getAvrageByteRate(){
		return b/i;
	}
	public double getAvrageKByteRate(){
		return b/i/(Math.pow(1024.0d, 1));
	}
	public double getAvrageMByteRate(){
		return b/i/(Math.pow(1024.0d, 2));
	}
	public double getAvrageGByteRate(){
		return b/i/(Math.pow(1024.0d, 3));
	}
	
	public void close(){
		task.cancel();
		timer.cancel();
		timer.purge();
	}
}
