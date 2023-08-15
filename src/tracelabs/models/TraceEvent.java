package tracelabs.models;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class TraceEvent {
	/** The event name. */
	private String name;
	
	/** The event id. This is the process id or thread id. */
	private long id;
	
	private int numEntry = 0;
	private int numExit = 0;
	
	// TODO: Keep a list of all entries and exits.
	// TODO: Add a map of performance counters.
	
	private long tmpEntryTimestamp = 0;
	private long duration = 0;
	
	class PartType {
		public static final String TYPE_LABEL = "type";		
		public static final String MOMENT_LABEL = "moment";
		public static final String NAME_LABEL = "name";
		
		public static final int TYPE_INDEX = 0;
		public static final int MOMENT_INDEX = 1;
		public static final int NAME_INDEX = 2;
	}
	
	public static void process_type(List<TraceEvent> events, String type, String name, Long id, Long timestamp) {
		String[] parts = splitNameIntoParts(name);
		if (!isValid(parts)) {
			return;
		}
		
		if (!get(parts, PartType.TYPE_LABEL).equals(type)) {
			return;
		}
		
		process(events, name, id, timestamp);
	}
	
	public static void process(List<TraceEvent> events, String name, Long id, Long timestamp) {		
		String[] parts = splitNameIntoParts(name);
		if (!isValid(parts)) {
			return;
		}
		
		String typePart = get(parts, PartType.TYPE_LABEL);
		if (!isValid(PartType.TYPE_LABEL, typePart)) {
			return;
		}
		
		String momentPart = get(parts, PartType.MOMENT_LABEL);
		if (!isValid(PartType.MOMENT_LABEL, momentPart)) {
			return;
		}
		
		String namePart = get(parts, PartType.NAME_LABEL);
		if (!isValid(PartType.NAME_LABEL, namePart)) {
			return;
		}
				
		TraceEvent existingEvent = events.stream()
			.filter(event -> event.getName().equals(namePart) && event.getId() == id)
			.findAny()
			.orElse(null);
		
		if (existingEvent == null) {
			TraceEvent event = new TraceEvent(namePart, momentPart, id, timestamp);
			events.add(event);
		} else {
			existingEvent.increment(momentPart, timestamp);
		}
	}
	
	private static String get(String[] parts, String partType) {
		switch (partType) {
		case PartType.MOMENT_LABEL:
			return parts[PartType.MOMENT_INDEX];
		case PartType.NAME_LABEL:
			return String.join("_", Arrays.copyOfRange(parts, PartType.NAME_INDEX, parts.length));
		case PartType.TYPE_LABEL:
			return parts[PartType.TYPE_INDEX];
		default:
			return "";
		}
	}
	
	private static boolean isValid(String[] parts) {
		return parts.length >= 3;
	}
	
	private static boolean isValid(String partType, String value) {
		switch (partType) {
		case PartType.MOMENT_LABEL:
			return value.equals("entry") || value.equals("exit");
		case PartType.NAME_LABEL:
		case PartType.TYPE_LABEL:
			return value.length() > 0;
		default:
			return false;
		}
	}
	
	private static String[] splitNameIntoParts(String name) {
		return name.split("_");
	}
	
	public TraceEvent(String name, String moment, long id, long timestamp) {
		this.name = name;
		this.id = id;
		
		increment(moment, timestamp);
	}
	
	public String getName() {
		return name;
	}
	
	public long getId() {
		return id;
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

