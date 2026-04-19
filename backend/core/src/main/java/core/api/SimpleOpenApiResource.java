package core.api;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.temporal.Temporal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight OpenAPI JSON generator for the embedded Grizzly/Jersey runtime.
 * Open Liberty uses MicroProfile OpenAPI as the primary document source.
 */
@Path("/openapi")
public class SimpleOpenApiResource {
    private static final Set<Class<?>> SIMPLE_TYPES = Set.of(
            String.class, Boolean.class, boolean.class,
            Integer.class, int.class, Long.class, long.class,
            Double.class, double.class, Float.class, float.class,
            Short.class, short.class, Byte.class, byte.class
    );

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response openapi() {
        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title("OpenForex API")
                        .version("1.0.0")
                        .description("Auto-generated fallback OpenAPI document for the embedded runtime."));

        Paths paths = new Paths();
        Components components = new Components();
        Map<Type, String> schemaNames = new ConcurrentHashMap<>();

        try (ScanResult scanResult = new ClassGraph().acceptPackages("core").enableAllInfo().scan()) {
            List<ClassInfo> classes = scanResult.getAllClasses();
            for (ClassInfo classInfo : classes) {
                Class<?> resourceClass = classInfo.loadClass();
                if (resourceClass.equals(SimpleOpenApiResource.class) || resourceClass.equals(SwaggerUiResource.class)) {
                    continue;
                }

                Path classPath = resourceClass.getAnnotation(Path.class);
                if (classPath == null) {
                    continue;
                }

                for (Method method : resourceClass.getDeclaredMethods()) {
                    String httpMethod = resolveHttpMethod(method);
                    if (httpMethod == null) {
                        continue;
                    }

                    String methodPath = method.isAnnotationPresent(Path.class) ? method.getAnnotation(Path.class).value() : "";
                    String fullPath = normalizePath(classPath.value(), methodPath);
                    PathItem pathItem = paths.get(fullPath);
                    if (pathItem == null) {
                        pathItem = new PathItem();
                        paths.addPathItem(fullPath, pathItem);
                    }

                    Operation operation = new Operation()
                            .operationId(resourceClass.getSimpleName() + "_" + method.getName())
                            .summary(toSummary(resourceClass.getSimpleName(), method.getName()))
                            .description("Fallback generated operation for " + resourceClass.getSimpleName() + "." + method.getName() + "().");

                    RequestBody requestBody = buildRequestBody(method, components, schemaNames);
                    if (requestBody != null) {
                        operation.requestBody(requestBody);
                    }

                    for (java.lang.reflect.Parameter parameter : method.getParameters()) {
                        Parameter openApiParameter = buildParameter(parameter);
                        if (openApiParameter != null) {
                            operation.addParametersItem(openApiParameter);
                        }
                    }

                    ApiResponses responses = new ApiResponses();
                    responses.addApiResponse("200", new ApiResponse()
                            .description("Successful response")
                            .content(buildJsonContent(inferResponseSchema(resourceClass, method, components, schemaNames))));
                    responses.addApiResponse("400", new ApiResponse().description("Validation error or invalid request"));
                    operation.responses(responses);

                    attachOperation(pathItem, httpMethod, operation);
                }
            }
        } catch (Exception ignored) {
            // keep returning a valid minimal document even if runtime scanning fails
        }

