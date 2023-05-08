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

package org.eclipse.tractusx.traceability.qualitynotification.domain.investigation.model;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.eclipse.tractusx.traceability.common.model.BPN;
import org.eclipse.tractusx.traceability.qualitynotification.application.investigation.response.InvestigationDTO;
import org.eclipse.tractusx.traceability.qualitynotification.application.investigation.response.InvestigationReason;
import org.eclipse.tractusx.traceability.qualitynotification.domain.base.QualityNotification;
import org.eclipse.tractusx.traceability.qualitynotification.domain.base.QualityNotificationMessage;
import org.eclipse.tractusx.traceability.qualitynotification.domain.base.QualityNotificationSide;
import org.eclipse.tractusx.traceability.qualitynotification.domain.base.QualityNotificationStatus;
import org.eclipse.tractusx.traceability.qualitynotification.domain.investigation.model.exception.InvestigationIllegalUpdate;
import org.eclipse.tractusx.traceability.qualitynotification.domain.investigation.model.exception.InvestigationStatusTransitionNotAllowed;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuperBuilder
@Data
public class Investigation extends QualityNotification {

    private InvestigationId investigationId;


    public static Investigation startInvestigation(Instant createDate, BPN bpn, String description) {
       return Investigation.builder()
                .bpn(bpn)
                .status(QualityNotificationStatus.CREATED)
                .side(QualityNotificationSide.SENDER)
                .description(description)
                .createdAt(createDate)
                .assetIds(Collections.emptyList())
                .notifications(Collections.emptyMap())
                .build();
    }

    public InvestigationDTO toDTO() {
        return InvestigationDTO
                .builder()
               /* .id(investigationId.value())
                .status(QualityNotificationStatus.name())
                .description(getDescription())
                .createdBy(getSenderBPN(notifications.values()))
                .createdByName(getSenderName(notifications.values()))
                .createdDate(createdAt.toString())
                .assetIds(Collections.unmodifiableList(assetIds))
                .channel(investigationSide)
                .reason(new InvestigationReason(
                        closeReason,
                        acceptReason,
                        declineReason
                ))
                .sendTo(getReceiverBPN(notifications.values()))
                .sendToName(getReceiverName(notifications.values()))
                .severity(notifications.entrySet().stream().findFirst().map(Map.Entry::getValue).map(Notification::getSeverity).orElse(Severity.MINOR).getRealName())
                .targetDate(notifications.entrySet().stream().findFirst().map(Map.Entry::getValue).map(Notification::getTargetDate).map(Instant::toString).orElse(null))*/.build();
    }

}
