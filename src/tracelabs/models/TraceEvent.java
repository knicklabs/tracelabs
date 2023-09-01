package tracelabs.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Models a trace event.
 */
public class TraceEvent {
	/**
	 * An error thrown on an invalid observation.
	 */
	public class InvalidObservation extends Exception {
		private static final long serialVersionUID = 1L;

		public InvalidObservation(String message) {
			super(message);
		}
	}
	
	private class Observation {
		private Map<String, Long> performanceCounters;
		private long timestamp;
		
		public Observation(long timestamp, Map<String, Long> performanceCounters) {
			this.performanceCounters = performanceCounters;
			this.timestamp = timestamp;
		}
		
		public Map<String, Long> getPerformanceCounters() {
			return performanceCounters;
		}
		
		public long getTimestamp() {
			return timestamp;
		}
	}
	
	private long id;
	private String name;
	private List<Long> durations = new ArrayList<Long>();
	private List<Observation> entries = new ArrayList<Observation>();
	private List<Observation> exits = new ArrayList<Observation>();
	
	/**
	 * Create a new instance of the TraceEvent.
	 * @param id
	 * @param name
	 */
	public TraceEvent(long id, String name) {
		this.id = id;
		this.name = name;
	}
	
	/**
	 * Get the number of times this event was observed.
	 * @return
	 */
	public int numObservations() {
		return durations.size();
	}
	
	/**
	 * Get the average duration of an observed instance of this event.
	 * @return
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
	 * @return
	 */
	public long totalDuration() {
		long result = 0;
		
		for (long duration : durations) {
			result += duration;
		}
		
		return result;
	}
	
	/**
	 * Get the last observed entry from this event.
	 * @return
	 */
	public long lastEntry() {
		return this.entries.get(this.entries.size() - 1).getTimestamp();
	}
	
	/**
	 * Get the last observed exit from this event.
	 * @return
	 */
	public long lastExit() {
		return this.exits.get(this.exits.size() - 1).getTimestamp();
	}
	
	/**
	 * Get the last observed performance counters on entry from this event.
	 * @return
	 */
	public Map<String, Long> lastPerformanceCountersOnEntry() {
		return this.entries.get(this.entries.size() - 1).getPerformanceCounters();
	}
	
	/**
	 * Get the last observed performance counters on exit from this event.
	 * @return
	 */
	public Map<String, Long> lastPerformanceCountersOnExit() {
		return this.exits.get(this.exits.size() - 1).getPerformanceCounters();
	}
	
	/**
	 * Observe an entry of this event.
	 * @param timestamp
	 * @param performanceCounters
	 */
	public void observeEntry(long timestamp, Map<String, Long> performanceCounters) {
		entries.add(new Observation(timestamp, performanceCounters));
	}
	
	/**
	 * Observe an exit of this event. This also computes a duration.
	 * @param timestamp
	 * @param performanceCounters
	 * @throws InvalidObservation
	 */
	public void observeExit(long timestamp, Map<String, Long> performanceCounters) throws InvalidObservation {
		// Occasionally we get an exit without an entry so we should guard against that.
		// This seams to always happen on first occurrence, i.e. entries = 0, exits = 1, so
		// likely this is because an entry happened before the trace started.
		if (entries.size() - exits.size() != 1) {
			throw new InvalidObservation("Unmatched exit (count) observed for id: " + id + ", name: " + name + ", entries: " + entries.size() + ", exits: " + (exits.size() + 1));
		}
		
		if (timestamp < lastEntry()) {
			throw new InvalidObservation("Unmatched exit (timestamp) observed for id: " + id + ", name: " + name + ", entry: " + lastEntry() + ", exit: " + timestamp);
		}
		
		exits.add(new Observation(timestamp, performanceCounters));
		durations.add(lastExit() - lastEntry());
	}
	
	/**
	 * Get the thread id of the event.
	 * @return
	 */
	public long getId() {
		return id;
	}
	
	/**
	 * Get the name of the event.
	 * @return
	 */
	public String getName() {
		return name;
	}
}

