package eu.fluffici.furraid.events.application;

/*
---------------------------------------------------------------------------------
File Name : ReadyStateListener.java

Developer : vakea
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 02/06/2024
Last Modified : 02/06/2024

---------------------------------------------------------------------------------
*/



/*
                            LICENCE PRO PROPRIETÁRNÍ SOFTWARE
            Verze 1, Organizace: Fluffici, z.s. IČO: 19786077, Rok: 2024
                            PODMÍNKY PRO POUŽÍVÁNÍ

    a. Použití: Software lze používat pouze podle přiložené dokumentace.
    b. Omezení reprodukce: Kopírování softwaru bez povolení je zakázáno.
    c. Omezení distribuce: Distribuce je povolena jen přes autorizované kanály.
    d. Oprávněné kanály: Distribuci určuje výhradně držitel autorských práv.
    e. Nepovolené šíření: Šíření mimo povolené podmínky je zakázáno.
    f. Právní důsledky: Porušení podmínek může vést k právním krokům.
    g. Omezení úprav: Úpravy softwaru jsou zakázány bez povolení.
    h. Rozsah oprávněných úprav: Rozsah úprav určuje držitel autorských práv.
    i. Distribuce upravených verzí: Distribuce upravených verzí je povolena jen s povolením.
    j. Zachování autorských atribucí: Kopie musí obsahovat všechny autorské atribuce.
    k. Zodpovědnost za úpravy: Držitel autorských práv nenese odpovědnost za úpravy.

    Celý text licence je dostupný na adrese:
    https://autumn.fluffici.eu/attachments/xUiAJbvhZaXW3QIiLMFFbVL7g7nPC2nfX7v393UjEn/fluffici_software_license_cz.pdf
*/

import eu.fluffici.furraid.FurRaidDB;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import java.security.SecureRandom;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("All")
public class ReadyStateListener extends ListenerAdapter {

    private final FurRaidDB instance;
    private final List<Activity> activities = new ArrayList<>();
    private final SecureRandom random = new SecureRandom();


    public ReadyStateListener(FurRaidDB instance) {
        this.instance = instance;

        this.instance.getEventBus().register(this);

        this.instance.getExecutorMonoThread().scheduleAtFixedRate(() -> {
            this.activities.clear();
            this.activities.add(Activity.watching(NumberFormat.getNumberInstance().format(this.instance.getBlacklistManager().blacklistCount()) + " blacklist(s)"));
            this.activities.add(Activity.watching(NumberFormat.getNumberInstance().format(this.instance.getJda().getGuilds().size()) + " servers"));
            this.activities.add(Activity.watching(NumberFormat.getNumberInstance().format(this.instance.getJda().getUsers().size()) + " users"));
            this.activities.add(Activity.listening("https://frdb.fluffici.eu"));
            this.activities.add(Activity.listening("https://frdbdocs.fluffici.eu"));
            this.activities.add(Activity.listening("V3 build-".concat(this.instance.getGitProperties().getProperty("git.build.version", "unofficial"))));
        }, 5, 5, TimeUnit.SECONDS);
    }


    @Override
    public void onReady(ReadyEvent event) {
        FurRaidDB.getInstance().getJda().getPresence().setStatus(OnlineStatus.ONLINE);
        FurRaidDB.getInstance().getInviteManager().init();

        this.instance.getLogger().info("System ready.");
        this.instance.setIsLoading(false);

        this.instance.getExecutorMonoThread().scheduleAtFixedRate(() -> this.instance.getJda().getPresence().setActivity(this.activities.get(random.nextInt(this.activities.size() - 1))), 6, 5, TimeUnit.SECONDS);
    }
}
