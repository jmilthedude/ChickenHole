package net.ninjadev.chickenhole.util;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HoleUtil {

    private static final double BLOCK_CENTER_OFFSET = 0.5;
    private static final double SPHERE_CENTER_Y_OFFSET = 1.0;
    private static final int MINIMUM_SAFE_Y = 2;

    public static Map<Integer, List<BlockPos>> collectReachablePositionsSorted(
            ServerWorld world, BlockPos center, HoleType holeType, int radius
    ) {
        if (radius <= 0) {
            return Map.of();
        }

        Map<Integer, List<BlockPos>> layers = new HashMap<>();

        final int minX = center.getX() - radius, maxX = center.getX() + radius;
        final int minZ = center.getZ() - radius, maxZ = center.getZ() + radius;
        final int feetMinY = Math.max(world.getBottomY() + MINIMUM_SAFE_Y, center.getY() - radius + 1);

        final double cx = center.getX() + BLOCK_CENTER_OFFSET;
        final double cz = center.getZ() + BLOCK_CENTER_OFFSET;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                double dx = (x + BLOCK_CENTER_OFFSET) - cx;
                double dz = (z + BLOCK_CENTER_OFFSET) - cz;

                if (!insideHorizontal(holeType, dx, dz, radius)) {
                    continue;
                }

                for (int y = center.getY(); y >= feetMinY; y--) {
                    BlockPos feet = new BlockPos(x, y, z);

                    if (!isInside(center, feet, holeType, radius)) {
                        continue;
                    }

                    if (world.getBlockState(feet).isAir()) {
                        continue;
                    }

                    layers.computeIfAbsent(y, layer -> new ArrayList<>()).add(feet);
                }
            }
        }

        if (!layers.isEmpty()) {
            sortLayers(layers, holeType, cx, cz);
        }

        return layers;
    }

    private static void sortLayers(Map<Integer, List<BlockPos>> layers, HoleType holeType, double cx, double cz) {
        for (Map.Entry<Integer, List<BlockPos>> layer : layers.entrySet()) {
            List<BlockPos> positions = layer.getValue();
            positions.sort((a, b) -> {
                double ax = (a.getX() + BLOCK_CENTER_OFFSET) - cx;
                double az = (a.getZ() + BLOCK_CENTER_OFFSET) - cz;
                double bx = (b.getX() + BLOCK_CENTER_OFFSET) - cx;
                double bz = (b.getZ() + BLOCK_CENTER_OFFSET) - cz;

                int bandA = switch (holeType) {
                    case CUBE -> (int) Math.ceil(maxAbs(ax, az));
                    case SEMISPHERE, CYLINDER -> (int) Math.ceil(Math.hypot(ax, az));
                };
                int bandB = switch (holeType) {
                    case CUBE -> (int) Math.ceil(maxAbs(bx, bz));
                    case SEMISPHERE, CYLINDER -> (int) Math.ceil(Math.hypot(bx, bz));
                };

                int cmpBand = Integer.compare(bandB, bandA);
                if (cmpBand != 0) {
                    return cmpBand;
                }

                double angA = Math.atan2(az, ax);
                double angB = Math.atan2(bz, bx);
                return Double.compare(angA, angB);
            });
        }
    }

    private static boolean insideHorizontal(HoleType type, double dx, double dz, int radius) {
        return switch (type) {
            case SEMISPHERE, CYLINDER -> (dx * dx + dz * dz) <= (radius * radius);
            case CUBE -> maxAbs(dx, dz) <= radius;
        };
    }

    private static boolean isInside(BlockPos center, BlockPos feet, HoleType type, int radius) {
        return switch (type) {
            case SEMISPHERE -> isInsideSphere(center, feet, radius);
            case CYLINDER -> isInsideDisk(center, feet, radius);
            case CUBE -> isInsideSquare(center, feet, radius);
        };
    }

    public static boolean isInsideSphere(BlockPos center, BlockPos feet, int radius) {
        double cx = center.getX() + BLOCK_CENTER_OFFSET;
        double cy = center.getY() + SPHERE_CENTER_Y_OFFSET;
        double cz = center.getZ() + BLOCK_CENTER_OFFSET;
        double dx = (feet.getX() + BLOCK_CENTER_OFFSET) - cx;
        double dy = feet.getY() - cy;
        double dz = (feet.getZ() + BLOCK_CENTER_OFFSET) - cz;
        return (dx * dx + dy * dy + dz * dz) <= (radius * radius);
    }

    private static boolean isInsideDisk(BlockPos center, BlockPos feet, int radius) {
        double cx = center.getX() + BLOCK_CENTER_OFFSET;
        double cz = center.getZ() + BLOCK_CENTER_OFFSET;
        double dx = (feet.getX() + BLOCK_CENTER_OFFSET) - cx;
        double dz = (feet.getZ() + BLOCK_CENTER_OFFSET) - cz;
        return (dx * dx + dz * dz) <= (radius * radius);
    }

    private static boolean isInsideSquare(BlockPos center, BlockPos feet, int radius) {
        double cx = center.getX() + BLOCK_CENTER_OFFSET;
        double cz = center.getZ() + BLOCK_CENTER_OFFSET;
        double dx = (feet.getX() + BLOCK_CENTER_OFFSET) - cx;
        double dz = (feet.getZ() + BLOCK_CENTER_OFFSET) - cz;
        return maxAbs(dx, dz) <= radius;
    }
    private static double maxAbs(double a, double b) {
        return Math.max(Math.abs(a), Math.abs(b));
    }
}
