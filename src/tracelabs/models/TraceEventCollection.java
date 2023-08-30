package tracelabs.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.tracecompass.analysis.os.linux.core.event.aspect.LinuxTidAspect;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

import tracelabs.models.TraceEvent.InvalidObservation;
import tracelabs.models.TraceEvent.PartType;

public class TraceEventCollection {
	class PartType {
		public static final String TYPE_LABEL = "type";		
		public static final String MOMENT_LABEL = "moment";
		public static final String NAME_LABEL = "name";
		
		public static final int TYPE_INDEX = 0;
		public static final int MOMENT_INDEX = 1;
		public static final int NAME_INDEX = 2;
	}

	
	protected static String get(String[] parts, String partType) {
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
	
	protected static boolean isValid(String[] parts) {
		return parts.length >= 3;
	}
	
	protected static boolean isValid(String partType, String value) {
		switch (partType) {
		case PartType.MOMENT_LABEL:
			return value.equals("entry") || value.equals("exit");
		case PartType.NAME_LABEL:
			return value.length() > 0;
		case PartType.TYPE_LABEL:
			return value.equals("syscall");
		default:
			return false;
		}
	}
	
	protected static String[] splitNameIntoParts(String name) {
		return name.split("_");
	}
	
	private List<TraceEvent> events = new ArrayList<TraceEvent>();
	private List<TraceEventAggregate> aggregateEvents = new ArrayList<TraceEventAggregate>();
	private PerformanceCounters performanceCounters = new PerformanceCounters();
	
	public void aggregate() {		
		List<String> names = events
				.stream()
				.map(e -> e.getName())
				.distinct()
				.collect(Collectors.toList());
		
		for (String name : names) {
			List<TraceEvent> matchingEvents = events
					.stream()
					.filter(e -> e.getName().equals(name))
					.collect(Collectors.toList());
			aggregateEvents.add(new TraceEventAggregate(name, matchingEvents));
		}
	}
	
	public List<TraceEventAggregate> getAggregateEvents() {
		return aggregateEvents;
	}
	
	public List<TraceEvent> getEvents() {
		return events;
	}
	
	public PerformanceCounters getPerformanceCounters() {
		return performanceCounters;
	}
	
	public void process(ITmfEvent rawEvent) throws InvalidObservation {		
		String[] parts = splitNameIntoParts(rawEvent.getName());
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
		if (!isValid(PartType.NAME_LABEL, namePart));
				
		
		Long tid = Long.valueOf(TmfTraceUtils.resolveIntEventAspectOfClassForEvent(rawEvent.getTrace(), LinuxTidAspect.class, rawEvent));
		Long timestamp = rawEvent.getTimestamp().toNanos();
		
		TraceEvent event = events
				.stream()
				.filter(e -> e.getName().equals(namePart) && e.getId() == tid)
				.findAny()
				.orElse(null);
		
		boolean shouldInsert = event == null;
		
		
		if (event == null) {
			event = new TraceEvent(tid, namePart);
		}
		
		Collection<String> fields = rawEvent.getContent().getFieldNames();
		List<String> performanceCounterFields = fields.stream().filter(f -> f.startsWith("context._perf_cpu_")).collect(Collectors.toList());
		
		Map<String, Long> performanceCounters = new HashMap<String, Long>();
		
		for (String field : performanceCounterFields) {
			performanceCounters.put(field.split("context._perf_cpu_")[1], rawEvent.getContent().getFieldValue(Long.class, field));
		}
		
		if (momentPart.equals("entry")) {
			event.observeEntry(timestamp, performanceCounters);
			
		} else if (momentPart.equals("exit")) {
			event.observeExit(timestamp, performanceCounters);
		}
		
		this.performanceCounters.update(timestamp, performanceCounters);
				
		if (shouldInsert) {
			events.add(event);
		}
	}
	
	public void reset() {
		aggregateEvents.clear();
		events.clear();
	}
}
