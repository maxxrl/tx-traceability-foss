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

package org.eclipse.tractusx.traceability.common.support

import org.glassfish.grizzly.http.util.HttpStatus
import org.springframework.http.HttpHeaders

import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp
import static com.xebialabs.restito.builder.verify.VerifyHttp.verifyHttp
import static com.xebialabs.restito.semantics.Action.header
import static com.xebialabs.restito.semantics.Action.ok
import static com.xebialabs.restito.semantics.Action.status
import static com.xebialabs.restito.semantics.Condition.get
import static com.xebialabs.restito.semantics.Condition.post
import static com.xebialabs.restito.semantics.Condition.startsWithUri
import static com.xebialabs.restito.semantics.Condition.withHeader
import static com.xebialabs.restito.semantics.Condition.withPostBodyContaining

trait IrsApiSupport implements RestitoProvider {

	void irsApiTriggerJob() {
		whenHttp(stubServer()).match(
			post("/irs/jobs"),
			withHeader(HttpHeaders.AUTHORIZATION)
		).then(
			ok(),
			header("Content-Type", "application/json"),
			jsonResponseFromFile("./stubs/irs/post/jobs/response_200.json")
		)
	}

	void irsApiTriggerJobFailed() {
		whenHttp(stubServer()).match(
			post("/irs/jobs"),
			withHeader(HttpHeaders.AUTHORIZATION)
		).then(
			status(HttpStatus.INTERNAL_SERVER_ERROR_500),
			header("Content-Type", "application/json")
		)
	}

	void irsApiReturnsJobDetailsWithDuplicatedCatenaXId() {
		whenHttp(stubServer()).match(
			get("/irs/jobs/ebb79c45-7bba-4169-bf17-3e719989ab54"),
			withHeader(HttpHeaders.AUTHORIZATION)
		)
			.then(
				ok(),
				header("Content-Type", "application/json"),
				jsonResponseFromFile("./stubs/irs/get/jobs/id/response_200_duplicated_ids.json")
			)
	}

	void irsApiReturnsJobDetails() {
		whenHttp(stubServer()).match(
			get("/irs/jobs/ebb79c45-7bba-4169-bf17-3e719989ab54"),
			withHeader(HttpHeaders.AUTHORIZATION)
		)
			.then(
				ok(),
				header("Content-Type", "application/json"),
				jsonResponseFromFile("./stubs/irs/get/jobs/id/response_200.json")
			)
	}

	void irsApiReturnsJobDetailsWithNoBPNs() {
		whenHttp(stubServer()).match(
			get("/irs/jobs/ebb79c45-7bba-4169-bf17-3e719989ab54"),
			withHeader(HttpHeaders.AUTHORIZATION)
		)
			.then(
				ok(),
				header("Content-Type", "application/json"),
				jsonResponseFromFile("./stubs/irs/get/jobs/id/response_no_bpns_200.json")
			)
	}

	void irsApiReturnsJobInRunningState() {
		whenHttp(stubServer()).match(
			get("/irs/jobs/ebb79c45-7bba-4169-bf17-3e719989ab54"),
			withHeader(HttpHeaders.AUTHORIZATION)
		)
			.then(
				ok(),
				header("Content-Type", "application/json"),
				jsonResponseFromFile("./stubs/irs/get/jobs/id/running_job_response_200.json")
			)
	}

	void irsApiReturnsJobInRunningAndCompleted() {
		whenHttp(stubServer()).match(
			get("/irs/jobs/ebb79c45-7bba-4169-bf17-3e719989ab54"),
			withHeader(HttpHeaders.AUTHORIZATION)
		)
			.then(
				ok(),
				header("Content-Type", "application/json")
			).withSequence(
			jsonResponseFromFile("./stubs/irs/get/jobs/id/running_job_response_200.json"),
			jsonResponseFromFile("./stubs/irs/get/jobs/id/response_200.json")
		)
	}

	void irsJobDetailsApiFailed() {
		whenHttp(stubServer()).match(
			get("/irs/jobs/ebb79c45-7bba-4169-bf17-3e719989ab54"),
			withHeader(HttpHeaders.AUTHORIZATION)
		)
			.then(
				status(HttpStatus.INTERNAL_SERVER_ERROR_500),
				header("Content-Type", "application/json"),
				jsonResponseFromFile("./stubs/irs/get/jobs/id/response_200.json")
			)
	}

	void verifyIrsApiTriggerJobCalledOnceFor(String ... globalAssetIds) {
		for (String globalAssetId : globalAssetIds) {
			verifyHttp(stubServer()).once(
				post("/irs/jobs"),
				withPostBodyContaining(globalAssetId)
			)
		}
	}

	void verifyIrsApiTriggerJobCalledOnce() {
		verifyHttp(stubServer()).once(
			post("/irs/jobs")
		)
	}

	void verifyIrsApiTriggerJobNotCalled() {
		verifyHttp(stubServer()).never(
			post("/irs/jobs")
		)
	}

	void verifyIrsJobDetailsApiNotCalled() {
		verifyHttp(stubServer()).never(
			startsWithUri("/irs/jobs/")
		)
	}
}
