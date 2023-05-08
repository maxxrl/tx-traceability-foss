package org.eclipse.tractusx.traceability.qualitynotification.domain.base;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.eclipse.tractusx.traceability.common.model.BPN;
import org.eclipse.tractusx.traceability.qualitynotification.domain.investigation.model.Notification;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@SuperBuilder
public class QualityNotification {
    private BPN bpn;
    private String description;
    private Instant createdAt;
    private List<String> assetIds;
    private Map<String, Notification> notifications;
    private String closeReason;
    private String acceptReason;
    private String declineReason;
}
