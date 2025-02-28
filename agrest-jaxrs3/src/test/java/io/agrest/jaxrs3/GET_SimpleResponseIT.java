package io.agrest.jaxrs3;

import io.agrest.SimpleResponse;
import io.agrest.jaxrs3.junit.AgPojoTester;
import io.agrest.jaxrs3.junit.PojoTest;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

public class GET_SimpleResponseIT extends PojoTest {

    @BQTestTool
    static final AgPojoTester tester = PojoTest.tester(Resource.class).build();

    @Test
    public void testWrite() {

        tester.target("/simple").get()
                .wasOk()
                .bodyEquals("{\"message\":\"Hi!\"}");

        tester.target("/simple/2").get()
                .wasOk()
                .bodyEquals("{\"message\":\"Hi2!\"}");
    }

    @Path("simple")
    public static class Resource {

        @GET
        public SimpleResponse get() {
            return SimpleResponse.of(200, "Hi!");
        }

        @GET
        @Path("2")
        public SimpleResponse get2() {
            return SimpleResponse.of(200, "Hi2!");
        }
    }
}
