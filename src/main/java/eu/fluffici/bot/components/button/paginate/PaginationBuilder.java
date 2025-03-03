/*
---------------------------------------------------------------------------------
File Name : PaginationBuilder

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 07/06/2024
Last Modified : 07/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.components.button.paginate;

import lombok.Builder;
import lombok.Getter;
import net.dv8tion.jda.api.entities.UserSnowflake;

import java.util.List;

/**
 * The PaginationBuilder class is used to build a pagination system for a collection of pages.
 * It allows adding pages, retrieving pages, and checking if there are any pages in the pagination system.
 */
@Getter
@Builder
@SuppressWarnings("All")
public class PaginationBuilder {
    private List<PageBuilder> pages;

    private UserSnowflake paginationOwner;
    private String paginationUniqueId;
    private boolean isEphemeral = false;

    /**
     * Adds a PageBuilder object to the list of pages in the PaginationBuilder.
     *
     * @param page the PageBuilder object to be added
     */
    public void addPage(PageBuilder page) {
        pages.add(page);
    }

    /**
     * Returns the maximum number of pages in the PaginationBuilder.
     *
     * @return the maximum number of pages
     */
    public int maxPages() {
        return this.pages.size();
    }

    /**
     * Gets the PageBuilder object at the specified index.
     *
     * @param index the index of the PageBuilder object to retrieve
     * @return the PageBuilder object at the specified index, or null if the index is out of bounds
     */
    public PageBuilder getPage(int index) {
        if (index < 0 || pages.isEmpty()) {
            return null;
        } else if (index >= pages.size()) {
            return pages.getLast();
        } else {
            return pages.get(index);
        }
    }

    /**
     * Checks if the PaginationBuilder has any pages.
     *
     * @return true if the PaginationBuilder has pages, false otherwise.
     */
    public boolean hasPages() {
        return !this.pages.isEmpty();
    }

    /**
     * Retrieves the message embed from the first page of the pagination system.
     *
     * @return the message embed from the first page
     */
    public PageBuilder getFirstPage() {
        return this.getPage(0);
    }
}