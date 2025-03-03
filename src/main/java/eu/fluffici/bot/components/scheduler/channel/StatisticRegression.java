package eu.fluffici.bot.components.scheduler.channel;

/*
---------------------------------------------------------------------------------
File Name : StatisticRegression.java

Developer : vakea
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 02/06/2024
Last Modified : 02/06/2024

---------------------------------------------------------------------------------
*/


import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.beans.players.Message;
import eu.fluffici.bot.logger.Logger;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import weka.classifiers.functions.LinearRegression;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;


public class StatisticRegression implements AutoCloseable {
    private boolean isLoaded = false;
    private final LinearRegression model;
    private final Instances dataset;

    public StatisticRegression() {
        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("previousDay"));
        attributes.add(new Attribute("today"));

        dataset = new Instances("MessageStats", attributes, 0);
        dataset.setClassIndex(1);

        model = new LinearRegression();
        model.setOutputAdditionalStats(true);
    }

    private void addDataPoint(long previousDay, long today) {
        DenseInstance instance = new DenseInstance(2);
        instance.setValue(dataset.attribute(0), previousDay);
        instance.setValue(dataset.attribute(1), today);
        dataset.add(instance);
    }

    private void trainModel() throws Exception {
        model.buildClassifier(dataset);
    }

    public Pair<Boolean, BoostStatus> determineBoostState(long today, long last) throws Exception {
        if (!isLoaded)
            return Pair.of(true, BoostStatus.GATHERING);

        DenseInstance newInstance = new DenseInstance(2);
        newInstance.setValue(dataset.attribute(0), last);
        newInstance.setDataset(dataset);
        double predictedToday =  Math.abs(model.classifyInstance(newInstance));

        if (predictedToday <= 0)
            return Pair.of(true, BoostStatus.UNCHANGED);

        if (today > predictedToday)
            return Pair.of(true, BoostStatus.GRANTED);
        else if (today < predictedToday)
            return Pair.of(true, BoostStatus.UNCHANGED);
        else
            return Pair.of(true, BoostStatus.DENIED);
    }

    public void initDataset() {
        Logger logger = new Logger(StatisticRegression.class.getCanonicalName());
        Instant startTime = Instant.now();

        logger.debug("Initializing dataset with the following configuration: [MAX_HISTORY_SIZE: 10,000]");

        CompletableFuture<List<Message>> messagesFuture = CompletableFuture.supplyAsync(() -> {
            try {
                List<Message> messages = FluffBOT.getInstance().getGameServiceManager().sumAll()
                        .stream()
                        .filter(message -> FluffBOT.getInstance().getJda().getUserById(message.getUserId()) != null)
                        .toList();
                logger.debug("Initializing dataset with %s messages.", messages.size());
                return messages;
            } catch (Exception e) {
                logger.error("Error while loading the linear-regression dataset, ", e);
                throw new RuntimeException(e);
            }
        });

        try {
            List<Message> messages = messagesFuture.get(30, TimeUnit.SECONDS); // Wait for messages to be retrieved
            Duration retrievalDuration = Duration.between(startTime, Instant.now());

            logger.debug("Pre-processing stage: Sorting %s values.", messages.size());

            Map<LocalDate, Long> dailyCounts = messages.parallelStream()
                    .collect(Collectors.groupingByConcurrent(
                            message -> message.getCreatedAt().toLocalDateTime().toLocalDate(),
                            Collectors.counting()
                    ));

            List<Long> counts = dailyCounts.values().stream()
                    .sorted()
                    .toList();

            if (counts.size() < 7) {
                logger.warn("[LR] 7 days of data collecting is needed before using the regression feature.");

                this.addDataPoint(0, 0);
                try {
                    this.trainModel();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }

            logger.debug("Post-processing stage: Add data point for all value(s)");

            for (int i = 1; i < counts.size(); i++) {
                long previousDay = counts.get(i - 1);
                long today = counts.get(i);
                this.addDataPoint(previousDay, today);
            }

            logger.debug("Final stage: Training linear-regression model...");
            logger.debug("Warning: this step can take up 3 to 5 minute(s)...");

            CompletableFuture<Void> trainModelFuture = CompletableFuture.runAsync(() -> {
                try {
                    this.trainModel();

                    logger.debug("Final stage: Success.");
                    Duration processingDuration = Duration.between(startTime, Instant.now());

                    logger.debug("Regression model loaded (Entries: %s, Time taken for retrieval: %s seconds, Time taken for processing: %s seconds)",
                            counts.size(), retrievalDuration.getSeconds(), processingDuration.getSeconds());

                    this.isLoaded = true;
                } catch (Exception e) {
                    logger.error("[LR] Cannot build a linear-regression model: ", e);
                    e.printStackTrace();
                }
            });
            trainModelFuture.get(5, TimeUnit.MINUTES);
        } catch (InterruptedException | ExecutionException e) {
            logger.error("[LR] A error occurred at loading-stage, ", e);
            e.printStackTrace();
        } catch (TimeoutException e) {
            logger.error("[LR] The loading process timed-out. ", e);
            e.printStackTrace();
        }
    }

    public String modelData() {
        return model.toString();
    }

    @Override
    public void close() throws Exception {}

    public static enum BoostStatus {
        GRANTED, DENIED, UNCHANGED, GATHERING
    }
}
