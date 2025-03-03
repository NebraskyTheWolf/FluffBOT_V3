package eu.fluffici.bot.components.button.misc;

/*
---------------------------------------------------------------------------------
File Name : ButtonLibrariesUsed.java

Developer : vakea
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 02/06/2024
Last Modified : 02/06/2024

---------------------------------------------------------------------------------
*/



import eu.fluffici.bot.api.interactions.ButtonBuilder;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

public class ButtonLibrariesUsed extends ButtonBuilder {

    private final String[] libraries = new String[] {
            "[Gson v2.10.1](https://github.com/google/gson)",
            "[Guava v32.1.3-jre](https://github.com/google/Guava)",
            "[PusherClient v2.4.2](https://github.com/pusher/pusher-websocket-java)",
            "[OkHttp v4.12.0](https://github.com/square/okhttp)",
            "[JDA v5.0.0](https://github.com/discord-jda/JDA)",
            "[RequestWrapper v1.0.5](https://github.com/RunarMC/RunarMCWrapper)",
            "[Lombok v1.18.32](https://projectlombok.org)",
            "[SnakeYaml v2.2](https://bitbucket.org/snakeyaml/snakeyaml)"
    };

    private final String[] persons = new String[] {
            "Asherro for the translations and design help",
            "All the Staff for their ideas to make the bot better",
            "All the beta testers for their help and feedbacks"
    };

    public ButtonLibrariesUsed() {
        super("developer:thanks", "Special thanks!", ButtonStyle.SECONDARY);
    }

    @Override
    public void execute(ButtonInteraction interaction) {
        StringBuilder message = new StringBuilder();

        message.append("Děkuji všem úžasným knihovnám použitým v tomto projektu, které usnadňují práci!").append("\n");
        for (String library : libraries) {
            message.append(library).append("\n");
        }

        message.append("\nDále bych rád poděkoval následujícím lidem za jejich podporu a pomoc:\n");
        for (String person : persons) {
            message.append("- ").append(person).append("\n");
        }

        interaction.replyEmbeds(this.getEmbed()
                .simpleAuthoredEmbed()
                .setAuthor("Všechny knihovny a lidé použité v tomto projektu <3", "https://fluffici.eu", "https://cdn.discordapp.com/attachments/1224419443300372592/1225861500384579645/heart.png")
                .setDescription(message.toString())
                .build()
        ).setEphemeral(true).queue();
    }
}
