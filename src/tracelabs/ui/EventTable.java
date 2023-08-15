package tracelabs.ui;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import tracelabs.models.TraceEvent;

public class EventTable {
	private static final int COLUMN_ID = 0;
	private static final int COLUMN_NAME = 1;
	private static final int COLUMN_CALLS = 2;
	private static final int COLUMN_DURATION = 3;
	
	private TableViewer tableViewer;
	
	private class ColumnLabelProvider extends LabelProvider implements ITableLabelProvider {
		@Override
		public String getColumnText(Object row, int columnNumber) {
			TraceEvent event = (TraceEvent) row;
			
			switch (columnNumber) {
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
	
	public void createTable(Composite parent) {
		tableViewer = new TableViewer(parent);
		tableViewer.setLabelProvider(new ColumnLabelProvider());
		tableViewer.setContentProvider(new ColumnContentProvider());
		
		Table table = tableViewer.getTable();
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		TableColumn column;
		
		column = new TableColumn(table, SWT.LEFT);
		column.setText("ID");
		
		column = new TableColumn(table, SWT.LEFT);
		column.setText("Name");
		
		column = new TableColumn(table, SWT.LEFT);
		column.setText("Calls");
		
		column = new TableColumn(table, SWT.LEFT);
		column.setText("Average Duration");
		
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
	}
	
	public void updateTable(List<TraceEvent> events) {		
		tableViewer.setInput(events);
		
		Table table = tableViewer.getTable();
		for (int i = 0, n = table.getColumnCount(); i < n; i++) {
			table.getColumn(i).pack();
		}
		
		tableViewer.refresh();
	}	
}
