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

package org.eclipse.tractusx.traceability.common.config

import com.xebialabs.restito.server.StubServer
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext

@TestConfiguration
class RestitoConfig {
	public static final String OAUTH2_JWK_PATH = "/auth/realms/CX-Central/protocol/openid-connect/certs"
	public static final String OAUTH2_TOKEN_PATH = "/auth/realms/CX-Central/protocol/openid-connect/token"

	private static final StubServer STUB_SERVER
	private static final int STUB_SERVER_PORT

	static {
		STUB_SERVER = new StubServer(1025, 65000).run()
		STUB_SERVER_PORT = STUB_SERVER.getPort()
	}

	static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		void initialize(ConfigurableApplicationContext configurableApplicationContext) {
			TestPropertyValues.of(
				"spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://127.0.0.1:$STUB_SERVER_PORT$OAUTH2_JWK_PATH",
				"spring.security.oauth2.client.provider.OKTA.token-uri=http://127.0.0.1:$STUB_SERVER_PORT$OAUTH2_TOKEN_PATH",
				"feign.bpnApi.url=http://127.0.0.1:$STUB_SERVER_PORT",
				"feign.irsApi.url=http://127.0.0.1:$STUB_SERVER_PORT",
				"feign.portalApi.url=http://127.0.0.1:$STUB_SERVER_PORT",
				"feign.irsApi.globalAssetId=testAssetId",
				"feign.registryApi.url=http://127.0.0.1:$STUB_SERVER_PORT",
				"feign.registryApi.defaultBpn=BPNL00000003AYRE",
				"edc.provider-edc-url=http://localhost:$STUB_SERVER_PORT",
				"edc.callbackUrls=http://localhost:$STUB_SERVER_PORT/callback/redirect"
			).applyTo(configurableApplicationContext.getEnvironment())
		}
	}

	static void clear() {
		STUB_SERVER.clear()
	}

	static StubServer getStubServer() {
		assert STUB_SERVER != null

		return STUB_SERVER
	}
}
