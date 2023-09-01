package tracelabs.models;

import java.util.List;
import java.util.Map;

/**
 * Models an aggregate of trace events across thread ids.
 */
public class TraceEventAggregate {
	private String name;
	private List<TraceEvent> events;
	private int numObservations = 0;
	private long averageDuration = 0;
	private long totalDuration = 0;
	
	/**
	 * Create an aggregate of several trace events of the same name captured
	 * on different threads.
	 * @param name
	 * @param events
	 */
	public TraceEventAggregate(String name, List<TraceEvent> events) {
		this.name = name;
		this.events = events;
		
		if (events.size() == 0) {
			return;
		}
		
		for (TraceEvent event : events) {
			numObservations += event.numObservations();
			totalDuration += event.totalDuration();
		}
		
		if (numObservations == 0) {
			return;
		}
		
		averageDuration = totalDuration / numObservations;
	}
	
	/**
	 * Get the name of the event.
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Get the number of times the event was called.
	 * @return
	 */
	public int getNumObservations() {
		return numObservations;
	}
	
	/**
	 * Get the average duration of the event.
	 * @return
	 */
	public long getAverageDuration() {
		return averageDuration;
	}
	
	/**
	 * Get the total duration of the event.
	 * @return
	 */
	public long getTotalDuration() {
		return totalDuration;
	}
	
	/**
	 * Get the performance counters captured on the event.
	 * @return
	 */
	public Map<String, Long> getPerformanceCounters() {
		TraceEvent event = events.get(events.size() - 1);
		Map<String, Long> performanceCounters = event.lastPerformanceCountersOnExit();
		return performanceCounters;
	}
}
