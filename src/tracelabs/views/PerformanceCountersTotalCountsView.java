package tracelabs.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtchart.Chart;
import org.eclipse.swtchart.IBarSeries;
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

/**
 * A view that shows performance counter counts in graphical format.
 */
public class PerformanceCountersTotalCountsView extends TmfView {
	public static final String VIEW_ID = "tracelabs.views.PerformanceCountersTotalCountsView";
	
	private ITmfTrace currentTrace;
	
	private TraceEventCollection collection = new TraceEventCollection();
	
	private Chart chart;
	private Composite pane;
	
	public PerformanceCountersTotalCountsView() {
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
						
						String[] labelArray = labels.toArray(new String[0]);
						double[] valueArray = new double[values.size()];
						
						for (int i = 0; i < values.size(); i++) {
							valueArray[i] = (double) values.get(i);
						}
						
						chart = new Chart(pane, SWT.NONE);
						chart.getTitle().setText("Performance Counters");
						chart.getAxisSet().getXAxis(0).getTitle().setText("Performance Counters");
						chart.getAxisSet().getYAxis(0).getTitle().setText("Count");
						
						chart.getAxisSet().getXAxis(0).enableCategory(true);
						chart.getAxisSet().getXAxis(0).setCategorySeries(new String[]{});
						
						chart.getAxisSet().getXAxis(0).enableCategory(true);
						chart.getAxisSet().getXAxis(0).setCategorySeries(labelArray);
						
						IBarSeries<?> barSeries1 = (IBarSeries<?>) chart.getSeriesSet().createSeries(SeriesType.BAR, "totals");
						barSeries1.setYSeries(valueArray);
						
						chart.getAxisSet().adjustRange();
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
