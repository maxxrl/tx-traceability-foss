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

package org.eclipse.tractusx.traceability.qualitynotification.application.investigation.request;

import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.eclipse.tractusx.traceability.qualitynotification.application.investigation.validation.ValidSeverity;

import java.time.Instant;
import java.util.List;

public record StartInvestigationRequest(
        @Size(min = 1, max = 100, message = "Specify at least 1 and at most 100 partIds")
        @ApiModelProperty(example = "[\"urn:uuid:fe99da3d-b0de-4e80-81da-882aebcca978\"]")
        List<String> partIds,
        @Size(min = 15, max = 1000, message = "Description should have at least 15 characters and at most 1000 characters")
        @ApiModelProperty(example = "The description of the investigation")
        String description,
        @ApiModelProperty(example = "2099-03-11T22:44:06.333826952Z")
        @Future(message = "Specify at least the current day or a date in future")
        Instant targetDate,
        @NotNull
        @ValidSeverity
        @ApiModelProperty(example = "MINOR")
        String severity
) {
}
