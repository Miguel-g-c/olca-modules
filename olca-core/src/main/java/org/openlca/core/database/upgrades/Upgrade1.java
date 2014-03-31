package org.openlca.core.database.upgrades;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.derby.DerbyDatabase;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

class Upgrade1 implements IUpgrade {

	private UpgradeUtil util;
	private IDatabase database;

	@Override
	public int[] getInitialVersions() {
		return new int[] { 1, 2 };
	}

	@Override
	public int getEndVersion() {
		return 3;
	}

	@Override
	public void exec(IDatabase database) throws Exception {
		this.database = database;
		this.util = new UpgradeUtil(database);
		createNwSetTable();
		createNwFactorTable();
		util.checkCreateColumn("tbl_sources", "external_file",
				"external_file VARCHAR(255)");
		util.checkCreateColumn("tbl_parameters", "external_source",
				"external_source VARCHAR(255)");
		util.checkCreateColumn("tbl_parameters", "source_type",
				"source_type VARCHAR(255)");
		util.checkCreateColumn("tbl_impact_factors", "formula",
				"formula VARCHAR(1000)");
		util.checkCreateColumn("tbl_processes", "kmz",
				"kmz " + util.getBlobType());
		updateParameterRedefs();
		updateMappingTable();
		addVersionColumns();
	}

	private void updateMappingTable() throws Exception {
		util.dropTable("tbl_mappings");
		String tableDef;
		if (database instanceof DerbyDatabase) {
			//@formatter:off
			tableDef = "CREATE TABLE tbl_mapping_files ("
					  	+ "id BIGINT NOT NULL, "
					  	+ "file_name VARCHAR(255), "
					  	+ "content BLOB(16 M), "
					  	+ "PRIMARY KEY (id))";
			//@formatter:on
		} else {
			//@formatter:off
			tableDef = "CREATE TABLE tbl_mapping_files ("
					  	+ "id BIGINT NOT NULL, "
					  	+ "file_name VARCHAR(255), "
					  	+ "content MEDIUMBLOB, "
					  	+ "PRIMARY KEY (id))";
			//@formatter:on
		}
		util.checkCreateTable("tbl_mapping_files", tableDef);
	}

	private void createNwSetTable() throws Exception {
		// @formatter:off
		String tableDef = "CREATE TABLE tbl_nw_sets (" 
				+ "id BIGINT NOT NULL, "
				+ "ref_id VARCHAR(36), " 
				+ "description " + util.getTextType() + ", " 
				+ "name VARCHAR(255), "
				+ "reference_system VARCHAR(255), "
				+ "f_impact_method BIGINT,  "
				+ "weighted_score_unit VARCHAR(255), " 
				+ "PRIMARY KEY (id))";
		// @formatter:on
		util.checkCreateTable("tbl_nw_sets", tableDef);
		copyNwSetTable();
		util.dropTable("tbl_normalisation_weighting_sets");
	}

	private void copyNwSetTable() throws Exception {
		if (!util.tableExists("tbl_normalisation_weighting_sets"))
			return;
		String query = "select * from tbl_normalisation_weighting_sets";
		NativeSql.on(database).query(query, new NativeSql.QueryResultHandler() {
			@Override
			public boolean nextResult(ResultSet result) throws SQLException {
				copyNwSet(result);
				return true;
			}
		});
	}

	private void copyNwSet(final ResultSet result) throws SQLException {
		String stmt = "insert into tbl_nw_sets (id, ref_id, name, "
				+ "f_impact_method, weighted_score_unit) values (?, ?, ?, ?, ?)";
		NativeSql.on(database).batchInsert(stmt, 1,
				new NativeSql.BatchInsertHandler() {
					@Override
					public boolean addBatch(int i, PreparedStatement stmt)
							throws SQLException {
						stmt.setLong(1, result.getLong("id"));
						stmt.setString(2, UUID.randomUUID().toString());
						stmt.setString(3, result.getString("reference_system"));
						stmt.setLong(4, result.getLong("f_impact_method"));
						stmt.setString(5, result.getString("unit"));
						return true;
					}
				});
	}

	private void createNwFactorTable() throws Exception {
		String tableDef = "CREATE TABLE tbl_nw_factors ("
				+ " id BIGINT NOT NULL," + " weighting_factor DOUBLE,"
				+ " normalisation_factor DOUBLE,"
				+ " f_impact_category BIGINT," + " f_nw_set BIGINT,"
				+ " PRIMARY KEY (id))";
		util.checkCreateTable("tbl_nw_factors", tableDef);
		copyNwFactorTable();
		util.dropTable("tbl_normalisation_weighting_factors");
	}

	private void copyNwFactorTable() throws Exception {
		if (!util.tableExists("tbl_normalisation_weighting_factors"))
			return;
		String query = "select * from tbl_normalisation_weighting_factors";
		NativeSql.on(database).query(query, new NativeSql.QueryResultHandler() {
			@Override
			public boolean nextResult(ResultSet result) throws SQLException {
				copyNwFactor(result);
				return true;
			}
		});
	}

	private void copyNwFactor(final ResultSet result) throws SQLException {
		String stmt = "insert into tbl_nw_factors(id, weighting_factor, "
				+ "normalisation_factor, f_impact_category, f_nw_set) "
				+ "values (?, ?, ?, ?, ?)";
		NativeSql.on(database).batchInsert(stmt, 1,
				new NativeSql.BatchInsertHandler() {
					@Override
					public boolean addBatch(int i, PreparedStatement stmt)
							throws SQLException {
						prepareFactorRecord(result, stmt);
						return true;
					}
				});
	}

	private void prepareFactorRecord(final ResultSet result,
			PreparedStatement stmt) throws SQLException {
		stmt.setLong(1, result.getLong("id"));
		double wf = result.getDouble("weighting_factor");
		if (result.wasNull())
			stmt.setNull(2, Types.DOUBLE);
		else
			stmt.setDouble(2, wf);
		double nf = result.getDouble("normalisation_factor");
		if (result.wasNull())
			stmt.setNull(3, Types.DOUBLE);
		else
			stmt.setDouble(3, nf);
		stmt.setLong(4, result.getLong("f_impact_category"));
		stmt.setLong(5, result.getLong("f_normalisation_weighting_set"));
	}

	/**
	 * Changes in parameter redefinitions: It is now allowed to redefine process
	 * and LCIA method parameters.
	 */
	private void updateParameterRedefs() throws Exception {
		util.renameColumn("tbl_parameter_redefs", "f_process", "f_context",
				"BIGINT");
		if (!util.columnExists("tbl_parameter_redefs", "context_type")) {
			util.checkCreateColumn("tbl_parameter_redefs", "context_type",
					"context_type VARCHAR(255)");
			NativeSql.on(database).runUpdate(
					"update tbl_parameter_redefs "
							+ "set context_type = 'PROCESS' "
							+ "where f_context is not null");
		}
	}

	private void addVersionColumns() throws Exception {
		String[] tables = { "tbl_actors", "tbl_sources",  "tbl_unit_groups",
				"tbl_flow_properties", "tbl_flows", "tbl_processes",
				"tbl_product_systems", "tbl_impact_methods", "tbl_projects" };
		for (String table : tables) {
			util.checkCreateColumn(table, "version", "version BIGINT");
			util.checkCreateColumn(table, "last_change", "last_change BIGINT");
		}
	}

}
