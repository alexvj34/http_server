package ru.otus.october.http.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {
    private int port;
    private final Dispatcher dispatcher;
    private final ExecutorService executorService;

    public HttpServer(int port) {
        this.port = port;
        this.dispatcher = new Dispatcher();
        this.executorService = Executors.newFixedThreadPool(10);  // Пул из 10 потоков
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту: " + port);
            // Цикл, который будет принимать новые соединения
            while (true) {
                try {
                    // Принять новое соединение
                    Socket socket = serverSocket.accept();
                    // Для каждого соединения создаем отдельный поток
                    executorService.submit(() -> handleRequest(socket));
                } catch (IOException e) {
                    System.out.println("Произошла ошибка: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Произошла ошибка: " + e.getMessage());
        }
    }

    private void handleRequest(Socket socket) {
        try (socket) {  // Автоматическое закрытие сокета после выполнения работы
            byte[] buffer = new byte[8192];
            int n = socket.getInputStream().read(buffer);
            String rawRequest = new String(buffer, 0, n);
            HttpRequest request = new HttpRequest(rawRequest);
            request.info(true);
            dispatcher.execute(request, socket.getOutputStream());
        } catch (IOException e) {
            System.out.println("Произошла ошибка: " + e.getMessage());
        }
    }
}
