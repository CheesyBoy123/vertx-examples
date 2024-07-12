package io.vertx.example.shell.prompt;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Launcher;
import io.vertx.core.VertxOptions;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.ext.shell.ShellServer;
import io.vertx.ext.shell.command.CommandResolver;
import io.vertx.ext.shell.term.TelnetTermOptions;
import io.vertx.ext.shell.term.TermServer;

import java.util.concurrent.atomic.AtomicInteger;

public class PromptCommand  extends AbstractVerticle {
  public static void main(String[] args) {
    Launcher launcher = new Launcher() {
      @Override
      public void beforeStartingVertx(VertxOptions options) {
        options.setMetricsOptions(new DropwizardMetricsOptions().setEnabled(true));
      }
    };
    launcher.dispatch(new String[]{"run", PromptCommand.class.getName()});
  }

  @Override
  public void start() throws Exception {

    ShellServer server = ShellServer.create(vertx);
    AtomicInteger ai = new AtomicInteger(0);
    server.shellHandler(shell -> {
      shell.setPrompt(s -> {
        try {
          return "C: " + ai.incrementAndGet();
        } catch (Exception e) {
          System.err.println("Counter");
          e.printStackTrace();
        }
        return "NOOP";
      });
    });
    TelnetTermOptions tto = new TelnetTermOptions().setPort(3000).setHost("localhost");
    TermServer telnetTermServer = TermServer.createTelnetTermServer(vertx,tto);
    server.registerTermServer(telnetTermServer);

    server.registerCommandResolver(CommandResolver.baseCommands(vertx));

    server.listen().onComplete(ar -> {
      if (!ar.succeeded()) {
        ar.cause().printStackTrace();
      }
    });


  }
}
