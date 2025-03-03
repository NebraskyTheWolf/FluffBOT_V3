/*
---------------------------------------------------------------------------------
File Name : WebRoute

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 26/06/2024
Last Modified : 26/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.interactions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import eu.fluffici.bot.api.hooks.RouteMethod;
import lombok.Getter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * An abstract class representing a web route.
 */
@Getter
public abstract class WebRoute {
    private final String routePath;
    private final RouteMethod routeMethod;
    private boolean isAuthEnabled;
    private int requiredPermission = 0;

    /**
     * A class representing a web route.
     */
    public WebRoute(String routePath, RouteMethod method) {
        this.routePath = routePath;
        this.routeMethod = method;
    }

    public WebRoute(String routePath, RouteMethod method, boolean isAuthEnabled) {
        this(routePath, method);

        this.isAuthEnabled = isAuthEnabled;
    }

    public WebRoute(String routePath, RouteMethod method, int requiredPermission) {
        this(routePath, method);

        this.isAuthEnabled = true;
        this.requiredPermission = requiredPermission;
    }

    /**
     * Abstract method called when a request is received.
     *
     * @param request the HttpExchange object representing the HTTP request and response
     */
    public abstract void onRequest(HttpExchange request);

    /**
     * Sets the response type to JSON, sends the JSON response, and closes the output stream.
     *
     * @param exchange the HttpExchange object representing the HTTP request and response
     * @param response the JsonObject containing the JSON response
     * @throws IOException if an I/O error occurs while sending the response
     */
    @SneakyThrows
    protected static void sendJsonResponse(@NotNull HttpExchange exchange, @NotNull JsonElement response) {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.toString().getBytes().length);

        OutputStream os = exchange.getResponseBody();
        os.write(response.toString().getBytes());
        os.close();
    }

    @SneakyThrows
    public void sendErrorResponse(@NotNull HttpExchange exchange, @NotNull String error, @NotNull String message) {
        sendErrorResponse(exchange, error, message, 401);
    }

    @SneakyThrows
    public void sendErrorResponse(@NotNull HttpExchange exchange, @NotNull String error, @NotNull String message, int code) {
        JsonObject errorBody = new JsonObject();
        errorBody.addProperty("status", false);
        errorBody.addProperty("error", error);
        errorBody.addProperty("message", message);

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(code, errorBody.toString().getBytes().length);

        OutputStream os = exchange.getResponseBody();
        os.write(errorBody.toString().getBytes());
        os.close();
    }

    protected static void sendSuccessResponse(@NotNull HttpExchange exchange) {
        try {
            JsonObject response = new JsonObject();
            response.addProperty("status", true);

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.toString().getBytes().length);

            OutputStream os = exchange.getResponseBody();
            os.write(response.toString().getBytes());
            os.close();
        } catch (Exception ignored) {}
    }

    @SneakyThrows
    protected static void sendMessageResponse(@NotNull HttpExchange exchange, String message) {
        JsonObject response = new JsonObject();
        response.addProperty("status", false);
        response.addProperty("message", message);

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.toString().getBytes().length);

        OutputStream os = exchange.getResponseBody();
        os.write(response.toString().getBytes());
        os.close();
    }

    /**
     * Sends an HTTP response with a specified HTML content.
     *
     * @param exchange the HttpExchange object representing the HTTP request and response
     * @param response the HTML content to be sent as the response
     * @throws IOException if an I/O error occurs while sending the response
     */
    protected static void sendHtmlResponse(HttpExchange exchange, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if the request method matches the expected route method. If they don't match, it sends a 405 response
     * (Method Not Allowed) and closes the connection.
     *
     * @param exchange the HttpExchange object representing the HTTP request and response
     * @return true if the request method doesn't match the expected route method, false otherwise
     */
    @SneakyThrows
    protected boolean preventWrongMethod(@NotNull HttpExchange exchange) {
        String requestMethod = exchange.getRequestMethod();
        if (!requestMethod.equalsIgnoreCase(routeMethod.name())) {
            exchange.sendResponseHeaders(405, -1);
            exchange.close();
            return true;
        }

        return false;
    }

    @Contract(pure = true)
    protected boolean isValidUserId(@NotNull String userId) {
        return userId.matches("[a-zA-Z0-9]+");
    }

    @SneakyThrows
    protected String readBody(InputStream stream) {
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        StringBuilder sb = new StringBuilder();
        String line;
        while((line = br.readLine()) != null){
            sb.append(line);
        }
        return sb.toString();
    }
}