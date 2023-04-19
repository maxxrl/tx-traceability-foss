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

package org.eclipse.tractusx.traceability.infrastructure.edc.blackbox.policy;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.jetbrains.annotations.Nullable;

import static java.util.stream.Collectors.joining;

/**
 * An obligation that must be performed if all its constraints are satisfied.
 * TODO: Do we need to support deserializing the parent permission setting?
 */
@JsonDeserialize(builder = Duty.Builder.class)
@JsonTypeName("dataspaceconnector:duty")
public class Duty extends Rule {

	private Permission parentPermission;

	@Nullable
	private Duty consequence;

	public Duty getConsequence() {
		return consequence;
	}

	/**
	 * If this duty is part of a permission, returns the parent permission; otherwise returns null.
	 */
	@Nullable
	public Permission getParentPermission() {
		return parentPermission;
	}

	void setParentPermission(Permission permission) {
		parentPermission = permission;
	}

	@Override
	public <R> R accept(Visitor<R> visitor) {
		return visitor.visitDuty(this);
	}

	@Override
	public String toString() {
		return "Duty constraint: [" + getConstraints().stream().map(Object::toString).collect(joining(",")) + "]";
	}

	/**
	 * Returns a copy of this duty with the specified target.
	 *
	 * @param target the target.
	 * @return a copy with the specified target.
	 */
	public Duty withTarget(String target) {
		return Builder.newInstance()
			.uid(this.uid)
			.assigner(this.assigner)
			.assignee(this.assignee)
			.action(this.action)
			.constraints(this.constraints)
			.parentPermission(this.parentPermission)
			.consequence(this.consequence == null ? null : this.consequence.withTarget(target))
			.target(target)
			.build();
	}

	@JsonPOJOBuilder(withPrefix = "")
	public static class Builder extends Rule.Builder<Duty, Builder> {

		private Builder() {
			rule = new Duty();
		}

		@JsonCreator
		public static Builder newInstance() {
			return new Builder();
		}

		public Builder uid(String uid) {
			rule.uid = uid;
			return this;
		}

		public Builder parentPermission(Permission parentPermission) {
			rule.parentPermission = parentPermission;
			return this;
		}

		public Builder consequence(Duty consequence) {
			rule.consequence = consequence;
			return this;
		}

		public Duty build() {
			return rule;
		}
	}

}
