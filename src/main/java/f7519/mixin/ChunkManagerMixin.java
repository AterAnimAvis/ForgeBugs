package f7519.mixin;

import com.mojang.datafixers.util.Either;
import f7519.F7519Fix;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.util.concurrent.ITaskExecutor;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.*;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ChunkManager;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.concurrent.CompletableFuture;

@Mixin(ChunkManager.class)
public class ChunkManagerMixin {

    @Shadow @Final private ServerWorld world;

    @Shadow @Final private LongSet loadedPositions;

    @Shadow @Final private ITaskExecutor<ChunkTaskPriorityQueueSorter.FunctionEntry<Runnable>> field_219265_s;

    /**
     * Potential Fix for Forge#7519
     * @author AterAnimAvis
     */
    @Overwrite
    private CompletableFuture<Either<IChunk, ChunkHolder.IChunkLoadingError>> func_219200_b(ChunkHolder holder) {
        CompletableFuture<Either<IChunk, ChunkHolder.IChunkLoadingError>> future = holder.func_219301_a/*getFutureIfPresentUnchecked*/(ChunkStatus.FULL.getParent());
        return future.thenApplyAsync((either) -> {
            ChunkStatus status = ChunkHolder.getChunkStatusFromLevel(holder.getChunkLevel());
            if (!status.isAtLeast(ChunkStatus.FULL)) return ChunkHolder.MISSING_CHUNK;

            return either.mapLeft((iChunk) -> {
                ChunkPos chunkpos = holder.getPosition();

                Chunk chunk = iChunk instanceof ChunkPrimerWrapper ? ((ChunkPrimerWrapper) iChunk).getChunk() : null;
                if (chunk == null) {
                    chunk = new Chunk(world, (ChunkPrimer)iChunk);
                    holder.func_219294_a/*replaceProtoChunk*/(new ChunkPrimerWrapper(chunk));
                }

                chunk.setLocationType(() -> ChunkHolder.getLocationTypeFromLevel(holder.getChunkLevel()));
                chunk.postLoad();
                if (loadedPositions.add(chunkpos.asLong())) {
                    chunk.setLoaded(true);
                    F7519Fix.optionallyApplyFix((ChunkManager) (Object) this, holder, chunk, world);
                }

                return chunk;
            });
        }, (r) -> this.field_219265_s/*mainThreadMailbox*/.enqueue(ChunkTaskPriorityQueueSorter.func_219069_a/*message*/(r, holder.getPosition().asLong(), holder::getChunkLevel)));
    }

}
