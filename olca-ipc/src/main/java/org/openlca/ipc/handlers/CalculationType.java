package org.openlca.ipc.handlers;

/**
 * The type of a calculation.
 *
 * TODO: we should move and use this in the core module.
 */
enum CalculationType {
	/**
	 * Calculates the total results for elementary flows, LCIA indicators,
	 * costs, etc. of a product system.
	 */
	SIMPLE_CALCULATION,

	/**
	 * Includes the total result vectors of a simple calculation but calculates
	 * also the direct contributions of each process (or better process product
	 * in case of multi-output processes) to these total results.
	 */
	CONTRIBUTION_ANALYSIS,

	/**
	 * Extends the contribution analysis by providing also the upstream results
	 * of each process (process product) in the product system. The upstream
	 * result contains the direct contributions of the respective process but
	 * also the result of the supply chain up to this process scaled to the
	 * demand of the process in the product system.
	 */
	UPSTREAM_ANALYSIS,

	/**
	 * A regionalized calculation is a contribution analysis but with an LCIA
	 * method that supports regionalized characterization factors (via region
	 * specific parameters in formulas) and a product system with processes that
	 * have geographic information assigned (point, line, or polygon shapes).
	 */
	REGIONALIZED_CALCULATION,

	/**
	 * A Monte Carlo simulation generates for each run, of a given number of a
	 * given number of iterations, random values according to the uncertainty
	 * distributions of process inputs/outputs, parameters, characterization
	 * factors, etc. of a product system and then performs a simple calculation
	 * for that specific run.
	 */
	MONTE_CARLO_SIMULATION
}
