package com.deskhand.api;

import com.deskhand.api.dto.HireProfileRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * The Java equivalent of the original's {@code GET /api/sample-hires}: returns a couple of
 * hardcoded sample hire profiles for quick demoing without hand-typing JSON, mirroring the
 * original's two contrasting examples (a remote engineer, an in-office sales hire) used
 * throughout its README and example outputs.
 * <p>
 * Implemented in the API-layer phase.
 */
@RestController
@RequestMapping("/api/sample-hires")
public class SampleHiresController {

    @GetMapping
    public List<HireProfileRequest> list() {
        return List.of(
                new HireProfileRequest("Priya Nakamura", "Software Engineer II", "Engineering", "remote", "2026-09-15"),
                new HireProfileRequest("Marcus Delgado", "Account Executive", "Sales", "in_office", "2026-09-08")
        );
    }
}
