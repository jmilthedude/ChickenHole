package net.ninjadev.chickenhole.mine;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class MineJob {

    private final NavigableMap<Integer, List<BlockPos>> blocksToMine;
    private final boolean enableDrops;
    private final float speed;
    private Integer currentLayer;
    private int index;


    public MineJob(Map<Integer, List<BlockPos>> blocksToMine, boolean enableDrops, float speed) {
        this.blocksToMine = new TreeMap<>(Comparator.reverseOrder());
        this.blocksToMine.putAll(blocksToMine);
        this.enableDrops = enableDrops;
        this.speed = speed;
        this.currentLayer = this.blocksToMine.isEmpty() ? null : this.blocksToMine.firstKey();
        this.index = 0;
    }

    public boolean isEnableDrops() {
        return this.enableDrops;
    }

    public float getSpeed() {
        return speed;
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
            layer.remove(index);
            if (layer.isEmpty()) {
                this.blocksToMine.remove(currentLayer);
                currentLayer = this.blocksToMine.higherKey(currentLayer);
                index = 0;
            }
            return this.getNext(world);
        }

        return Optional.of(pos);
    }

    public void removeMinedBlock(BlockPos toMine) {
        if (currentLayer == null) {
            return;
        }

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
