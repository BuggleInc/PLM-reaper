package core;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;


public class Session {
	
	private Collection<Event> events;
	private Instant sessionStart, sessionEnd, currentLimitSession;
	private static int maxMinuteSession = 120;
	//private String exoName;
	
	public Session(Event e){
		events = new ArrayList<Event>();
		events.add(e);
		sessionStart = e.getCommitTime();		
		currentLimitSession =  Instant.from(sessionStart);
		currentLimitSession.plusSeconds(maxMinuteSession*60);	
	}
	
	public boolean add(Event e){
		if(e.getCommitTime().isAfter(currentLimitSession))
			return false;
		else{
			events.add(e);
			currentLimitSession = Instant.from(e.getCommitTime());
			currentLimitSession.plusSeconds(maxMinuteSession*60);
			sessionEnd = e.getCommitTime();
			return true;
		}
	}
	
	public void printSession(){
		String date = sessionStart.toString();
		String date2 = sessionEnd.toString();
				
		System.out.println("\n----------------------------------------------------------------------"
						 + "\nSession : Beginning at " + date + " fin : " + date2 + "\n");
		for(Event e:events)
			e.printEvent();
	}
	
	
	
}