        openAPI.components(components);
        openAPI.paths(paths);
        return Response.ok(Json.pretty(openAPI), MediaType.APPLICATION_JSON).build();
    }

    private void attachOperation(PathItem pathItem, String httpMethod, Operation operation) {
        switch (httpMethod) {
            case "GET" -> pathItem.get(operation);
            case "POST" -> pathItem.post(operation);
            case "PUT" -> pathItem.put(operation);
            case "DELETE" -> pathItem.delete(operation);
            case "PATCH" -> pathItem.patch(operation);
            case "HEAD" -> pathItem.head(operation);
            case "OPTIONS" -> pathItem.options(operation);
            default -> {
            }
        }
    }

    private String resolveHttpMethod(Method method) {
        if (method.isAnnotationPresent(GET.class)) return "GET";
        if (method.isAnnotationPresent(POST.class)) return "POST";
        if (method.isAnnotationPresent(PUT.class)) return "PUT";
        if (method.isAnnotationPresent(DELETE.class)) return "DELETE";
        if (method.isAnnotationPresent(PATCH.class)) return "PATCH";
        if (method.isAnnotationPresent(HEAD.class)) return "HEAD";
        if (method.isAnnotationPresent(OPTIONS.class)) return "OPTIONS";
        return null;
    }

    private String normalizePath(String classPath, String methodPath) {
        String joined = ("/" + trimSlashes(classPath) + "/" + trimSlashes(methodPath)).replaceAll("//+", "/");
        return joined.endsWith("/") && joined.length() > 1 ? joined.substring(0, joined.length() - 1) : joined;
    }

    private String trimSlashes(String value) {
        if (value == null || value.isBlank() || "/".equals(value)) {
            return "";
        }
        String trimmed = value.trim();
        while (trimmed.startsWith("/")) {
            trimmed = trimmed.substring(1);
        }
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private String toSummary(String className, String methodName) {
        String readableMethod = methodName.replaceAll("([a-z])([A-Z])", "$1 $2").toLowerCase();
        return className + ": " + Character.toUpperCase(readableMethod.charAt(0)) + readableMethod.substring(1);
    }

    private Parameter buildParameter(java.lang.reflect.Parameter parameter) {
        if (parameter.isAnnotationPresent(Context.class) || parameter.isAnnotationPresent(BeanParam.class)) {
            return null;
        }

        Parameter openApiParameter = null;
        if (parameter.isAnnotationPresent(PathParam.class)) {
            openApiParameter = new Parameter().in("path").required(true).name(parameter.getAnnotation(PathParam.class).value());
        } else if (parameter.isAnnotationPresent(QueryParam.class)) {
            openApiParameter = new Parameter().in("query").name(parameter.getAnnotation(QueryParam.class).value());
        } else if (parameter.isAnnotationPresent(HeaderParam.class)) {
            openApiParameter = new Parameter().in("header").name(parameter.getAnnotation(HeaderParam.class).value());
        } else if (parameter.isAnnotationPresent(CookieParam.class)) {
            openApiParameter = new Parameter().in("cookie").name(parameter.getAnnotation(CookieParam.class).value());
        }

        if (openApiParameter == null) {
            return null;
        }

        openApiParameter.schema(toPrimitiveSchema(parameter.getParameterizedType()));
        if (parameter.isAnnotationPresent(DefaultValue.class)) {
            openApiParameter.setExample(parameter.getAnnotation(DefaultValue.class).value());
        }
        return openApiParameter;
    }

    private RequestBody buildRequestBody(Method method, Components components, Map<Type, String> schemaNames) {
        for (java.lang.reflect.Parameter parameter : method.getParameters()) {
            if (parameter.isAnnotationPresent(Context.class)
                    || parameter.isAnnotationPresent(BeanParam.class)
                    || parameter.isAnnotationPresent(PathParam.class)
                    || parameter.isAnnotationPresent(QueryParam.class)
                    || parameter.isAnnotationPresent(HeaderParam.class)
                    || parameter.isAnnotationPresent(CookieParam.class)) {
                continue;
            }

            Schema<?> schema = toSchema(parameter.getParameterizedType(), components, schemaNames);
            return new RequestBody().required(true).content(buildJsonContent(schema));
        }
        return null;
    }

    private Content buildJsonContent(Schema<?> schema) {
        if (schema == null) {
            return null;
        }
        return new Content().addMediaType(MediaType.APPLICATION_JSON, new io.swagger.v3.oas.models.media.MediaType().schema(schema));
    }

    private Schema<?> inferResponseSchema(Class<?> resourceClass, Method method, Components components, Map<Type, String> schemaNames) {
        if (!Response.class.equals(method.getReturnType())) {
            return toSchema(method.getGenericReturnType(), components, schemaNames);
        }

        Class<?> requestType = null;
        for (java.lang.reflect.Parameter parameter : method.getParameters()) {
            if (!parameter.isAnnotationPresent(PathParam.class)
                    && !parameter.isAnnotationPresent(QueryParam.class)
                    && !parameter.isAnnotationPresent(HeaderParam.class)
                    && !parameter.isAnnotationPresent(CookieParam.class)
                    && !parameter.isAnnotationPresent(Context.class)
                    && !parameter.isAnnotationPresent(BeanParam.class)) {
                requestType = parameter.getType();
                break;
            }
        }

        for (Class<?> nestedClass : resourceClass.getDeclaredClasses()) {
            if (nestedClass.equals(requestType)) {
                continue;
            }
            String simpleName = nestedClass.getSimpleName().toLowerCase();
            if (simpleName.contains("result") || simpleName.contains("snapshot") || simpleName.contains("tick")) {
                return toSchema(nestedClass, components, schemaNames);
            }
        }
        return new ObjectSchema().description("Opaque response entity");
    }

    private Schema<?> toSchema(Type type, Components components, Map<Type, String> schemaNames) {
        if (type instanceof Class<?> clazz) {
            if (clazz.isEnum()) {
                Schema<String> schema = new Schema<>();
                schema.setType("string");
                schema.setEnum(java.util.Arrays.stream(clazz.getEnumConstants()).map(String::valueOf).toList());
                return schema;
            }
            if (SIMPLE_TYPES.contains(clazz)) {
                return toPrimitiveSchema(clazz);
            }
            if (Number.class.isAssignableFrom(clazz)) {
                return new NumberSchema();
            }
            if (clazz.isArray()) {
                return new ArraySchema().items(toSchema(clazz.getComponentType(), components, schemaNames));
            }
            if (Map.class.isAssignableFrom(clazz)) {
                return new ObjectSchema().additionalProperties(new ObjectSchema());
            }
            if (clazz.equals(Void.class) || clazz.equals(void.class)) {
                return null;
            }
            if (clazz.equals(String.class) || Temporal.class.isAssignableFrom(clazz)) {
                return new Schema<>().type("string");
            }

            String schemaName = schemaNames.computeIfAbsent(type, ignored -> clazz.getSimpleName());
            if (components.getSchemas() != null && components.getSchemas().containsKey(schemaName)) {
                return new Schema<>().$ref("#/components/schemas/" + schemaName);
            }

            ObjectSchema objectSchema = new ObjectSchema();
            objectSchema.setDescription("Generated schema for " + clazz.getSimpleName());
            objectSchema.setProperties(new LinkedHashMap<>());
            components.addSchemas(schemaName, objectSchema);

            for (Field field : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                    continue;
                }
                objectSchema.addProperties(field.getName(), toSchema(field.getGenericType(), components, schemaNames));
            }

            if (objectSchema.getProperties().isEmpty()) {
                for (Method getter : clazz.getMethods()) {
                    if (Modifier.isStatic(getter.getModifiers()) || getter.getParameterCount() != 0 || getter.getDeclaringClass().equals(Object.class)) {
                        continue;
                    }
                    String propertyName = getter.getName().startsWith("get") && getter.getName().length() > 3
                            ? Character.toLowerCase(getter.getName().charAt(3)) + getter.getName().substring(4)
                            : getter.getName().startsWith("is") && getter.getName().length() > 2
                            ? Character.toLowerCase(getter.getName().charAt(2)) + getter.getName().substring(3)
                            : null;
                    if (propertyName != null && !propertyName.equals("class")) {
                        objectSchema.addProperties(propertyName, toSchema(getter.getGenericReturnType(), components, schemaNames));
                    }
                }
            }

            return new Schema<>().$ref("#/components/schemas/" + schemaName);
        }

        if (type instanceof ParameterizedType parameterizedType) {
            Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class<?> rawClass && List.class.isAssignableFrom(rawClass)) {
                return new ArraySchema().items(toSchema(parameterizedType.getActualTypeArguments()[0], components, schemaNames));
            }
            if (rawType instanceof Class<?> rawClass && Map.class.isAssignableFrom(rawClass)) {
                return new ObjectSchema().additionalProperties(toSchema(parameterizedType.getActualTypeArguments()[1], components, schemaNames));
            }
            return toSchema(rawType, components, schemaNames);
        }

        return new ObjectSchema();
    }

    private Schema<?> toPrimitiveSchema(Type type) {
        if (!(type instanceof Class<?> clazz)) {
            return new Schema<>().type("string");
        }
        if (clazz.equals(Boolean.class) || clazz.equals(boolean.class)) {
            return new BooleanSchema();
        }
        if (clazz.equals(Integer.class) || clazz.equals(int.class)
                || clazz.equals(Long.class) || clazz.equals(long.class)
                || clazz.equals(Short.class) || clazz.equals(short.class)
                || clazz.equals(Byte.class) || clazz.equals(byte.class)) {
            return new IntegerSchema();
        }
        if (clazz.equals(Float.class) || clazz.equals(float.class)
                || clazz.equals(Double.class) || clazz.equals(double.class)) {
            return new NumberSchema();
        }
        return new Schema<>().type("string");
    }
}

