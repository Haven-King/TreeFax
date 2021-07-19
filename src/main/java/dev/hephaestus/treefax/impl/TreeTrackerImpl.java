package dev.hephaestus.treefax.impl;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.hephaestus.treefax.api.Tree;
import dev.hephaestus.treefax.api.TreeTracker;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class TreeTrackerImpl implements TreeTracker {
    private final Chunk chunk;
    private final Multimap<BlockPos, Tree> trees = HashMultimap.create();

    public TreeTrackerImpl(Chunk chunk) {
        this.chunk = chunk;
    }

    @Override
    public void addTree(Tree tree) {
        ChunkPos chunkPos = this.chunk.getPos();

        tree.forEachLog(log -> {
            if (contains(chunkPos, log)) {
                this.trees.put(log, tree);
            }
        });
    }

    @Override
    public boolean breakTree(ServerWorld world, @Nullable ServerPlayerEntity player, @Nullable ItemStack stack, BlockPos pos, boolean fullFell) {
        Tree tree = this.getTree(pos);

        if (tree != null) {
            List<BlockPos> removed = new ArrayList<>();

            tree.forEachLog(log -> {
                if (log.getY() >= pos.getY() || fullFell) {
                    BlockState state = world.getBlockState(log);
                    Block block = state.getBlock();

                    if (state.isIn(BlockTags.LOGS)) {
                        if (player != null) {
                            block.onBreak(world, log, state, player);
                        } else {
                            world.syncWorldEvent(null, WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(state));
                            world.emitGameEvent(null, GameEvent.BLOCK_DESTROY, pos);
                        }

                        if (world.removeBlock(log, false)) {
                            world.emitGameEvent(player, GameEvent.BLOCK_DESTROY, log);
                            block.onBroken(world, log, state);

                            if (player != null && stack != null) {
                                if (!player.isCreative()) {
                                    stack.postMine(world, state, log, player);
                                    state.getBlock().afterBreak(world, player, log, state, null, stack);
                                }
                            }

                            removed.add(log);

                            TreeTrackerImpl tracker = ((TreeChunk) (world.getChunk(log))).getTracker();

                            tracker.trees.remove(log, tracker.getTree(log));
                        }
                    }
                }
            });

            removed.forEach(tree::removeLog);

            return true;
        }

        return false;
    }

    @Override
    public boolean breakTree(ServerWorld world, BlockPos pos, boolean fullFell) {
        return breakTree(world, null, null, pos, fullFell);
    }

    @Override
    public @Nullable Tree getTree(@NotNull BlockPos pos) {
        for (Tree tree : this.trees.get(pos)) {
            if (tree.containsLog(pos) && pos.equals(tree.getLog(pos))) {
                return tree;
            }
        }

        return null;
    }

    public void readFromNbt(NbtCompound tag) {
        if (tag.contains("Trees", NbtElement.LIST_TYPE)) {
            NbtList list = tag.getList("Trees", NbtElement.COMPOUND_TYPE);

            for (NbtElement element : list) {
                if (element.getType() == NbtElement.COMPOUND_TYPE) {
                    NbtCompound compound = (NbtCompound) element;

                    List<BlockPos> logs = compound.contains("Logs", NbtElement.LIST_TYPE)
                            ? fromTag(compound.getList("Logs", NbtElement.INT_ARRAY_TYPE))
                            : Collections.emptyList();

                    Tree tree = new Tree(logs);

                    for (BlockPos log : logs) {
                        this.trees.put(log, tree);
                    }
                }
            }
        }
    }

    public void writeToNbt(NbtCompound tag) {
        NbtList list = new NbtList();

        for (Tree tree : new HashSet<>(this.trees.values())) {
            NbtCompound treeTag = new NbtCompound();

            NbtList logs = new NbtList();
            NbtList leaves = new NbtList();

            tree.forEachLog(pos -> logs.add(toTag(pos)));

            treeTag.put("Logs", logs);
            treeTag.put("Leaves", leaves);

            list.add(treeTag);
        }

        tag.put("Trees", list);
    }

    private static NbtIntArray toTag(BlockPos pos) {
        return new NbtIntArray(new int[] {pos.getX(), pos.getY(), pos.getZ()});
    }

    private static BlockPos fromTag(NbtIntArray tag) {
        return new BlockPos(tag.get(0).intValue(), tag.get(1).intValue(), tag.get(2).intValue());
    }

    private static List<BlockPos> fromTag(NbtList list) {
        List<BlockPos> logs = new ArrayList<>(list.size());

        for (NbtElement element : list) {
            if (element.getType() == NbtElement.COMPOUND_TYPE) {
                logs.add(fromTag((NbtIntArray) element));
            }
        }

        return logs;
    }

    private static boolean contains(ChunkPos chunkPos, BlockPos blockPos) {
        return blockPos.getX() >= chunkPos.getStartX() && blockPos.getX() <= chunkPos.getEndX()
                && blockPos.getZ() >= chunkPos.getStartZ() && blockPos.getZ() <= chunkPos.getEndZ();
    }

    public void remove(BlockPos pos, Tree tree) {
        this.trees.remove(pos, tree);
    }
}
