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

package org.eclipse.tractusx.traceability.qualitynotification.adapters.rest

import io.restassured.http.ContentType
import org.apache.commons.lang3.RandomStringUtils
import org.eclipse.tractusx.traceability.IntegrationSpecification
import org.eclipse.tractusx.traceability.assets.domain.model.Asset
import org.eclipse.tractusx.traceability.common.security.JwtRole
import org.eclipse.tractusx.traceability.common.support.*
import org.eclipse.tractusx.traceability.infrastructure.edc.blackbox.model.EDCNotification
import org.eclipse.tractusx.traceability.infrastructure.edc.blackbox.model.EDCNotificationFactory
import org.eclipse.tractusx.traceability.qualitynotification.domain.investigation.model.AffectedPart
import org.eclipse.tractusx.traceability.qualitynotification.domain.investigation.model.InvestigationStatus
import org.eclipse.tractusx.traceability.qualitynotification.domain.investigation.model.Notification
import org.eclipse.tractusx.traceability.qualitynotification.domain.investigation.model.Severity
import org.eclipse.tractusx.traceability.qualitynotification.domain.investigation.service.InvestigationsReceiverService
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import spock.lang.Unroll

import java.time.Instant

import static io.restassured.RestAssured.given
import static org.eclipse.tractusx.traceability.common.security.JwtRole.ADMIN

class PublisherInvestigationsControllerIT extends IntegrationSpecification implements IrsApiSupport, AssetsSupport, InvestigationsSupport, NotificationsSupport, BpnSupport {
    @Autowired
    InvestigationsReceiverService investigationsReceiverService

    @Transactional
    def "should receive notification"() {
        given:
        defaultAssetsStored()

        and:
        EDCNotification notification = EDCNotificationFactory.createQualityInvestigation(
                "it",
                new Notification(
                        "some-id",
                        null,
                        "bpn-a",
                        "Sender Manufacturer name",
                        "BPNL00000003AXS3",
                        "Receiver manufacturer name",
                        "edcUrl",
                        null,
                        "description",
                        InvestigationStatus.SENT,
                        [new AffectedPart("urn:uuid:d387fa8e-603c-42bd-98c3-4d87fef8d2bb")],
                        Instant.parse("2018-11-30T18:35:24.00Z"),
                        Severity.MINOR,
                        "some-id",
                        null,
                        null,
                        "messageid",
                        false
                )
        )

        when:
        investigationsReceiverService.handleNotificationReceive(notification)

        then:
        assertInvestigationsSize(1)
        assertNotificationsSize(1)
    }

    def "should start investigation"() {
        given:
        List<String> partIds = [
                "urn:uuid:fe99da3d-b0de-4e80-81da-882aebcca978", // BPN: BPNL00000003AYRE
                "urn:uuid:d387fa8e-603c-42bd-98c3-4d87fef8d2bb", // BPN: BPNL00000003AYRE
                "urn:uuid:0ce83951-bc18-4e8f-892d-48bad4eb67ef"  // BPN: BPNL00000003AXS3
        ]
        String description = "at least 15 characters long investigation description"
        String severity = "MINOR"
        and:
        defaultAssetsStored()

        when:
        given()
                .contentType(ContentType.JSON)
                .body(
                        asJson(
                                [
                                        partIds    : partIds,
                                        description: description,
                                        severity   : severity
                                ]
                        )
                )
                .header(jwtAuthorization(ADMIN))
                .when()
                .post("/api/investigations")
                .then()
                .statusCode(201)
                .body("id", Matchers.isA(Number.class))

        then:
        partIds.each { partId ->
            Asset asset = assetRepository().getAssetById(partId)
            assert asset
            assert asset.isUnderInvestigation()
        }

        and:
        assertNotificationsSize(2)

        and:
        given()
                .header(jwtAuthorization(ADMIN))
                .param("page", "0")
                .param("size", "10")
                .contentType(ContentType.JSON)
                .when()
                .get("/api/investigations/created")
                .then()
                .statusCode(200)
                .body("page", Matchers.is(0))
                .body("pageSize", Matchers.is(10))
                .body("content", Matchers.hasSize(1))
    }

