package dev.hephaestus.treefax.mixin.world.gen.feature;

import dev.hephaestus.treefax.api.Tree;
import dev.hephaestus.treefax.impl.TreeChunk;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.TreeFeature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;

@Mixin(TreeFeature.class)
public class MixinTreeFeature {
    @Inject(method = "generate(Lnet/minecraft/world/gen/feature/util/FeatureContext;)Z", at = @At("RETURN"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void groupTrunks(FeatureContext<TreeFeatureConfig> context, CallbackInfoReturnable<Boolean> cir, StructureWorldAccess structureWorldAccess, Random random, BlockPos blockPos, TreeFeatureConfig treeFeatureConfig, Set<BlockPos> logsSet, Set<BlockPos> leavesSet, Set<BlockPos> decorationSet, BiConsumer<BlockPos, BlockState> biConsumer, BiConsumer<BlockPos, BlockState> biConsumer2, BiConsumer<BlockPos, BlockState> biConsumer3, boolean bl) {
        ((TreeChunk) structureWorldAccess.getChunk(blockPos)).getTracker().addTree(new Tree(structureWorldAccess, new ArrayList<>(logsSet)));
    }
}
