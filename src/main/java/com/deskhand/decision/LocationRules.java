package com.deskhand.decision;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Lookup table for the location branch of the decision engine. Ported as-is from the original's
 * two hardcoded tracks: "remote" (ship laptop, VPN, stipend, remote-work policy acknowledgment)
 * and "in_office" (badge, desk/locker, on-site IT help desk, parking/transit). An unrecognized
 * location string falls back to "remote" if it contains that substring, otherwise "in_office" -
 * same fallback rule as the original.
 */
final class LocationRules {

    static final String REMOTE = "remote";
    static final String IN_OFFICE = "in_office";

    private static final Map<String, List<String>> STEPS = Map.of(
            REMOTE, List.of(
                    "IT ships a company laptop and docking station to your home address",
                    "Install the company VPN client and connect to the corporate network",
                    "Submit the one-time $500 home-office equipment stipend request",
                    "Read and electronically acknowledge the Remote Work Policy"
            ),
            IN_OFFICE, List.of(
                    "Pick up your physical badge and access card at the front desk",
                    "Confirm your assigned desk (and locker, if requested) with Facilities",
                    "Visit the on-site IT help desk to pick up your laptop and peripherals",
                    "Collect your parking permit or public-transit subsidy card"
            )
    );

    private LocationRules() {
    }

    static String normalizeTrack(String rawLocation) {
        String normalized = rawLocation == null
                ? ""
                : rawLocation.trim().toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        if (STEPS.containsKey(normalized)) {
            return normalized;
        }
        return normalized.contains(REMOTE) ? REMOTE : IN_OFFICE;
    }

    static List<String> stepsFor(String locationTrack) {
        return STEPS.getOrDefault(locationTrack, STEPS.get(IN_OFFICE));
    }
}
