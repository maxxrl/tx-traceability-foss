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

import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { CtaSnackbarService } from '@shared/components/call-to-action-snackbar/cta-snackbar.service';
import { Severity } from '@shared/model/severity.model';
import {
  RequestContext,
  RequestNotificationBase,
} from '@shared/components/request-notification/request-notification.base';
import { ALERT_BASE_ROUTE, getRoute } from '@core/known-route';
import { NotificationStatusGroup } from '@shared/model/notification.model';
import { Part } from '@page/parts/model/parts.model';
import { bpnRegex } from '@page/admin/presentation/bpn-configuration/bpn-configuration.component';
import { BaseInputHelper } from '@shared/abstraction/baseInput/baseInput.helper';
import { AlertsService } from '@shared/service/alert.service';

@Component({
  selector: 'app-request-alert',
  templateUrl: './request-notification.base.html',
})
export class RequestAlertComponent extends RequestNotificationBase {
  @Input() selectedItems: Part[];
  @Input() showHeadline = true;

  @Output() deselectPart = new EventEmitter<Part>();
  @Output() restorePart = new EventEmitter<Part>();
  @Output() clearSelected = new EventEmitter<void>();
  @Output() submitted = new EventEmitter<void>();

  public readonly context: RequestContext = 'requestAlert';

  constructor(ctaSnackbarService: CtaSnackbarService, private readonly alertsService: AlertsService) {
    super(ctaSnackbarService);
  }

  public readonly formGroup = new FormGroup({
    description: new FormControl('', [Validators.required, Validators.maxLength(1000), Validators.minLength(15)]),
    severity: new FormControl(Severity.MINOR, [Validators.required]),
    bpn: new FormControl(null, [Validators.required, BaseInputHelper.getCustomPatternValidator(bpnRegex, 'bpn')]),
  });

  public submit(): void {
    this.prepareSubmit();
    if (this.formGroup.invalid) return;

    const partIds = this.selectedItems.map(part => part.id);
    const { description, bpn, severity } = this.formGroup.value;
    const { link, queryParams } = getRoute(ALERT_BASE_ROUTE, NotificationStatusGroup.QUEUED_AND_REQUESTED);

    this.alertsService.postAlert(partIds, description, severity, bpn).subscribe({
      next: () => this.onSuccessfulSubmit(link, queryParams),
      error: () => this.onUnsuccessfulSubmit(),
    });
  }
}
