package com.deskhand.decision;

import com.deskhand.api.dto.HireProfileRequest;
import com.deskhand.decision.model.DecisionResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure-logic tests for {@link DecisionEngine} - no mocking needed (zero Azure/Spring AI
 * dependencies), mirroring the original's fully offline decision-engine test suite.
 */
class DecisionEngineTest {

    private final DecisionEngine engine = new DecisionEngine();

    @Test
    void remoteLocationYieldsRemoteSteps() {
        HireProfileRequest hire = new HireProfileRequest(
                "Priya Nakamura", "Software Engineer II", "Engineering", "remote", "2026-09-15");

        DecisionResult result = engine.decide(hire);

        assertThat(result.locationTrack()).isEqualTo("remote");
        assertThat(result.locationSteps()).contains("Install the company VPN client and connect to the corporate network");
        assertThat(result.departmentTrack()).isEqualTo("engineering");
        assertThat(result.departmentSteps()).contains("Accept your GitHub organization invite");
        assertThat(result.explanation()).hasSize(2);
    }

    @Test
    void inOfficeLocationYieldsInOfficeSteps() {
        HireProfileRequest hire = new HireProfileRequest(
                "Marcus Delgado", "Account Executive", "Sales", "in_office", "2026-09-08");

        DecisionResult result = engine.decide(hire);

        assertThat(result.locationTrack()).isEqualTo("in_office");
        assertThat(result.locationSteps()).contains("Pick up your physical badge and access card at the front desk");
        assertThat(result.departmentTrack()).isEqualTo("sales");
        assertThat(result.departmentSteps()).contains("Get Salesforce provisioned for your account");
    }

    @Test
    void unrecognizedLocationContainingRemoteFallsBackToRemote() {
        HireProfileRequest hire = new HireProfileRequest(
                "Jordan Reyes", "Analyst", "Finance", "Remote - APAC", "2026-09-01");

        DecisionResult result = engine.decide(hire);

        assertThat(result.locationTrack()).isEqualTo("remote");
    }

    @Test
    void unrecognizedLocationNotContainingRemoteFallsBackToInOffice() {
        HireProfileRequest hire = new HireProfileRequest(
                "Jordan Reyes", "Analyst", "Finance", "HQ", "2026-09-01");

        DecisionResult result = engine.decide(hire);

        assertThat(result.locationTrack()).isEqualTo("in_office");
    }

    @Test
    void unrecognizedDepartmentFallsBackToGeneral() {
        HireProfileRequest hire = new HireProfileRequest(
                "Jordan Reyes", "Analyst", "Finance", "remote", "2026-09-01");

        DecisionResult result = engine.decide(hire);

        assertThat(result.departmentTrack()).isEqualTo("general");
        assertThat(result.departmentSteps()).contains("Get access to your team's shared drive and tools");
    }
}
