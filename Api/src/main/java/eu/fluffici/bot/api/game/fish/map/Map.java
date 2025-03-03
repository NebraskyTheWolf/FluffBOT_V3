package eu.fluffici.bot.api.game.fish.map;

import eu.fluffici.bot.api.game.fish.chunk.Chunk;
import eu.fluffici.bot.api.game.fish.environment.Environment;
import eu.fluffici.bot.api.game.fish.environment.TerrainType;
import eu.fluffici.bot.api.game.fish.environment.WeatherType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
public class Map {
    private final Chunk[][][] chunks;
    private final int width;
    private final int height;
    private final Random random;
    private Environment primaryEnv = null;

    public Map(int width, int height) {
        this.width = width;
        this.height = height;
        this.chunks = new Chunk[16][256][16];
        this.random = new Random();
    }

    private void generateMap(ChunkLoadingCallback callback, CountDownLatch latch) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        int batchSize = 10;

        for (int x = 0; x < chunks.length; x += batchSize) {
            for (int y = 0; y < chunks[0].length; y += batchSize) {
                for (int z = 0; z < chunks[0][0].length; z += batchSize) {
                    int finalX = x;
                    int finalY = y;
                    int finalZ = z;
                    executor.submit(() -> {
                        try {
                            for (int i = finalX; i < finalX + batchSize && i < chunks.length; i++) {
                                for (int j = finalY; j < finalY + batchSize && j < chunks[0].length; j++) {
                                    for (int k = finalZ; k < finalZ + batchSize && k < chunks[0][0].length; k++) {
                                        Environment env = generateRandomEnvironment();
                                        this.primaryEnv = env;
                                        chunks[i][j][k] = new Chunk(env, 16, 256, 16); // 16x256x16 chunks
                                        callback.step(i, j, k, env);
                                    }
                                }
                            }
                        } finally {
                            latch.countDown();
                        }
                    });
                }
            }
        }

        executor.shutdown();
    }

    @NotNull
    private Environment generateRandomEnvironment() {
        TerrainType terrainType = getRandomTerrainType();
        WeatherType weatherType = getRandomWeatherType();
        int temperature = random.nextInt(50) - 20;
        int humidity = random.nextInt(101);
        return new Environment(terrainType, weatherType, temperature, humidity);
    }

    private TerrainType getRandomTerrainType() {
        TerrainType[] terrainTypes = TerrainType.values();
        return terrainTypes[random.nextInt(terrainTypes.length)];
    }

    private WeatherType getRandomWeatherType() {
        WeatherType[] weatherTypes = WeatherType.values();
        return weatherTypes[random.nextInt(weatherTypes.length)];
    }

    public Chunk getChunk(int x, int y, int z) {
        if (x < 0 || x >= chunks.length || y < 0 || y >= chunks[0].length || z < 0 || z >= chunks[0][0].length) {
            throw new IndexOutOfBoundsException("Chunk coordinates out of bounds");
        }
        return chunks[x][y][z];
    }

    public boolean loadChunks(ChunkLoadingCallback callback) {
        CountDownLatch latch = new CountDownLatch(chunks.length * chunks[0].length * chunks[0][0].length);
        generateMap(callback, latch);
        try {
            latch.await();  // Wait for all chunks to be loaded
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}