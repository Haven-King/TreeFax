package dev.hephaestus.treefax.mixin.world;

import dev.hephaestus.treefax.impl.TreeChunk;
import dev.hephaestus.treefax.impl.TreeTrackerImpl;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkTickScheduler;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.UpgradeData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ProtoChunk.class)
public class MixinProtoChunk implements TreeChunk {
    private TreeTrackerImpl tracker;

    @Inject(method = "<init>(Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/world/chunk/UpgradeData;[Lnet/minecraft/world/chunk/ChunkSection;Lnet/minecraft/world/ChunkTickScheduler;Lnet/minecraft/world/ChunkTickScheduler;Lnet/minecraft/world/HeightLimitView;)V", at = @At("TAIL"))
    private void initializeTracker(ChunkPos pos, UpgradeData upgradeData, ChunkSection[] chunkSections, ChunkTickScheduler<Block> blockTickScheduler, ChunkTickScheduler<Fluid> fluidTickScheduler, HeightLimitView world, CallbackInfo ci) {
        this.tracker = new TreeTrackerImpl((ProtoChunk) (Object) this);
    }

    @Override
    public TreeTrackerImpl getTracker() {
        return this.tracker;
    }

    @Inject(method = "setBlockState", at = @At("RETURN"))
    private void removeTree(BlockPos pos, BlockState state, boolean moved, CallbackInfoReturnable<BlockState> cir) {
        if (!state.isIn(BlockTags.LOGS)) {
            this.tracker.remove(pos);
        }
    }
}
