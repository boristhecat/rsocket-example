package rsocketclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.FluxSink;

@ShellComponent
public class RSocketShellClient {

  private static final Logger logger = LoggerFactory.getLogger(RSocketShellClient.class);
  private final RSocketRequester.Builder rsocketRequesterBuilder;
  private final RSocketStrategies rsocketStrategies;
  private final FluxProcessor<String, String> fluxProcessor;
  private final FluxSink<String> fluxSink;
  private RSocketRequester rsocketRequester;
  private String name;

  @Autowired
  public RSocketShellClient(RSocketRequester.Builder builder, RSocketStrategies strategies) {
    this.fluxProcessor = DirectProcessor.create();
    this.fluxSink = this.fluxProcessor.sink();
    this.rsocketRequesterBuilder = builder;
    this.rsocketStrategies = strategies;
  }

  @ShellMethod("Connect to the server")
  public void connect(String name) {
    this.name = name;
    this.rsocketRequester = rsocketRequesterBuilder
        .rsocketStrategies(rsocketStrategies)
        .connectTcp("localhost", 7000)
        .block();
  }

  @ShellMethod("Establish a channel")
  public void channel() {
    this.rsocketRequester
        .route("channel")
        .data(this.fluxProcessor.doOnNext(message -> logger.info("[Sent] {}", message)))
        .retrieveFlux(String.class)
        .subscribe(message -> logger.info("[Received] {}", message));
  }

  @ShellMethod("Send a lower case message")
  public void send(String message) {
    this.fluxSink.next(message.toLowerCase());
  }
}

