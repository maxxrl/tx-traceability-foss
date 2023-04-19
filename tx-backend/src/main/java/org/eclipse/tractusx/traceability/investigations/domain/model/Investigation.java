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

package org.eclipse.tractusx.traceability.investigations.domain.model;

import org.eclipse.tractusx.traceability.common.model.BPN;
import org.eclipse.tractusx.traceability.investigations.adapters.rest.model.InvestigationData;
import org.eclipse.tractusx.traceability.investigations.adapters.rest.model.InvestigationReason;
import org.eclipse.tractusx.traceability.investigations.domain.model.exception.InvestigationIllegalUpdate;
import org.eclipse.tractusx.traceability.investigations.domain.model.exception.InvestigationStatusTransitionNotAllowed;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


public class Investigation {

	public static final Comparator<Investigation> COMPARE_BY_NEWEST_INVESTIGATION_CREATION_TIME = (o1, o2) -> {
		Instant o1CreationTime = o1.createdAt;
		Instant o2CreationTime = o2.createdAt;

		if (o1CreationTime.equals(o2CreationTime)) {
			return 0;
		}

		if (o1CreationTime.isBefore(o2CreationTime)) {
			return 1;
		}

		return -1;
	};

	private InvestigationId investigationId;
	private BPN bpn;
	private InvestigationStatus investigationStatus;
	private InvestigationSide investigationSide;
	private String description;
	private Instant createdAt;
	private List<String> assetIds;
	private Map<String, Notification> notifications;
	private String closeReason;
	private String acceptReason;
	private String declineReason;

	public Investigation(InvestigationId investigationId,
						 BPN bpn,
						 InvestigationStatus investigationStatus,
						 InvestigationSide investigationSide,
						 String closeReason,
						 String acceptReason,
						 String declineReason,
						 String description,
						 Instant createdAt,
						 List<String> assetIds,
						 List<Notification> notifications
	) {
		this.investigationId = investigationId;
		this.bpn = bpn;
		this.investigationStatus = investigationStatus;
		this.investigationSide = investigationSide;
		this.closeReason = closeReason;
		this.acceptReason = acceptReason;
		this.declineReason = declineReason;
		this.description = description;
		this.createdAt = createdAt;
		this.assetIds = assetIds;
		this.notifications = notifications.stream()
			.collect(Collectors.toMap(Notification::getId, Function.identity()));
	}

	public static Investigation startInvestigation(Instant createDate, BPN bpn, String description) {
		return new Investigation(null,
			bpn,
			InvestigationStatus.CREATED,
			InvestigationSide.SENDER,
			null,
			null,
			null,
			description,
			createDate,
			new ArrayList<>(),
			new ArrayList<>()
		);
	}

	public List<String> getAssetIds() {
		return Collections.unmodifiableList(assetIds);
	}

	public InvestigationStatus getInvestigationStatus() {
		return investigationStatus;
	}

	public InvestigationSide getInvestigationSide() {
		return investigationSide;
	}

	public String getDescription() {
		return description;
	}

    public InvestigationData toData() {
        return new InvestigationData(
                investigationId.value(),
                investigationStatus.name(),
                description,
                getSenderBPN(notifications.values()),
                getSenderName(notifications.values()),
                createdAt.toString(),
                Collections.unmodifiableList(assetIds),
                investigationSide,
                new InvestigationReason(
                        closeReason,
                        acceptReason,
                        declineReason
                ),
                getReceiverBPN(notifications.values()),
                getReceiverName(notifications.values()),
                notifications.entrySet().stream().findFirst().map(Map.Entry::getValue).map(Notification::getSeverity).orElse(Severity.MINOR).getRealName(),
                notifications.entrySet().stream().findFirst().map(Map.Entry::getValue).map(Notification::getTargetDate).map(Instant::toString).orElse(null));
    }

	public String getBpn() {
		return bpn.value();
	}

	public void cancel(BPN applicationBpn) {
		validateBPN(applicationBpn);
		changeStatusTo(InvestigationStatus.CANCELED);
		this.closeReason = "canceled";
	}

	public void close(BPN applicationBpn, String reason) {
		validateBPN(applicationBpn);
		changeStatusTo(InvestigationStatus.CLOSED);
		this.closeReason = reason;
		this.notifications.values()
			.forEach(notification -> notification.setDescription(reason));
	}

	public void send(BPN applicationBpn) {
		validateBPN(applicationBpn);
		changeStatusTo(InvestigationStatus.SENT);
	}




    private void setInvestigationStatusAndReasonForNotification(Notification notificationDomain, InvestigationStatus investigationStatus, String reason) {
        for (Notification notification : this.notifications.values()) {
                if (notification.getId().equals(notificationDomain.getId())) {
                    if (reason != null){
                        notification.setDescription(reason);
                    }
                    notification.setInvestigationStatus(investigationStatus);
                    break;
                }
        }
    }

