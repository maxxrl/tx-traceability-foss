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

package org.eclipse.tractusx.traceability.assets.infrastructure.rest;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.apache.groovy.util.Maps;
import org.eclipse.tractusx.traceability.infrastructure.edc.blackbox.Constants;
import org.eclipse.tractusx.traceability.infrastructure.edc.blackbox.asset.Asset;
import org.eclipse.tractusx.traceability.infrastructure.edc.blackbox.cache.EndpointDataReference;
import org.eclipse.tractusx.traceability.infrastructure.edc.blackbox.cache.InMemoryEndpointDataReferenceCache;
import org.eclipse.tractusx.traceability.infrastructure.edc.blackbox.catalog.Catalog;
import org.eclipse.tractusx.traceability.infrastructure.edc.blackbox.model.EDCNotification;
import org.eclipse.tractusx.traceability.infrastructure.edc.blackbox.notification.ContractNegotiationDto;
import org.eclipse.tractusx.traceability.infrastructure.edc.blackbox.notification.NegotiationInitiateRequestDto;
import org.eclipse.tractusx.traceability.infrastructure.edc.blackbox.notification.TransferId;
import org.eclipse.tractusx.traceability.infrastructure.edc.blackbox.offer.ContractOffer;
import org.eclipse.tractusx.traceability.infrastructure.edc.blackbox.policy.Policy;
import org.eclipse.tractusx.traceability.infrastructure.edc.blackbox.transfer.TransferRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Hidden;

import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@Hidden
@RequestMapping("/edc/")
public class EDCReceiverController {

	private static final Logger logger = LoggerFactory.getLogger(EDCReceiverController.class);

	private final TestRestTemplate restTemplate;

	private final InMemoryEndpointDataReferenceCache endpointDataReferenceCache;

	@Value("${server.port}")
	private int port;

	public EDCReceiverController(TestRestTemplate restTemplate, InMemoryEndpointDataReferenceCache endpointDataReferenceCache) {
		this.restTemplate = restTemplate;
		this.endpointDataReferenceCache = endpointDataReferenceCache;
	}

	@GetMapping("/api/v1/management/catalog")
	public Catalog getDataCatalog(@RequestParam String providerUrl) {
		logger.info("Returning data catalog for provider {}", providerUrl);
		return Catalog.Builder.newInstance()
			.id("contract-id")
			.contractOffers(List.of(ContractOffer.Builder.newInstance()
				.id("contract-id")
				.asset(Asset.Builder.newInstance()
					.id("asset-id")
					.property(Constants.ASSET_KEY_NOTIFICATION_TYPE, Constants.ASSET_VALUE_QUALITY_INVESTIGATION)
					.build()
				).policy(Policy.Builder.newInstance()
					.build()
				).build())
			).build();
	}

	@PostMapping("/api/v1/management/contractnegotiations")
	public TransferId contractNegotiations(@RequestBody NegotiationInitiateRequestDto request) {
		logger.info("Initializing contract negotiations");
		return TransferId.Builder.newInstance()
			.id("transfer-id-1")
			.build();
	}

	@PostMapping("/api/v1/management/transferprocess")
	public TransferId transferProcess(@RequestBody TransferRequestDto transferRequest) {
		logger.info("Processing transfer");
		return TransferId.Builder.newInstance()
			.id("transfer-id-2")
			.build();
	}

	@PostMapping("/receive-notification")
	public void receiveNotification(@RequestBody EDCNotification notification) {
		logger.info("Notification received");
	}

	@PostMapping("/callback")
	public void receiveCallback(@RequestBody EndpointDataReference dataReference) {
		logger.info("Callback received");
	}

	@GetMapping("/api/v1/management/contractnegotiations/{transferId}")
	public ContractNegotiationDto getContractNegotiations(@PathVariable String transferId) {
		logger.info("Returning contract negotiations");
		String contractAgreementId = "contract-agreement-id";

		endpointDataReferenceCache.storeAgreementId(contractAgreementId);

		restTemplate.postForEntity("/callback/endpoint-data-reference",
			EndpointDataReference.Builder.newInstance()
				.authCode(JWT.create()
					.withExpiresAt(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)))
					.sign(Algorithm.HMAC256("test-token")))
				.authKey("auth-key")
				.endpoint("http://localhost:" + port + "/api/edc/receive-notification")
				.properties(Maps.of("cid", contractAgreementId))
				.build(),
			Void.class
		);

		return ContractNegotiationDto.Builder.newInstance()
			.contractAgreementId(contractAgreementId)
			.state("CONFIRMED")
			.build();
	}

}
