package io.vertx.example.reactivex.database.sqlclient;

import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.vertx.core.Launcher;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.jdbcclient.JDBCPool;
import io.vertx.reactivex.sqlclient.Row;
import io.vertx.reactivex.sqlclient.RowSet;
import io.vertx.reactivex.sqlclient.SqlConnection;

/*
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
public class Streaming extends AbstractVerticle {

  public static void main(String[] args) {
    Launcher.executeCommand("run", Streaming.class.getName());
  }

  @Override
  public void start() throws Exception {

    JsonObject config = new JsonObject().put("url", "jdbc:hsqldb:mem:test?shutdown=true")
      .put("driver_class", "org.hsqldb.jdbcDriver");

    JDBCPool jdbc = JDBCPool.pool(vertx, config);

    // Connect to the database
    Maybe<RowSet<Row>> maybe = jdbc.rxWithConnection((Function<SqlConnection, Maybe<RowSet<Row>>>) conn -> {
      // With the connection...
      Single<RowSet<Row>> single = conn
        // ...create test table
        .query("CREATE TABLE test(col VARCHAR(20))").rxExecute()
        // ...insert a row
        .flatMap(result -> conn.query("INSERT INTO test (col) VALUES ('val1')").rxExecute())
        // ...another one
        .flatMap(result -> conn.query("INSERT INTO test (col) VALUES ('val2')").rxExecute())
        // ...get values
        .flatMap(result -> conn.query("SELECT * FROM test").rxExecute());
      return Maybe.fromSingle(single);
    });

    maybe.subscribe(rows -> {
      for (Row row : rows) {
        System.out.println("Row : " + row.toJson().encode());
      }
    });
  }
}
