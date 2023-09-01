package tracelabs.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * PerformanceCounters is a collection of entries where each entry has
 * the performance counters recorded at a specific timestamp.
 */
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
	
	private List<Entry> entries = new ArrayList<Entry>();
	
	/**
	 * Update the collection of entries by adding the performance
	 * counters recorded at a specific timestamp.
	 * @param timestamp
	 * @param counters
	 */
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
	
	/**
	 * Get the number of performance counter entries in the collection.
	 * @return
	 */
	public int getNumCounters() {
		if (entries.size() == 0) {
			return 0;
		}
		
		Entry entry = entries.get(0);
		return entry.getCounters().values().size();
	}
	
	/**
	 * Get the names of the performance counters captured in the collection.
	 * @return
	 */
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
	
	/**
	 * Get the timestamps that the performance counters were captured at as
	 * a series compatible with SWT charts.
	 * @return
	 */
	public double[] getTimestampSeries() {
		List<Double> values = new ArrayList<Double>();
		
		for (Entry entry : entries) {
			values.add(entry.getTimestamp().doubleValue());
		}
		
		return listToArray(values);
	}
	
	/**
	 * Get the captured counts for a type of performance counter by name
	 * as a series compatible with SWT charts.
	 * @param counter
	 * @return
	 */
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
