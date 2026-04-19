package core.health;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

/**
 * Reports whether the core service process is alive.
 */
@ApplicationScoped
@Liveness
public class CoreLivenessCheck implements HealthCheck {
    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.named("core-liveness")
                .up()
                .withData("module", "backend-core")
                .withData("runtime", "openliberty")
                .build();
    }
}


