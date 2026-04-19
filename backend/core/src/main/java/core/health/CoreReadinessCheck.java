package core.health;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

/**
 * Reports whether the core service is ready to receive requests.
 */
@ApplicationScoped
@Readiness
public class CoreReadinessCheck implements HealthCheck {
    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.named("core-readiness")
                .up()
                .withData("restBasePath", "/core/api")
                .withData("openApiPath", "/openapi")
                .build();
    }
}


