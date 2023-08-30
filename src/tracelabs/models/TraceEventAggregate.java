package tracelabs.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TraceEventAggregate {
	private String name;
	private List<TraceEvent> events;
	private int numObservations = 0;
	private long averageDuration = 0;
	private long totalDuration = 0;
	
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
	
	public String getName() {
		return name;
	}
	
	public int getNumObservations() {
		return numObservations;
	}
	
	public long getAverageDuration() {
		return averageDuration;
	}
	
	public long getTotalDuration() {
		return totalDuration;
	}
	
	public Map<String, Long> getPerformanceCounters() {
		TraceEvent event = events.get(events.size() - 1);
		Map<String, Long> performanceCounters = event.lastPerformanceCountersOnExit();
		return performanceCounters;
	}
}
