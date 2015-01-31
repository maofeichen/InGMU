import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class Reminder {
	Timer timer;
	int seconds;

	public Reminder(int seconds) {
		this.seconds = seconds;
		timer = new Timer();
		//timer.schedule(new HeartBeat(), 0, seconds*1000);
		timer.schedule(new ProcessingDelay(), seconds*1000);
	}

	class ProcessingDelay extends TimerTask {
		public void run() {
			
			Scanner s = new Scanner(System.in);
			System.out.print("Got Token? : ");
			String request = s.nextLine();
			
			if (request.equals("yes")){
				System.out.println("Task completed - Server got the token.");
				System.out.println("Task scheduled - Waiting for server to exit its critical section..");
				timer.schedule(new CriticalSectionDelay(), seconds*1000);
			} else {
				System.out.println("Task failed - Server did not get the token.");
				timer.schedule(new HeartBeat(), 0, seconds*1000);
				//timer.cancel(); //Terminate the timer thread
			}
		}
	}

	class CriticalSectionDelay extends TimerTask {
		@Override
		public void run() {
			System.out.println("Task completed - Server finished executing its critical section.");
			timer.cancel(); //Terminate the timer thread
		}
	}
	
	class HeartBeat extends TimerTask{
		int numWarningBeeps = 3;
		@Override
		public void run() {
			if (numWarningBeeps > 0) {
                System.out.println("Beep!");
                numWarningBeeps--;
            } else {
                System.out.println("Server has failed!");
                timer.cancel();
            }
		}
		
	}

	public static void main(String args[]) {
		System.out.println("Task scheduled - Waiting for server to get token...");
		Reminder p = new Reminder(2);
	}
}