    def "should throw bad request on start investigation missing required parameter severity"() {
        given:
        List<String> partIds = [
                "urn:uuid:fe99da3d-b0de-4e80-81da-882aebcca978", // BPN: BPNL00000003AYRE
                "urn:uuid:d387fa8e-603c-42bd-98c3-4d87fef8d2bb", // BPN: BPNL00000003AYRE
                "urn:uuid:0ce83951-bc18-4e8f-892d-48bad4eb67ef"  // BPN: BPNL00000003AXS3
        ]
        String description = "at least 15 characters long investigation description"

        expect:
        given()
                .contentType(ContentType.JSON)
                .body(
                        asJson(
                                [
                                        partIds    : partIds,
                                        description: description
                                ]
                        )
                )
                .header(jwtAuthorization(ADMIN))
                .when()
                .post("/api/investigations")
                .then()
                .statusCode(400)
    }

    def "should throw bad request on start investigation description exceeds maximum length"() {
        given:
        List<String> partIds = [
                "urn:uuid:fe99da3d-b0de-4e80-81da-882aebcca978", // BPN: BPNL00000003AYRE
                "urn:uuid:d387fa8e-603c-42bd-98c3-4d87fef8d2bb", // BPN: BPNL00000003AYRE
                "urn:uuid:0ce83951-bc18-4e8f-892d-48bad4eb67ef"  // BPN: BPNL00000003AXS3
        ]

        String description = RandomStringUtils.random(1001);

        expect:
        given()
                .contentType(ContentType.JSON)
                .body(
                        asJson(
                                [
                                        partIds    : partIds,
                                        description: description,
                                        severity   : "MINOR"
                                ]
                        )
                )
                .header(jwtAuthorization(ADMIN))
                .when()
                .post("/api/investigations")
                .then()
                .statusCode(400)
                .body(Matchers.containsString("Description should have at least 15 characters and at most 1000 characters"))
    }

    def "should throw bad request on update investigation reason too long"() {
        given:
        List<String> partIds = [
                "urn:uuid:fe99da3d-b0de-4e80-81da-882aebcca978", // BPN: BPNL00000003AYRE
                "urn:uuid:d387fa8e-603c-42bd-98c3-4d87fef8d2bb", // BPN: BPNL00000003AYRE
                "urn:uuid:0ce83951-bc18-4e8f-892d-48bad4eb67ef"  // BPN: BPNL00000003AXS3
        ]
        String description = RandomStringUtils.random(1001);

        expect:
        given()
                .contentType(ContentType.JSON)
                .body(
                        asJson(
                                [
                                        status: "ACCEPTED",
                                        reason: description
                                ]
                        )
                )
                .header(jwtAuthorization(JwtRole.SUPERVISOR))
                .when()
                .post("/api/investigations/1/update")
                .then()
                .statusCode(400)
                .body(Matchers.containsString("Reason should have at least 15 characters and at most 1000 characters"))
    }

    def "should throw bad request on update investigation wrong status"() {
        given:
        List<String> partIds = [
                "urn:uuid:fe99da3d-b0de-4e80-81da-882aebcca978", // BPN: BPNL00000003AYRE
                "urn:uuid:d387fa8e-603c-42bd-98c3-4d87fef8d2bb", // BPN: BPNL00000003AYRE
                "urn:uuid:0ce83951-bc18-4e8f-892d-48bad4eb67ef"  // BPN: BPNL00000003AXS3
        ]
        String description = RandomStringUtils.random(15);

        expect:
        given()
                .contentType(ContentType.JSON)
                .body(
                        asJson(
                                [
                                        status: "anything",
                                        reason: description
                                ]
                        )
                )
                .header(jwtAuthorization(JwtRole.SUPERVISOR))
                .when()
                .post("/api/investigations/1/update")
                .then()
                .statusCode(400)
                .body(Matchers.containsString("message\":\"NoSuchElementException: Unsupported UpdateInvestigationStatus: anything. Must be one of: ACKNOWLEDGED, ACCEPTED, DECLINED"))
    }

