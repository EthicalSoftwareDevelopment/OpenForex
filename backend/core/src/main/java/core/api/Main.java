package core.api;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

/**
 * Main entrypoint for the OpenForex core module.
 * Starts an embedded Grizzly HTTP server with Jersey scanning the `core` package for resources.
 */
public class Main {
    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://0.0.0.0:8080/";

    public static void main(String[] args) throws IOException {
        System.out.println("OpenForex core starting...");

        final ResourceConfig rc = new ResourceConfig().packages("core");
        rc.register(SimpleOpenApiResource.class);
        rc.register(SwaggerUiResource.class);

        // Create and start a new instance of grizzly http server
        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down OpenForex core...");
            server.shutdownNow();
        }));

        System.out.println("Application started. Core REST endpoints are available under " + BASE_URI + "api");
        System.out.println("Swagger UI is available at " + BASE_URI + "swagger");
        System.out.println("Press Ctrl+C to stop.");

        try {
            Thread.currentThread().join(); // keep main thread alive until interrupted
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}


