package core.api;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.servers.Server;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Activates Jakarta REST resources for the OpenForex core module.
 */
@OpenAPIDefinition(
		info = @Info(
				title = "OpenForex Core API",
				version = "1.0.0",
				description = "Core trading, execution, pricing, portfolio, and analytics endpoints for OpenForex.",
				contact = @Contact(name = "OpenForex", email = "support@openforex.local")
		),
		servers = {
				@Server(url = "/", description = "Open Liberty or embedded runtime root")
		},
		tags = {
				@Tag(name = "Order Matching", description = "Order submission and match state retrieval."),
				@Tag(name = "Execution", description = "Execution policy and fill simulation endpoints."),
				@Tag(name = "Portfolio", description = "Portfolio state and trade/cash updates."),
				@Tag(name = "Price Feed", description = "Synthetic and live price feed access."),
				@Tag(name = "Leverage", description = "Leverage and margin calculations.")
		}
)
@ApplicationPath("/api")
public class OpenForexApplication extends Application {
}

