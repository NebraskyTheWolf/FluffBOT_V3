/*
---------------------------------------------------------------------------------
File Name : PatchSettingsRoute

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 26/06/2024
Last Modified : 26/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.furraid.server.guilds;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sun.net.httpserver.HttpExchange;
import eu.fluffici.bot.api.beans.furraid.FurRaidConfig;
import eu.fluffici.bot.api.beans.furraid.GuildSettings;
import eu.fluffici.bot.api.beans.furraid.patch.PatchSettings;
import eu.fluffici.bot.api.bucket.Validation;
import eu.fluffici.bot.api.bucket.ValidationType;
import eu.fluffici.bot.api.furraid.AuditLogEntry;
import eu.fluffici.bot.api.furraid.AuditLogGenerator;
import eu.fluffici.bot.api.hooks.RouteMethod;
import eu.fluffici.bot.api.interactions.WebRoute;
import eu.fluffici.furraid.FurRaidDB;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static eu.fluffici.bot.api.furraid.permissive.Permissions.*;

@SuppressWarnings("All")
public class PatchSettingsRoute extends WebRoute {
    private final int MAX_REQUESTS = 30;
    private final long TIME_WINDOW = 60 * 1000;
    private final Map<String, Map<String, Long>> rateLimitMap = new HashMap<>();

    private AuditLogGenerator auditLogGenerator = new AuditLogGenerator();

    public PatchSettingsRoute() {
        super("/patch-settings", RouteMethod.PATCH, calculatePermissions(PATCH_SERVERS));
    }

    /**
     * Abstract method called when a request is received.
     *
     * @param request the HttpExchange object representing the HTTP request and response
     */
    @Override
    @SneakyThrows
    public void onRequest(HttpExchange request) {
        if (!this.preventWrongMethod(request)) {
            PatchSettings guildSettings = new Gson().fromJson(this.readBody(request.getRequestBody()), PatchSettings.class);
            if (guildSettings == null || guildSettings.getData() == null) {
                sendErrorResponse(request, "400: Bad Request. Invalid patched settings.");
                return;
            }

            Guild guild = FurRaidDB.getInstance()
                    .getJda()
                    .getGuildById(guildSettings.getData().getGuildId());

            FurRaidConfig patchedSettings = guildSettings.getData().getConfig();
            FurRaidConfig currentSettings = FurRaidDB.getInstance().getBlacklistManager().fetchGuildSettings(guild).getConfig();

            if (!this.checkRateLimit(request, guildSettings.getData().getGuildId())) {
                sendErrorResponse(request, "429: Too Many Requests");
                return;
            }

            if (guild != null) {
                Member member = guild.getMemberById(guildSettings.getUserId());

                if (member != null) {
                    if (member.hasPermission(Permission.getPermissions(guildSettings.getBitfield()))) {
                        FurRaidDB.getInstance()
                                .getGameServiceManager()
                                .updateGuildSettings(guildSettings.getData());

                        this.compareSettings(guild, member, currentSettings, patchedSettings, "");

                        sendSuccessResponse(request);
                    } else {
                        sendErrorResponse(request, "403: Forbidden");
                    }
                } else {
                    sendErrorResponse(request, "401: Unauthorized");
                }
            } else {
                sendErrorResponse(request, "Guild not found");
            }
        }
    }

    /**
     * Checks the rate limit for a given guild.
     *
     * @param request  the HttpExchange object representing the HTTP request and response
     * @param guildId  the ID of the guild
     * @return true if the request is allowed, false if the rate limit has been exceeded
     */
    private boolean checkRateLimit(HttpExchange request, String guildId) {
        long currentTimestamp = System.currentTimeMillis();

        if (!rateLimitMap.containsKey(guildId)) {
            Map<String, Long> newGuildRateMap = new HashMap<>();
            newGuildRateMap.put("timestamp", currentTimestamp);
            newGuildRateMap.put("counter", 1L);
            rateLimitMap.put(guildId, newGuildRateMap);
            return true;
        } else {
            Map<String, Long> guildRateMap = rateLimitMap.get(guildId);
            long lastRequestTimestamp = guildRateMap.get("timestamp");
            long requestsCounter = guildRateMap.get("counter");

            if (currentTimestamp - lastRequestTimestamp < TIME_WINDOW) {
                if (requestsCounter < MAX_REQUESTS) {
                    guildRateMap.put("counter", requestsCounter + 1);
                    return true;
                } else {
                    request.getResponseHeaders().set("X-RateLimit-Limit", String.valueOf(MAX_REQUESTS));
                    request.getResponseHeaders().set("X-RateLimit-Remaining", String.valueOf(MAX_REQUESTS - requestsCounter));
                    long resetTimestamp = (lastRequestTimestamp + TIME_WINDOW - System.currentTimeMillis()) / 1000;
                    request.getResponseHeaders().set("X-RateLimit-Reset", String.valueOf(resetTimestamp >= 0 ? resetTimestamp : 0));
                    return false;
                }
            } else {
                guildRateMap.put("timestamp", currentTimestamp);
                guildRateMap.put("counter", 1L);
                return true;
            }
        }
    }

    /**
     * Traverses the entire graph of an object, analyzing each of its declared fields,
     * including the ones from nested/inner classes. The traversal uses depth-first
     * approach facilitated by recursive calls.
     *
     * Each field is examined for the presence of a `Validation` annotation. If a field
     * has this annotation, the function retrieves Field's current value for validating
     * against the rules defined by the `Validation` annotation attributes.
     *
     * Validation rules can handle different types of fields, including: OBJECT, STRING,
     * INTEGER, BOOLEAN, and ARRAY. Special validation is integrated for fields of
     * OBJECT type. If such field is encountered, and it's current value is non-null,
     * the function calls itself recursively with the value of this field as the next
     * object to analyze. This procedure enables traversal through the entire object graph.
     *
     * If the validation fails, this function uses the prefix which carries the path
     * from the root object to the current field (not including the name of the current field),
     * and the field's name to construct an error message, then sends an HTTP error
     * response immediately.
     *
     * @param object  The instance of the object which is to be validated.
     *                It can be a complex object containing multiple levels of nested classes.
     * @param prefix  The string to prepend to the field name to create a fully-qualified name
     *                for each field, thereby providing a path to it within the object hierarchy.
     * @param request The HttpExchange object which encapsulates an HTTP request received
     *                and the response to be optionally sent in one exchange.
     * @throws IllegalAccessException if the currently executed method does not have
     *                                access to the definition of the specified field.
     * @throws IOException            if an I/O error occurs while sending the error response.
     */
    private boolean validateFields(@NotNull Object object, Object current, String prefix, HttpExchange request) throws IllegalAccessException, IOException {
        Field[] fields = object.getClass().getDeclaredFields();

        boolean isErrored = false;

        for (Field field : fields) {
            if (field.isAnnotationPresent(Validation.class)) {
                field.setAccessible(true);
                Validation validation = field.getAnnotation(Validation.class);
                Object fieldValue = field.get(object);
                Object fieldCurrentValue = field.get(current);

                String fieldName = prefix + field.getName();

                byte[] bytes = objectToBytes(fieldValue);
                if (bytes.length > 2 * 1024 * 1024) {
                    sendValidationErrorResponse(request, fieldName, "Request body size exceeds the maximum limit (2MB).");
                    isErrored = true;
                    break;
                }

                if (validation.type() == ValidationType.OBJECT && fieldValue != null) {
                    validateFields(fieldValue, fieldCurrentValue, fieldName + ".", request);
                } else {
                    if (fieldValue != null) {
                        switch (validation.type()) {
                            case STRING: {
                                if (!fieldValue.toString().isEmpty() && !fieldValue.toString().isBlank()) {
                                    if ((fieldValue.toString().isEmpty() && validation.required()) ||
                                            !(fieldValue instanceof String)) {
                                        sendValidationErrorResponse(request, fieldName, "Can't be empty and value must be a String.");
                                        isErrored = true;
                                    } else if (!validation.regex().isEmpty() && !((String) fieldValue).matches(validation.regex())) {
                                        sendValidationErrorResponse(request, fieldName, "Does not match the required pattern.");
                                        isErrored = true;
                                    } else if (((String) fieldValue).length() > validation.maxLength()) {
                                        sendValidationErrorResponse(request, fieldName, "Exceeds maximum length (" + validation.maxLength() + ").");
                                        isErrored = true;
                                    } else if (((String) fieldValue).length() < validation.minLength()) {
                                        sendValidationErrorResponse(request, fieldName, "Is less than the minimum length (" + validation.minLength() + ").");
                                        isErrored = true;
                                    } else if (validation.readOnly() && !((String) fieldValue).equals(((String) fieldCurrentValue))) {
                                        sendValidationErrorResponse(request, fieldName, "Field is read-only and cannot be modified.");
                                        isErrored = true;
                                    }
                                }
                                break;
                            }
                            case INTEGER: {
                                if (!(fieldValue instanceof Integer)) {
                                    sendValidationErrorResponse(request, fieldName, "Value is not an integer.");
                                    isErrored = true;
                                } else if ((int) fieldValue < validation.minLength()) {
                                    sendValidationErrorResponse(request, fieldName, "Value is less than permitted minimum value (" + validation.minLength() + ").");
                                    isErrored = true;
                                } else if ((int) fieldValue > validation.maxLength()) {
                                    sendValidationErrorResponse(request, fieldName, "Value exceeds maximum permitted value (" + validation.maxLength() + ").");
                                    isErrored = true;
                                } else if (validation.readOnly() && ((int) fieldValue != (int) fieldCurrentValue)) {
                                    sendValidationErrorResponse(request, fieldName, "Field is read-only and cannot be modified.");
                                    isErrored = true;
                                }
                                break;
                            }
                            case BOOLEAN: {
                                if (!(fieldValue instanceof Boolean)) {
                                    sendValidationErrorResponse(request, fieldName, "Value is not a boolean.");
                                    isErrored = true;
                                } else if (validation.readOnly() && ((boolean) fieldValue != (boolean) fieldCurrentValue)) {
                                    sendValidationErrorResponse(request, fieldName, "Field is read-only and cannot be modified.");
                                    isErrored = true;
                                }
                                break;
                            }
                            case ARRAY: {
                                if (!(fieldValue instanceof List<?>)) {
                                    sendValidationErrorResponse(request, fieldName, "Value is not a list.");
                                    isErrored = true;
                                } else if (((List<?>) fieldValue).size() > validation.maxLength()) {
                                    sendValidationErrorResponse(request, fieldName, "List size exceeds the maximum permissible size (" + validation.maxLength() + ").");
                                    isErrored = true;
                                } else if (validation.readOnly() && ((List<?>) fieldValue).hashCode() != ((List<?>) fieldCurrentValue).hashCode()) {
                                    sendValidationErrorResponse(request, fieldName, "Field is read-only and cannot be modified.");
                                    isErrored = true;
                                }
                                break;
                            }
                            case OBJECT: {
                                if (!validation.instancedOf().isInstance(fieldValue)) {
                                    sendValidationErrorResponse(request, fieldName, "Is not an instance of " + validation.instancedOf() + ".");
                                    isErrored = true;
                                } else if (validation.readOnly() && ((Object) fieldValue.hashCode() != (Object) fieldCurrentValue.hashCode())) {
                                    sendValidationErrorResponse(request, fieldName, "Field is read-only and cannot be modified.");
                                    isErrored = true;
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }

        return isErrored;
    }

    /**
     * Compares the settings of two objects and generates audit log entries for any differences.
     *
     * @param guild         The guild associated with the settings.
     * @param author        The member who initiated the comparison.
     * @param currentObject The current object representing the settings.
     * @param patchedObject The patched object representing the settings to be compared against.
     * @param prefix        The prefix to be prepended to the field names in the audit log entries.
     */
    public void compareSettings(Guild guild, Member author, @NotNull Object currentObject, Object patchedObject, String prefix) {
        Field[] fields = currentObject.getClass().getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);

            if (field.isAnnotationPresent(Validation.class)) {
                try {
                    Object currentValue = field.get(currentObject);
                    Object patchedValue = field.get(patchedObject);

                    Validation validation = field.getAnnotation(Validation.class);
                    String fieldName = prefix + field.getName();

                    if (validation.type() == ValidationType.OBJECT && currentValue != null && patchedValue != null) {
                        compareSettings(guild, author, currentValue, patchedValue, fieldName + ".");
                    } else {
                        if (currentValue != null ? !currentValue.equals(patchedValue) : patchedValue != null) {
                            AuditLogEntry logEntry = new AuditLogEntry(guild, author, fieldName, currentValue, patchedValue, new Timestamp(System.currentTimeMillis()));
                            auditLogGenerator.addEntry(logEntry);
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void saveAuditlog(Guild guild) {
        List<AuditLogEntry> entries = this.auditLogGenerator.getAuditLogEntries()
                .stream()
                .filter(auditLogEntry -> auditLogEntry.getGuild().getId().equals(guild.getId()))
                .distinct()
                .toList();

        entries.clear();
    }

    private void sendErrorResponse(HttpExchange request, String error) {
        JsonObject response = new JsonObject();
        response.addProperty("status", false);
        response.addProperty("error", error);
        sendJsonResponse(request, response);
    }

    /**
     * Sends a validation error response to the client.
     *
     * @param request   the HttpExchange object representing the HTTP request and response
     * @param fieldName the name of the field that failed the validation
     * @param message   the validation error message
     */
    private void sendValidationErrorResponse(HttpExchange request, String fieldName, String message) {
        JsonObject response = new JsonObject();
        response.addProperty("status", false);
        response.addProperty("type", "VALIDATION_ERROR");
        response.addProperty("fieldName", fieldName);
        response.addProperty("message", message);
        sendJsonResponse(request, response);
    }

    /**
     * Converts an object to a byte array.
     *
     * @param object the object to be converted
     * @return the byte array representation of the object
     * @throws IOException if an I/O error occurs while converting the object to a byte array
     */
    public byte[] objectToBytes(Object object) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(object);
            return bos.toByteArray();
        }
    }
}