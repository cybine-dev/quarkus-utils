package de.cybine.quarkus.api.v1.library;

import de.cybine.quarkus.api.response.*;
import de.cybine.quarkus.data.library.*;
import de.cybine.quarkus.util.api.query.*;
import jakarta.validation.*;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.jboss.resteasy.reactive.*;

import java.util.*;

@Path("/api/v1/library")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface LibraryApi
{
    @POST
    RestResponse<ApiResponse<List<Library>>> fetch(@Valid ApiQuery query);

    @POST
    @Path("/find")
    RestResponse<ApiResponse<Library>> fetchSingle(@Valid ApiQuery query);

    @POST
    @Path("/count")
    RestResponse<ApiResponse<List<ApiCountInfo>>> fetchCount(@Valid ApiQuery query);

    @POST
    @Path("/options")
    RestResponse<ApiResponse<List<Map<String, Object>>>> fetchOptions(@Valid ApiQuery query);
}
