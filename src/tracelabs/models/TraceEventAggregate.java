package tracelabs.models;

import java.util.List;

public class TraceEventAggregate {
	private String name;
	private int numObservations = 0;
	private long averageDuration = 0;
	private long totalDuration = 0;
	
	public TraceEventAggregate(String name, List<TraceEvent> events) {
		this.name = name;
		
		if (events.size() == 0) {
			return;
		}
		
		for (TraceEvent event : events) {
			averageDuration += event.averageDuration();
			numObservations += event.numObservations();
			totalDuration += event.totalDuration();
		}
		
		averageDuration = averageDuration / events.size();
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
}
