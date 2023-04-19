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

package org.eclipse.tractusx.traceability.assets.domain.service;

import org.eclipse.tractusx.traceability.assets.domain.model.Dashboard;
import org.eclipse.tractusx.traceability.assets.domain.ports.AssetRepository;
import org.eclipse.tractusx.traceability.common.security.JwtAuthentication;
import org.eclipse.tractusx.traceability.investigations.domain.ports.InvestigationsRepository;
import org.springframework.stereotype.Component;

@Component
public class DashboardService {

	private final AssetRepository assetRepository;
	private final InvestigationsRepository investigationsRepository;

	public DashboardService(AssetRepository assetRepository, InvestigationsRepository investigationsRepository) {
		this.assetRepository = assetRepository;
		this.investigationsRepository = investigationsRepository;
	}

	public Dashboard getDashboard(JwtAuthentication jwtAuthentication) {
		long totalAssets = assetRepository.countAssets();
		long myParts = assetRepository.countMyAssets();
		long pendingInvestigations = investigationsRepository.countPendingInvestigations();
		return new Dashboard(myParts, totalAssets - myParts, pendingInvestigations);
	}
}
