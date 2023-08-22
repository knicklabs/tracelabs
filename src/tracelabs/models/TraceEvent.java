package tracelabs.models;

import java.util.ArrayList;
import java.util.List;

public class TraceEvent {
	/**
	 * An error thrown on an invalid observation.
	 */
	class InvalidObservation extends Exception {
		private static final long serialVersionUID = 1L;

		public InvalidObservation(String message) {
			super(message);
		}
	}
	
	/**
	 * An Observation of a TraceEvent.
	 */
	class Observation {
		private long timestamp;
		
		public Observation(long timestamp) {
			this.timestamp = timestamp;
		}
		
		public long getTimestamp() {
			return timestamp;
		}
	}
	
	/** The event (process or thread) id. */
	private long id;
	
	/** The event name. */
	private String name;
	
	/** The durations of each occurrence of this event. */
	private List<Long> durations = new ArrayList<Long>();
	
	/** Each observed entry of this event. */
	private List<Observation> entries = new ArrayList<Observation>();
	
	/** Each observed exit of this event. */
	private List<Observation> exits = new ArrayList<Observation>();
	
	/**
	 * Create a new instance of the TraceEvent.
	 * 
	 * @param id	The (process or thread) id of the event.
	 * @param name  The name of the event.
	 */
	public TraceEvent(long id, String name) {
		this.id = id;
		this.name = name;
	}
	
	/**
	 * Get the number of times this event was observed.
	 * 
	 * @return 	The number of times the event was observed.
	 */
	public int numObservations() {
		return durations.size();
	}
	
	/**
	 * Get the average duration of an observed instance of this event.
	 * 
	 * @return 	The average duration.
	 */
	public long averageDuration() {
		int count = durations.size();
		long duration = totalDuration();
		
		if (count == 0 || duration == 0) {
			return 0;
		}
		
		return duration / count;
	}
	
	/**
	 * Get the sum of each duration of each observed instance of this event.
	 * 
	 * @return	The total duration.
	 */
	public long totalDuration() {
		long result = 0;
		
		for (long duration : durations) {
			result += duration;
		}
		
		return result;
	}
	
	/**
	 * Get the last observed exit from this event.
	 * 
	 * @return	The last observed exit.
	 */
	public long lastEntry() {
		return this.entries.get(this.entries.size() - 1).getTimestamp();
	}
	
	/**
	 * Get the last observed entry from this event.
	 * 
	 * @return	The last observed entry.
	 */
	public long lastExit() {
		return this.exits.get(this.exits.size() - 1).getTimestamp();
	}
	
	/**
	 * Observe an entry of this event.
	 * 
	 * @param timestamp	The timestamp of entry.
	 */
	public void observeEntry(long timestamp) {
		entries.add(new Observation(timestamp));
	}
	
	/**
	 * Observe an exit of this event. This also computes a duration.
	 * 
	 * @param timestamp	The timestamp of exit.
	 * @throws InvalidObservation
	 */
	public void observeExit(long timestamp) throws InvalidObservation {
		// Occassionally we get an exit without an entry so we should guard against that.
		if (entries.size() - exits.size() == 1) {
			exits.add(new Observation(timestamp));
			durations.add(lastExit() - lastEntry());
		} else {
			throw new InvalidObservation("Unmatched exit observed for name: " + name + ", entries: " + entries.size() + ", exits: " + exits.size());
		}
	}
	
	/**
	 * Get the (process or thread) id of the event.
	 * 
	 * @return The event id.
	 */
	public long getId() {
		return id;
	}
	
	/**
	 * Get the name of the event.
	 * 
	 * @return The event name.
	 */
	public String getName() {
		return name;
	}
}

