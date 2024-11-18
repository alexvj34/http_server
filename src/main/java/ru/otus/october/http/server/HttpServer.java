package ru.otus.october.http.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServer {
    private int port;
    private final Dispatcher dispatcher;
    private final ExecutorService executorService;
    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class); // Логгер теперь статический

    public HttpServer(int port) {
        this.port = port;
        this.dispatcher = new Dispatcher();
        this.executorService = Executors.newFixedThreadPool(10);
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Сервер запущен на порту: " + port);
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    executorService.submit(() -> handleRequest(socket));
                } catch (IOException e) {
                    logger.error("Произошла ошибка при принятии соединения: ", e);
                }
            }
        } catch (IOException e) {
            logger.error("Произошла ошибка при запуске сервера: ", e);
        }
    }

    private void handleRequest(Socket socket) {
        try (socket) {
            byte[] buffer = new byte[8192];
            int n = socket.getInputStream().read(buffer);
            String rawRequest = new String(buffer, 0, n);
            HttpRequest request = new HttpRequest(rawRequest);
            request.info(true);
            dispatcher.execute(request, socket.getOutputStream());
        } catch (IOException e) {
            logger.error("Произошла ошибка при обработке запроса: ", e);
        }
    }
}