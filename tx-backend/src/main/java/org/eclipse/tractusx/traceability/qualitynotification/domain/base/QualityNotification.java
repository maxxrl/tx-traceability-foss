package org.eclipse.tractusx.traceability.qualitynotification.domain.base;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.eclipse.tractusx.traceability.common.model.BPN;
import org.eclipse.tractusx.traceability.qualitynotification.domain.investigation.model.AffectedPart;
import org.eclipse.tractusx.traceability.qualitynotification.domain.investigation.model.Investigation;
import org.eclipse.tractusx.traceability.qualitynotification.domain.investigation.model.Notification;
import org.eclipse.tractusx.traceability.qualitynotification.domain.investigation.model.exception.InvestigationIllegalUpdate;
import org.eclipse.tractusx.traceability.qualitynotification.domain.investigation.model.exception.InvestigationStatusTransitionNotAllowed;

import java.time.Instant;
import java.util.*;

@Data
@SuperBuilder
public class QualityNotification {
    private BPN bpn;
    private String description;
    private QualityNotificationStatus status;
    private QualityNotificationSide side;
    private Instant createdAt;
    private List<String> assetIds;
    private Map<String, QualityNotificationMessage> notifications;
    private String closeReason;
    private String acceptReason;
    private String declineReason;

    public static final Comparator<QualityNotification> COMPARE_BY_NEWEST_INVESTIGATION_CREATION_TIME = (o1, o2) -> {
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

    private static String getSenderBPN(Collection<QualityNotificationMessage> notifications) {
        return notifications.stream()
                .findFirst()
                .map(QualityNotificationMessage::getSenderBpnNumber)
                .orElse(null);
    }

    private static String getReceiverBPN(Collection<QualityNotificationMessage> notifications) {
        return notifications.stream()
                .findFirst()
                .map(QualityNotificationMessage::getReceiverBpnNumber)
                .orElse(null);
    }

    public List<String> getAssetIds() {
        return Collections.unmodifiableList(assetIds);
    }
    public String getBpn() {
        return bpn.value();
    }




    public void cancel(BPN applicationBpn) {
        validateBPN(applicationBpn);
        changeStatusTo(QualityNotificationStatus.CANCELED);
        this.closeReason = "canceled";
    }

    public void close(BPN applicationBpn, String reason) {
        validateBPN(applicationBpn);
        changeStatusTo(QualityNotificationStatus.CLOSED);
        this.closeReason = reason;
        this.notifications.values()
                .forEach(notification -> notification.setDescription(reason));
    }

    public void acknowledge(QualityNotificationMessage notification) {
        changeStatusToWithoutNotifications(QualityNotificationStatus.ACKNOWLEDGED);
        setInvestigationStatusAndReasonForNotification(notification, QualityNotificationStatus.ACKNOWLEDGED, null);
        notification.setInvestigationStatus(QualityNotificationStatus.ACKNOWLEDGED);
    }

    public void accept(String reason, QualityNotificationMessage notification) {
        changeStatusToWithoutNotifications(QualityNotificationStatus.ACCEPTED);
        this.acceptReason = reason;
        setInvestigationStatusAndReasonForNotification(notification, QualityNotificationStatus.ACCEPTED, reason);
        notification.setInvestigationStatus(QualityNotificationStatus.ACCEPTED);
        notification.setDescription(reason);
    }

    public void decline(String reason, QualityNotificationMessage notification) {
        changeStatusToWithoutNotifications(QualityNotificationStatus.DECLINED);
        this.declineReason = reason;
        setInvestigationStatusAndReasonForNotification(notification, QualityNotificationStatus.DECLINED, reason);
        notification.setInvestigationStatus(QualityNotificationStatus.DECLINED);
        notification.setDescription(reason);
    }

    public void close(String reason, QualityNotificationMessage notification) {
        changeStatusToWithoutNotifications(QualityNotificationStatus.CLOSED);
        this.closeReason = reason;
        setInvestigationStatusAndReasonForNotification(notification, QualityNotificationStatus.CLOSED, reason);
        notification.setInvestigationStatus(QualityNotificationStatus.CLOSED);
        notification.setDescription(reason);
    }

    public void send(BPN applicationBpn) {
        validateBPN(applicationBpn);
        changeStatusTo(QualityNotificationStatus.SENT);
    }

    private void validateBPN(BPN applicationBpn) {
        if (!applicationBpn.equals(this.bpn)) {
            throw new InvestigationIllegalUpdate("%s bpn has no permissions to update investigation with %s id.".formatted(applicationBpn.value(), investigationId.value()));
        }
    }

    private void changeStatusTo(QualityNotificationStatus to) {
        boolean transitionAllowed = status.transitionAllowed(to);

        if (!transitionAllowed) {
            throw new InvestigationStatusTransitionNotAllowed(investigationId, status, to);
        }

        notifications.values()
                .forEach(notification -> notification.changeStatusTo(to));

        this.status = to;
    }

    private void changeStatusToWithoutNotifications(QualityNotificationStatus to) {
        boolean transitionAllowed = status.transitionAllowed(to);

        if (!transitionAllowed) {
            throw new InvestigationStatusTransitionNotAllowed(investigationId, status, to);
        }

        this.status = to;
    }

    public void addNotification(QualityNotificationMessage notification) {
        notifications.put(notification.getId(), notification);

        List<String> newAssetIds = new ArrayList<>(assetIds); // create a mutable copy of assetIds
        notification.getAffectedParts().stream()
                .map(AffectedPart::assetId)
                .forEach(newAssetIds::add);

        assetIds = Collections.unmodifiableList(newAssetIds); //
    }
    private void setInvestigationStatusAndReasonForNotification(QualityNotificationMessage notificationDomain, QualityNotificationStatus investigationStatus, String reason) {
        for (QualityNotificationMessage notification : this.notifications.values()) {
            if (notification.getId().equals(notificationDomain.getId())) {
                if (reason != null) {
                    notification.setDescription(reason);
                }
                notification.setInvestigationStatus(investigationStatus);
                break;
            }
        }
    }
}



