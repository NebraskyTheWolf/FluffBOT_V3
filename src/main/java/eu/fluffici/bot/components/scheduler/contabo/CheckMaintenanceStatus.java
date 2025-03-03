/*
---------------------------------------------------------------------------------
File Name : CheckMaintenanceStatus

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 09/07/2024
Last Modified : 09/07/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.components.scheduler.contabo;

import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.interactions.Task;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.awt.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static eu.fluffici.bot.api.IconRegistry.ICON_ALERT;

@SuppressWarnings("All")
public class CheckMaintenanceStatus extends Task {
    @Override
    public void execute() {
        FluffBOT.getInstance().getScheduledExecutorService().scheduleAtFixedRate(() -> {
            try {
                MaintenanceInfo maintenanceInfo = this.getMaintenanceDetailsForHostSystem("https://contabo-status.com", "21264");
                if (maintenanceInfo != null && !FluffBOT.getInstance()
                        .getGameServiceManager()
                        .hasMaintenance(maintenanceInfo.getId())
                ) {
                    FluffBOT.getInstance().getGameServiceManager().addMaintenance(maintenanceInfo.getId());

                    EmbedBuilder maintenance = FluffBOT.getInstance().getEmbed().simpleAuthoredEmbed();
                    maintenance.setAuthor("Scheduled maintenance for the VPS - #" + maintenanceInfo.getId(), "https://contabo-status.com", ICON_ALERT);
                    maintenance.setColor(Color.RED);
                    maintenance.setDescription(maintenanceInfo.getDetails());
                    maintenance.setFooter("This maintenance has been scheduled by Contabo");

                    FluffBOT.getInstance().getJda().getTextChannelById(FluffBOT.getInstance().getChannelConfig().getProperty("channel.logging")).sendMessageEmbeds(maintenance.build())
                            .addActionRow(
                                    Button.link("https://contabo-status.com", "Contabo Status")
                            )
                            .setContent("<@&782578470135660585> <@&606535408117088277>")
                            .queue();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, 1, 10, TimeUnit.MINUTES);
    }

    /**
     * Retrieves the maintenance details for a specific host system.
     *
     * @param url           The URL of the website containing the maintenance information.
     * @param hostSystemId  The ID of the host system.
     * @return The MaintenanceInfo object containing the maintenance ID and details, or null if no maintenance is found.
     * @throws IOException If an I/O error occurs while retrieving the maintenance information.
     */
    @Nullable
    public MaintenanceInfo getMaintenanceDetailsForHostSystem(String url, String hostSystemId) throws IOException {
        Document doc = Jsoup.connect(url).get();
        Elements maintenanceTables = doc.select("table.maintenance");

        for (Element table : maintenanceTables) {
            Elements hostSystemElements = table.select("td.maintenence_content:contains(All VPS on host system " + hostSystemId + ")");
            if (!hostSystemElements.isEmpty()) {
                StringBuilder details = new StringBuilder();
                Elements rows = table.select("tr");
                String maintenanceId = table.attr("id");

                for (Element row : rows) {
                    String subject = row.select("td.maintenence_subject").text();
                    String content = row.select("td.maintenence_content").text();
                    details.append("**").append(subject).append("**").append(" : ").append(content).append("\n");
                }

                return new MaintenanceInfo(maintenanceId, details.toString());
            }
        }

        return null;
    }

    /**
     * Represents maintenance information for a specific host system.
     */
    @Getter
    @AllArgsConstructor
    public static class MaintenanceInfo {
        private final String id;
        private final String details;
    }
}