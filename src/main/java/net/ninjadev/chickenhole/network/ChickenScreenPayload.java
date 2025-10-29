package net.ninjadev.chickenhole.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import net.ninjadev.chickenhole.util.Constants;

public class ChickenScreenPayload implements CustomPayload {
    public static final Id<ChickenScreenPayload> ID = new Id<>(Constants.SCREEN_ID);
    public static final PacketCodec<RegistryByteBuf, ChickenScreenPayload> CODEC = PacketCodec.of(
            ChickenScreenPayload::encode,
            ChickenScreenPayload::decode
    );

    private void encode(RegistryByteBuf registryByteBuf) {
        registryByteBuf.writeBlockPos(this.origin);
    }

    private static ChickenScreenPayload decode(RegistryByteBuf registryByteBuf) {
        BlockPos origin = registryByteBuf.readBlockPos();
        return new ChickenScreenPayload(origin);
    }

    private final BlockPos origin;

    public ChickenScreenPayload(BlockPos origin) {
        this.origin = origin;
    }

    public BlockPos getOrigin() {
        return origin;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
