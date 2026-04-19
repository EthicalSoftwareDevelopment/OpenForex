package core.api;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Activates Jakarta REST resources for the OpenForex core module.
 */
@ApplicationPath("/api")
public class OpenForexApplication extends Application {
}

