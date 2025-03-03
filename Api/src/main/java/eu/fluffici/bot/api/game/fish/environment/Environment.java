package eu.fluffici.bot.api.game.fish.environment;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@Setter
public class Environment {
    private boolean isNightTime;
    private TerrainType terrainType;
    private WeatherType weatherCondition;
    private int temperature; // in Celsius
    private int humidity; // percentage

    public Environment() {
        updateEnvironment();
    }

    public Environment(TerrainType terrainType, WeatherType weatherCondition, int temperature, int humidity) {
        this.terrainType = terrainType;
        this.weatherCondition = weatherCondition;
        this.temperature = temperature;
        this.humidity = humidity;
        updateNightTime();
    }

    public void updateEnvironment() {
        terrainType = this.generateTerrainType();
        weatherCondition = this.generateWeatherCondition();
        temperature = this.generateTemperature();
        humidity = this.generateHumidity();
        this.updateNightTime();
    }

    private void updateNightTime() {
        LocalTime now = LocalTime.now(ZoneId.systemDefault());
        isNightTime = now.isBefore(LocalTime.of(6, 0)) || now.isAfter(LocalTime.of(18, 0));
    }

    private TerrainType generateTerrainType() {
        TerrainType[] terrainTypes = TerrainType.values();
        return terrainTypes[ThreadLocalRandom.current().nextInt(terrainTypes.length)];
    }

    private WeatherType generateWeatherCondition() {
        double randomValue = ThreadLocalRandom.current().nextDouble();
        if (randomValue < 0.5) {
            return WeatherType.SUNNY; // 50% chance
        } else if (randomValue < 0.75) {
            return WeatherType.CLOUDY; // 25% chance
        } else if (randomValue < 0.9) {
            return WeatherType.RAINY; // 15% chance
        } else {
            return WeatherType.STORMY; // 10% chance
        }
    }

    private int generateTemperature() {
        // Generate random temperature between -20°C and 30°C
        return ThreadLocalRandom.current().nextInt(-20, 31);
    }

    private int generateHumidity() {
        // Generate random humidity between 0% and 100%
        return ThreadLocalRandom.current().nextInt(101);
    }

    public double calculateEnvironmentalImpact() {
        double impact = 1.0;

        if (isNightTime) {
            impact += 0.2; // Higher impact (more difficult to catch fish) at night
        }

        switch (weatherCondition) {
            case SUNNY:
                impact += 0.1;
                break;
            case CLOUDY:
                impact += 0.2;
                break;
            case RAINY:
                impact += 0.4;
                break;
            case STORMY:
                impact += 0.6;
                break;
        }

        // Additional impact based on terrain type could be added here

        return impact;
    }

    @Override
    public String toString() {
        return "Environment{" +
                "isNightTime=" + isNightTime +
                ", terrainType=" + terrainType +
                ", weatherCondition=" + weatherCondition +
                ", temperature=" + temperature +
                ", humidity=" + humidity +
                '}';
    }
}
