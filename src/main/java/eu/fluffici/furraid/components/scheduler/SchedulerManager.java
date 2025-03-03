package eu.fluffici.furraid.components.scheduler;

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


import eu.fluffici.bot.api.interactions.Task;
import eu.fluffici.furraid.FurRaidDB;
import eu.fluffici.furraid.components.scheduler.sanction.MigrateServers;
import eu.fluffici.furraid.components.scheduler.sanction.UpdateAllSanction;
import eu.fluffici.furraid.components.scheduler.stats.SendStatistics;
import eu.fluffici.furraid.components.scheduler.verification.CheckSpammerUpdate;
import eu.fluffici.furraid.components.scheduler.verification.RedisSyncData;
import eu.fluffici.furraid.components.scheduler.verification.VerificationSyncTask;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class SchedulerManager {

    private final FurRaidDB instance;

    private final List<Task> tasks = new CopyOnWriteArrayList<>();
    private final Object[] lock = new Object[] {};

    public SchedulerManager(FurRaidDB instance) {
        this.instance = instance;

        synchronized (this.lock) {
            this.tasks.clear();

            this.tasks.add(new UpdateAllSanction(this.instance));
            this.tasks.add(new MigrateServers(this.instance));

            this.tasks.add(new SendStatistics(this.instance));
            this.tasks.add(new VerificationSyncTask(this.instance));

            //this.tasks.add(new RedisSyncData(this.instance));
            this.tasks.add(new CheckSpammerUpdate(this.instance));
        }
    }

    public void enableAll() {
        this.tasks.forEach((task -> {
            this.instance.getLogger().info("[JOB] %s <-> Started", task.getClass().getCanonicalName());
            task.execute();
        }));
    }
}
