package tracelabs.events;

import java.util.Arrays;
import java.util.List;

public class SysCallEvent {
	private String name;	
	private long tid;
	
	private int numEntry = 0;
	private int numExit = 0;
	
	// TODO: Add a map of performance counters
	
	private long tmpEntryTimestamp = 0;
	private long duration = 0;
	
	public static void process(List<SysCallEvent> sysCallEvents, String sysCallEventName, Long tid, Long timestamp) {
		String[] eventNameParts = splitEventNameIntoParts(sysCallEventName);
		if (!isValid(eventNameParts)) {
			return;
		}
		
		String eventType = get("type", eventNameParts);
		if (!isValid("type", eventType)) {
			return;
		}
		
		String eventMoment = get("moment", eventNameParts);
		if (!isValid("moment", eventMoment)) {
			return;
		}
		
		String eventName = get("name", eventNameParts);
		if (!isValid("name", eventName)) {
			return;
		}
		
		SysCallEvent existingEvent = sysCallEvents.stream()
				.filter(event -> event.getName().equals(eventName) && event.getTid() == tid)
				.findAny()
				.orElse(null);
		
		if (existingEvent ==  null) {
			SysCallEvent event = new SysCallEvent(eventName, eventMoment, tid, timestamp);
			sysCallEvents.add(event);
		} else {
			existingEvent.increment(eventMoment, timestamp);
		}
	}
	
	private static String[] splitEventNameIntoParts(String eventName) {
		return eventName.split("_");
	}
	
	private static boolean isValid(String[] eventNameParts) {
		return eventNameParts.length >= 3;
	}
	
	private static boolean isValid(String type, String value) {
		switch (type) {
		case "type":
			return value.equals("syscall");
		case "moment":
			return value.equals("entry") || value.equals("exit");
		case "name":
			return value.length() > 0;
		default:
			return false;
		}
	}
	
	private static String get(String type, String[] eventNameParts) {
		switch (type) {
		case "type":
			return eventNameParts[0];
		case "moment":
			return eventNameParts[1];
		case "name":
			return String.join("_",  Arrays.copyOfRange(eventNameParts, 2, eventNameParts.length));
		default:
			return "";
		}
	}
	
	public SysCallEvent(String name, String moment, long tid, long timestamp) {
		this.name = name;
		this.tid = tid;
		
		increment(moment, timestamp);
	}
	
	public String getName() {
		return name;
	}
	
	public long getTid() {
		return tid;
	}
	
	public int getNumCalls() {
		return numEntry;
	}
	
	public long getAverageDuration() {
		if (numExit == 0) return 0;
		
		return duration / numExit;
	}
	
	public void increment(String moment, long timestamp) {
		if (moment.equals("entry")) {
			numEntry++;
			tmpEntryTimestamp = timestamp;
		} else if (moment.equals("exit")) {
			numExit++;
			duration = duration + (timestamp - tmpEntryTimestamp);
		}
	}
	
	public String toString() {
		return name + "\t call: " + getNumCalls() + "x \t avg: " + getAverageDuration() + "ns";
	}
}
