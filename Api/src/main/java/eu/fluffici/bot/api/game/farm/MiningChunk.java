/*
---------------------------------------------------------------------------------
File Name : MiningChunk

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 08/06/2024
Last Modified : 10/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.game.farm;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;


public class MiningChunk {
    private final String[][][] blocks;
    private static final Map<String, Double> baseProbabilities = new HashMap<>();
    private static final Random random = new Random();

    @Getter
    private int x;
    @Getter
    private int y;
    @Getter
    private int z;

    public MiningChunk(FarmingType farmingType) {
        this.x = random.nextInt(0, 6);
        this.y = random.nextInt(0, 128);
        this.z = random.nextInt(0, 6);

        this.blocks = new String[this.x][this.y][this.z];
        this.loadBaseProbabilities(farmingType);
        generateChunk();
    }

    /**
     * Generates a chunk of blocks in the MiningChunk.
     *
     * This method randomly selects resources from the baseProbabilities
     * and assigns them to each block in the MiningChunk.
     */
    private void generateChunk() {
        Set<String> availableResources = selectRandomResources();

        for (int x = 0; x < this.x; x++) {
            for (int y = 0; y < this.y; y++) {
                for (int z = 0; z < this.z; z++) {
                    this.blocks[x][y][z] = getRandomBlock(availableResources);
                }
            }
        }
    }

    /**
     * Selects a random subset of resources to be included in this chunk.
     *
     * @return A set of resource names to be included in the chunk.
     */
    private Set<String> selectRandomResources() {
        int numberOfResources = random.nextInt(baseProbabilities.size()) + 1;
        return random.ints(0, baseProbabilities.size())
                .distinct()
                .limit(numberOfResources)
                .mapToObj(i -> baseProbabilities.keySet().toArray(new String[0])[i])
                .collect(Collectors.toSet());
    }

    /**
     * Generates a random block based on predefined probabilities and available resources.
     *
     * @param availableResources The set of resources available in this chunk.
     * @return The randomly generated block.
     */
    private String getRandomBlock(Set<String> availableResources) {
        double totalProbability = availableResources.stream()
                .mapToDouble(baseProbabilities::get)
                .sum();
        double randomValue = random.nextDouble() * totalProbability;
        double cumulativeProbability = 0.0;

        for (String resource : availableResources) {
            cumulativeProbability += baseProbabilities.get(resource);
            if (randomValue < cumulativeProbability) {
                return resource;
            }
        }

        return "stone";
    }

    /**
     * Returns the block at the specified coordinates and marks it as mined.
     *
     * @param x The x-coordinate of the block.
     * @param y The y-coordinate of the block.
     * @param z The z-coordinate of the block.
     * @return The block at the specified coordinates, or "air" if the coordinates are out of range.
     */
    public String mineBlock(int x, int y, int z) {
        if (x < 0 || x >= this.x || y < 0 || y >= this.y || z < 0 || z >= this.z) {
            return "air";
        }
        String block = this.blocks[x][y][z];
        this.blocks[x][y][z] = "air";
        return block;
    }

    /**
     * Loads specific base probabilities for each FarmingType.
     *
     * @param farmingType The FarmingType identifier.
     */
    public void loadBaseProbabilities(@NotNull FarmingType farmingType) {
        baseProbabilities.clear();
        switch (farmingType) {
            case MINER:
                baseProbabilities.put("stone", 0.5);
                baseProbabilities.put("dirt", 0.2);
                baseProbabilities.put("coal", 0.008);
                baseProbabilities.put("iron", 0.005);
                baseProbabilities.put("gold", 0.003);
                baseProbabilities.put("diamond", 0.0015);
                baseProbabilities.put("emerald", 0.0008);
                baseProbabilities.put("ruby", 0.0004);
                baseProbabilities.put("dragonium", 0.0002);
                break;
            case LUMBERJACK:
                baseProbabilities.put("oak_log", 0.2);
                baseProbabilities.put("birch_log", 0.15);
                baseProbabilities.put("spruce_log", 0.15);
                baseProbabilities.put("jungle_log", 0.1);
                baseProbabilities.put("acacia_log", 0.1);
                baseProbabilities.put("dark_oak_log", 0.1);
                baseProbabilities.put("bamboo", 0.05);
                baseProbabilities.put("willow_log", 0.05);
                baseProbabilities.put("cherry_log", 0.05);
                baseProbabilities.put("cedar_log", 0.05);
                break;
            case FARMER:
                baseProbabilities.put("wheat", 0.2);
                baseProbabilities.put("carrot", 0.15);
                baseProbabilities.put("potato", 0.15);
                baseProbabilities.put("beetroot", 0.1);
                baseProbabilities.put("corn", 0.1);
                baseProbabilities.put("tomato", 0.1);
                baseProbabilities.put("pumpkin", 0.05);
                baseProbabilities.put("melon", 0.05);
                baseProbabilities.put("lettuce", 0.05);
                baseProbabilities.put("cabbage", 0.05);
                break;
            case BUTCHER:
                baseProbabilities.put("beef", 0.3);
                baseProbabilities.put("pork", 0.2);
                baseProbabilities.put("chicken", 0.2);
                baseProbabilities.put("mutton", 0.1);
                baseProbabilities.put("venison", 0.1);
                baseProbabilities.put("rabbit", 0.05);
                baseProbabilities.put("duck", 0.05);
                break;
            case HUNTER:
                baseProbabilities.put("venison", 0.3);
                baseProbabilities.put("rabbit", 0.2);
                baseProbabilities.put("boar", 0.2);
                baseProbabilities.put("pheasant", 0.1);
                break;
            case ALCHEMIST:
                baseProbabilities.put("herb", 0.3);
                baseProbabilities.put("mushroom", 0.25);
                baseProbabilities.put("crystal", 0.2);
                baseProbabilities.put("essence", 0.1);
                baseProbabilities.put("root", 0.05);
                baseProbabilities.put("flower", 0.05);
                baseProbabilities.put("fungus", 0.05);
                baseProbabilities.put("elixir", 0.05);
                break;
            case FISHERMAN:
                baseProbabilities.put("cod", 0.2);
                baseProbabilities.put("salmon", 0.2);
                baseProbabilities.put("tuna", 0.15);
                baseProbabilities.put("trout", 0.1);
                baseProbabilities.put("bass", 0.1);
                baseProbabilities.put("halibut", 0.05);
                baseProbabilities.put("catfish", 0.05);
                baseProbabilities.put("herring", 0.05);
                baseProbabilities.put("mackerel", 0.05);
                baseProbabilities.put("shrimp", 0.05);
                break;
            case HERBALIST:
                baseProbabilities.put("lavender", 0.2);
                baseProbabilities.put("rosemary", 0.15);
                baseProbabilities.put("sage", 0.15);
                baseProbabilities.put("thyme", 0.1);
                baseProbabilities.put("basil", 0.1);
                baseProbabilities.put("mint", 0.1);
                baseProbabilities.put("oregano", 0.05);
                baseProbabilities.put("parsley", 0.05);
                baseProbabilities.put("dill", 0.05);
                baseProbabilities.put("chamomile", 0.05);
                break;
            case BLACKSMITH:
                baseProbabilities.put("iron_ingot", 0.3);
                baseProbabilities.put("steel_ingot", 0.25);
                baseProbabilities.put("mithril_ingot", 0.2);
                baseProbabilities.put("adamantite_ingot", 0.1);
                baseProbabilities.put("copper_ingot", 0.05);
                baseProbabilities.put("bronze_ingot", 0.05);
                baseProbabilities.put("silver_ingot", 0.05);
                baseProbabilities.put("gold_ingot", 0.05);
                break;
            case CARPENTER:
                baseProbabilities.put("plank", 0.3);
                baseProbabilities.put("beam", 0.25);
                baseProbabilities.put("panel", 0.2);
                baseProbabilities.put("joint", 0.1);
                baseProbabilities.put("sawdust", 0.05);
                baseProbabilities.put("veneer", 0.05);
                baseProbabilities.put("pulp", 0.05);
                baseProbabilities.put("chipboard", 0.05);
                break;
            case GARDENER:
                baseProbabilities.put("rose", 0.2);
                baseProbabilities.put("tulip", 0.15);
                baseProbabilities.put("daisy", 0.15);
                baseProbabilities.put("orchid", 0.1);
                baseProbabilities.put("sunflower", 0.1);
                baseProbabilities.put("lily", 0.1);
                baseProbabilities.put("dahlia", 0.05);
                baseProbabilities.put("peony", 0.05);
                baseProbabilities.put("marigold", 0.05);
                baseProbabilities.put("lavender", 0.05);
                break;
            case BREWER:
                baseProbabilities.put("barley", 0.3);
                baseProbabilities.put("hops", 0.25);
                baseProbabilities.put("yeast", 0.2);
                baseProbabilities.put("grapes", 0.1);
                baseProbabilities.put("wheat", 0.05);
                baseProbabilities.put("corn", 0.05);
                baseProbabilities.put("sugar", 0.05);
                baseProbabilities.put("apple", 0.05);
                break;
            default:
                throw new IllegalArgumentException("Unsupported farming type: " + farmingType);
        }
    }

    /**
     * Checks if the chunk is fully mined.
     *
     * @return true if all blocks in the chunk are mined (i.e., set to "air"), false otherwise.
     */
    public boolean isFullyMined() {
        for (int x = 0; x < this.x; x++)
            for (int y = 0; y < this.y; y++)
                for (int z = 0; z < this.z; z++)
                    if (!this.blocks[x][y][z].equals("air"))
                        return false;
        return true;
    }

    @Override
    public String toString() {
        return "MiningChunk{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
