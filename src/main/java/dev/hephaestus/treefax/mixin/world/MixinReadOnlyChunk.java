package dev.hephaestus.treefax.mixin.world;

import dev.hephaestus.treefax.impl.TreeChunk;
import dev.hephaestus.treefax.impl.TreeTrackerImpl;
import net.minecraft.block.BlockState;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ReadOnlyChunk;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ReadOnlyChunk.class)
public class MixinReadOnlyChunk implements TreeChunk {
    private TreeTrackerImpl tracker;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initializeTracker(WorldChunk wrapped, CallbackInfo ci) {
        this.tracker = new TreeTrackerImpl((ReadOnlyChunk) (Object) this);
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