    def "should cancel investigation"() {
        given:
        defaultAssetsStored()

        and:
        def investigationId = given()
                .contentType(ContentType.JSON)
                .body(
                        asJson(
                                [
                                        partIds    : ["urn:uuid:fe99da3d-b0de-4e80-81da-882aebcca978"],
                                        description: "at least 15 characters long investigation description",
                                        severity   : "MAJOR"
                                ]
                        )
                )
                .header(jwtAuthorization(ADMIN))
                .when()
                .post("/api/investigations")
                .then()
                .statusCode(201)
                .extract().path("id")

        and:
        given()
                .header(jwtAuthorization(ADMIN))
                .param("page", "0")
                .param("size", "10")
                .contentType(ContentType.JSON)
                .when()
                .get("/api/investigations/created")
                .then()
                .statusCode(200)
                .body("page", Matchers.is(0))
                .body("pageSize", Matchers.is(10))
                .body("content", Matchers.hasSize(1))

        expect:
        given()
                .header(jwtAuthorization(ADMIN))
                .contentType(ContentType.JSON)
                .when()
                .post("/api/investigations/$investigationId/cancel")
                .then()
                .statusCode(204)

        and:
        given()
                .header(jwtAuthorization(ADMIN))
                .param("page", "0")
                .param("size", "10")
                .contentType(ContentType.JSON)
                .when()
                .get("/api/investigations/created")
                .then()
                .statusCode(200)
                .body("page", Matchers.is(0))
                .body("pageSize", Matchers.is(10))
                .body("content", Matchers.hasSize(1))
    }

    def "should approve investigation status"() {
        given:
        List<String> partIds = [
                "urn:uuid:fe99da3d-b0de-4e80-81da-882aebcca978", // BPN: BPNL00000003AYRE
                "urn:uuid:0ce83951-bc18-4e8f-892d-48bad4eb67ef"  // BPN: BPNL00000003AXS3
        ]
        String description = "at least 15 characters long investigation description"

        String severity = "MINOR"
        and:
        defaultAssetsStored()

        when:
        def investigationId = given()
                .contentType(ContentType.JSON)
                .body(asJson([
                        partIds    : partIds,
                        description: description,
                        severity   : severity
                ]))
                .header(jwtAuthorization(ADMIN))
                .when()
                .post("/api/investigations")
                .then()
                .statusCode(201)
                .extract().path("id")

        then:
        assertInvestigationsSize(1)

        when:
        given()
                .contentType(ContentType.JSON)
                .header(jwtAuthorization(ADMIN))
                .when()
                .post("/api/investigations/{investigationId}/approve", investigationId)
                .then()
                .statusCode(204)

        then:
        given()
                .header(jwtAuthorization(ADMIN))
                .param("page", "0")
                .param("size", "10")
                .contentType(ContentType.JSON)
                .when()
                .get("/api/investigations/created")
                .then()
                .statusCode(200)
                .body("page", Matchers.is(0))
                .body("pageSize", Matchers.is(10))
                .body("content", Matchers.hasSize(1))
                .body("content[0].sendTo", Matchers.is(Matchers.not(Matchers.blankOrNullString())))

        // will be fixed in: https://jira.catena-x.net/browse/TRACEFOSS-1063
        /*and:
        eventually {
            assertNotificationsSize(2)
            assertNotifications { NotificationEntity notification ->
                assert notification.edcUrl != null
                assert notification.contractAgreementId != null
            }
        }*/
    }

