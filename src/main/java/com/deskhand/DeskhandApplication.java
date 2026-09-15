package com.deskhand;

import me.paulschwarz.springdotenv.spring.DotenvApplicationInitializer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Entry point for both modes this jar supports: serving the REST API (default) and running the
 * one-shot document ingestion pipeline (java -jar deskhand-variant.jar --ingest), mirroring the
 * original DeskHand's single-pipeline-multiple-entry-points design (CLI / API / webhook all
 * funneling into one run_onboarding() function).
 * <p>
 * spring-dotenv ships no auto-configuration file (verified by inspecting its jar - no
 * spring.factories or AutoConfiguration.imports), so its {@link DotenvApplicationInitializer}
 * must be registered explicitly here rather than relying on the dependency alone to activate it.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class DeskhandApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(DeskhandApplication.class)
                .initializers(new DotenvApplicationInitializer())
                .run(args);
    }
}
