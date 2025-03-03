/*
---------------------------------------------------------------------------------
File Name : UserMiningInventory

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 08/06/2024
Last Modified : 10/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.game.farm;

import eu.fluffici.bot.api.inventory.InventoryItem;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * The UserMiningInventory class represents the mining inventory of a user in a mining game.
 * It keeps track of the items collected, tool durability, and the current position in the mining area.
 */
public class UserMiningInventory {
    @Getter
    private Map<String, Integer> items;

    private int toolDurability;
    private int posX;
    private int posY;
    private int posZ;

    @Getter
    private final InventoryItem playerTool;

    @Getter
    private final MiningChunk miningChunk;

    /**
     * The UserMiningInventory class represents the mining inventory of a user in a mining game.
     * It keeps track of the items collected, tool durability, and the current position in the mining area.
     */
    public UserMiningInventory(@NotNull InventoryItem tool, FarmingType farmingType) {
        this.miningChunk = new MiningChunk(farmingType);
        this.items = new HashMap<>();
        this.toolDurability = tool.getDurability();
        this.playerTool = tool;
        this.posX = 0;
        this.posY = 0;
        this.posZ = 0;

        if (!this.playerTool.getEquipmentType().equals(farmingType.getEquipmentType())) {
            throw new IllegalArgumentException("The equipment type does not match the required batch for this farming group");
        }
    }

    /**
     * Adds an item to the UserMiningInventory.
     *
     * This method adds the specified item to the UserMiningInventory. If the item is already present in the inventory,
     * its quantity is incremented by 1. If the item is not present, a new entry is created with a quantity of 1.
     *
     * @param item The item to be added to the inventory.
     */
    public void addItem(String item) {
        this.items.put(item, this.items.getOrDefault(item, 0) + 1);
    }

    /**
     * Decreases the tool durability by 1.
     *
     * This method reduces the durability of the tool by 1. It is called when a block is mined with the tool.
     * It updates the toolDurability variable of the UserMiningInventory object.
     */
    public void decreaseToolDurability() {
        this.playerTool.setDurability(Math.max(0, this.playerTool.getDurability() - this.toolDurability--));
    }

    /**
     * Moves the position to the next block in the mining area.
     * If the current position reaches the end of a layer, it moves to the next layer. If the current layer reaches
     * the end of the mining area, it moves to the next z-coordinate. If the z-coordinate reaches the end, it wraps
     * around to the beginning.
     */
    public void moveToNextBlock() {
        posX++;
        if (posX >= this.miningChunk.getX()) {
            posX = 0;
            posY++;
            if (posY >= this.miningChunk.getY()) {
                posY = 0;
                posZ++;
                if (posZ >= this.miningChunk.getZ()) {
                    posZ = 0;
                }
            }
        }
    }

    /**
     * Mines the next block in the given mining chunk.
     *
     * @param chunk The mining chunk from which to mine the block.
     * @return true if the block was successfully mined, false otherwise.
     */
    public boolean mineNextBlock(MiningChunk chunk) {
        if (this.toolDurability <= 0) {
            return false;
        }

        String minedItem = chunk.mineBlock(posX, posY, posZ);
        if (minedItem.equalsIgnoreCase("air")) {
            return true;
        } else {
            addItem(minedItem);
        }

        moveToNextBlock();
        return true;
    }
}