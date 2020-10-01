package examples;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.database.mysql.MySQLDatabase;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.DataStructures;
import org.openlca.core.math.LcaCalculator;
import org.openlca.core.matrix.IndexFlow;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.results.FullResult;
import org.openlca.julia.Julia;
import org.openlca.julia.JuliaSolver;

public class Benchmark {

	public static void main(String[] args) {
		if (Julia.load())
			return;
		var solver = new JuliaSolver();

		int runs = 1;
		// IDatabase db = new
		// MySQLDatabase("jdbc:mysql://localhost:3306/openlca",
		// "root", "");
		IDatabase db = new MySQLDatabase(
				"jdbc:mysql://localhost:3306/openlca_ei3_pre", "root", "");
		ProductSystem system = new ProductSystemDao(db).getForId(654886);
		CalculationSetup setup = new CalculationSetup(system);
		setup.allocationMethod = AllocationMethod.USE_DEFAULT;
		MatrixData data = DataStructures.matrixData(db, setup);
		LcaCalculator calculator = new LcaCalculator(solver, data);

		System.out.println("Inventory ready. Type enter to start!");
		try {
			InputStreamReader r = new InputStreamReader(System.in);
			BufferedReader reader = new BufferedReader(r);
			reader.readLine();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Run new benchmark");
		System.out.println("run \t t(quick)[ms] \t t(analyse)[ms] \t mem[MB]");
		FullResult result = null;
		for (int run = 1; run <= runs; run++) {
			System.gc();
			long start = System.currentTimeMillis();
			calculator.calculateSimple();
			long quick = System.currentTimeMillis() - start;
			System.gc();
			start = System.currentTimeMillis();
			calculator.calculateFull();
			long analysis = System.currentTimeMillis() - start;
			Runtime r = Runtime.getRuntime();
			double mem = (r.totalMemory() - r.freeMemory()) / (1024 * 1024);
			System.out.printf("%d \t %d \t %d \t %.2f \n", run, quick,
					analysis, mem);
		}

		System.out.println("done");
		System.out.println("\nResults:\n");
		System.out.println("flowId \t result");
		for (IndexFlow f : result.getFlows()) {
			System.out.printf("%s \t %.10f \n", f.flow.name,
					result.getTotalFlowResult(f));
		}
	}
}
