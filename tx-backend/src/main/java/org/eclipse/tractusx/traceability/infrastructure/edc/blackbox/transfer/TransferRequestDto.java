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

package org.eclipse.tractusx.traceability.infrastructure.edc.blackbox.transfer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@JsonDeserialize(builder = TransferRequestDto.Builder.class)
public class TransferRequestDto {

	@NotNull
	private String connectorAddress;
	@NotNull
	private String id;
	@NotNull
	private String contractId;
	@NotNull
	private DataAddress dataDestination;
	private boolean managedResources = true;
	private Map<String, String> properties = new HashMap<>();
	@NotNull
	private TransferType transferType = new TransferType();
	@NotNull
	private String protocol = "ids-multipart";
	@NotNull
	private String connectorId;
	@NotNull
	private String assetId;

	public String getConnectorAddress() {
		return connectorAddress;
	}

	public String getId() {
		return id;
	}

	public String getContractId() {
		return contractId;
	}

	public DataAddress getDataDestination() {
		return dataDestination;
	}

	public boolean isManagedResources() {
		return managedResources;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public TransferType getTransferType() {
		return transferType;
	}

	public String getProtocol() {
		return protocol;
	}

	public String getConnectorId() {
		return connectorId;
	}

	public String getAssetId() {
		return assetId;
	}

	@JsonPOJOBuilder(withPrefix = "")
	public static final class Builder {
		private final TransferRequestDto request;

		private Builder() {
			request = new TransferRequestDto();
		}

		@JsonCreator
		public static Builder newInstance() {
			return new Builder();
		}

		public Builder connectorAddress(String connectorAddress) {
			request.connectorAddress = connectorAddress;
			return this;
		}

		public Builder id(String id) {
			request.id = id;
			return this;
		}

		public Builder contractId(String contractId) {
			request.contractId = contractId;
			return this;
		}

		public Builder dataDestination(DataAddress dataDestination) {
			request.dataDestination = dataDestination;
			return this;
		}

		public Builder managedResources(boolean managedResources) {
			request.managedResources = managedResources;
			return this;
		}

		public Builder properties(Map<String, String> properties) {
			request.properties = properties;
			return this;
		}

		public Builder transferType(TransferType transferType) {
			request.transferType = transferType;
			return this;
		}

		public Builder protocol(String protocol) {
			request.protocol = protocol;
			return this;
		}

		public Builder connectorId(String connectorId) {
			request.connectorId = connectorId;
			return this;
		}

		public Builder assetId(String assetId) {
			request.assetId = assetId;
			return this;
		}

		public TransferRequestDto build() {
			return request;
		}
	}
}
