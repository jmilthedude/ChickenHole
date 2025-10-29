package net.ninjadev.chickenhole.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.ninjadev.chickenhole.mine.MineJobManager;
import net.ninjadev.chickenhole.util.Constants;
import net.ninjadev.chickenhole.util.HoleType;
import net.ninjadev.chickenhole.util.HoleUtil;

import java.util.List;
import java.util.Map;

public class DigPayload implements CustomPayload {

    public static final Id<DigPayload> ID = new Id<>(Identifier.of(Constants.MOD_ID, "dig_payload"));
    public static final PacketCodec<RegistryByteBuf, DigPayload> CODEC = PacketCodec.of(
            DigPayload::write,
            DigPayload::read
    );

    private final BlockPos origin;
    private final int size;
    private final HoleType holeType;
    private final double speed;
    private final boolean enableDrops;

    public DigPayload(RegistryByteBuf buf) {
        this.origin = buf.readBlockPos();
        this.size = buf.readInt();
        this.holeType = HoleType.values()[buf.readInt()];
        this.speed = buf.readDouble();
        this.enableDrops = buf.readBoolean();
    }

    public DigPayload(BlockPos origin, int size, HoleType holeType, double speed, boolean enableDrops) {
        this.origin = origin;
        this.size = size;
        this.holeType = holeType;
        this.speed = speed;
        this.enableDrops = enableDrops;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void write(DigPayload payload, RegistryByteBuf buf) {
        buf.writeBlockPos(payload.origin);
        buf.writeInt(payload.size);
        buf.writeInt(payload.holeType.ordinal());
        buf.writeDouble(payload.speed);
        buf.writeBoolean(payload.enableDrops);
    }

    public static DigPayload read(RegistryByteBuf buf) {
        return new DigPayload(buf);
    }

    public void handle(ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            ServerWorld serverWorld = context.player().getEntityWorld();

            MobEntity entity = EntityType.CHICKEN.create(serverWorld, SpawnReason.SPAWN_ITEM_USE);
            if (entity == null) return;

            entity.refreshPositionAndAngles(this.origin.up(), 0f, 0f);
            entity.setAiDisabled(false);
            entity.setInvulnerable(true);
            entity.setCustomName(Text.of("RumSoakedChicken"));
            entity.setCustomNameVisible(true);

            if (!serverWorld.spawnEntity(entity)) return;

            serverWorld.getServer().execute(() -> {
                Map<Integer, List<BlockPos>> targets = HoleUtil.collectReachablePositionsSorted(serverWorld, this.origin, this.holeType, this.size);
                if (targets.isEmpty()) {
                    return;
                }
                MineJobManager.getInstance().addPlan(entity.getUuid(), targets, this.enableDrops, (float) this.speed);
            });
        });
    }
}
