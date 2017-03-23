package com.github.t1.swap;

import io.swagger.annotations.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

/** The main API */
@Api
@Path("/p")
public class SwaggerApiClass {
    /** get-op. description */
    @GET
    @Path("some/{path-param}/path")
    @SuppressWarnings("unused")
    public SwaggerEnumModel getEnum(
            @Context UriInfo uriInfo,
            @PathParam("path-param") String pathParam,
            @HeaderParam("header-param") String headerParam,
            @QueryParam("query-param") String queryParam,
            @MatrixParam("matrix-param-0") String matrixParam0,
            @MatrixParam("matrix-param-1") String matrixParam1
    ) {
        return SwaggerEnumModel.A;
    }
}