    def "should close investigation status"() {
        given:
        List<String> partIds = [
                "urn:uuid:fe99da3d-b0de-4e80-81da-882aebcca978" // BPN: BPNL00000003AYRE
        ]
        String description = "at least 15 characters long investigation description"
        String severity = "MINOR"
        and:
        defaultAssetsStored()

        when:
        def investigationId = given()
                .contentType(ContentType.JSON)
                .body(asJson([
                        partIds    : partIds,
                        description: description,
                        severity   : severity
                ]))
                .header(jwtAuthorization(ADMIN))
                .when()
                .post("/api/investigations")
                .then()
                .statusCode(201)
                .extract().path("id")

        then:
        assertInvestigationsSize(1)

        when:
        given()
                .contentType(ContentType.JSON)
                .header(jwtAuthorization(ADMIN))
                .when()
                .post("/api/investigations/{investigationId}/approve", investigationId)
                .then()
                .statusCode(204)

        then:
        given()
                .header(jwtAuthorization(ADMIN))
                .param("page", "0")
                .param("size", "10")
                .contentType(ContentType.JSON)
                .when()
                .get("/api/investigations/created")
                .then()
                .statusCode(200)
                .body("page", Matchers.is(0))
                .body("pageSize", Matchers.is(10))
                .body("content", Matchers.hasSize(1))
                .body("content[0].sendTo", Matchers.is(Matchers.not(Matchers.blankOrNullString())))
        when:
        given()
                .contentType(ContentType.JSON)
                .body(asJson([
                        reason: "this is the close reason for that investigation"
                ]))
                .header(jwtAuthorization(ADMIN))
                .when()
                .post("/api/investigations/{investigationId}/close", investigationId)
                .then()
                .statusCode(204)

        then:
        given()
                .header(jwtAuthorization(ADMIN))
                .param("page", "0")
                .param("size", "10")
                .contentType(ContentType.JSON)
                .when()
                .get("/api/investigations/created")
                .then()
                .statusCode(200)
                .body("page", Matchers.is(0))
                .body("pageSize", Matchers.is(10))
                .body("content", Matchers.hasSize(1))

        then:
        assertInvestigationsSize(1)
        assertInvestigationStatus(InvestigationStatus.CLOSED)
    }

    def "should not cancel not existing investigation"() {
        expect:
        given()
                .header(jwtAuthorization(ADMIN))
                .contentType(ContentType.JSON)
                .when()
                .post("/api/investigations/1/cancel")
                .then()
                .statusCode(404)
                .body("message", Matchers.is("Investigation not found for 1 id"))
    }

    @Unroll
    def "should not #action investigations without authentication"() {
        expect:
        given()
                .param("page", "0")
                .param("size", "10")
                .contentType(ContentType.JSON)
                .when()
                .post("/api/investigations/1/cancel")
                .then()
                .statusCode(401)

        where:
        action << ["approve", "cancel", "close"]
    }

    def "should be created by sender"() {
        given:
        List<String> partIds = [
                "urn:uuid:fe99da3d-b0de-4e80-81da-882aebcca978", // BPN: BPNL00000003AYRE
                "urn:uuid:d387fa8e-603c-42bd-98c3-4d87fef8d2bb", // BPN: BPNL00000003AYRE
                "urn:uuid:0ce83951-bc18-4e8f-892d-48bad4eb67ef"  // BPN: BPNL00000003AXS3
        ]
        String description = "at least 15 characters long investigation description"
        String severity = "MINOR"
        and:
        defaultAssetsStored()
        when:
        given()
                .contentType(ContentType.JSON)
                .body(
                        asJson(
                                [
                                        partIds    : partIds,
                                        description: description,
                                        severity   : severity
                                ]
                        )
                )
                .header(jwtAuthorization(ADMIN))
                .when()
                .post("/api/investigations")
                .then()
                .statusCode(201)
                .body("id", Matchers.isA(Number.class))
        then:
        partIds.each { partId ->
            Asset asset = assetRepository().getAssetById(partId)
            assert asset
            assert asset.isUnderInvestigation()
        }
        and:
        assertNotificationsSize(2)
        and:
        given()
                .header(jwtAuthorization(ADMIN))
                .param("page", "0")
                .param("size", "10")
                .contentType(ContentType.JSON)
                .when()
                .get("/api/investigations/created")
                .then()
                .statusCode(200)
                .body("page", Matchers.is(0))
                .body("pageSize", Matchers.is(10))
                .body("content", Matchers.hasSize(1))
    }

}

