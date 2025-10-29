package net.ninjadev.chickenhole.init;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.ninjadev.chickenhole.mine.MineJobManager;
import net.ninjadev.chickenhole.network.ChickenScreenPayload;

public class ModEvents {

    public static void init() {
        UseBlockCallback.EVENT.register(ModEvents::onUseBlock);
        ServerTickEvents.END_SERVER_TICK.register(ModEvents::onServerTick);
    }

    private static void onServerTick(MinecraftServer server) {
        MineJobManager.getInstance().tick(server);
    }

    private static ActionResult onUseBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hit) {
        if (world.isClient() || hand == Hand.OFF_HAND) {
            return ActionResult.PASS;
        }

        ItemStack stackInHand = player.getStackInHand(hand);
        if (!stackInHand.isEmpty()) {
            return ActionResult.PASS;
        }

        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            return ActionResult.PASS;
        }

        BlockPos clicked = hit.getBlockPos();
        ServerPlayNetworking.send(serverPlayer, new ChickenScreenPayload(clicked));
        return ActionResult.SUCCESS;
    }
}
