package org.openlca.core.math;

import org.openlca.core.matrix.CostVector;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.matrix.solvers.IMatrixSolver;
import org.openlca.core.results.ContributionResult;
import org.openlca.core.results.FullResult;
import org.openlca.core.results.SimpleResult;

public class LcaCalculator {

	private final IMatrixSolver solver;
	private final MatrixData data;

	public LcaCalculator(IMatrixSolver solver, MatrixData data) {
		this.solver = solver;
		this.data = data;
	}

	public SimpleResult calculateSimple() {

		SimpleResult result = new SimpleResult();
		result.flowIndex = data.enviIndex;
		result.productIndex = data.techIndex;

		IMatrix techMatrix = data.techMatrix;
		TechIndex productIndex = data.techIndex;
		int idx = productIndex.getIndex(productIndex.getRefFlow());
		double[] s = solver.solve(techMatrix, idx, productIndex.getDemand());
		result.scalingFactors = s;
		result.totalRequirements = getTotalRequirements(techMatrix, s);
		IMatrix enviMatrix = data.enviMatrix;

		result.totalFlowResults = solver.multiply(enviMatrix, s);

		if (data.impactMatrix != null) {
			addTotalImpacts(result);
		}

		if (data.costVector != null) {
			result.hasCostResults = true;
			addTotalCosts(result, s);
		}

		return result;
	}

	public ContributionResult calculateContributions() {

		ContributionResult result = new ContributionResult();
		result.flowIndex = data.enviIndex;
		result.productIndex = data.techIndex;

		IMatrix techMatrix = data.techMatrix;
		TechIndex productIndex = data.techIndex;
		int idx = productIndex.getIndex(productIndex.getRefFlow());
		double[] s = solver.solve(techMatrix, idx, productIndex.getDemand());
		result.scalingFactors = s;
		result.totalRequirements = getTotalRequirements(techMatrix, s);

		IMatrix enviMatrix = data.enviMatrix;
		IMatrix singleResult = enviMatrix.copy();
		solver.scaleColumns(singleResult, s);
		result.singleFlowResults = singleResult;
		result.totalFlowResults = solver.multiply(enviMatrix, s);

		if (data.impactMatrix != null) {
			addTotalImpacts(result);
			addDirectImpacts(result);
		}

		if (data.costVector != null) {
			result.hasCostResults = true;
			addTotalCosts(result, s);
			addDirectCosts(result, s);
		}
		return result;
	}

	public FullResult calculateFull() {

		FullResult result = new FullResult();
		result.flowIndex = data.enviIndex;
		result.productIndex = data.techIndex;

		TechIndex productIdx = data.techIndex;
		IMatrix techMatrix = data.techMatrix;
		IMatrix enviMatrix = data.enviMatrix;
		IMatrix inverse = solver.invert(techMatrix);
		double[] scalingVector = getScalingVector(inverse, productIdx);
		result.scalingFactors = scalingVector;

		// direct results
		result.techMatrix = techMatrix.copy();
		solver.scaleColumns(result.techMatrix, scalingVector);
		result.singleFlowResults = enviMatrix.copy();
		solver.scaleColumns(result.singleFlowResults, scalingVector);
		result.totalRequirements = getTotalRequirements(techMatrix,
				scalingVector);

		// upstream results
		double[] demands = getRealDemands(result.totalRequirements, productIdx);
		IMatrix totalResult = solver.multiply(enviMatrix, inverse);
		if (data.costVector == null) {
			inverse = null; // allow GC
		}
		solver.scaleColumns(totalResult, demands);
		result.upstreamFlowResults = totalResult;
		int refIdx = productIdx.getIndex(productIdx.getRefFlow());
		result.totalFlowResults = totalResult.getColumn(refIdx);

		if (data.impactMatrix != null) {
			addDirectImpacts(result);
			IMatrix factors = data.impactMatrix;
			IMatrix totalImpactResult = solver.multiply(factors, totalResult);
			result.upstreamImpactResults = totalImpactResult;
			// total impacts = upstream result of reference product
			result.impactIndex = data.impactIndex;
			result.totalImpactResults = totalImpactResult.getColumn(refIdx);
		}

		if (data.costVector != null) {
			result.hasCostResults = true;
			addDirectCosts(result, scalingVector);
			IMatrix costValues = CostVector.asMatrix(solver, data.costVector);
			IMatrix upstreamCosts = solver.multiply(costValues, inverse);
			solver.scaleColumns(upstreamCosts, demands);
			result.totalCostResult = upstreamCosts.get(0, refIdx);
			result.upstreamCostResults = upstreamCosts;
		}

		return result;

	}

