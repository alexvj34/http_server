package ru.otus.october.http.server;

import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private String rawRequest;
    private HttpMethod method;
    private String uri;
    private Map<String, String> parameters;
    private Map<String, String> headers;  // Поле для заголовков
    private Exception exception;

    public HttpRequest(String rawRequest) {
        this.rawRequest = rawRequest;
        this.parameters = new HashMap<>();  // Инициализация параметров
        this.headers = new HashMap<>();  // Инициализация заголовков
        this.parse();  // Разбор запроса
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public String getUri() {
        return uri;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getParameter(String key) {
        return parameters.get(key);
    }

    public boolean containsParameter(String key) {
        return parameters.containsKey(key);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    private void parse() {
        String[] lines = rawRequest.split("\r\n");  // Разделяем запрос на строки
        int startIndex = rawRequest.indexOf(' ');
        int endIndex = rawRequest.indexOf(' ', startIndex + 1);
        uri = rawRequest.substring(startIndex + 1, endIndex);
        method = HttpMethod.valueOf(rawRequest.substring(0, startIndex));  // Парсим метод

        // Разбираем параметры запроса
        if (uri.contains("?")) {
            String[] elements = uri.split("[?]");
            uri = elements[0];
            String[] keysValues = elements[1].split("[&]");
            for (String o : keysValues) {
                String[] keyValue = o.split("=");
                parameters.put(keyValue[0], keyValue[1]);
            }
        }

        // Парсим заголовки
        int i = 1;
        while (i < lines.length && !lines[i].isEmpty()) {  // Обрабатываем строки до пустой
            String[] headerParts = lines[i].split(": ");
            if (headerParts.length == 2) {
                headers.put(headerParts[0], headerParts[1]);  // Добавляем в заголовки
            }
            i++;
        }
    }

    // Метод для вывода информации о запросе
    public void info(boolean debug) {
        if (debug) {
            System.out.println(rawRequest);  // Выводим сам запрос для дебага
        }
        System.out.println("Method: " + method);
        System.out.println("URI: " + uri);
        System.out.println("Parameters: " + parameters);
        System.out.println("Headers: " + headers);  // Выводим заголовки
    }
}