    public void acknowledge(Notification notification) {
        changeStatusToWithoutNotifications(InvestigationStatus.ACKNOWLEDGED);
        setInvestigationStatusAndReasonForNotification(notification, InvestigationStatus.ACKNOWLEDGED, null);
        notification.setInvestigationStatus(InvestigationStatus.ACKNOWLEDGED);
    }

    public void accept(String reason, Notification notification) {
        changeStatusToWithoutNotifications(InvestigationStatus.ACCEPTED);
        this.acceptReason = reason;
        setInvestigationStatusAndReasonForNotification(notification, InvestigationStatus.ACCEPTED, reason);
        notification.setInvestigationStatus(InvestigationStatus.ACCEPTED);
        notification.setDescription(reason);
    }

    public void decline(String reason, Notification notification) {
        changeStatusToWithoutNotifications(InvestigationStatus.DECLINED);
        this.declineReason = reason;
        setInvestigationStatusAndReasonForNotification(notification, InvestigationStatus.DECLINED, reason);
        notification.setInvestigationStatus(InvestigationStatus.DECLINED);
        notification.setDescription(reason);
    }

    public void close(String reason, Notification notification) {
        changeStatusToWithoutNotifications(InvestigationStatus.CLOSED);
        this.closeReason = reason;
        setInvestigationStatusAndReasonForNotification(notification, InvestigationStatus.CLOSED, reason);
        notification.setInvestigationStatus(InvestigationStatus.CLOSED);
        notification.setDescription(reason);
    }

	private void validateBPN(BPN applicationBpn) {
		if (!applicationBpn.equals(this.bpn)) {
			throw new InvestigationIllegalUpdate("%s bpn has no permissions to update investigation with %s id.".formatted(applicationBpn.value(), investigationId.value()));
		}
	}

	private void changeStatusTo(InvestigationStatus to) {
		boolean transitionAllowed = investigationStatus.transitionAllowed(to);

		if (!transitionAllowed) {
			throw new InvestigationStatusTransitionNotAllowed(investigationId, investigationStatus, to);
		}

        notifications.values()
			.forEach(notification -> notification.changeStatusTo(to));

		this.investigationStatus = to;
	}

	private void changeStatusToWithoutNotifications(InvestigationStatus to) {
		boolean transitionAllowed = investigationStatus.transitionAllowed(to);

		if (!transitionAllowed) {
			throw new InvestigationStatusTransitionNotAllowed(investigationId, investigationStatus, to);
		}

		this.investigationStatus = to;
	}

	public InvestigationId getId() {
		return investigationId;
	}

	public Instant getCreationTime() {
		return createdAt;
	}

	public List<Notification> getNotifications() {
		return new ArrayList<>(notifications.values());
	}

	public String getCloseReason() {
		return closeReason;
	}

	public String getAcceptReason() {
		return acceptReason;
	}

	public String getDeclineReason() {
		return declineReason;
	}

	public void addNotification(Notification notification) {
		notifications.put(notification.getId(), notification);

		List<String> newAssetIds = new ArrayList<>(assetIds); // create a mutable copy of assetIds
		notification.getAffectedParts().stream()
			.map(AffectedPart::assetId)
			.forEach(newAssetIds::add);

		assetIds = Collections.unmodifiableList(newAssetIds); //
	}

	@Override
	public String toString() {
		return "Investigation{" +
			"investigationId=" + investigationId +
			", bpn=" + bpn +
			", investigationStatus=" + investigationStatus +
			", investigationSide=" + investigationSide +
			", description='" + description + '\'' +
			", createdAt=" + createdAt +
			", assetIds=" + assetIds +
			", notifications=" + notifications +
			", closeReason='" + closeReason + '\'' +
			", acceptReason='" + acceptReason + '\'' +
			", declineReason='" + declineReason + '\'' +
			'}';
	}

	private static String getSenderBPN(Collection<Notification> notifications) {
		return notifications.stream()
			.findFirst()
			.map(Notification::getSenderBpnNumber)
			.orElse(null);
	}

    private static String getSenderName(Collection<Notification> notifications) {
        return notifications.stream()
                .findFirst()
                .map(Notification::getSenderManufacturerName)
                .orElse(null);
    }

	private static String getReceiverBPN(Collection<Notification> notifications) {
		return notifications.stream()
			.findFirst()
			.map(Notification::getReceiverBpnNumber)
			.orElse(null);
	}

    private static String getReceiverName(Collection<Notification> notifications) {
        return notifications.stream()
                .findFirst()
                .map(Notification::getReceiverManufacturerName)
                .orElse(null);
    }
}
