package io.vertx.example.unit.test;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunnerWithParametersFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

/**
 * Example showing a JUnit parameterized test with Vert.x . The xample is quite simple : a simple http client/server
 * request using a parameterized port.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@RunWith(Parameterized.class)
@Parameterized.UseParametersRunnerFactory(VertxUnitRunnerWithParametersFactory.class)
public class ParameterizedTest {

  /**
   * @return the test ports
   */
  @Parameterized.Parameters
  public static Iterable<Integer> ports() {
    return Arrays.asList(8080, 8081);
  }

  private final int port;
  private Vertx vertx;

  public ParameterizedTest(int port) {
    this.port = port;
  }

  @Before
  public void before() {
    vertx = Vertx.vertx();
  }

  @Test
  public void test(TestContext context) {
    HttpServer server = vertx.createHttpServer().requestHandler(req -> {
      context.assertEquals(port, req.localAddress().port());
      req.response().end();
    });
    server.listen(port, "localhost")
      .onComplete(context.asyncAssertSuccess(s -> {
      HttpClient client = vertx.createHttpClient();
      client
        .request(HttpMethod.GET, port, "localhost", "/")
        .compose(req -> req
          .send()
          .map(HttpClientResponse::statusCode))
        .onComplete(context.asyncAssertSuccess(statusCode -> {
          context.assertEquals(200, statusCode);
      }));
    }));
  }

  @After
  public void after(TestContext context) {
    vertx.close().onComplete(context.asyncAssertSuccess());
  }
}
