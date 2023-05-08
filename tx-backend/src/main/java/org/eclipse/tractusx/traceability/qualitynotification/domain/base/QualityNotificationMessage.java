package org.eclipse.tractusx.traceability.qualitynotification.domain.base;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.eclipse.tractusx.traceability.qualitynotification.domain.investigation.model.AffectedPart;
import org.eclipse.tractusx.traceability.qualitynotification.domain.investigation.model.Severity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@SuperBuilder
@Getter
@Setter
public class QualityNotificationMessage {
    private String id;
    private String notificationReferenceId;
    private String senderBpnNumber;
    private String senderManufacturerName;
    private String receiverBpnNumber;
    private String receiverManufacturerName;
    private String edcUrl;
    private String contractAgreementId;
    private List<AffectedPart> affectedParts;
    private String description;
    private String edcNotificationId;
    private LocalDateTime created;
    private LocalDateTime updated;
    private Instant targetDate;
    private Severity severity;
    private String messageId;
    private Boolean isInitial;


}
