/*
---------------------------------------------------------------------------------
File Name : PaginationHelper

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 26/06/2024
Last Modified : 26/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.function.Function;

/**
 * The PaginationHelper class provides methods for generating paginated JSON responses based on a list of items and pagination parameters.
 *
 * @param <T> the type of items in the list
 */
public class PaginationHelper<T> {
    private final List<T> items;
    private final int page;
    private final int limit;

    /**
     * The PaginationHelper class provides methods for generating paginated JSON responses based on a list of items and pagination parameters.
     */
    public PaginationHelper(List<T> items, int page, int limit) {
        this.items = items;
        this.page = page;
        this.limit = limit;
    }

    /**
     * Generates a paginated JSON response based on the provided items and pagination parameters.
     *
     * @param toJson a function that converts an item to a JsonElement
     * @return the paginated JSON response object
     */
    public JsonObject paginate(Function<T, JsonElement> toJson) {
        int totalItems = items.size();
        int startIndex = (page - 1) * limit;
        int endIndex = Math.min(startIndex + limit, totalItems);
        JsonArray paginatedArray = new JsonArray();

        for (int i = startIndex; i < endIndex; i++) {
            paginatedArray.add(toJson.apply(items.get(i)));
        }

        int totalPages = (int) Math.ceil((double) totalItems / limit);
        boolean isFirstPage = page == 1;
        boolean isLastPage = page >= totalPages;

        JsonObject paginationData = new JsonObject();
        paginationData.addProperty("currentPage", page);
        paginationData.addProperty("totalPages", totalPages);
        paginationData.addProperty("totalItems", totalItems);
        paginationData.addProperty("itemsPerPage", paginatedArray.size());
        paginationData.addProperty("isFirstPage", isFirstPage);
        paginationData.addProperty("isLastPage", isLastPage);

        JsonObject response = new JsonObject();
        response.add("data", paginatedArray);
        response.add("pagination", paginationData);

        return response;
    }
}
