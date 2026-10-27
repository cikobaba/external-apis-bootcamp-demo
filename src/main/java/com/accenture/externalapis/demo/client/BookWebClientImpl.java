package com.accenture.externalapis.demo.client;

import com.accenture.externalapis.demo.dto.BookDto;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class BookWebClientImpl implements BookWebClient {

    @Override
    public Mono<BookDto> getBookAsync(Long id) {
        return Mono.error(
                new UnsupportedOperationException("Task 2 not implemented")
        );
    }

    @Override
    public Flux<BookDto> getAllBooksAsync() {
        return Flux.error(
                new UnsupportedOperationException("Task 2 not implemented")
        );
    }

    @Override
    public Mono<List<BookDto>> getBooksInParallel(Long id1, Long id2) {
        return Mono.error(
                new UnsupportedOperationException("Task 2 not implemented")
        );
    }
}
