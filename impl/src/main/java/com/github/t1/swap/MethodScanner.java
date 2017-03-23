package com.github.t1.swap;

import com.github.t1.exap.reflection.*;
import com.github.t1.exap.reflection.Parameter;
import io.swagger.annotations.*;
import io.swagger.models.*;
import io.swagger.models.parameters.*;
import org.slf4j.*;

import javax.ws.rs.*;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Context;
import java.util.List;

import static com.github.t1.swap.Helpers.*;

public class MethodScanner {
    private static final Logger log = LoggerFactory.getLogger(MethodScanner.class);

    private final Method method;
    private final Swagger swagger;
    private final String typePath;
    private final List<String> defaultTags;

    public MethodScanner(Method method, Swagger swagger, String typePath, List<String> defaultTags) {
        this.method = method;
        this.swagger = swagger;
        this.typePath = typePath;
        this.defaultTags = defaultTags;
    }

    public void scan() {
        String type = httpMethodType();
        if (type == null)
            return;
        log.debug("scan {} method {}", type, method);
        io.swagger.models.Path pathModel = pathModel();
        set(pathModel, type, operation());
    }

    public String httpMethodType() {
        for (AnnotationWrapper annotation : method.getAnnotationWrappers())
            if (annotation.getAnnotation(HttpMethod.class) != null)
                return annotation.getAnnotation(HttpMethod.class).value();
        return null;
    }

    private io.swagger.models.Path pathModel() {
        String methodPath = methodPath();
        io.swagger.models.Path pathModel = swagger.getPath(methodPath);
        if (pathModel == null) {
            pathModel = new io.swagger.models.Path();
            swagger.path(methodPath, pathModel);
        }
        return pathModel;
    }

    private String methodPath() {
        javax.ws.rs.Path methodPath = method.getAnnotation(javax.ws.rs.Path.class);
        if (methodPath == null)
            return typePath;
        return typePath + prefixedPath(methodPath.value());
    }

    private Operation operation() {
        Operation operation = new Operation()
                .operationId(method.getName())
                .deprecated(method.getAnnotation(Deprecated.class) != null);
        scanApiOperation(operation);
        scanResponses(operation);
        scanParams(operation);
        return operation;
    }

    private void scanApiOperation(Operation operation) {
        ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);
        if (apiOperation != null) {
            operation.setSummary(apiOperation.value());
            operation.setDescription(nonEmpty(apiOperation.notes()));
            for (String tag : apiOperation.tags())
                if (!tag.isEmpty())
                    operation.addTag(tag);
        }
        if (operation.getTags() == null)
            operation.setTags(defaultTags);
    }

    private void scanResponses(Operation operation) {
        ApiResponses responses = method.getAnnotation(ApiResponses.class);
        if (responses == null)
            return;
        for (ApiResponse response : responses.value())
            operation.addResponse(Integer.toString(response.code()), new Response()
                    .description(response.message())
            );
    }

    private void scanParams(Operation operation) {
        for (Parameter param : method.getParameters()) {
            if (param.getAnnotation(Context.class) != null)
                continue;
            HeaderParam headerParam = param.getAnnotation(HeaderParam.class);
            MatrixParam matrixParam = param.getAnnotation(MatrixParam.class);
            QueryParam queryParam = param.getAnnotation(QueryParam.class);
            PathParam pathParam = param.getAnnotation(PathParam.class);

            io.swagger.models.parameters.Parameter paramModel;

            if (headerParam != null) {
                paramModel = new HeaderParameter().name(headerParam.value());
            } else if (matrixParam != null) {
                log.warn("matrix params are not supported by Swagger; treating like a body param {}", param);
                paramModel = new BodyParameter();
            } else if (queryParam != null) {
                paramModel = new QueryParameter().name(queryParam.value());
            } else if (pathParam != null) {
                paramModel = new PathParameter().name(pathParam.value());
            } else {
                paramModel = new BodyParameter().name("body");
            }
            operation.addParameter(paramModel);
            scan(param.getAnnotation(ApiParam.class), paramModel);
        }
    }

    private void scan(ApiParam apiParam, io.swagger.models.parameters.Parameter model) {
        if (apiParam == null)
            return;
        if (!apiParam.name().isEmpty())
            model.setName(apiParam.name());
        if (!apiParam.value().isEmpty())
            model.setDescription(apiParam.value());
    }

    private void set(io.swagger.models.Path pathModel, String type, Operation operation) {
        try {
            java.lang.reflect.Method setter =
                    io.swagger.models.Path.class.getMethod(type.toLowerCase(), Operation.class);
            setter.invoke(pathModel, operation);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("no method found for " + type, e);
        }
    }
}
