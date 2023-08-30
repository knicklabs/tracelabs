package tracelabs.views;

import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtchart.Chart;
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

import tracelabs.models.TraceEventAggregate;
import tracelabs.models.TraceEventCollection;

public class PerformanceCountersGrowthView extends TmfView {
	public static final String VIEW_ID = "tracelabs.views.PerformanceCountersGrowthView";
	
	private ITmfTrace currentTrace;
	
	private TraceEventCollection collection = new TraceEventCollection();
	
	private Chart chart;
	private Composite pane;
	
	public PerformanceCountersGrowthView() {
		super(VIEW_ID);
	}
	
	@Override
	public void createPartControl(Composite parent) {
		pane = parent;
		
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
		
		collection.reset();
		
		currentTrace = signal.getTrace();
		
		TmfEventRequest req = new TmfEventRequest(TmfEvent.class,
				TmfTimeRange.ETERNITY, 0, ITmfEventRequest.ALL_DATA,
				ITmfEventRequest.ExecutionType.BACKGROUND) {
			
			@Override
			public void handleData(ITmfEvent data) {
				// Called for each event
				super.handleData(data);
				
				try {
					collection.process(data);
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}
			
			@Override
			public void handleSuccess() {
				// Request successful, no more data available
				super.handleSuccess();
				
				// Update UI In the UI thread.
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						collection.aggregate();
						List<TraceEventAggregate> events = collection.getAggregateEvents();
					}
				});
			}
		};
	}
}
