package tracelabs.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PerformanceCounters {
	private class Entry {
		private Long timestamp;
		private Map<String, Long> counters;
		
		public Entry(Long timestamp, Map<String, Long> counters) {
			this.timestamp = timestamp;
			this.counters = counters;
		}
		
		public Long getTimestamp() {
			return timestamp;
		}
		
		public Map<String, Long> getCounters() {
			return counters;
		}
	}
	
	List<Entry> entries = new ArrayList<Entry>();
	
	public void update(Long timestamp, Map<String, Long> counters) {
		if (entries.size() == 0) {
			entries.add(new Entry(timestamp, counters));	
		}
		
		Entry lastEntry = entries.get(entries.size() - 1);
		Map<String, Long> lastCounters = lastEntry.getCounters();
		
		for (Map.Entry<String, Long> lastCounter : lastCounters.entrySet()) {
			String key = lastCounter.getKey();
			Long value = lastCounter.getValue();
			
			Long nextValue = counters.get(key);
			if (nextValue == null) {
				// Do nothing
			} else {
				counters.replace(key, value + nextValue);
			}
		}
		
		entries.add(new Entry(timestamp, counters));
	}
	
	public int getNumCounters() {
		if (entries.size() == 0) {
			return 0;
		}
		
		Entry entry = entries.get(0);
		return entry.getCounters().values().size();
	}
	
	public List<String> getCounters() {
		List<String> results = new ArrayList<String>();
		
		if (entries.size() == 0) {
			return results;
		}
		
		Map<String, Long> counters = entries.get(0).getCounters();
		
		for (Map.Entry<String, Long> entry : counters.entrySet()) {
			results.add(entry.getKey());
		}
		
		return results;
	}
	
	public double[] getTimestampSeries() {
		List<Double> values = new ArrayList<Double>();
		
		for (Entry entry : entries) {
			values.add(entry.getTimestamp().doubleValue());
		}
		
		return listToArray(values);
	}
	
	public double[] getCounterSeries(String counter) {
		if (entries.size() == 0) {
			return new double[] {};
		}
		
		List<Double> values = new ArrayList<Double>();
		
		for (Entry entry : entries) {
			values.add(entry.getCounters().get(counter).doubleValue());
		}
		
		return listToArray(values);
	}
	
	private double[] listToArray(List<Double> values) {
		double[] results = new double[values.size()];
		
		for (int i = 0; i < values.size(); i++) {
			results[i] = (double) values.get(i);
		}
		
		return results;
	}
}