	/**
	 * Calculates the scaling vector for the reference product i from the given
	 * inverse of the technology matrix:
	 * 
	 * s = d[i] .* Inverse[:, i]
	 * 
	 * where d is the demand vector and.
	 * 
	 */
	public double[] getScalingVector(IMatrix inverse, TechIndex productIdx) {
		LongPair refProduct = productIdx.getRefFlow();
		int idx = productIdx.getIndex(refProduct);
		double[] s = inverse.getColumn(idx);
		double demand = productIdx.getDemand();
		for (int i = 0; i < s.length; i++)
			s[i] *= demand;
		return s;
	}

	/**
	 * Calculates the total requirements of the respective product amounts to
	 * fulfill the demand of the product system:
	 * 
	 * tr = s .* diag(A)
	 * 
	 * where s is the scaling vector and A the technology matrix.
	 * 
	 */
	public double[] getTotalRequirements(IMatrix techMatrix,
			double[] scalingVector) {
		double[] tr = new double[scalingVector.length];
		for (int i = 0; i < scalingVector.length; i++) {
			tr[i] = scalingVector[i] * techMatrix.get(i, i);
		}
		return tr;
	}

	/**
	 * Calculate the real demand vector for the analysis.
	 */
	public double[] getRealDemands(double[] totalRequirements,
			TechIndex productIdx) {
		double refDemand = productIdx.getDemand();
		int i = productIdx.getIndex(productIdx.getRefFlow());
		double[] rd = new double[totalRequirements.length];
		if (Math.abs(totalRequirements[i] - refDemand) > 1e-9) {
			// 'self-loop' correction for total result scale
			double f = refDemand / totalRequirements[i];
			for (int k = 0; k < totalRequirements.length; k++)
				rd[k] = f * totalRequirements[k];
		} else {
			int length = totalRequirements.length;
			System.arraycopy(totalRequirements, 0, rd, 0, length);
		}
		return rd;
	}

	private void addTotalImpacts(SimpleResult result) {
		result.impactIndex = data.impactIndex;
		IMatrix factors = data.impactMatrix;
		double[] totals = solver.multiply(factors, result.totalFlowResults);
		result.totalImpactResults = totals;
	}

	private void addDirectImpacts(ContributionResult result) {
		IMatrix factors = data.impactMatrix;
		result.impactFactors = factors;
		IMatrix directResults = solver.multiply(factors,
				result.singleFlowResults);
		result.singleImpactResults = directResults;
		IMatrix singleFlowImpacts = factors.copy();
		solver.scaleColumns(singleFlowImpacts, result.totalFlowResults);
		result.singleFlowImpacts = singleFlowImpacts;
	}

	private void addTotalCosts(SimpleResult result, double[] scalingVector) {
		double[] costValues = data.costVector;
		double total = 0;
		for (int i = 0; i < scalingVector.length; i++) {
			total += scalingVector[i] * costValues[i];
		}
		result.totalCostResult = total;
	}

	private void addDirectCosts(ContributionResult result,
			double[] scalingVector) {
		double[] costValues = data.costVector;
		double[] directCosts = new double[costValues.length];
		for (int i = 0; i < scalingVector.length; i++) {
			directCosts[i] = costValues[i] * scalingVector[i];
		}
		result.singleCostResults = directCosts;
	}

}
