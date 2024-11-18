package ru.otus.october.http.server;

import ru.otus.october.http.server.processors.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class Dispatcher {
    private Map<String, RequestProcessor> processors;
    private RequestProcessor defaultNotFoundProcessor;
    private RequestProcessor defaultInternalServerErrorProcessor;
    private RequestProcessor defaultBadRequestProcessor;
    private RequestProcessor defaultMethodNotAllowedProcessor; // Добавим эту переменную

    public Dispatcher() {
        this.processors = new HashMap<>();
        this.processors.put("/", new HelloWorldProcessor());
        this.processors.put("/calculator", new CalculatorProcessor());
        this.defaultNotFoundProcessor = new DefaultNotFoundProcessor();
        this.defaultInternalServerErrorProcessor = new DefaultInternalServerErrorProcessor();
        this.defaultBadRequestProcessor = new DefaultBadRequestProcessor();
        this.defaultMethodNotAllowedProcessor = new DefaultMethodNotAllowedProcessor(); // Инициализируем обработчик 405
    }

    public void execute(HttpRequest request, OutputStream out) throws IOException {
        try {
            RequestProcessor processor = processors.get(request.getUri());

            if (processor == null) {
                // Если процессор для URI не найден
                defaultNotFoundProcessor.execute(request, out);
                return;
            }

            // Проверяем, разрешен ли метод для данного URI
            if (!isMethodAllowed(request.getMethod(), request.getUri())) {
                // Если метод не разрешен, возвращаем 405
                defaultMethodNotAllowedProcessor.execute(request, out);
                return;
            }

            // Если все проверки прошли, выполняем процессор для URI
            processor.execute(request, out);
        } catch (BadRequestException e) {
            // Если возникла ошибка BadRequest, возвращаем 400
            request.setException(e);
            defaultBadRequestProcessor.execute(request, out);
        } catch (Exception e) {
            // Обработка всех остальных исключений, 500 ошибка
            e.printStackTrace();
            defaultInternalServerErrorProcessor.execute(request, out);
        }
    }

    private boolean isMethodAllowed(HttpMethod method, String uri) {
        // Пример проверки для /calculator
        if ("/calculator".equals(uri) && method != HttpMethod.GET) {
            return false; // Только GET поддерживается
        }
        return true; // Все остальные методы разрешены
    }
}
