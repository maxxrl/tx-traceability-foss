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

import { Component, Inject, Injector, Input, TemplateRef } from '@angular/core';
import { BaseInputComponent } from '@shared/abstraction/baseInput/baseInput.component';
import { StaticIdService } from '@shared/service/staticId.service';

export interface SelectOption {
  label: string;
  value?: string;
}

@Component({
  selector: 'app-select',
  templateUrl: './select.component.html',
  styleUrls: ['./select.component.scss'],
})
export class SelectComponent extends BaseInputComponent<string> {
  @Input() options: SelectOption[];
  @Input() optionsRenderer: TemplateRef<unknown>;

  constructor(@Inject(Injector) injector: Injector, staticIdService: StaticIdService) {
    super(injector, staticIdService);
  }
}
