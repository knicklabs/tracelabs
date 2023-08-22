package tracelabs.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class PerformanceCountersTable {
	private static final int COLUMN_NAME = 0;
	private static final int COLUMN_COUNT = 1;
	
	public class Row {
		private long count = 0;
		private String name = "";
		
		public long getCount() {
			return count;
		}
		
		public void setCount(long count) {
			this.count = count;
		}
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
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
			case COLUMN_NAME:
				result = a.getName().compareTo(b.getName());
				break;
			case COLUMN_COUNT:
				result = a.getCount() > b.getCount() ? 1 : -1;
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
	
	private class ColumnContentProvider implements IStructuredContentProvider {
		@SuppressWarnings("unchecked")
		public Object[] getElements(Object input) {
			return ((List<TraceEvent>) input).toArray();	
		}
		
		public void dispose() {}
		
		public void inputChanged(Viewer viewer, Object previousInput, Object nextInput) {}
	}
	
	private class ColumnLabelProvider extends LabelProvider implements ITableLabelProvider {
		@Override
		public String getColumnText(Object data, int columnNumber) {
			Row row = (Row) data;
			
			switch (columnNumber) {
			case COLUMN_NAME:
				return row.getName();
			case COLUMN_COUNT:
				return "" + row.getCount();
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
	
	private RowComparator comparator = new RowComparator();
	private List<Row> rows = new ArrayList<Row>();
	private TableViewer tableViewer;
	private Table table;
	
	public void createTable(Composite parent) {
		tableViewer = new TableViewer(parent);
		tableViewer.setLabelProvider(new ColumnLabelProvider());
		tableViewer.setContentProvider(new ColumnContentProvider());
		
		table = tableViewer.getTable();
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		TableColumn column;
		
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
		column.setText("Count");
		column.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				comparator.sortOn(COLUMN_COUNT);
				Collections.sort(rows, comparator);
				updateTable();
			}
		});
		
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
		collection.aggregate();
		
		List<TraceEventAggregate> aggregateEvents = collection.getAggregateEvents();
		TraceEventAggregate aggregateEvent = aggregateEvents.get(aggregateEvents.size() - 1);
		Map<String, Long> performanceCounters = aggregateEvent.getPerformanceCounters();
		
		for (Map.Entry<String, Long> entry : performanceCounters.entrySet()) {
			Row row = new Row();
			row.setName(entry.getKey());
			row.setCount(entry.getValue());
			rows.add(row);
		}
	
		updateTable();
	}
}
