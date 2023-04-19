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

package org.eclipse.tractusx.traceability.assets.infrastructure.adapters.rest.assets;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.eclipse.tractusx.traceability.assets.application.RegistryFacade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Registry")
@RequestMapping(path = "/registry", produces = "application/json")
public class RegistryController {

	private final RegistryFacade registryFacade;

	public RegistryController(RegistryFacade registryFacade) {
		this.registryFacade = registryFacade;
	}

	@Operation(operationId = "reload",
		summary = "Triggers reload of shell descriptors",
		tags = {"Registry"},
		description = "The endpoint Triggers reload of shell descriptors.",
		security = @SecurityRequirement(name = "oAuth2", scopes = "profile email"))
	@ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Created."),
		@ApiResponse(responseCode = "401", description = "Authorization failed."),
		@ApiResponse(responseCode = "403", description = "Forbidden.")})
	@GetMapping("/reload")
	public void reload() {
		registryFacade.loadShellDescriptors();
	}
}
