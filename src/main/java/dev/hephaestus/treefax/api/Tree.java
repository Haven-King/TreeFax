package dev.hephaestus.treefax.api;

import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class Tree {
    private final List<BlockPos> logList;
    private final Collection<BlockPos> logs;

    public Tree(StructureWorldAccess structureWorldAccess, Collection<BlockPos> logs) {
        this.logList = new ArrayList<>(logs.size());
        this.logs = new HashSet<>(logs.size());

        for (BlockPos log : logs) {
            if (structureWorldAccess.getBlockState(log).isIn(BlockTags.LOGS)) {
                log = log.toImmutable();

                this.logList.add(log);
                this.logs.add(log);
            }
        }
    }

    public Tree(List<BlockPos> logs) {
        this.logList = logs;
        this.logs = new HashSet<>(logs);
    }

    public void forEachLog(Consumer<BlockPos> consumer) {
        this.logList.forEach(consumer);
    }

    public boolean containsLog(BlockPos pos) {
        return this.logs.contains(pos);
    }

    public @Nullable BlockPos getLog(BlockPos pos) {
        for (BlockPos log : this.logList) {
            if (log.equals(pos)) return log;
        }

        return null;
    }

    public void removeLog(BlockPos pos) {
        this.logList.remove(pos);
        this.logs.remove(pos);
    }
}
