package core.api;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.UUID;
import java.util.logging.Logger;

@ApplicationScoped
@Path("/v1/trading/orders")
@Tag(name = "Order Matching", description = "Order submission and match state retrieval.")
public class TradingResource {

    private static final Logger logger = Logger.getLogger(TradingResource.class.getName());

    public static class OrderRequest {
        public String type;       // "MARKET" | "LIMIT" | "STOP"
        public String direction;  // "BUY" | "SELL"
        public double quantity;
        public String instrument;
        public Double price;      // Optional for MARKET
    }

    public static class OrderResponse {
        public String orderId;
        public String status;    // "PENDING" | "EXECUTED" | "REJECTED"

        public OrderResponse(String orderId, String status) {
            this.orderId = orderId;
            this.status = status;
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Submit a Trading Order", description = "Forwards an executed trade request from edge proxy into JVM CORE memory queues.")
    @APIResponse(responseCode = "201", content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = OrderResponse.class)
    ))
    public Response placeOrder(
            @RequestBody(description = "Order constraints", required = true,
                         content = @Content(schema = @Schema(implementation = OrderRequest.class)))
            OrderRequest request) {

        logger.info("JVM Core Received Order -> " + request.direction + " " + request.quantity + "x " + request.instrument);

        // Normally we'd queue it via OrderQueue / OrderMatcher to assess Margin limits and Pricing matching
        // For now, simulate rapid execution back toward the client via synchronous acknowledge
        OrderResponse result = new OrderResponse(UUID.randomUUID().toString(), "PENDING");

        // Simulating immediate execution for market orders
        if ("MARKET".equals(request.type)) {
            result.status = "EXECUTED";
            logger.info("Order instantly fulfilled by liquidity provider.");
        }

        return Response.status(201).entity(result).build(); // 201 Created
    }
}

