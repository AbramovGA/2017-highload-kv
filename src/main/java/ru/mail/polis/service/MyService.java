package ru.mail.polis.service;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.jetbrains.annotations.NotNull;
import ru.mail.polis.KVService;
import ru.mail.polis.dao.MyDAO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.NoSuchElementException;

/**
 * Created by germanium on 10.10.17.
 */
public class MyService implements KVService {
    private static final String PREFIX="id=";


    @NotNull
    private final HttpServer server;
    @NotNull
    private final MyDAO dao;

    @NotNull
    private static String extractID(@NotNull final String query){
        if(!query.startsWith(PREFIX)){
            throw new IllegalArgumentException("Bad String");
        }

        return query.substring(PREFIX.length());
    }

    public MyService(int port, @NotNull MyDAO dao) throws IOException {

        server = HttpServer.create(new InetSocketAddress(port), 0);

        this.dao=dao;

        server.createContext("/v0/status", exchange -> {
            final String response="ONLINE";
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        });

        server.createContext("/v0/entity", exchange -> {

            final String id = extractID(exchange.getRequestURI().getQuery());

            switch (exchange.getRequestMethod()){
                case "GET":
                    final byte[] getValue;
                    try {
                        getValue = dao.get(id);
                        if(getValue != null){
                            exchange.sendResponseHeaders(200, getValue.length);
                            exchange.getResponseBody().write(getValue);
                        } else{
                            exchange.sendResponseHeaders(200, 0);
                        }

                    } catch(NoSuchElementException e) {
                        exchange.sendResponseHeaders(404, 0);
                        exchange.close();
                    } catch(IllegalArgumentException e) {
                        exchange.sendResponseHeaders(400, 0);
                        exchange.close();
                    }
                    break;

                case "DELETE":
                    try {
                        dao.delete(id);
                        exchange.sendResponseHeaders(202, 0);
                    } catch (IllegalArgumentException e) {
                            exchange.sendResponseHeaders(400, 0);
                    }
                    break;

                case "PUT":

                    final int contentLength =
                            Integer.valueOf(exchange.getRequestHeaders().getFirst("Content-Length"));

                    final byte[] putValue = new byte[contentLength];

                    if(contentLength !=0) {
                        if (exchange.getRequestBody().read(putValue) != putValue.length) {
                            throw new IOException("Can't read at once");
                        }
                    }
                    try {
                        dao.upsert(id, putValue);
                        exchange.sendResponseHeaders(201, 0);
                    } catch (IllegalArgumentException e) {
                        exchange.sendResponseHeaders(400, 0);
                    }
                    break;
                default:
                    exchange.sendResponseHeaders(405,0);



            }
            exchange.close();
        });
    }

    @Override
    public void start() {
        this.server.start();
    }

    @Override
    public void stop() {
        this.server.stop(0);
    }
}
