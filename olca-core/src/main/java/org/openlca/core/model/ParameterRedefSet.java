package org.openlca.core.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * An instance of this class is just a set of parameter redefinitions with a
 * name and an optional description that is attached to a product system.
 */
@Entity
@Table(name = "tbl_parameter_redef_sets")
public class ParameterRedefSet extends AbstractEntity
	implements Copyable<ParameterRedefSet> {

	@Column(name = "name")
	public String name;

	@Column(name = "description")
	public String description;

	@Column(name = "is_baseline")
	public boolean isBaseline;

	@JoinColumn(name = "f_owner")
	@OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
	public final List<ParameterRedef> parameters = new ArrayList<>();

	@Override
	public ParameterRedefSet copy() {
		ParameterRedefSet clone = new ParameterRedefSet();
		clone.name = name;
		clone.description = description;
		clone.isBaseline = isBaseline;
		for (ParameterRedef p : parameters) {
			if (p == null)
				continue;
			clone.parameters.add(p.copy());
		}
		return clone;
	}
}
