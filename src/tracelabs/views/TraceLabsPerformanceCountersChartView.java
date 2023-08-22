package tracelabs.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtchart.Chart;
import org.eclipse.swtchart.ICircularSeries;
import org.eclipse.swtchart.ISeries.SeriesType;
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
import tracelabs.ui.PerformanceCountersTable;

public class TraceLabsPerformanceCountersChartView extends TmfView {
	public static final String VIEW_ID = "tracelabs.views.TraceLabsPerformanceCountersView";
	
	private ITmfTrace currentTrace;
	
	private TraceEventCollection collection = new TraceEventCollection();
	private PerformanceCountersTable table;
	
	private Chart chart;
	
	public TraceLabsPerformanceCountersChartView() {
		super(VIEW_ID);
	}
	
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		
		chart = new Chart(parent, SWT.NONE);
		chart.getTitle().setText("Performance Counters");
		
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
				
				// Update UI in the UI thread.
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						collection.aggregate();
						List<TraceEventAggregate> events = collection.getAggregateEvents();
						TraceEventAggregate event = events.get(events.size() - 1);
						Map<String, Long> performanceCounters = event.getPerformanceCounters();
						List<String> labels = new ArrayList<String>();
						List<Double> values = new ArrayList<Double>();
						
						for (Map.Entry<String, Long> entry : performanceCounters.entrySet()) {
							labels.add(entry.getKey());
							values.add(entry.getValue().doubleValue());
						}
						ICircularSeries<?> circularSeries = (ICircularSeries<?>)chart.getSeriesSet().createSeries(SeriesType.PIE, "counters");
						String[] labelArray = (String[]) labels.toArray();
						double[] valueArray = new double[values.size()];
						
						for (int i = 0; i < values.size(); i++) {
							valueArray[i] = (double) values.get(i);
						}
						
						circularSeries.setSeries(labelArray, valueArray);				
						
						chart.redraw();
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
