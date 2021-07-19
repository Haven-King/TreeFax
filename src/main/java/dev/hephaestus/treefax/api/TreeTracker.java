package dev.hephaestus.treefax.api;

import dev.hephaestus.treefax.impl.TreeChunk;
import dev.hephaestus.treefax.impl.TreeTrackerImpl;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.feature.TreeFeature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TreeTracker {
    /**
     * Adds a tree to the tracker.
     *
     * Can be used by worldgen mods to add support for their trees that don't derive from {@link TreeFeature}.
     */
    void addTree(Tree tree);

    /**
     * @param player the player that is breaking this tree
     * @param stack the stack that is being used to chop this tree
     * @param pos the block being actively broken
     * @param fullFell if true, will break all logs in this tree regardless of location
     *                 if false, will only break logs above the block actively being broken
     * @return whether or not the tree was broken
     */
    boolean breakTree(ServerWorld world, @Nullable ServerPlayerEntity player, @Nullable ItemStack stack, BlockPos pos, boolean fullFell);

    /**
     * @param pos the block being actively broken
     * @param fullFell if true, will break all logs in this tree regardless of location
     *                 if false, will only break logs above the block actively being broken
     * @return whether or not the tree was broken
     */
    boolean breakTree(ServerWorld world, BlockPos pos, boolean fullFell);

    /**
     * @param pos the position of any log that may be in a tree
     * @return a tree if the position passed belongs to one
     */
    @Nullable Tree getTree(@NotNull BlockPos pos);

    static TreeTracker getInstance(Chunk chunk) {
        return ((TreeChunk) chunk).getTracker();
    }

    static void removeTree(ServerWorld world, Tree tree) {
        tree.forEachLog(pos -> {
            TreeTrackerImpl tracker = ((TreeChunk) (world.getChunk(pos))).getTracker();
            tracker.remove(pos, tree);
        });
    }
}
