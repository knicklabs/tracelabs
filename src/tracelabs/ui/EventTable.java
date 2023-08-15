package tracelabs.ui;

import java.util.Collections;
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
import tracelabs.models.TraceEventComparator;

public class EventTable {
	private static final int COLUMN_ID = 0;
	private static final int COLUMN_NAME = 1;
	private static final int COLUMN_CALLS = 2;
	private static final int COLUMN_DURATION = 3;
		
	private TraceEventComparator comparator;
	private List<TraceEvent> events;
	private TableViewer tableViewer;
	
	private boolean includeId = true;
	
	private class ColumnLabelProvider extends LabelProvider implements ITableLabelProvider {
		@Override
		public String getColumnText(Object row, int columnNumber) {
			TraceEvent event = (TraceEvent) row;
			
			int adjustedColumnNumber = includeId ? columnNumber : columnNumber + 1;
			
			switch (adjustedColumnNumber) {
			case COLUMN_ID:
				return "" + event.getId();
			case COLUMN_NAME:
				return event.getName();
			case COLUMN_CALLS:
				return "" + event.getNumCalls();
			case COLUMN_DURATION:
				return "" + event.getAverageDuration();
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
	
	public EventTable(List<TraceEvent> events) {
		this.comparator = new TraceEventComparator();
		this.events = events;
	}
	
	public EventTable(List<TraceEvent> events, boolean includeId) {
		this.comparator = new TraceEventComparator();
		this.events = events;
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
					comparator.sortOn(TraceEventComparator.FIELD_ID);
					Collections.sort(events, comparator);
					updateTable();
				}
			});	
		}
		
		column = new TableColumn(table, SWT.LEFT);
		column.setText("Name");
		column.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				comparator.sortOn(TraceEventComparator.FIELD_NAME);
				Collections.sort(events, comparator);
				updateTable();
			}
		});
		
		column = new TableColumn(table, SWT.LEFT);
		column.setText("Calls");
		column.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				comparator.sortOn(TraceEventComparator.FIELD_CALLS);
				Collections.sort(events, comparator);
				updateTable();
			}
		});
		
		column = new TableColumn(table, SWT.LEFT);
		column.setText("Average Duration");
		column.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				comparator.sortOn(TraceEventComparator.FIELD_DURATION);
				Collections.sort(events, comparator);
				updateTable();
			}
		});
		
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
	}
	
	public void updateTable() {		
		tableViewer.setInput(events);
		
		Table table = tableViewer.getTable();
		for (int i = 0, n = table.getColumnCount(); i < n; i++) {
			table.getColumn(i).pack();
		}
		
		tableViewer.refresh();
	}	
}
