package eu.fluffici.bot.components.scheduler;

/*
---------------------------------------------------------------------------------
File Name : SchedulerManager.java

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

import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.interactions.Task;
import eu.fluffici.bot.components.scheduler.channel.ChannelDeletionNotice;
import eu.fluffici.bot.components.scheduler.channel.DeleteUnpaidChannels;
import eu.fluffici.bot.components.scheduler.channel.SendDailyStatistics;
import eu.fluffici.bot.components.scheduler.channel.UpdateChannelMember;
import eu.fluffici.bot.components.scheduler.check.CheckRewrites;
import eu.fluffici.bot.components.scheduler.contabo.CheckMaintenanceStatus;
import eu.fluffici.bot.components.scheduler.sanction.SendReport;
import eu.fluffici.bot.components.scheduler.sanction.UpdateAllSanction;
import eu.fluffici.bot.components.scheduler.user.BirthdateScheduler;
import eu.fluffici.bot.components.scheduler.user.UpdateAllInteraction;
import eu.fluffici.bot.components.scheduler.user.UpdateAllInvites;
import eu.fluffici.bot.components.scheduler.user.UpdateStatistics;
import eu.fluffici.bot.components.scheduler.verification.LockExpiredReminder;
import eu.fluffici.bot.components.scheduler.verification.NotifyReminder;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class SchedulerManager {

    private final FluffBOT instance;

    private final List<Task> tasks = new CopyOnWriteArrayList<>();
    private final Object[] lock = new Object[] {};

    public SchedulerManager(FluffBOT instance) {
        this.instance = instance;

        synchronized (this.lock) {
            this.tasks.clear();

            this.tasks.add(new UpdateAllSanction(this.instance));
            this.tasks.add(new UpdateAllInvites(this.instance));
            this.tasks.add(new UpdateAllInteraction(this.instance));
            this.tasks.add(new UpdateChannelMember(this.instance));
            this.tasks.add(new BirthdateScheduler(this.instance));
            this.tasks.add(new CheckRewrites(this.instance));
            this.tasks.add(new UpdateStatistics(this.instance));

            this.tasks.add(new DeleteUnpaidChannels(this.instance));
            this.tasks.add(new ChannelDeletionNotice(this.instance));

            this.tasks.add(new SendDailyStatistics(this.instance));
            this.tasks.add(new SendReport(this.instance));

            this.tasks.add(new LockExpiredReminder());
            this.tasks.add(new NotifyReminder());

            this.tasks.add(new CheckMaintenanceStatus());
        }
    }

    public void enableAll() {
        this.instance.getExecutorMonoThread().schedule(() -> {
            this.tasks.forEach(Task::execute);
        }, 10, TimeUnit.SECONDS);
    }
}
