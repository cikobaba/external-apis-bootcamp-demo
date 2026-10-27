package com.accenture.externalapis.demo.client;

import com.accenture.externalapis.demo.config.ExternalServiceProperties;
import com.accenture.externalapis.demo.dto.BookApiResponse;
import com.accenture.externalapis.demo.dto.BookDto;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Component
public class BookRestClientImpl implements BookRestClient {

    private final RestClient restClient;

    public BookRestClientImpl(
            RestClient.Builder builder,
            ExternalServiceProperties properties) {

        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(3));

        this.restClient = builder
                .baseUrl(properties.baseUrl())
                .requestFactory(factory)
                .build();
    }

    @Override
    public BookDto getBook(Long id) {
        try {
            BookApiResponse response = restClient
                    .get()
                    .uri("/books/{id}", id)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, res) -> {
                        throw new ClientException(
                                "External API returned HTTP " +
                                        res.getStatusCode().value()
                        );
                    })
                    .body(BookApiResponse.class);

            if (response == null) {
                throw new ClientException("Empty book response");
            }

            return mapToDto(response);

        } catch (RestClientException e) {
            throw new ClientException(
                    "Failed to fetch book with ID: " + id, e
            );
        }
    }

    @Override
    public List<BookDto> getAllBooks() {
        try {
            BookApiResponse[] responses = restClient
                    .get()
                    .uri("/books")
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, res) -> {
                        throw new ClientException(
                                "External API returned HTTP " +
                                        res.getStatusCode().value()
                        );
                    })
                    .body(BookApiResponse[].class);

            if (responses == null) {
                throw new ClientException("Empty books response");
            }

            return Arrays.stream(responses)
                    .map(this::mapToDto)
                    .toList();

        } catch (RestClientException e) {
            throw new ClientException(
                    "Failed to fetch books", e
            );
        }
    }

    private BookDto mapToDto(BookApiResponse response) {
        return new BookDto(
                response.title(),
                response.author(),
                response.genre(),
                response.price()
        );
    }
}
