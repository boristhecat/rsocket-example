package rsocketserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.Disposable;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.FluxSink;

import java.time.Duration;

@Controller
public class RSocketController {

  private static final Logger logger = LoggerFactory.getLogger(RSocketController.class);
  private final FluxProcessor<String, String> processor;
  private final FluxSink<String> sink;

  public RSocketController() {
    this.processor = DirectProcessor.<String>create().serialize();
    this.sink = this.processor.sink();
  }

  @MessageMapping("channel")
  Flux<String> channel(final Flux<String> messages) {
    this.registerProducer(messages);
    return processor.doOnNext(message -> logger.info("[Sent] " + message));
  }

  private Disposable registerProducer(Flux<String> flux) {
    return flux
        .doOnNext(message -> logger.info("[Received] " + message))
        .map(String::toUpperCase)
        .delayElements(Duration.ofSeconds(1))
        .subscribe(this.sink::next);
  }
}
