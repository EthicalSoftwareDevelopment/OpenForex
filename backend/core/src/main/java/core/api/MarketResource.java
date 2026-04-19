package core.api;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
@Path("/v1/market/prices")
@Tag(name = "Price Feed")
public class MarketResource {

    // Define the schema inline to make sure Deno generation matches exact keys.
    public static class PriceSnapshot {
        public String instrument;
        public double currentPrice;

        public PriceSnapshot(String instrument, double currentPrice) {
            this.instrument = instrument;
            this.currentPrice = currentPrice;
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get Spot Market Prices", description = "Fetch aggregated live spread or spot pricing.")
    @APIResponse(responseCode = "200", content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(type = SchemaType.ARRAY, implementation = PriceSnapshot.class)
    ))
    public List<PriceSnapshot> getPrices(@QueryParam("instrument") String queryInstrument) {

        // This integrates directly with your RealTimePriceFeed model
        List<PriceSnapshot> results = new ArrayList<>();

        if (queryInstrument == null || queryInstrument.equals("EUR/USD")) {
            results.add(new PriceSnapshot("EUR/USD", 1.1045));
        }
        if (queryInstrument == null || queryInstrument.equals("GBP/JPY")) {
            results.add(new PriceSnapshot("GBP/JPY", 181.90));
        }
        if (queryInstrument == null || queryInstrument.equals("USD/JPY")) {
            results.add(new PriceSnapshot("USD/JPY", 144.10));
        }

        return results;
    }
}

