package core.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Simple resource that serves a tiny Swagger UI page.
 * It prefers the MicroProfile OpenAPI document when running on Open Liberty and
 * falls back to the embedded generator output for the standalone launcher.
 */
@Path("/swagger")
public class SwaggerUiResource {

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response ui() {
        // generate OpenAPI JSON inline using SimpleOpenApiResource so the UI doesn't need to fetch /openapi
        String specJson = "{}";
        try {
            Object ent = new SimpleOpenApiResource().openapi().getEntity();
            if (ent instanceof String) {
                specJson = (String) ent;
            }
        } catch (Exception ignored) {
        }

        String html = "<!doctype html>\n" +
                "<html>\n" +
                "  <head>\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                "    <title>OpenForex API - Swagger UI</title>\n" +
                "    <link rel=\"stylesheet\" href=\"https://cdn.jsdelivr.net/npm/swagger-ui-dist@4/swagger-ui.css\">\n" +
                "  </head>\n" +
                "  <body>\n" +
                "    <div id=\"swagger-ui\"></div>\n" +
                "    <script id=\"openapi-spec\" type=\"application/json\">" + specJson + "</script>\n" +
                "    <script src=\"https://cdn.jsdelivr.net/npm/swagger-ui-dist@4/swagger-ui-bundle.js\"></script>\n" +
                "    <script>\n" +
                "      window.onload = async function() {\n" +
                "        const specEl = document.getElementById('openapi-spec');\n" +
                "        let spec = specEl ? JSON.parse(specEl.textContent) : {};\n" +
                "        try {\n" +
                "          const response = await fetch('openapi', { headers: { 'Accept': 'application/json' } });\n" +
                "          if (response.ok) {\n" +
                "            spec = await response.json();\n" +
                "          }\n" +
                "        } catch (error) {\n" +
                "          console.debug('Falling back to embedded OpenAPI spec', error);\n" +
                "        }\n" +
                "        SwaggerUIBundle({\n" +
                "          spec: spec,\n" +
                "          dom_id: '#swagger-ui',\n" +
                "          presets: [SwaggerUIBundle.presets.apis],\n" +
                "          layout: 'BaseLayout'\n" +
                "        });\n" +
                "      }\n" +
                "    </script>\n" +
                "  </body>\n" +
                "</html>\n";

        return Response.ok(html).build();
    }
}

