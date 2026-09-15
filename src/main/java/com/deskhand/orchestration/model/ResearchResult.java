package com.deskhand.orchestration.model;

import java.util.List;

/**
 * Output of {@link com.deskhand.orchestration.step.ResearchStep}: the grounded findings gathered
 * from at least {@code deskhand.onboarding.min-research-searches} calls to
 * {@link com.deskhand.rag.SearchCompanyDocsTool}, plus a synthesized notes paragraph - the Java
 * equivalent of the original Research agent's task output ({@code research_notes}).
 * <p>
 * Carries the upstream {@link IntakeResult} forward so {@link com.deskhand.orchestration.step.ReportingStep}
 * has access to both prior outputs through a single typed input - the Java analogue of CrewAI's
 * accumulating {@code Task(context=[intake_task, research_task])} list.
 *
 * @param intake   the upstream Intake step's output, threaded through for Reporting
 * @param findings the cited findings gathered during research
 * @param notes    a short synthesized summary tying the findings together
 */
public record ResearchResult(IntakeResult intake, List<ResearchFinding> findings, String notes) {
}
