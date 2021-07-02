package com.sharifyy;

import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.concurrent.CompletionStage;

@Path("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookRepository bookRepository;


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public CompletionStage<Response> getAllBooks() {
        return bookRepository.findAll()
                .thenApply(books -> Response.ok(books).build())
                .exceptionally(throwable -> Response.serverError().entity(throwable.getMessage()).build());

    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public CompletionStage<Response> saveBook(Book book) {
        return bookRepository.save(book)
                .thenApply(savedBook -> Response.status(Status.CREATED).entity(savedBook).build())
                .exceptionally(throwable -> Response.serverError().entity(throwable.getMessage()).build());
    }

    @DELETE
    @Path("/{id}")
    public CompletionStage<Response> deleteBookById(@PathParam("id") Long id) {
        return bookRepository.delete(id)
                .thenApply(savedBook -> Response.status(Status.NO_CONTENT).build())
                .exceptionally(throwable -> Response.serverError().entity(throwable.getMessage()).build());
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public CompletionStage<Response> findBookById(@PathParam("id") Long id) {
        return bookRepository.findById(id)
                .thenApply(book -> Response.status(Status.OK).entity(book).build())
                .exceptionally(throwable -> {
                    System.out.println("ERROR: :(" + throwable);
                    return Response.status(Status.NOT_FOUND).entity(throwable.getMessage()).build();
                });

    }
}
