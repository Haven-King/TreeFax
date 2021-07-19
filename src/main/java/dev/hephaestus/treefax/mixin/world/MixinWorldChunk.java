package dev.hephaestus.treefax.mixin.world;

import dev.hephaestus.treefax.impl.TreeChunk;
import dev.hephaestus.treefax.impl.TreeTrackerImpl;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.TickScheduler;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(WorldChunk.class)
public class MixinWorldChunk implements TreeChunk {
    private TreeTrackerImpl tracker;

    @Inject(method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/world/biome/source/BiomeArray;Lnet/minecraft/world/chunk/UpgradeData;Lnet/minecraft/world/TickScheduler;Lnet/minecraft/world/TickScheduler;J[Lnet/minecraft/world/chunk/ChunkSection;Ljava/util/function/Consumer;)V", at = @At("TAIL"))
    private void initializeTracker(World world, ChunkPos pos, BiomeArray biomes, UpgradeData upgradeData, TickScheduler<Block> blockTickScheduler, TickScheduler<Fluid> fluidTickScheduler, long inhabitedTime, ChunkSection[] sections, Consumer<WorldChunk> loadToWorldConsumer, CallbackInfo ci) {
        this.tracker = new TreeTrackerImpl((WorldChunk) (Object) this);
    }

    @Inject(method = "<init>(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/ProtoChunk;Ljava/util/function/Consumer;)V", at = @At("TAIL"))
    private void copyTrackerFromProtoChunk(ServerWorld serverWorld, ProtoChunk protoChunk, Consumer<WorldChunk> consumer, CallbackInfo ci) {
        this.tracker = ((TreeChunk) protoChunk).getTracker();
    }

    @Override
    public TreeTrackerImpl getTracker() {
        return this.tracker;
    }
}
