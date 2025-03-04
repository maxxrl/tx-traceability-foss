/********************************************************************************
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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


import org.eclipse.tractusx.traceability.bpn.mapping.infrastructure.adapters.rest.BpnEdcMappingRequest

trait BpnEdcMappingSupport implements BpnEdcRepositoryProvider {

    void defaultBpnEdcMappingStored() {
        def bpnEdcMappingRequests = List.of(new BpnEdcMappingRequest("BPN123", "https://test123.de"), new BpnEdcMappingRequest("BPN456", "https://test456.de"))
        bpnEdcRepository().saveAll(bpnEdcMappingRequests)
    }
}
