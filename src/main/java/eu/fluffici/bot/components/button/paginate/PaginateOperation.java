/*
---------------------------------------------------------------------------------
File Name : PaginateOperation

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 07/06/2024
Last Modified : 12/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.components.button.paginate;

import eu.fluffici.bot.FluffBOT;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static eu.fluffici.bot.api.MessageUtil.updateInteraction;

public class PaginateOperation extends ListenerAdapter {

    private int currentPage = 0;
    private final PaginationBuilder paginationBuilder;
    private boolean isEditOriginal;
    private Message original;

    public PaginateOperation(PaginationBuilder paginationBuilder) {
        this.paginationBuilder = paginationBuilder;
        this.isEditOriginal = false;
        this.original = null;
    }

    public PaginateOperation(PaginationBuilder paginationBuilder, boolean isEditOriginal, Message original) {
        this(paginationBuilder);
        this.isEditOriginal = isEditOriginal;
        this.original = original;
    }

    /**
     * Handles button interactions.
     *
     * @param event The button interaction event.
     */
    @Override
    public void onButtonInteraction(@NonNull ButtonInteractionEvent event) {
       try {
           if (!this.paginationBuilder.getPaginationOwner().equals(event.getUser())) {
               event.getInteraction().reply("This interaction is not for you.").setEphemeral(true).queue();
               return;
           }

           event.getInteraction().deferEdit().queue();

           Message message = event.getMessage();
           String buttonId = event.getButton().getId();
           String uniqueId = this.paginationBuilder.getPaginationUniqueId();
           String nextButton = "button:next_" + uniqueId;
           String previousButton = "button:previous_" + uniqueId;

           if (buttonId.equals(nextButton) && this.currentPage < this.paginationBuilder.maxPages() - 1) {
               this.currentPage++;
           } else if (buttonId.equals(previousButton) && this.currentPage > 0) {
               this.currentPage = this.currentPage - 1;
           }

           PageBuilder currentPageBuilder = this.paginationBuilder.getPage(this.currentPage);

            if (this.isEditOriginal) {
                if (currentPageBuilder.isTextured() && currentPageBuilder.getTexture() != null) {
                    this.original.editMessageEmbeds(currentPageBuilder.getMessage())
                            .setFiles(FileUpload.fromData(getTexture(currentPageBuilder.getTexture()), "item.png"))
                            .queue();
                } else {
                    this.original.editMessageEmbeds(currentPageBuilder.getMessage()).queue();
                }

                this.editMessage(this.original, nextButton, this.currentPage >= this.paginationBuilder.maxPages() - 1);
                this.editMessage(this.original, previousButton, this.currentPage > 0);
            } else {
                if (currentPageBuilder.isTextured() && currentPageBuilder.getTexture() != null) {
                    message.editMessageEmbeds(currentPageBuilder.getMessage())
                            .setFiles(FileUpload.fromData(getTexture(currentPageBuilder.getTexture()), "item.png"))
                            .queue();
                } else {
                    message.editMessageEmbeds(currentPageBuilder.getMessage()).queue();
                }

                this.editMessage(message, nextButton, this.currentPage >= this.paginationBuilder.maxPages() - 1);
                this.editMessage(message, previousButton, this.currentPage > 0);
            }

            FluffBOT.getInstance().getScheduledExecutorService().schedule(() -> {
                if (this.isEditOriginal) {
                    updateInteraction(this.original);
                } else {
                    updateInteraction(event.getMessage());
                }

                event.getJDA().removeEventListener(this);
            }, 10, TimeUnit.SECONDS);
       }catch (Exception e) {
           e.printStackTrace();
       }
    }

    private InputStream getTexture(String itemName) {
        InputStream is = null;
        try {
            is = FluffBOT.getInstance().getClass().getResourceAsStream(String.format("/assets/items/%s.png", itemName));
            if (is == null) {
                is = FluffBOT.getInstance().getClass().getResourceAsStream("/assets/items/missingno.png");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return is;
    }

    /**
     * Edits the message commands of a given message by modifying the specified button's state.
     * If the state is true, the button is enabled and styled as primary.
     * If the state is false, the button is disabled and styled as secondary.
     *
     * @param message   The message to edit the commands of.
     * @param buttonId  The ID of the button to modify.
     * @param state     The state of the button: true for enabled, false for disabled.
     */
    private void editMessage(@NotNull Message message, String buttonId, boolean state) {
        List<ItemComponent> modifiedComponentsRow = new ArrayList<>();
        List<ActionRow> actionRows = message.getActionRows();

        for (ActionRow actionRow : actionRows) {
            List<ItemComponent> components = actionRow.getComponents();

            for (ItemComponent component : components) {
                if (component instanceof Button button) {
                    if (Objects.equals(button.getId(), buttonId)) {
                        Button modifiedButton = button.withDisabled(!state);
                        modifiedComponentsRow.add(modifiedButton);
                    } else {
                        modifiedComponentsRow.add(button);
                    }
                } else {
                    modifiedComponentsRow.add(component);
                }
            }
        }

        message.editMessageComponents(ActionRow.of(modifiedComponentsRow)).queue();
    }
}