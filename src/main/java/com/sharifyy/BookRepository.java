package com.sharifyy;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.SqlResult;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@ApplicationScoped
@RequiredArgsConstructor
public class BookRepository {

    private final PgPool client;
    private static final Long TIMEOUT = 3L;


    public CompletableFuture<List<Book>> findAll() {
        return client.query("SELECT * FROM books")
                .execute().convert().toCompletableFuture()
                .thenApply(rows -> StreamSupport.stream(rows.spliterator(), false)
                        .map(this::from)
                        .collect(Collectors.toList()))
                .orTimeout(TIMEOUT, TimeUnit.SECONDS);
    }

    public CompletableFuture<Book> save(Book data) {
        return client.preparedQuery("INSERT INTO books(title, author) VALUES ($1, $2) RETURNING *")
                .execute(Tuple.of(data.getTitle(), data.getAuthor())).convert().toCompletableFuture()
                .thenApply(rows -> StreamSupport.stream(rows.spliterator(), false)
                        .map(this::from).findFirst().orElseThrow(() -> new RuntimeException("insertion error :("))
                ).orTimeout(TIMEOUT, TimeUnit.SECONDS);
    }

    public CompletableFuture<Long> delete(Long id) {
        return client.preparedQuery("DELETE FROM books WHERE id=$1")
                .execute(Tuple.of(id)).convert().toCompletableFuture()
                .thenApply(SqlResult::rowCount)
                .thenApply(Integer::longValue)
                .orTimeout(TIMEOUT, TimeUnit.SECONDS);

    }

    public CompletableFuture<Book> findById(Long id) {
        return client.preparedQuery("SELECT id, title,author FROM books WHERE id = $1")
                .execute(Tuple.of(id))
                .convert().toCompletableFuture()
                .thenApply(rows ->
                        StreamSupport.stream(rows.spliterator(), false)
                                .map(this::from)
                                .findFirst()
                                .orElseThrow(() -> new NotFoundException("book not found"))
                );
    }

    private Book from(Row row) {
        return new Book(row.getLong("id"), row.getString("title"), row.getString("author"));
    }
}
