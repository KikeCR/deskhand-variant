package com.deskhand.api.dto;

/**
 * Uniform error body returned by the REST layer (validation failures, upstream Azure errors, etc.)
 * so API consumers get a consistent shape regardless of failure cause.
 *
 * @param message a human-readable description of what went wrong
 */
public record ErrorResponse(String message) {
}
