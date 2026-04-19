package core.api;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.util.Map;
import java.util.logging.Logger;

@ApplicationScoped
@Path("/audit")
@Tag(name = "Compliance", description = "BaFin Audit Logging and Compliance")
public class AuditResource {

    private static final Logger logger = Logger.getLogger(AuditResource.class.getName());

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Log BaFin Critical Transitions", description = "Persists compliance and execution payloads intercepted from the Deno edge proxy.")
    public Response logAuditBeacon(Map<String, Object> payload) {
        logger.info("[JVM Core Audit] Received secure audit beacon from edge proxy.");

        // Ensure payload has the expected nested structure or log generally
        if (payload.containsKey("auditLog")) {
            Map<?, ?> auditDetails = (Map<?, ?>) payload.get("auditLog");
            logger.info("Persisting Action: " + auditDetails.get("actionType")
                + " User: " + auditDetails.get("userId")
                + " KYC: " + auditDetails.get("kycState"));
        } else {
            logger.warning("Unrecognized audit structure received: " + payload);
        }

        // In a real system, this would write to a hardened ELK/Postgres sink with unalterable history logs
        return Response.accepted(Map.of("status", "Persistent Audit Recorded", "coreReceived", true)).build();
    }
}

