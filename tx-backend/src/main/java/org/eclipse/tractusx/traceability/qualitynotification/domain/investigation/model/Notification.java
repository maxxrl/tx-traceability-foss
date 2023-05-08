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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.eclipse.tractusx.traceability.common.model.BPN;
import org.eclipse.tractusx.traceability.qualitynotification.domain.base.QualityNotification;
import org.eclipse.tractusx.traceability.qualitynotification.domain.base.QualityNotificationMessage;
import org.eclipse.tractusx.traceability.qualitynotification.domain.base.QualityNotificationStatus;
import org.eclipse.tractusx.traceability.qualitynotification.domain.investigation.model.exception.NotificationStatusTransitionNotAllowed;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.requireNonNullElseGet;

@SuperBuilder
@Getter
@Setter
@ToString
public class Notification extends QualityNotificationMessage {

    private QualityNotificationStatus investigationStatus;

    public QualityNotificationMessage copyAndSwitchSenderAndReceiver(BPN applicationBpn) {
        final String notificationId = UUID.randomUUID().toString();
        String receiver = super.getReceiverBpnNumber();
        String sender = super.getSenderBpnNumber();
        String receiverManufactureName = super.getReceiverManufacturerName();
        String senderManufactureName = super.getSenderManufacturerName();

        // This is needed to make sure that the app can send a message to the receiver and not addresses itself
        if (applicationBpn.value().equals(receiver)) {
            receiver = sender;
            sender = receiver;
            receiverManufactureName = senderManufactureName;
            senderManufactureName = receiverManufactureName;
        }

        return Notification.builder()
                .id(notificationId)
                .senderBpnNumber(sender)
                .senderManufacturerName(senderManufactureName)
                .receiverBpnNumber(receiver)
                .receiverManufacturerName(receiverManufactureName)
                .edcUrl(getEdcUrl())
                .contractAgreementId(getContractAgreementId())
                .description(getDescription())
                .investigationStatus(investigationStatus)
                .affectedParts(getAffectedParts())
                .targetDate(getTargetDate())
                .severity(getSeverity())
                .edcNotificationId(getEdcNotificationId())
                .created(getCreated())
                .updated(getUpdated())
                .messageId(UUID.randomUUID().toString())
                .isInitial(false)
                .build();
    }

    void changeStatusTo(QualityNotificationStatus to) {
        boolean transitionAllowed = investigationStatus.transitionAllowed(to);

        if (!transitionAllowed) {
            throw new NotificationStatusTransitionNotAllowed(getId(), investigationStatus, to);
        }
        this.investigationStatus = to;
    }

    // Important - receiver and sender will be saved in switched order

}
