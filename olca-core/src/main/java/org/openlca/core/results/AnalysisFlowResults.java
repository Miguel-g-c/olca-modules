package org.openlca.core.results;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.indices.FlowIndex;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

/**
 * A helper class that generates instances of {@link AnalysisFlowResult} from an
 * analysis result.
 */
public final class AnalysisFlowResults {

	private AnalysisFlowResults() {
	}

	public List<AnalysisFlowResult> getForFlow(AnalysisResult result,
			FlowDescriptor flow, List<ProcessDescriptor> processes) {
		List<AnalysisFlowResult> results = new ArrayList<>();
		for (ProcessDescriptor process : processes) {
			AnalysisFlowResult r = getResult(result, process, flow);
			results.add(r);
		}
		return results;
	}

	public List<AnalysisFlowResult> getInputResults(AnalysisResult result,
			ProcessDescriptor process, List<FlowDescriptor> flows) {
		return getProcessResults(result, process, flows, true);
	}

	public List<AnalysisFlowResult> getOutputResults(AnalysisResult result,
			ProcessDescriptor process, List<FlowDescriptor> flows) {
		return getProcessResults(result, process, flows, false);
	}

	private List<AnalysisFlowResult> getProcessResults(AnalysisResult result,
			ProcessDescriptor process, List<FlowDescriptor> flows,
			boolean inputs) {
		List<AnalysisFlowResult> results = new ArrayList<>();
		FlowIndex index = result.getFlowIndex();
		for (FlowDescriptor flow : flows) {
			if (index.isInput(flow.getId()) == inputs) {
				AnalysisFlowResult r = getResult(result, process, flow);
				results.add(r);
			}
		}
		return results;
	}

	public static AnalysisFlowResult getResult(AnalysisResult result,
			ProcessDescriptor process, FlowDescriptor flow) {
		long flowId = flow.getId();
		long processId = process.getId();
		double single = result.getSingleFlowResult(processId, flowId);
		double total = result.getTotalFlowResult(processId, flowId);
		AnalysisFlowResult r = new AnalysisFlowResult();
		r.setFlow(flow);
		r.setProcess(process);
		r.setSingleResult(single);
		r.setTotalResult(total);
		return r;
	}

}
