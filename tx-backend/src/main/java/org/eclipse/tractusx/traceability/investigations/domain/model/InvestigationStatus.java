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

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;
import static java.util.Set.of;

// One approve notification should set InvestigationStatus to SENT
// Currently investigation already initialized with SENT
// We need to update the receiver and sender on each new created notification to make sure that the notification will be adressed correctly


public enum InvestigationStatus {
	CREATED(InvestigationSide.SENDER, emptySet()),
	SENT(InvestigationSide.SENDER, Set.of(InvestigationSide.SENDER)),
	RECEIVED(InvestigationSide.RECEIVER, emptySet()),
	ACKNOWLEDGED(InvestigationSide.RECEIVER, Set.of(InvestigationSide.RECEIVER, InvestigationSide.SENDER)),
	ACCEPTED(InvestigationSide.RECEIVER, Set.of(InvestigationSide.RECEIVER)),
	DECLINED(InvestigationSide.RECEIVER, Set.of(InvestigationSide.RECEIVER)),
	CANCELED(InvestigationSide.SENDER, Set.of(InvestigationSide.SENDER)),
	CLOSED(InvestigationSide.SENDER, of(InvestigationSide.SENDER, InvestigationSide.RECEIVER));

	private final InvestigationSide investigationSide;
	private final Set<InvestigationSide> allowedTransitionFromSide;

	InvestigationStatus(InvestigationSide investigationSide, Set<InvestigationSide> allowedTransitionFromSide) {
		this.investigationSide = investigationSide;
		this.allowedTransitionFromSide = allowedTransitionFromSide;
	}

	private static final Map<InvestigationStatus, Set<InvestigationStatus>> STATE_MACHINE;

	private static final Set<InvestigationStatus> NO_TRANSITION_ALLOWED = emptySet();

	private static final Map<String, InvestigationStatus> MAPPINGS;

	static {
		STATE_MACHINE = Map.of(
			CREATED, of(SENT, CANCELED),
			SENT, of(RECEIVED, CLOSED, ACKNOWLEDGED),
			RECEIVED, of(ACKNOWLEDGED, CLOSED),
			ACKNOWLEDGED, of(DECLINED, ACCEPTED, CLOSED),
			ACCEPTED, of(CLOSED),
			DECLINED, of(CLOSED),
			CLOSED, NO_TRANSITION_ALLOWED,
			CANCELED, NO_TRANSITION_ALLOWED
		);

		MAPPINGS = Arrays.stream(InvestigationStatus.values())
			.collect(Collectors.toMap(Enum::name, investigationStatus -> investigationStatus));
	}

	public boolean transitionAllowed(InvestigationStatus to) {

		Set<InvestigationStatus> allowedStatusesToTransition = STATE_MACHINE.get(this);

		if (!allowedStatusesToTransition.contains(to)) {
			return false;
		}

		return isSideEligibleForTransition(this, to);
	}

	private boolean isSideEligibleForTransition(InvestigationStatus from, InvestigationStatus to) {
		return to.allowedTransitionFromSide.contains(from.investigationSide);
	}

	public static Optional<InvestigationStatus> fromValue(String value) {
		return Optional.ofNullable(MAPPINGS.get(value));
	}
}
