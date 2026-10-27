package com.accenture.externalapis.demo.client;

import com.accenture.externalapis.demo.config.ExternalServiceProperties;
import com.accenture.externalapis.demo.dto.BookApiResponse;
import com.accenture.externalapis.demo.dto.BookDto;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Component
public class BookWebClientImpl implements BookWebClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    private final WebClient webClient;

    public BookWebClientImpl(
            WebClient.Builder builder,
            ExternalServiceProperties properties) {

        this.webClient = builder
                .baseUrl(properties.baseUrl())
                .build();
    }

    @Override
    public Mono<BookDto> getBookAsync(Long id) {
        return webClient
                .get()
                .uri("/books/{id}", id)
                .retrieve()
                .bodyToMono(BookApiResponse.class)
                .switchIfEmpty(Mono.error(
                        new ClientException("Empty response for book " + id)
                ))
                .map(this::mapToDto)
                .timeout(TIMEOUT)
                .onErrorMap(error ->
                        mapError(error, "fetch book " + id));
    }

    @Override
    public Flux<BookDto> getAllBooksAsync() {
        return webClient
                .get()
                .uri("/books")
                .retrieve()
                .bodyToFlux(BookApiResponse.class)
                .map(this::mapToDto)
                .timeout(TIMEOUT)
                .onErrorMap(error ->
                        mapError(error, "fetch all books"));
    }

    @Override
    public Mono<List<BookDto>> getBooksInParallel(
            Long id1, Long id2) {

        return Mono.zip(
                getBookAsync(id1),
                getBookAsync(id2)
        ).map(tuple -> List.of(
                tuple.getT1(),
                tuple.getT2()
        ));
    }

    private BookDto mapToDto(BookApiResponse response) {
        return new BookDto(
                response.title(),
                response.author(),
                response.genre(),
                response.price()
        );
    }

    private ClientException mapError(
            Throwable error, String operation) {

        if (error instanceof ClientException clientException) {
            return clientException;
        }

        if (error instanceof WebClientResponseException ex) {
            return new ClientException(
                    "Failed to " + operation +
                            ": HTTP " + ex.getStatusCode().value(),
                    ex
            );
        }

        if (error instanceof WebClientRequestException) {
            return new ClientException(
                    "Connection error while trying to " + operation,
                    error
            );
        }

        if (error instanceof TimeoutException) {
            return new ClientException(
                    "Timeout while trying to " + operation,
                    error
            );
        }

        return new ClientException(
                "Failed to " + operation,
                error
        );
    }
}
