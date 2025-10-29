package net.ninjadev.chickenhole.mine;

import net.minecraft.block.BlockState;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MineJobManager {

    private static final double MINING_REACH = 1.5;
    private static final int PATH_BUFFER = 0;

    private static MineJobManager instance;

    public static synchronized MineJobManager getInstance() {
        if (instance == null) {
            instance = new MineJobManager();
        }
        return instance;
    }

    private final Map<UUID, MineJob> jobs = new HashMap<>();

    public void addPlan(UUID entityId, Map<Integer, List<BlockPos>> blocksToMine, boolean enableDrops, float speed) {
        jobs.put(entityId, new MineJob(blocksToMine, enableDrops, speed));
    }

    public void tick(MinecraftServer server) {
        ServerWorld world = server.getWorld(ServerWorld.OVERWORLD);
        if (world == null) return;

        Iterator<Map.Entry<UUID, MineJob>> iterator = jobs.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, MineJob> entry = iterator.next();
            UUID id = entry.getKey();
            MineJob job = entry.getValue();

            Entity entity = world.getEntity(id);
            if (!(entity instanceof MobEntity mobEntity) || entity.isRemoved()) {
                iterator.remove();
                continue;
            }

            if (job.isDone()) {
                mobEntity.kill(world);
                iterator.remove();
                continue;
            }

            Optional<BlockPos> nextPositionOptional = job.getNext(world);
            if (nextPositionOptional.isEmpty()) {
                continue;
            }

            BlockPos toMine = nextPositionOptional.get();
            entity.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, toMine.toCenterPos());

            if (mobEntity.getBlockPos().isWithinDistance(toMine, MINING_REACH)) {
                handleMining(world, toMine, job);
            } else {
                moveEntityTowards(mobEntity, world, toMine, job.getSpeed());
            }
        }
    }

    private void handleMining(ServerWorld world, BlockPos toMine, MineJob job) {
        BlockState state = world.getBlockState(toMine);
        if (!state.isAir()) {
            if (world.breakBlock(toMine, job.isEnableDrops())) {
                job.removeMinedBlock(toMine);
            }
        }
    }

    private void moveEntityTowards(MobEntity entity, ServerWorld world, BlockPos toMine, float speed) {
        EntityNavigation navigation = entity.getNavigation();

        if (navigation instanceof MobNavigation mobNavigation) {
            if (!mobNavigation.canSwim()) {
                mobNavigation.setCanOpenDoors(true);
                mobNavigation.setCanSwim(true);
            }
        }

        BlockPos target = findBestApproachPosition(entity, world, toMine);
        Path path = navigation.findPathTo(target, PATH_BUFFER);

        if (path != null) {
            navigation.startMovingAlong(path, speed);
        } else {
            navigation.startMovingTo(target.getX(), target.getY(), target.getZ(), speed);
        }
    }

    private BlockPos findBestApproachPosition(MobEntity mobEntity, ServerWorld world, BlockPos toMine) {
        List<BlockPos> candidates = new ArrayList<>(4);

        for (Direction dir : Direction.Type.HORIZONTAL) {
            BlockPos neighbor = toMine.offset(dir);
            if (world.isAir(neighbor)) {
                candidates.add(neighbor);
            }
        }

        if (candidates.isEmpty()) {
            return toMine;
        }

        return getClosest(mobEntity, candidates);
    }

    @NotNull
    private BlockPos getClosest(MobEntity mobEntity, List<BlockPos> candidates) {
        BlockPos closest = candidates.getFirst();
        double closestDistSq = closest.getSquaredDistance(mobEntity.getX(), mobEntity.getY(), mobEntity.getZ());

        for (int i = 1; i < candidates.size(); i++) {
            BlockPos candidate = candidates.get(i);
            double distSq = candidate.getSquaredDistance(mobEntity.getX(), mobEntity.getY(), mobEntity.getZ());
            if (distSq < closestDistSq) {
                closest = candidate;
                closestDistSq = distSq;
            }
        }
        return closest;
    }
}
