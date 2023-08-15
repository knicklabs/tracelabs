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


/*
public class TraceLabsDefaultView extends TmfView {
	public static final String ID = "tracelabs.views.TraceLabsDefaultView";

	@Inject IWorkbench workbench;
	
	// TABLE
	private TableViewer viewer;
	private Action action1;
	private Action action2;
	private Action doubleClickAction;
	
	// TRACE
	private ITmfTrace currentTrace;
	
	// TRACE EVENTS
	private List<SysCallEvent> sysCallEvents = new ArrayList<SysCallEvent>();
	
	public TraceLabsDefaultView() {
		super(ID);
	}
	 

	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		@Override
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}
		@Override
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}
		@Override
		public Image getImage(Object obj) {
			return workbench.getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setInput(new String[] { "Loading..." });
		viewer.setLabelProvider(new ViewLabelProvider());

		// Create the help context id for the viewer's control
		workbench.getHelpSystem().setHelp(viewer.getControl(), "TraceLabs.viewer");
		getSite().setSelectionProvider(viewer);
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		
		TmfTraceManager traceManager = TmfTraceManager.getInstance();
        ITmfTrace trace = traceManager.getActiveTrace();
        if (trace != null) {
            traceSelected(new TmfTraceSelectedSignal(this, trace));
        }
	}
	
	@TmfSignalHandler
	public void traceSelected(final TmfTraceSelectedSignal signal) {
		// Don't populate the view again if we're already showing the trace
		if (currentTrace == signal.getTrace()) {
			return;
		}
		
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
				
				for (SysCallEvent event : sysCallEvents) {
					System.out.println(event.toString());
				}
			}
			
			@Override
			public void handleSuccess() {
				// Request successful, no more data available.
				super.handleSuccess();
				
				for (SysCallEvent event : sysCallEvents) {
					System.out.println(event.toString());
				}
				
				// Update UI in the UI thread.
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						updateTable();
					}
				});
			}
			
			@Override
			public void handleFailure() {
				// Request failed, no more data available.
				super.handleFailure();
			}
		};
		
		ITmfTrace trace = signal.getTrace();
		trace.sendRequest(req);
				
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				TraceLabsDefaultView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		manager.add(action2);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(action2);
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				showMessage("Action 1 executed");
			}
		};
		action1.setText("Action 1");
		action1.setToolTipText("Action 1 tooltip");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		
		action2 = new Action() {
			public void run() {
				showMessage("Action 2 executed");
			}
		};
		action2.setText("Action 2");
		action2.setToolTipText("Action 2 tooltip");
		action2.setImageDescriptor(workbench.getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		doubleClickAction = new Action() {
			public void run() {
				IStructuredSelection selection = viewer.getStructuredSelection();
				Object obj = selection.getFirstElement();
				showMessage("Double-click detected on "+obj.toString());
			}
		};
	}
	
	private void updateTable() {
		String sysCallEventLogs[] = sysCallEvents.stream().map(
			event -> event.toString()
		).toArray(String[]::new);
		
		viewer.setInput(sysCallEventLogs);
	
		for (String log : sysCallEventLogs) {
			System.out.println(log);
		}
		
		viewer.refresh();
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"TraceLabs Default View",
			message);
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}
*/