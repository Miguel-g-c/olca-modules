package examples;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.database.Derby;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.math.Simulator;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.core.results.SimpleResult;
import org.openlca.julia.Julia;
import org.openlca.julia.JuliaSolver;

public class SimulatorExample {

	public static void main(String[] args) {
		var db = Derby.fromDataDir("ecoinvent_2_2_unit");
		var system = db.get(ProductSystem.class,
			"53f9b9db-139f-4617-bf2b-8fc715b3cd16");
		var setup = CalculationSetup.monteCarlo(system, 200);
		setup.impactMethod = db.get(ImpactMethod.class,
			"207ffac9-aaa8-401d-ac90-874defd3751a");
		ImpactDescriptor gwp = null;
		for (var i : setup.impactMethod.impactCategories) {
			if (i.name.equals("Climate change - GWP100")) {
				gwp = Descriptor.of(i);
				break;
			}
		}
		System.out.println("Tacking results of " + gwp.name);

		String juliaLibPath = "C:/Users/ms/Projects/openLCA/eclipse";
		Julia.loadFromDir(new File(juliaLibPath));
		JuliaSolver solver = new JuliaSolver();

		Simulator simulator = Simulator.create(setup, db, solver);

		double min = 0;
		double max = 0;
		long start = System.currentTimeMillis();
		for (int i = 0; i < 200; i++) {
			SimpleResult r = simulator.nextRun();
			double val = r.getTotalImpactResult(gwp);
			if (i == 0) {
				min = val;
				max = val;
			} else {
				min = Math.min(min, val);
				max = Math.max(max, val);
			}
			if ((i + 1) % 20 == 0) {
				double t = (System.currentTimeMillis() - start) / 1000.0;
				System.out.printf(
						"after %d iterations min=%.2f max=%.2f t=%.2f\n",
						i + 1, min, max, t);
			}
		}
		System.out.println("all done");
	}

}
