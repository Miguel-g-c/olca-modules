/*******************************************************************************
 * Copyright (c) 2007 - 2013 GreenDeltaTC. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Mozilla
 * Public License v1.1 which accompanies this distribution, and is available at
 * http://www.openlca.org/uploads/media/MPL-1.1.html
 * 
 * Contributors: GreenDeltaTC - initial API and implementation
 * www.greendeltatc.com tel.: +49 30 4849 6030 mail: gdtc@greendeltatc.com
 ******************************************************************************/
package org.openlca.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * A process link is a link between a providing process and a receiving process.
 * The link is realized with a providing exchange and a receiving exchange,
 * which must have the same flow
 */
@Entity
@Table(name = "tbl_process_links")
public class ProcessLink extends AbstractEntity implements Cloneable {

	@Column(name = "f_flow")
	private long flowId;

	@Column(name = "f_provider")
	private long providerId;

	@Column(name = "f_recipient")
	private long recipientId;

	@Override
	public ProcessLink clone() {
		ProcessLink clone = new ProcessLink();
		clone.setFlowId(getFlowId());
		clone.setProviderId(getProviderId());
		clone.setRecipientId(getRecipientId());
		return clone;
	}

	public long getFlowId() {
		return flowId;
	}

	public void setFlowId(long flowId) {
		this.flowId = flowId;
	}

	public long getProviderId() {
		return providerId;
	}

	public void setProviderId(long providerId) {
		this.providerId = providerId;
	}

	public long getRecipientId() {
		return recipientId;
	}

	public void setRecipientId(long recipientId) {
		this.recipientId = recipientId;
	}

}
