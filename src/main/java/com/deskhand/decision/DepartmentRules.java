package com.deskhand.decision;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Lookup table for the department branch of the decision engine. Ported as-is from the original's
 * table: "engineering" and "sales" get named tracks with tailored steps; any other department
 * normalizes to "general" and gets one generic shared-drive/tools step - same fallback behavior as
 * the original's DEFAULT_DEPARTMENT_STEPS.
 */
final class DepartmentRules {

    static final String ENGINEERING = "engineering";
    static final String SALES = "sales";
    static final String GENERAL = "general";

    private static final Map<String, List<String>> STEPS = Map.of(
            ENGINEERING, List.of(
                    "Accept your GitHub organization invite",
                    "Get access to the cloud sandbox environment",
                    "Complete local dev environment setup",
                    "Shadow one on-call rotation as an observer"
            ),
            SALES, List.of(
                    "Get Salesforce provisioned for your account",
                    "Complete sales enablement training",
                    "Review the commission plan with your manager",
                    "Shadow two live sales calls"
            ),
            GENERAL, List.of(
                    "Get access to your team's shared drive and tools"
            )
    );

    private DepartmentRules() {
    }

    static String normalizeTrack(String rawDepartment) {
        String normalized = rawDepartment == null
                ? ""
                : rawDepartment.trim().toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        return STEPS.containsKey(normalized) ? normalized : GENERAL;
    }

    static List<String> stepsFor(String departmentTrack) {
        return STEPS.getOrDefault(departmentTrack, STEPS.get(GENERAL));
    }
}
