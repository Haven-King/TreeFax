package dev.hephaestus.treefax.api;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

public class Tree {
    private final List<BlockPos> logList;
    private final Collection<BlockPos> logs;

    public Tree(Collection<BlockPos> logs) {
        this.logList = new ArrayList<>(logs.size());
        this.logs = new HashSet<>(logs.size());

        for (BlockPos log : logs) {
            log = log.toImmutable();

            this.logList.add(log);
            this.logs.add(log);
        }
    }

    public Tree(List<BlockPos> logs) {
        this.logList = logs;
        this.logs = new HashSet<>(logs);
    }

    public void forEachLog(Consumer<BlockPos> consumer) {
        for (int i = 0; i < this.logList.size(); ++i) {
            consumer.accept(this.logList.get(i));
        }
    }

    public boolean containsLog(BlockPos pos) {
        return this.logs.contains(pos);
    }

    public @Nullable BlockPos getLog(BlockPos pos) {
        for (int i = 0; i < this.logList.size(); ++i) {
            BlockPos log = this.logList.get(i);
            if (log.equals(pos)) return log;
        }

        return null;
    }

    public void removeLog(BlockPos pos) {
        this.logList.remove(pos);
        this.logs.remove(pos);
    }
}
