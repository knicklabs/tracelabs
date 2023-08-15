package tracelabs.models;

import java.util.Comparator;

public class TraceEventComparator implements Comparator<TraceEvent> {
	public static final int FIELD_ID = 0;
	public static final int FIELD_NAME = 1;
	public static final int FIELD_CALLS = 2;
	public static final int FIELD_DURATION = 3;
	
	private static final int ASCENDING = 0;
	private static final int DESCENDING = 1;
	
	private int field;
	
	private int direction;
	
	@Override
	public int compare(TraceEvent a, TraceEvent b) {
		int result = 0;
		
		switch (field) {
		case FIELD_ID:
			result = a.getId() > b.getId() ? 1 : -1;
			break;
		case FIELD_NAME:
			result = a.getName().compareTo(b.getName());
			break;
		case FIELD_CALLS:
			result = a.getNumCalls() > b.getNumCalls() ? 1 : -1;
			break;
		case FIELD_DURATION:
			result = a.getAverageDuration() > b.getAverageDuration() ? 1 : -1;
			break;
		}
		
		if (direction == DESCENDING) {
			result = -1 * result;
		}
		
		return result;
	}
	
	public void sortOn(int field) {
		if (field == this.field) {
			direction = direction == DESCENDING ? ASCENDING : DESCENDING;
		} else {
			this.field = field;
			direction = ASCENDING;
		}
	}
}
