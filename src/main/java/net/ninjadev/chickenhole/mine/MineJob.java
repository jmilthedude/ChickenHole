package net.ninjadev.chickenhole;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class MinePlan {

    private final UUID entityId;
    private final BlockPos center;
    private final int radius;
    private final NavigableMap<Integer, List<BlockPos>> blocksToMine;
    private Integer currentLayer;
    private int index;
    private int cooldown;

    public MinePlan(UUID entityId, BlockPos center, int radius, Map<Integer, List<BlockPos>> blocksToMine) {
        this.entityId = entityId;
        this.center = center;
        this.radius = radius;
        this.blocksToMine = new TreeMap<>(Comparator.reverseOrder());
        this.blocksToMine.putAll(blocksToMine);
        this.currentLayer = this.blocksToMine.isEmpty() ? Integer.MIN_VALUE : this.blocksToMine.firstKey();
        this.index = 0;
        this.cooldown = 0;
    }

    public UUID getEntityId() {
        return this.entityId;
    }

    public BlockPos getCenter() {
        return this.center;
    }

    public int getRadius() {
        return this.radius;
    }

    public Map<Integer, List<BlockPos>> getBlocksToMine() {
        return this.blocksToMine;
    }

    public int getCooldown() {
        return this.cooldown;
    }

    public boolean isDone() {
        return this.currentLayer == null;
    }

    public Optional<BlockPos> getNext(ServerWorld world) {
        if (currentLayer == null) {
            return Optional.empty();
        }
        List<BlockPos> layer = this.blocksToMine.get(currentLayer);
        if (layer == null || layer.isEmpty()) {
            currentLayer = this.blocksToMine.higherKey(currentLayer);
            index = 0;
            return this.getNext(world);
        }

        if (index >= layer.size()) {
            index = 0;
        }

        BlockPos pos = layer.get(index);
        if (world.getBlockState(pos).isAir()) {
            layer.remove(pos);
            index++;
        }
        if (layer.isEmpty()) {
            currentLayer--;
            index = 0;
        }
        return Optional.of(pos);
    }

    public void tickCooldown() {
        if (this.cooldown > 0) {
            this.cooldown--;
        }
    }

    public void advance() {
        if (!this.isDone()) {
            this.index++;
        }
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public void removeMinedBlock(BlockPos toMine) {
        List<BlockPos> layer = this.blocksToMine.get(currentLayer);
        if (layer != null) {
            layer.remove(toMine);
            if (layer.isEmpty()) {
                this.blocksToMine.remove(currentLayer);
                currentLayer = this.blocksToMine.higherKey(currentLayer);
                index = 0;
            }
        }
    }
}
