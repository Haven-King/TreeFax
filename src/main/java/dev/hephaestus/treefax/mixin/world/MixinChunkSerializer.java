package dev.hephaestus.treefax.mixin.world;

import dev.hephaestus.treefax.impl.TreeChunk;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.ReadOnlyChunk;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkSerializer.class)
public class MixinChunkSerializer {
    @Inject(method = "deserialize", at = @At("RETURN"))
    private static void deserialize(ServerWorld world, StructureManager structureManager, PointOfInterestStorage poiStorage, ChunkPos pos, NbtCompound tag, CallbackInfoReturnable<ProtoChunk> cir) {
        ProtoChunk protoChunk = cir.getReturnValue();
        Chunk chunk = protoChunk instanceof ReadOnlyChunk ? ((ReadOnlyChunk) protoChunk).getWrappedChunk() : protoChunk;
        NbtCompound levelData = tag.getCompound("Level");

        if (levelData.contains("TreeTracker", NbtElement.COMPOUND_TYPE)) {
            ((TreeChunk) chunk).getTracker().readFromNbt(levelData.getCompound("TreeTracker"));
        }
    }

    @Inject(method = "serialize", at = @At("RETURN"))
    private static void serialize(ServerWorld world, Chunk chunk, CallbackInfoReturnable<NbtCompound> cir) {
        NbtCompound levelData = cir.getReturnValue().getCompound("Level");
        NbtCompound treeTracker = new NbtCompound();

        ((TreeChunk) chunk).getTracker().writeToNbt(treeTracker);
        levelData.put("TreeTracker", treeTracker);
    }
}
