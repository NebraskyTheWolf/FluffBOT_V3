/*
---------------------------------------------------------------------------------
File Name : Chunk

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 04/06/2024
Last Modified : 10/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.game.fish.chunk;

import eu.fluffici.bot.api.game.fish.environment.Environment;
import eu.fluffici.bot.api.game.fish.environment.TerrainType;
import eu.fluffici.bot.api.game.fish.environment.WeatherType;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

@Setter
@Getter
public class Chunk {
    private Environment environment;
    private BufferedImage image;

    public Chunk(Environment environment, int width, int height, int depth) {
        this.environment = environment;
        this.image = generateImage(width, height, depth);
    }

    @NotNull
    private BufferedImage generateImage(int width, int height, int depth) {
        BufferedImage img = new BufferedImage(width * 16, depth * 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                for (int y = 0; y < height; y++) {
                    renderTerrain(g2d, x, y, z + 1, environment);
                }
            }
        }

        g2d.dispose();
        return img;
    }

    private void renderTerrain(Graphics2D g2d, int x, int y, int width, @NotNull Environment env) {
        TerrainType terrainType = env.getTerrainType();
        WeatherType weatherType = env.getWeatherCondition();

        switch (terrainType) {
            case GRASSLAND -> renderGrasslandTerrain(g2d, x, y, width, 16, weatherType);
            case FOREST -> renderForestTerrain(g2d, x, y, width, 16, weatherType);
            case MOUNTAIN -> renderMountainTerrain(g2d, x, y, width, 16, weatherType);
            case DESERT -> renderDesertTerrain(g2d, x, y, width, 16, weatherType);
            case WATER -> renderWaterTerrain(g2d, x, y, width, 16, weatherType);
            case SNOWY -> renderSnowyTerrain(g2d, x, y, width, 16, weatherType);
            case SWAMP -> renderSwampTerrain(g2d, x, y, width, 16, weatherType);
            case BEACH -> renderBeachTerrain(g2d, x, y, width, 16, weatherType);
            case PLAIN -> renderPlainTerrain(g2d, x, y, width, 16, weatherType);
            case TUNDRA -> renderTundraTerrain(g2d, x, y, width, 16, weatherType);
            case VOLCANIC -> renderVolcanicTerrain(g2d, x, y, width, 16, weatherType);
            case CANYON -> renderCanyonTerrain(g2d, x, y, width, 16, weatherType);
            case JUNGLE -> renderJungleTerrain(g2d, x, y, width, 16, weatherType);
            case TAIGA -> renderTaigaTerrain(g2d, x, y, width, 16, weatherType);
            case SAVANNA -> renderSavannaTerrain(g2d, x, y, width, 16, weatherType);
        }
    }


    /**
     * Renders the interactive elements on the graphics context based on the weather condition in the environment.
     *
     * @param g2d     the Graphics2D context to render on
     * @param x       the starting x-coordinate of the interactive elements
     * @param y       the starting y-coordinate of the interactive elements
     * @param width   the width of the interactive elements
     * @param height  the height of the interactive elements
     * @param env     the environment used to determine the weather condition
     */
    private void renderInteractiveElements(Graphics2D g2d, int x, int y, int width, int height, @NotNull Environment env) {
        if (env.getWeatherCondition() == WeatherType.SUNNY) {
            renderFlowers(g2d, x, y, width, height);
        }

        renderTrees(g2d, x, y, width, height);
        renderRiver(g2d, x, y, width, height, env);
    }

    private void renderGrasslandTerrain(Graphics2D g2d, int x, int y, int width, int height, WeatherType weatherType) {
        switch (weatherType) {
            case SUNNY -> renderSunnyGrasslandTerrain(g2d, x, y, width, height);
            case CLOUDY -> renderCloudyGrasslandTerrain(g2d, x, y, width, height);
            case RAINY -> renderRainyGrasslandTerrain(g2d, x, y, width, height);
            case STORMY -> renderStormyGrasslandTerrain(g2d, x, y, width, height);
        }
    }

    private void renderForestTerrain(Graphics2D g2d, int x, int y, int width, int height, WeatherType weatherType) {
        switch (weatherType) {
            case SUNNY -> renderSunnyForestTerrain(g2d, x, y, width, height);
            case CLOUDY -> renderCloudyForestTerrain(g2d, x, y, width, height);
            case RAINY -> renderRainyForestTerrain(g2d, x, y, width, height);
            case STORMY -> renderStormyForestTerrain(g2d, x, y, width, height);
        }
    }

    private void renderMountainTerrain(Graphics2D g2d, int x, int y, int width, int height, WeatherType weatherType) {
        switch (weatherType) {
            case SUNNY -> renderSunnyMountainTerrain(g2d, x, y, width, height);
            case CLOUDY -> renderCloudyMountainTerrain(g2d, x, y, width, height);
            case RAINY -> renderRainyMountainTerrain(g2d, x, y, width, height);
            case STORMY -> renderStormyMountainTerrain(g2d, x, y, width, height);
        }
    }

    private void renderDesertTerrain(Graphics2D g2d, int x, int y, int width, int height, WeatherType weatherType) {
        switch (weatherType) {
            case SUNNY -> renderSunnyDesertTerrain(g2d, x, y, width, height);
            case CLOUDY -> renderCloudyDesertTerrain(g2d, x, y, width, height);
            case RAINY -> renderRainyDesertTerrain(g2d, x, y, width, height);
            case STORMY -> renderStormyDesertTerrain(g2d, x, y, width, height);
        }
    }

    private void renderWaterTerrain(Graphics2D g2d, int x, int y, int width, int height, WeatherType weatherType) {
        switch (weatherType) {
            case SUNNY -> renderSunnyWaterTerrain(g2d, x, y, width, height);
            case CLOUDY -> renderCloudyWaterTerrain(g2d, x, y, width, height);
            case RAINY -> renderRainyWaterTerrain(g2d, x, y, width, height);
            case STORMY -> renderStormyWaterTerrain(g2d, x, y, width, height);
        }
    }

    private void renderSnowyTerrain(Graphics2D g2d, int x, int y, int width, int height, WeatherType weatherType) {
        switch (weatherType) {
            case SUNNY -> renderSunnySnowyTerrain(g2d, x, y, width, height);
            case CLOUDY -> renderCloudySnowyTerrain(g2d, x, y, width, height);
            case RAINY -> renderRainySnowyTerrain(g2d, x, y, width, height);
            case STORMY -> renderStormySnowyTerrain(g2d, x, y, width, height);
        }
    }

    private void renderSwampTerrain(Graphics2D g2d, int x, int y, int width, int height, WeatherType weatherType) {
        switch (weatherType) {
            case SUNNY -> renderSunnySwampTerrain(g2d, x, y, width, height);
            case CLOUDY -> renderCloudySwampTerrain(g2d, x, y, width, height);
            case RAINY -> renderRainySwampTerrain(g2d, x, y, width, height);
            case STORMY -> renderStormySwampTerrain(g2d, x, y, width, height);
        }
    }

    private void renderBeachTerrain(Graphics2D g2d, int x, int y, int width, int height, WeatherType weatherType) {
        switch (weatherType) {
            case SUNNY -> renderSunnyBeachTerrain(g2d, x, y, width, height);
            case CLOUDY -> renderCloudyBeachTerrain(g2d, x, y, width, height);
            case RAINY -> renderRainyBeachTerrain(g2d, x, y, width, height);
            case STORMY -> renderStormyBeachTerrain(g2d, x, y, width, height);
        }
    }

    private void renderPlainTerrain(Graphics2D g2d, int x, int y, int width, int height, WeatherType weatherType) {
        switch (weatherType) {
            case SUNNY -> renderSunnyPlainTerrain(g2d, x, y, width, height);
            case CLOUDY -> renderCloudyPlainTerrain(g2d, x, y, width, height);
            case RAINY -> renderRainyPlainTerrain(g2d, x, y, width, height);
            case STORMY -> renderStormyPlainTerrain(g2d, x, y, width, height);
        }
    }

    private void renderTundraTerrain(Graphics2D g2d, int x, int y, int width, int height, WeatherType weatherType) {
        switch (weatherType) {
            case SUNNY -> renderSunnyTundraTerrain(g2d, x, y, width, height);
            case CLOUDY -> renderCloudyTundraTerrain(g2d, x, y, width, height);
            case RAINY -> renderRainyTundraTerrain(g2d, x, y, width, height);
            case STORMY -> renderStormyTundraTerrain(g2d, x, y, width, height);
        }
    }

    private void renderVolcanicTerrain(Graphics2D g2d, int x, int y, int width, int height, WeatherType weatherType) {
        switch (weatherType) {
            case SUNNY -> renderSunnyVolcanicTerrain(g2d, x, y, width, height);
            case CLOUDY -> renderCloudyVolcanicTerrain(g2d, x, y, width, height);
            case RAINY -> renderRainyVolcanicTerrain(g2d, x, y, width, height);
            case STORMY -> renderStormyVolcanicTerrain(g2d, x, y, width, height);
        }
    }

    private void renderCanyonTerrain(Graphics2D g2d, int x, int y, int width, int height, WeatherType weatherType) {
        switch (weatherType) {
            case SUNNY -> renderSunnyCanyonTerrain(g2d, x, y, width, height);
            case CLOUDY -> renderCloudyCanyonTerrain(g2d, x, y, width, height);
            case RAINY -> renderRainyCanyonTerrain(g2d, x, y, width, height);
            case STORMY -> renderStormyCanyonTerrain(g2d, x, y, width, height);
        }
    }

    private void renderJungleTerrain(Graphics2D g2d, int x, int y, int width, int height, WeatherType weatherType) {
        switch (weatherType) {
            case SUNNY -> renderSunnyJungleTerrain(g2d, x, y, width, height);
            case CLOUDY -> renderCloudyJungleTerrain(g2d, x, y, width, height);
            case RAINY -> renderRainyJungleTerrain(g2d, x, y, width, height);
            case STORMY -> renderStormyJungleTerrain(g2d, x, y, width, height);
        }
    }

    private void renderTaigaTerrain(Graphics2D g2d, int x, int y, int width, int height, WeatherType weatherType) {
        switch (weatherType) {
            case SUNNY:
                renderSunnyTaigaTerrain(g2d, x, y, width, height);
                break;
            case CLOUDY:
                renderCloudyTaigaTerrain(g2d, x, y, width, height);
                break;
            case RAINY:
                renderRainyTaigaTerrain(g2d, x, y, width, height);
                break;
            case STORMY:
                renderStormyTaigaTerrain(g2d, x, y, width, height);
                break;
        }
    }

    private void renderSavannaTerrain(Graphics2D g2d, int x, int y, int width, int height, WeatherType weatherType) {
        switch (weatherType) {
            case SUNNY:
                renderSunnySavannaTerrain(g2d, x, y, width, height);
                break;
            case CLOUDY:
                renderCloudySavannaTerrain(g2d, x, y, width, height);
                break;
            case RAINY:
                renderRainySavannaTerrain(g2d, x, y, width, height);
                break;
            case STORMY:
                renderStormySavannaTerrain(g2d, x, y, width, height);
                break;
        }
    }

    private void renderMarshTerrain(Graphics2D g2d, int x, int y, int width, int height, WeatherType weatherType) {}

    private void renderIceFieldTerrain(Graphics2D g2d, int x, int y, int width, int height, WeatherType weatherType) {}

    private void renderOasisTerrain(Graphics2D g2d, int x, int y, int width, int height, WeatherType weatherType) {}

    private void renderBadlandsTerrain(Graphics2D g2d, int x, int y, int width, int height, WeatherType weatherType) {}

    private void renderGlacierTerrain(Graphics2D g2d, int x, int y, int width, int height, WeatherType weatherType) {}

    private void renderCoralReefTerrain(Graphics2D g2d, int x, int y, int width, int height, WeatherType weatherType) {}

    private void renderLavaFlowTerrain(Graphics2D g2d, int x, int y, int width, int height, WeatherType weatherType) {}

    private void renderFrozenOceanTerrain(Graphics2D g2d, int x, int y, int width, int height, WeatherType weatherType) { }

    private void renderUndergroundTerrain(Graphics2D g2d, int x, int y, int width, int height, WeatherType weatherType) { }

    private void renderCraterTerrain(Graphics2D g2d, int x, int y, int width, int height, WeatherType weatherType) {}

    private void renderArchipelagoTerrain(Graphics2D g2d, int x, int y, int width, int height, WeatherType weatherType) {}

    private void renderCaveTerrain(Graphics2D g2d, int x, int y, int width, int height, WeatherType weatherType) {}

    private void renderCloudsTerrain(Graphics2D g2d, int x, int y, int width, int height, WeatherType weatherType) {}

    private void renderSunnySavannaTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        g2d.setColor(new Color(218, 165, 32)); // Goldenrod color for sunny savanna
        g2d.fillRect(x, y, width, height);
        // Render additional sunny savanna elements such as trees or rocks
    }

    private void renderCloudySavannaTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        g2d.setColor(new Color(222, 184, 135)); // Burlywood color for cloudy savanna
        g2d.fillRect(x, y, width, height);

        int cloudWidth = 80;
        int cloudHeight = 40;
        int numClouds = 3;
        for (int i = 0; i < numClouds; i++) {
            int cloudX = x + (width / numClouds) * i;
            int cloudY = y - 60;
            g2d.setColor(Color.WHITE);
            g2d.fillOval(cloudX, cloudY, cloudWidth, cloudHeight);
        }
    }

    private void renderRainySavannaTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        g2d.setColor(new Color(218, 165, 32));
        g2d.fillRect(x, y, width, height);
        // Render rain effect or puddles on the terrain
        renderRainEffect(g2d, x, y, width, height);
    }

    private void renderStormySavannaTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        g2d.setColor(new Color(218, 165, 32)); // Goldenrod color for stormy savanna (same as sunny)
        g2d.fillRect(x, y, width, height);
        // Render storm effect such as heavy rain or lightning
        renderStormEffect(g2d, x, y, width, height);
    }

    private void renderSunnyTaigaTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        g2d.setColor(new Color(34, 139, 34)); // Dark green for sunny taiga
        g2d.fillRect(x, y, width, height);
    }

    private void renderCloudyTaigaTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        g2d.setColor(new Color(154, 205, 50)); // Olive green for cloudy taiga
        g2d.fillRect(x, y, width, height);
    }

    private void renderRainyTaigaTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        g2d.setColor(new Color(34, 139, 34)); // Dark green for rainy taiga (same as sunny)
        g2d.fillRect(x, y, width, height);
        g2d.drawLine(x, y, x + width, y + height);
    }

    private void renderStormyTaigaTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        g2d.setColor(new Color(34, 139, 34)); // Dark green for stormy taiga (same as sunny)
        g2d.fillRect(x, y, width, height);
        g2d.drawLine(x, y, x + width, y + height);
    }

    private void renderSunnyGrasslandTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        TexturePaint grassTexture = createGrassTexture(Color.GREEN);
        g2d.setPaint(grassTexture);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderCloudyGrasslandTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        TexturePaint grassTexture = createGrassTexture(Color.DARK_GRAY);
        g2d.setPaint(grassTexture);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderRainyGrasslandTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        TexturePaint wetTexture = createWetTexture();
        g2d.setPaint(wetTexture);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderStormyGrasslandTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        TexturePaint floodedTexture = createFloodedTexture();
        g2d.setPaint(floodedTexture);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderSunnyForestTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        TexturePaint forestTexture = createForestTexture(Color.GREEN);
        g2d.setPaint(forestTexture);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderCloudyForestTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        TexturePaint forestTexture = createForestTexture(Color.DARK_GRAY);
        g2d.setPaint(forestTexture);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderRainyForestTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        TexturePaint wetTexture = createWetTexture();
        g2d.setPaint(wetTexture);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderStormyForestTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        TexturePaint floodedTexture = createFloodedTexture();
        g2d.setPaint(floodedTexture);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }


    private void renderSunnyMountainTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        TexturePaint mountainTexture = createMountainTexture(Color.LIGHT_GRAY); // Light gray for sunny mountains
        g2d.setPaint(mountainTexture);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderCloudyMountainTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        TexturePaint mountainTexture = createMountainTexture(Color.GRAY); // Gray for cloudy mountains
        g2d.setPaint(mountainTexture);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderRainyMountainTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        TexturePaint mountainTexture = createMountainTexture(Color.DARK_GRAY); // Dark gray for rainy mountains
        g2d.setPaint(mountainTexture);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderStormyMountainTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        TexturePaint mountainTexture = createMountainTexture(Color.BLACK); // Black for stormy mountains
        g2d.setPaint(mountainTexture);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderSunnyDesertTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        TexturePaint desertTexture = createDesertTexture(Color.YELLOW); // Yellow for sunny desert
        g2d.setPaint(desertTexture);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderCloudyDesertTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        TexturePaint desertTexture = createDesertTexture(Color.LIGHT_GRAY); // Light gray for cloudy desert
        g2d.setPaint(desertTexture);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderRainyDesertTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        TexturePaint desertTexture = createDesertTexture(Color.BLUE); // Blue for rainy desert
        g2d.setPaint(desertTexture);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderStormyDesertTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        TexturePaint desertTexture = createDesertTexture(Color.DARK_GRAY); // Dark gray for stormy desert
        g2d.setPaint(desertTexture);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderSunnyWaterTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        TexturePaint waterTexture = createWaterTexture(Color.CYAN); // Cyan for sunny water
        g2d.setPaint(waterTexture);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderCloudyWaterTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        TexturePaint waterTexture = createWaterTexture(Color.LIGHT_GRAY); // Light gray for cloudy water
        g2d.setPaint(waterTexture);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderRainyWaterTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        TexturePaint waterTexture = createWaterTexture(Color.BLUE); // Blue for rainy water
        g2d.setPaint(waterTexture);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderStormyWaterTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        TexturePaint waterTexture = createWaterTexture(Color.DARK_GRAY); // Dark gray for stormy water
        g2d.setPaint(waterTexture);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderSunnySnowyTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        TexturePaint snowyTexture = createSnowyTexture(Color.WHITE); // White for sunny snowy terrain
        g2d.setPaint(snowyTexture);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderCloudySnowyTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        TexturePaint snowyTexture = createSnowyTexture(Color.LIGHT_GRAY); // Light gray for cloudy snowy terrain
        g2d.setPaint(snowyTexture);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderRainySnowyTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        TexturePaint snowyTexture = createSnowyTexture(Color.BLUE); // Blue for rainy snowy terrain
        g2d.setPaint(snowyTexture);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderStormySnowyTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        TexturePaint snowyTexture = createSnowyTexture(Color.DARK_GRAY); // Dark gray for stormy snowy terrain
        g2d.setPaint(snowyTexture);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderSunnySwampTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        TexturePaint swampTexture = createSwampTexture(Color.GREEN); // Green for sunny swamp terrain
        g2d.setPaint(swampTexture);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderCloudySwampTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        TexturePaint swampTexture = createSwampTexture(Color.DARK_GRAY); // Dark gray for cloudy swamp terrain
        g2d.setPaint(swampTexture);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderRainySwampTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        TexturePaint swampTexture = createSwampTexture(Color.BLUE); // Blue for rainy swamp terrain
        g2d.setPaint(swampTexture);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderStormySwampTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        TexturePaint swampTexture = createSwampTexture(Color.BLACK); // Black for stormy swamp terrain
        g2d.setPaint(swampTexture);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    /**
     * Creates a swamp texture with the specified color.
     *
     * @param color the color of the swamp
     * @return a TexturePaint object representing the swamp texture
     */
    private TexturePaint createSwampTexture(Color color) {
        BufferedImage swampImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = swampImage.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, 10, 10);
        g2d.dispose();
        return new TexturePaint(swampImage, new Rectangle(10, 10));
    }


    /**
     * Creates a snowy texture with the specified color.
     *
     * @param color the color of the snow
     * @return a TexturePaint object representing the snowy texture
     */
    private TexturePaint createSnowyTexture(Color color) {
        BufferedImage snowyImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = snowyImage.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, 10, 10);
        g2d.dispose();
        return new TexturePaint(snowyImage, new Rectangle(10, 10));
    }

    /**
     * Creates a water texture with the specified color.
     *
     * @param color the color of the water
     * @return a TexturePaint object representing the water texture
     */
    private TexturePaint createWaterTexture(Color color) {
        BufferedImage waterImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = waterImage.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, 10, 10);
        g2d.dispose();
        return new TexturePaint(waterImage, new Rectangle(10, 10));
    }


    /**
     * Creates a desert texture with the specified color.
     *
     * @param color the color of the desert
     * @return a TexturePaint object representing the desert texture
     */
    private TexturePaint createDesertTexture(Color color) {
        BufferedImage desertImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = desertImage.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, 10, 10);
        g2d.dispose();
        return new TexturePaint(desertImage, new Rectangle(10, 10));
    }


    /**
     * Creates a mountain texture with the specified color.
     *
     * @param color the color of the mountain
     * @return a TexturePaint object representing the mountain texture
     */
    private TexturePaint createMountainTexture(Color color) {
        BufferedImage mountainImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = mountainImage.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, 10, 10);
        g2d.dispose();
        return new TexturePaint(mountainImage, new Rectangle(10, 10));
    }

    private void renderSunnyBeachTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        TexturePaint beachTexture = createBeachTexture(new Color(255, 220, 150)); // Light yellow for sunny beach terrain
        g2d.setPaint(beachTexture);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderCloudyBeachTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        TexturePaint beachTexture = createBeachTexture(Color.GRAY); // Gray for cloudy beach terrain
        g2d.setPaint(beachTexture);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderRainyBeachTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        TexturePaint beachTexture = createBeachTexture(Color.LIGHT_GRAY); // Light gray for rainy beach terrain
        g2d.setPaint(beachTexture);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderStormyBeachTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        TexturePaint beachTexture = createBeachTexture(Color.DARK_GRAY); // Dark gray for stormy beach terrain
        g2d.setPaint(beachTexture);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderSunnyPlainTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        TexturePaint plainTexture = createPlainTexture(new Color(100, 200, 100)); // Light green for sunny plain terrain
        g2d.setPaint(plainTexture);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderCloudyPlainTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        TexturePaint plainTexture = createPlainTexture(Color.LIGHT_GRAY); // Light gray for cloudy plain terrain
        g2d.setPaint(plainTexture);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderRainyPlainTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        TexturePaint plainTexture = createPlainTexture(new Color(50, 120, 180)); // Blue for rainy plain terrain
        g2d.setPaint(plainTexture);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderStormyPlainTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        TexturePaint plainTexture = createPlainTexture(Color.DARK_GRAY); // Dark gray for stormy plain terrain
        g2d.setPaint(plainTexture);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    /**
     * Creates a plain texture with the specified color.
     *
     * @param color the color of the plain
     * @return a TexturePaint object representing the plain texture
     */
    private TexturePaint createPlainTexture(Color color) {
        BufferedImage plainImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = plainImage.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, 10, 10);
        g2d.dispose();
        return new TexturePaint(plainImage, new Rectangle(10, 10));
    }

    private void renderSunnyTundraTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        // Implement rendering logic for sunny tundra terrain
        g2d.setColor(new Color(220, 220, 220)); // Light gray color for tundra
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderCloudyTundraTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        // Implement rendering logic for cloudy tundra terrain
        g2d.setColor(new Color(200, 200, 200)); // Gray color for cloudy tundra
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderRainyTundraTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        // Implement rendering logic for rainy tundra terrain
        g2d.setColor(new Color(180, 180, 180)); // Dark gray color for rainy tundra
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderStormyTundraTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        // Implement rendering logic for stormy tundra terrain
        g2d.setColor(new Color(160, 160, 160)); // Gray color for stormy tundra
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    /**
     * Creates a beach texture with the specified color.
     *
     * @param color the color of the beach
     * @return a TexturePaint object representing the beach texture
     */
    private TexturePaint createBeachTexture(Color color) {
        BufferedImage beachImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = beachImage.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, 10, 10);
        g2d.dispose();
        return new TexturePaint(beachImage, new Rectangle(10, 10));
    }

    /**
     * Renders sunny terrain on the given Graphics2D context.
     *
     * @param g2d     the Graphics2D context to render on
     * @param x       the starting x-coordinate of the terrain
     * @param y       the starting y-coordinate of the terrain
     * @param width   the width of the terrain
     * @param height  the height of the terrain
     */
    private void renderSunnyTerrain(@NotNull Graphics2D g2d, int x, int y, int width, int height) {
        TexturePaint grassTexture = createGrassTexture(Color.GREEN);
        g2d.setPaint(grassTexture);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    /**
     * Renders cloudy terrain on the given Graphics2D context.
     *
     * @param g2d     the Graphics2D context to render on
     * @param x       the starting x-coordinate of the terrain
     * @param y       the starting y-coordinate of the terrain
     * @param width   the width of the terrain
     * @param height  the height of the terrain
     */
    private void renderCloudyTerrain(@NotNull Graphics2D g2d, int x, int y, int width, int height) {
        TexturePaint grassTexture = createCloudyGrassTexture();
        g2d.setPaint(grassTexture);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderSunnyCanyonTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        // Render sunny canyon terrain
        g2d.setColor(new Color(205, 133, 63)); // Sandy color for sunny canyon terrain
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderCloudyCanyonTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        // Render cloudy canyon terrain
        g2d.setColor(new Color(139, 69, 19)); // Brown color for cloudy canyon terrain
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderRainyCanyonTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        // Render rainy canyon terrain
        g2d.setColor(new Color(112, 128, 144)); // Grayish color for wet canyon terrain
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderStormyCanyonTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        // Render stormy canyon terrain
        g2d.setColor(new Color(112, 128, 144)); // Grayish color for flooded canyon terrain
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }


    /**
     * Renders rainy terrain on the given Graphics2D context.
     *
     * @param g2d     the Graphics2D context to render on
     * @param x       the starting x-coordinate of the terrain
     * @param y       the starting y-coordinate of the terrain
     * @param width   the width of the terrain
     * @param height  the height of the terrain
     */
    private void renderRainyTerrain(@NotNull Graphics2D g2d, int x, int y, int width, int height) {
        TexturePaint wetTexture = createWetTexture();
        g2d.setPaint(wetTexture);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    /**
     * Renders stormy terrain on the given Graphics2D context.
     *
     * @param g2d     the Graphics2D context to render on
     * @param x       the starting x-coordinate of the terrain
     * @param y       the starting y-coordinate of the terrain
     * @param width   the width of the terrain
     * @param height  the height of the terrain
     */
    private void renderStormyTerrain(@NotNull Graphics2D g2d, int x, int y, int width, int height) {
        TexturePaint floodedTexture = createFloodedTexture();
        g2d.setPaint(floodedTexture);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderSunnyVolcanicTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        // Render sunny volcanic terrain
        g2d.setColor(new Color(210, 105, 30)); // Brownish color for volcanic terrain
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderCloudyVolcanicTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        // Render cloudy volcanic terrain
        g2d.setColor(new Color(160, 82, 45)); // Darker brown for cloudy volcanic terrain
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderRainyVolcanicTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        // Render rainy volcanic terrain
        g2d.setColor(new Color(139, 69, 19)); // Brown color for wet volcanic terrain
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderStormyVolcanicTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        // Render stormy volcanic terrain
        g2d.setColor(new Color(139, 69, 19)); // Brown color for flooded volcanic terrain
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderSunnyJungleTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        // Render sunny jungle terrain
        g2d.setColor(new Color(34, 139, 34)); // Green color for sunny jungle terrain
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderCloudyJungleTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        // Render cloudy jungle terrain
        g2d.setColor(new Color(0, 100, 0)); // Dark green color for cloudy jungle terrain
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderRainyJungleTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        // Render rainy jungle terrain
        g2d.setColor(new Color(34, 139, 34)); // Green color for wet jungle terrain
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    private void renderStormyJungleTerrain(Graphics2D g2d, int x, int y, int width, int height) {
        // Render stormy jungle terrain
        g2d.setColor(new Color(34, 139, 34)); // Green color for flooded jungle terrain
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }

    /**
     * Creates a grass texture.
     *
     * @return A TexturePaint object representing the grass texture.
     */
    /**
     * Creates a grass texture with the specified color.
     *
     * @param color the color of the grass
     * @return a TexturePaint object representing the grass texture
     */
    @NotNull
    @Contract("_ -> new")
    private TexturePaint createGrassTexture(Color color) {
        BufferedImage grassImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = grassImage.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, 10, 10);
        g2d.dispose();
        return new TexturePaint(grassImage, new Rectangle(10, 10));
    }


    /**
     * Creates a cloudy grass texture.
     *
     * @return A TexturePaint object representing the cloudy grass texture.
     */
    @NotNull
    @Contract(" -> new")
    private TexturePaint createCloudyGrassTexture() {
        BufferedImage grassImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = grassImage.createGraphics();
        g2d.setColor(new Color(0, 100, 0)); // Darker green for cloudy grass
        g2d.fillRect(0, 0, 10, 10);
        g2d.dispose();
        return new TexturePaint(grassImage, new Rectangle(10, 10));
    }

    /**
     * Creates a wet texture for rendering stormy terrain.
     *
     * @return A TexturePaint object representing the wet texture
     */
    @NotNull
    @Contract(" -> new")
    private TexturePaint createWetTexture() {
        BufferedImage wetImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = wetImage.createGraphics();
        g2d.setColor(new Color(0, 50, 100)); // Blueish color for wet terrain
        g2d.fillRect(0, 0, 10, 10);
        g2d.dispose();
        return new TexturePaint(wetImage, new Rectangle(10, 10));
    }

    /**
     * Creates a flooded texture for rendering stormy terrain.
     *
     * @return a TexturePaint object representing the flooded texture
     */
    @NotNull
    @Contract(" -> new")
    private TexturePaint createFloodedTexture() {
        BufferedImage floodedImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = floodedImage.createGraphics();
        g2d.setColor(new Color(0, 0, 100)); // Dark blue for flooded terrain
        g2d.fillRect(0, 0, 10, 10);
        g2d.dispose();
        return new TexturePaint(floodedImage, new Rectangle(10, 10));
    }

    /**
     * Renders trees on the given Graphics2D context.
     *
     * @param g2d    the Graphics2D context to render on
     * @param x      the starting x-coordinate of the trees
     * @param y      the starting y-coordinate of the trees
     * @param width  the width of the trees
     * @param height the height of the trees
     */
    private void renderTrees(@NotNull Graphics2D g2d, int x, int y, int width, int height) {
        g2d.setColor(new Color(139, 69, 19));
        g2d.fillRect(x + width / 2 - 2, y + height / 2, 4, height / 2);
        g2d.setColor(new Color(0, 100, 0));
        g2d.fillOval(x + width / 2 - 10, y, 20, 20);
    }

    /**
     * Renders flowers on the given Graphics2D context.
     *
     * @param g2d    the Graphics2D context to render on
     * @param x      the starting x-coordinate of the flowers
     * @param y      the starting y-coordinate of the flowers
     * @param width  the width of the flowers
     * @param height the height of the flowers
     */
    private void renderFlowers(@NotNull Graphics2D g2d, int x, int y, int width, int height) {
        g2d.setColor(Color.RED);
        g2d.fillOval(x + 2, y + 2, 6, 6);
    }

    /**
     * Renders a river on the given graphics context.
     *
     * @param g2d     the Graphics2D context to render on
     * @param x       the starting x-coordinate of the river
     * @param y       the starting y-coordinate of the river
     * @param width   the width of the river
     * @param height  the height of the river
     * @param env     the environment used to determine the weather condition
     */
    private void renderRiver(Graphics2D g2d, int x, int y, int width, int height, @NotNull Environment env) {
        if (env.getWeatherCondition() == WeatherType.RAINY || env.getWeatherCondition() == WeatherType.STORMY) {
            g2d.setColor(Color.BLUE);
            int riverWidth = width / 4;
            g2d.fillRect(x + width / 2 - riverWidth / 2, y, riverWidth, height);
        }
    }

    /**
     * Creates a forest texture with the specified color.
     *
     * @param color the color of the forest
     * @return a TexturePaint object representing the forest texture
     */
    @NotNull
    @Contract("_ -> new")
    private TexturePaint createForestTexture(Color color) {
        BufferedImage forestImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = forestImage.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, 10, 10);
        g2d.dispose();
        return new TexturePaint(forestImage, new Rectangle(10, 10));
    }

    /**
     * Renders a rain effect on the given graphics context within the specified area.
     *
     * @param g2d     the graphics context to render with
     * @param x       the x-coordinate of the top-left corner of the area
     * @param y       the y-coordinate of the top-left corner of the area
     * @param width   the width of the area
     * @param height  the height of the area
     */
    private void renderRainEffect(Graphics2D g2d, int x, int y, int width, int height) {
        Random random = new Random();
        int widthBound = Math.max(width, 1);
        int heightBound = Math.max(height, 1);
        int numRaindrops = random.nextInt(10) + 5;
        for (int i = 0; i < numRaindrops; i++) {
            int raindropX = x + random.nextInt(widthBound);
            int raindropY = y + random.nextInt(heightBound);
            g2d.setColor(Color.BLUE);
            g2d.fillRect(raindropX, raindropY, 1, 5);
        }
        int numPuddles = random.nextInt(3) + 1;
        for (int i = 0; i < numPuddles; i++) {
            int puddleX = x + random.nextInt(Math.max(widthBound - 10, 1));
            int puddleY = y + random.nextInt(Math.max(heightBound - 10, 1));
            int puddleWidth = random.nextInt(10) + 5;
            int puddleHeight = random.nextInt(5) + 5;
            g2d.setColor(Color.BLUE);
            g2d.fillOval(puddleX, puddleY, puddleWidth, puddleHeight);
        }
    }

    /**
     * Renders a storm effect on the given Graphics2D object.
     *
     * @param g2d    the Graphics2D object to render on
     * @param x      the x-coordinate of the top-left corner of the area to render the storm effect
     * @param y      the y-coordinate of the top-left corner of the area to render the storm effect
     * @param width  the width of the area to render the storm effect
     * @param height the height of the area to render the storm effect
     */
    private void renderStormEffect(Graphics2D g2d, int x, int y, int width, int height) {
        int cloudX = x + width / 2;
        int cloudY = y + height / 2;
        int cloudHeight = height / 2;
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillOval(cloudX - width / 2, cloudY - cloudHeight / 2, width, cloudHeight);
        int lightningX = x + width / 2;
        int lightningY = y + 5;
        g2d.setColor(Color.WHITE);
        g2d.drawLine(lightningX, lightningY, lightningX, lightningY + height / 2);
    }

    @Override
    public String toString() {
        return "Chunk{" +
                "environment=" + environment +
                ", image=" + image.getTileWidth() +
                '}';
    }
}
