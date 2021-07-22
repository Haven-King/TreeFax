package dev.hephaestus.treefax.impl;

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
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TreeTrackerImpl implements TreeTracker {
    private final Chunk chunk;

    private Tree[][][][] trees = null;

    public TreeTrackerImpl(Chunk chunk) {
        this.chunk = chunk;
    }

    @Override
    public void addTree(Tree tree) {
        ChunkPos chunkPos = this.chunk.getPos();

        tree.forEachLog(log -> {
            if (contains(chunkPos, log)) {
                this.put(log, tree);
            }
        });
    }

    private void init(BlockPos pos) {
        int chunkX = MathHelper.floorMod(pos.getX(), 16), chunkY = (pos.getY() - this.chunk.getBottomY()) / 16, dY = MathHelper.floorMod(pos.getY() - this.chunk.getBottomY(), 16);

        if (this.trees == null) this.trees = new Tree[16][][][];
        if (this.trees[chunkX] == null) this.trees[chunkX] = new Tree[this.chunk.getHeight() / 16][][];
        if (this.trees[chunkX][chunkY] == null) this.trees[chunkX][chunkY] = new Tree[16][];
        if (this.trees[chunkX][chunkY][dY] == null) this.trees[chunkX][chunkY][dY] = new Tree[16];
    }

    private void put(BlockPos pos, Tree tree) {
        int chunkX = MathHelper.floorMod(pos.getX(), 16), chunkY = (pos.getY() - this.chunk.getBottomY()) / 16, dY = MathHelper.floorMod(pos.getY() - this.chunk.getBottomY(), 16), chunkZ = MathHelper.floorMod(pos.getZ(), 16);

        this.init(pos);

        this.trees[chunkX][chunkY][dY][chunkZ] = tree;
    }

    public void remove(BlockPos pos) {
        if (this.trees == null) return;

        int chunkX = MathHelper.floorMod(pos.getX(), 16), chunkY = (pos.getY() - this.chunk.getBottomY()) / 16, dY = MathHelper.floorMod(pos.getY() - this.chunk.getBottomY(), 16), chunkZ = MathHelper.floorMod(pos.getZ(), 16);

        if (this.trees[chunkX] == null) return;

        if (this.trees[chunkX][chunkY] == null) return;

        if (this.trees[chunkX][chunkY][dY] == null) return;

        this.trees[chunkX][chunkY][dY][chunkZ] = null;
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

                            tracker.remove(log);
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
        return this.getTree(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public @Nullable Tree getTree(int x, int y, int z) {
        if (this.trees == null) return null;

        int chunkX = MathHelper.floorMod(x, 16), chunkY = (y - this.chunk.getBottomY()) / 16, dY = MathHelper.floorMod(y - this.chunk.getBottomY(), 16), chunkZ = MathHelper.floorMod(z, 16);

        return this.trees[chunkX] == null || this.trees[chunkX][chunkY] == null || this.trees[chunkX][chunkY][dY] == null
                ? null : this.trees[chunkX][chunkY][dY][chunkZ];

    }

    public void readFromNbt(NbtCompound tag) {
        if (tag.contains("Trees", NbtElement.LIST_TYPE)) {
            NbtList list = tag.getList("Trees", NbtElement.LIST_TYPE);

            for (NbtElement element : list) {
                if (element.getType() == NbtElement.LIST_TYPE) {
                    List<BlockPos> logs = fromTag((NbtList) element);

                    Tree tree = new Tree(logs);

                    for (int i = 0; i < logs.size(); ++i) {
                        this.put(logs.get(i), tree);
                    }
                }
            }
        }
    }

    public void writeToNbt(NbtCompound tag) {
        NbtList list = new NbtList();

        if (this.trees != null) {
            for (int x = 0; x < 16; ++x) {
                if (this.trees[x] == null) continue;

                for (int chunkY = 0; chunkY < this.chunk.getHeight() / 16; ++chunkY) {
                    if (this.trees[x][chunkY] == null) continue;

                    for (int dY = 0; dY < 16; ++dY) {
                        if (this.trees[x][chunkY][dY] == null) continue;

                        for (int z = 0; z < 16; ++z) {
                            if (this.trees[x][chunkY][dY][z] == null) continue;

                            Tree tree = trees[x][chunkY][dY][z];

                            NbtList logs = new NbtList();

                            tree.forEachLog(pos -> logs.add(toTag(pos)));
                            list.add(logs);
                        }
                    }
                }
            }
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
            if (element.getType() == NbtElement.INT_ARRAY_TYPE) {
                logs.add(fromTag((NbtIntArray) element));
            }
        }

        return logs;
    }

    private static boolean contains(ChunkPos chunkPos, BlockPos blockPos) {
        return blockPos.getX() >= chunkPos.getStartX() && blockPos.getX() <= chunkPos.getEndX()
                && blockPos.getZ() >= chunkPos.getStartZ() && blockPos.getZ() <= chunkPos.getEndZ();
    }
}
