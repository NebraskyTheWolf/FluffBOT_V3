package eu.fluffici.bot.api.game.fish.map;

import eu.fluffici.bot.api.game.fish.chunk.Chunk;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
public class MapVisualizer extends JPanel {
    private final Map map;
    private final BufferedImage mapImage;

    private static final int TILE_SIZE = 16; // Size of each tile in pixels
    private static final int CHUNK_WIDTH = 16;
    private static final int CHUNK_HEIGHT = 256;
    private static final int CHUNK_DEPTH = 16;

    private boolean isRunning = true;

    public MapVisualizer(@NotNull Map map, ChunkLoadingCallback callback) {
        this.map = map;
        boolean success = map.loadChunks(callback);
        int chunkWidth = map.getWidth();
        int chunkDepth = map.getHeight();
        this.mapImage = new BufferedImage(chunkWidth * TILE_SIZE, chunkDepth * TILE_SIZE, BufferedImage.TYPE_INT_ARGB);

        Thread thread = new Thread(() -> {
            while (isRunning) {
                if (success) {
                    drawMap();
                    isRunning = false;
                } else {
                    System.out.println("awaiting chunks loading callback.");
                }

                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.setDaemon(true);
        thread.setName("Drawing map thread");
        thread.start();
    }

    /**
     * Draws the map by generating and rendering chunk images in parallel.
     *
     * The method uses the map's width and height to iterate through each tile in the map.
     * For each tile, it fetches the corresponding chunk from the map and retrieves its image.
     * It then scales the chunk image to the desired size and draws it onto the map image.
     * The map image is shared among all threads, so access to it is synchronized to avoid conflicts.
     *
     * The method uses an ExecutorService with a fixed thread pool size based on the available processors,
     * allowing concurrent execution of the tasks. Each task represents the rendering of a single chunk.
     *
     * Note: This method assumes the existence of the following variables:
     *   - map: The map instance holding the tiles and chunks.
     *   - mapImage: The image representing the entire map.
     *   - TILE_SIZE: The size of a single tile in pixels.
     *   - CHUNK_WIDTH: The width of a chunk in tiles.
     *   - CHUNK_HEIGHT: The height of a chunk in tiles.
     *   - CHUNK_DEPTH: The depth of a chunk in tiles.
     *
     * The map's width and height are obtained using the `getWidth()` and `getHeight()` methods respectively.
     * Each chunk's image is fetched from the corresponding chunk using the `getChunk()` method,
     * and the scaled chunk image is generated using `BufferedImage` operations.
     *
     * Access to the map image is synchronized using a lock obtained on the `mapImage` variable.
     *
     * The method shuts down the executor and waits for the task completion.
     * If interrupted during the waiting process, the method interrupts the current thread.
     */
    private void drawMap() {
        int mapWidth = map.getWidth();
        int mapHeight = map.getHeight();
        int chunkPixelWidth = TILE_SIZE * CHUNK_WIDTH;
        int chunkPixelHeight = TILE_SIZE * CHUNK_DEPTH;

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                for (int z = 0; z < CHUNK_DEPTH; z++) {
                    int finalX = x;
                    int finalY = y;
                    int finalZ = z;
                    executor.submit(() -> {
                        Chunk chunk = map.getChunk(finalX, finalZ, finalY);
                        BufferedImage chunkImage = chunk.getImage();

                        BufferedImage scaledChunkImage = new BufferedImage(chunkPixelWidth, chunkPixelHeight, BufferedImage.TYPE_INT_ARGB);
                        Graphics2D g2d = scaledChunkImage.createGraphics();
                        g2d.drawImage(chunkImage, 0, 0, chunkPixelWidth, chunkPixelHeight, null);
                        g2d.dispose();

                        synchronized (mapImage) {
                            Graphics g = mapImage.getGraphics();
                            g.drawImage(scaledChunkImage, finalX * chunkPixelWidth, finalY * chunkPixelHeight, null);
                            g.dispose();
                        }
                    });
                }
            }
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, java.util.concurrent.TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    public void update() {
        SwingUtilities.invokeLater(this::repaint);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(mapImage, 0, 0, 800, 600, this);
    }
}