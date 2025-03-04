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

package org.eclipse.tractusx.traceability.assets.infrastructure.adapters.feign.irs.model;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

record SerialPartTypization(
	String catenaXId,
	PartTypeInformation partTypeInformation,
	ManufacturingInformation manufacturingInformation,
	List<LocalId> localIdentifiers
) {
	SerialPartTypization(String catenaXId, PartTypeInformation partTypeInformation, ManufacturingInformation manufacturingInformation, List<LocalId> localIdentifiers) {
		this.catenaXId = catenaXId;
		this.partTypeInformation = partTypeInformation;
		this.manufacturingInformation = manufacturingInformation;
		this.localIdentifiers = Objects.requireNonNullElse(localIdentifiers, Collections.emptyList());
	}

	public Optional<String> getLocalId(LocalIdKey key) {
		return localIdentifiers.stream()
			.filter(localId -> localId.key() == key)
			.findFirst()
			.map(LocalId::value);
	}
}

record ManufacturingInformation(
	String country,
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ss", timezone = "CET") Date date
) {}

record PartTypeInformation(
	String nameAtManufacturer,
	String nameAtCustomer,
	String manufacturerPartId,
	String customerPartId
) {}

record LocalId(
	@JsonProperty("key") LocalIdKey key,
	String value
) {}

enum LocalIdKey {
	@JsonProperty("manufacturerId") MANUFACTURER_ID,
    @JsonProperty("manufacturerPartId") MANUFACTURER_PART_ID,
	@JsonProperty("partInstanceId") PART_INSTANCE_ID,
	@JsonProperty("batchId") BATCH_ID,
	@JsonEnumDefaultValue UNKNOWN,
	@JsonProperty("van") VAN,
}
