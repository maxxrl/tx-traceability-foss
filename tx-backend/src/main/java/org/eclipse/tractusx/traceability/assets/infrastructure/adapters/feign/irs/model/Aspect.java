/********************************************************************************
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

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.List;

public enum Aspect {
    BATCH("Batch"),
    SERIAL_PART_TYPIZATION("SerialPartTypization"),
    ASSEMBLY_PART_RELATIONSHIP("AssemblyPartRelationship"),
    SINGLE_LEVEL_USAGE_AS_BUILT("SingleLevelUsageAsBuilt");


    private final String aspectName;

    Aspect(String aspectName) {
        this.aspectName = aspectName;
    }

    @JsonValue
    public String getAspectName() {
        return aspectName;
    }

    public static List<String> downwardAspects() {
        return List.of(BATCH.getAspectName(), SERIAL_PART_TYPIZATION.getAspectName(), ASSEMBLY_PART_RELATIONSHIP.getAspectName());
    }

    public static List<String> upwardAspects() {
        return List.of(BATCH.getAspectName(), SERIAL_PART_TYPIZATION.getAspectName(), SINGLE_LEVEL_USAGE_AS_BUILT.getAspectName());
    }
}
