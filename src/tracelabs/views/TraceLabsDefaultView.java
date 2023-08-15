package tracelabs.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;
import org.eclipse.ui.IWorkbench;

import tracelabs.events.SysCallEvent;

public class TraceLabsDefaultView extends TmfView {
	public static final String VIEW_ID = "tracelabs.views.TraceLabsDefaultView";
	
	private ITmfTrace currentTrace;
	
	private List<SysCallEvent> sysCallEvents = new ArrayList<SysCallEvent>();
	
	private EventTable table;
	
	@Inject IWorkbench workbench;
	
	class EventTable {
		private static final int COLUMN_NAME = 0;
		private static final int COLUMN_CALLS = 1;
		private static final int COLUMN_DURATION = 2;
		
		private TableViewer tableViewer;
		
		private class ColumnLabelProvider extends LabelProvider implements ITableLabelProvider {
			@Override
			public String getColumnText(Object row, int columnNumber) {
				SysCallEvent event = (SysCallEvent) row;
				
				switch (columnNumber) {
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
				return ((List<SysCallEvent>) input).toArray();	
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
			
			TableColumn column = new TableColumn(table, SWT.LEFT);
			column.setText("Event Name");
			
			column = new TableColumn(table, SWT.LEFT);
			column.setText("Number of Calls");
			
			column = new TableColumn(table, SWT.LEFT);
			column.setText("Average Duration");
			
			table.setHeaderVisible(true);
			table.setLinesVisible(true);
		}
		
		@SuppressWarnings("unused")
		private void updateTable(List<SysCallEvent> events) {		
			tableViewer.setInput(events);
			
			Table table = tableViewer.getTable();
			for (int i = 0, n = table.getColumnCount(); i < n; i++) {
				table.getColumn(i).pack();
			}
			
			tableViewer.refresh();
		}
		
	}
	
	
	public TraceLabsDefaultView() {
		super(VIEW_ID);
	}
	
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		
		table = new EventTable();
		
		table.createTable(parent);
		table.updateTable(sysCallEvents);
		
		TmfTraceManager traceManager = TmfTraceManager.getInstance();
        ITmfTrace trace = traceManager.getActiveTrace();
        
        if (trace != null) {
        	traceSelected(new TmfTraceSelectedSignal(this, trace));
        }
	}
	
	@Override
	public void setFocus() {
		// do nothing yet...
	}
	
	@TmfSignalHandler
	public void traceSelected(final TmfTraceSelectedSignal signal) {		
		// Don't populate the view again if we're already showing this trace
		if (currentTrace == signal.getTrace()) {
			return;
		}
		
		sysCallEvents.clear();
		
		currentTrace = signal.getTrace();
		
		TmfEventRequest req = new TmfEventRequest(TmfEvent.class,
                TmfTimeRange.ETERNITY, 0, ITmfEventRequest.ALL_DATA,
                ITmfEventRequest.ExecutionType.BACKGROUND) {
			
			@Override
			public void handleData(ITmfEvent data) {
				
				// Called for each event
				super.handleData(data);
				
				String name = data.getName();
				Long tid = data.getContent().getFieldValue(Long.class, "context.cpu_id");
				Long timestamp = data.getTimestamp().toNanos();
				
				SysCallEvent.process(sysCallEvents, name, tid, timestamp);
			}
			
			@Override
			public void handleSuccess() {				
				// Request successful, not more data available
				super.handleSuccess();
				
				// Update UI in the UI thread.
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						table.updateTable(sysCallEvents);
					}
				});
			}
			
			@Override
			public void handleFailure() {
				// Request failed, not more data available
				super.handleFailure();
			}
		};
		
		currentTrace.sendRequest(req);
	}
}
