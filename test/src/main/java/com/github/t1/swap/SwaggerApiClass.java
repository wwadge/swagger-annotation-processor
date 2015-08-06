package com.github.t1.swap;

import javax.ws.rs.*;

import io.swagger.annotations.*;

@Api
@Path("/p")
public class SwaggerApiClass {
    @ApiModel
    public enum SwaggerEnumModel {
        A,
        B;
    }

    @GET
    @ApiOperation("get-op")
    public SwaggerEnumModel getEnum() {
        return SwaggerEnumModel.A;
    }
}
