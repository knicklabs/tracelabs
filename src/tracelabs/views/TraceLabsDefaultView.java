package tracelabs.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
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

import tracelabs.models.TraceEvent;
import tracelabs.ui.EventTable;

public class TraceLabsDefaultView extends TmfView {
	public static final String VIEW_ID = "tracelabs.views.TraceLabsDefaultView";
	
	private ITmfTrace currentTrace;
	
	private List<TraceEvent> events = new ArrayList<TraceEvent>();
	
	private EventTable table;
	
	public TraceLabsDefaultView() {
		super(VIEW_ID);
	}
	
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		
		table = new EventTable(events);
		
		table.createTable(parent);
		table.updateTable();
		
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
		
		events.clear();
		
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
				
				TraceEvent.process_type(events, "syscall", name, tid, timestamp);
			}
			
			@Override
			public void handleSuccess() {				
				// Request successful, not more data available
				super.handleSuccess();
				
				// Update UI in the UI thread.
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						table.updateTable();
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
