package tracelabs.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import tracelabs.models.TraceEvent;
import tracelabs.models.TraceEventAggregate;
import tracelabs.models.TraceEventCollection;

public class EventTable {
	private static final int COLUMN_ID = 0;
	private static final int COLUMN_NAME = 1;
	private static final int COLUMN_CALLS = 2;
	private static final int COLUMN_AVERAGE_DURATION = 3;
	private static final int COLUMN_TOTAL_DURATION = 4;
	
	public class Row {
		private long id = -1;
		private String name = "";
		private int numCalls = 0;
		private long averageDuration = 0;
		private long totalDuration = 0;
		
		public long getId() {
			return id;
		}
		
		public void setId(long id) {
			this.id = id;
		}
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public int getNumCalls() {
			return numCalls;
		}
		
		public void setNumCalls(int numCalls) {
			this.numCalls = numCalls;
		}
		
		public long getAverageDuration() {
			return averageDuration;
		}
		
		public void setAverageDuration(long averageDuration) {
			this.averageDuration = averageDuration;
		}
		
		public long getTotalDuration() {
			return totalDuration;
		}
		
		public void setTotalDuration(long totalDuration) {
			this.totalDuration = totalDuration;
		}
	}
	
	public class RowComparator implements Comparator<Row> {		
		private static final int ASCENDING = 0;
		private static final int DESCENDING = 1;
		
		private int field;
		
		private int direction;
		
		@Override
		public int compare(Row a, Row b) {
			int result = 0;
			
			switch (field) {
			case COLUMN_ID:
				result = a.getId() > b.getId() ? 1 : -1;
				break;
			case COLUMN_NAME:
				result = a.getName().compareTo(b.getName());
				break;
			case COLUMN_CALLS:
				result = a.getNumCalls() > b.getNumCalls() ? 1 : -1;
				break;
			case COLUMN_AVERAGE_DURATION:
				result = a.getAverageDuration() > b.getAverageDuration() ? 1 : -1;
				break;
			case COLUMN_TOTAL_DURATION:
				result = a.getTotalDuration() > b.getTotalDuration() ? 1 : -1;
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
		
	private RowComparator comparator = new RowComparator();
	private List<Row> rows = new ArrayList<Row>();
	private TableViewer tableViewer;
	
	private boolean includeId = true;
	
	private class ColumnLabelProvider extends LabelProvider implements ITableLabelProvider {
		@Override
		public String getColumnText(Object data, int columnNumber) {
			Row row = (Row) data;
			
			int adjustedColumnNumber = row.getId() >= 0 ? columnNumber : columnNumber + 1;
			
			switch (adjustedColumnNumber) {
			case COLUMN_ID:
				return "" + row.getId();
			case COLUMN_NAME:
				return row.getName();
			case COLUMN_CALLS:
				return "" + row.getNumCalls();
			case COLUMN_AVERAGE_DURATION:
				return "" + row.getAverageDuration();
			case COLUMN_TOTAL_DURATION:
				return "" + row.getTotalDuration();
			default:
				return "";
			}
		}
		
		@Override
		public Image getColumnImage(Object row, int columnNumber) {
			return getImage(row);
		}
		
		@Override
		public Image getImage(Object row) {
			return null;
		}
	}
	
	private class ColumnContentProvider implements IStructuredContentProvider {
		@SuppressWarnings("unchecked")
		public Object[] getElements(Object input) {
			return ((List<TraceEvent>) input).toArray();	
		}
		
		public void dispose() {}
		
		public void inputChanged(Viewer viewer, Object previousInput, Object nextInput) {}
	}
	
	public EventTable(boolean includeId) {
		this.includeId = includeId;
	}
	
	public void createTable(Composite parent) {		
		tableViewer = new TableViewer(parent);
		tableViewer.setLabelProvider(new ColumnLabelProvider());
		tableViewer.setContentProvider(new ColumnContentProvider());
		
		Table table = tableViewer.getTable();
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		TableColumn column;
		
		if (this.includeId) {
			column = new TableColumn(table, SWT.LEFT);
			column.setText("ID");
			column.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					comparator.sortOn(COLUMN_ID);
					Collections.sort(rows, comparator);
					updateTable();
				}
			});	
		}
		
		column = new TableColumn(table, SWT.LEFT);
		column.setText("Name");
		column.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				comparator.sortOn(COLUMN_NAME);
				Collections.sort(rows, comparator);
				updateTable();
			}
		});
		
		column = new TableColumn(table, SWT.LEFT);
		column.setText("Calls");
		column.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				comparator.sortOn(COLUMN_CALLS);
				Collections.sort(rows, comparator);
				updateTable();
			}
		});
		
		column = new TableColumn(table, SWT.LEFT);
		column.setText("Average Duration");
		column.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				comparator.sortOn(COLUMN_AVERAGE_DURATION);
				Collections.sort(rows, comparator);
				updateTable();
			}
		});
		
		if (this.includeId) {
			column = new TableColumn(table, SWT.LEFT);
			column.setText("Total Duration");
			column.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					comparator.sortOn(COLUMN_TOTAL_DURATION);
					Collections.sort(rows, comparator);
					updateTable();
				}
			});	
		}
		
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
	}
	
	public void updateTable() {
		tableViewer.setInput(rows);
		
		Table table = tableViewer.getTable();
		for (int i = 0, n = table.getColumnCount(); i < n; i++) {
			table.getColumn(i).pack();
		}
		
		tableViewer.refresh();
	}
	
	public void updateTable(TraceEventCollection collection) {
		rows.clear();
		
		if (includeId) {
			eventsToRows(collection.getEvents());
		} else {
			collection.aggregate();
			aggregateEventsToRows(collection.getAggregateEvents());
		}
		
		updateTable();
	}
	
	private void eventsToRows(List<TraceEvent> events) {
		for (TraceEvent event : events) {
			Row row = new Row();
			row.setId(event.getId());
			row.setName(event.getName());
			row.setNumCalls(event.numObservations());
			row.setAverageDuration(event.averageDuration());
			row.setTotalDuration(event.totalDuration());
			rows.add(row);
		}
	}
	
	private void aggregateEventsToRows(List<TraceEventAggregate> events) {		
		for (TraceEventAggregate event : events) {
			Row row = new Row();
			row.setName(event.getName());
			row.setNumCalls(event.getNumObservations());
			row.setAverageDuration(event.getAverageDuration());
			row.setTotalDuration(event.getTotalDuration());
			rows.add(row);
		}
	}
}
