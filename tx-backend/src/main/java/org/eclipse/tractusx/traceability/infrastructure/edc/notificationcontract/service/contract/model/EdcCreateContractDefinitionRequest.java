/********************************************************************************
 * Copyright (c) 2022, 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 * Copyright (c) 2022, 2023 ZF Friedrichshafen AG
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.traceability.infrastructure.edc.notificationcontract.service.contract.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class EdcCreateContractDefinitionRequest {

	@JsonProperty("id")
	private final String id;

	@JsonProperty("accessPolicyId")
	private final String accessPolicyId;

	@JsonProperty("contractPolicyId")
	private final String contractPolicyId;

	@JsonProperty("criteria")
	private final List<EdcContractDefinitionCriteria> criteria;

	public EdcCreateContractDefinitionRequest(String id,
											  String accessPolicyId,
											  String contractPolicyId,
											  List<EdcContractDefinitionCriteria> criteria) {
		this.id = id;
		this.accessPolicyId = accessPolicyId;
		this.contractPolicyId = contractPolicyId;
		this.criteria = criteria;
	}

	public String getId() {
		return id;
	}

	public String getAccessPolicyId() {
		return accessPolicyId;
	}

	public String getContractPolicyId() {
		return contractPolicyId;
	}

	public List<EdcContractDefinitionCriteria> getCriteria() {
		return criteria;
	}
}
