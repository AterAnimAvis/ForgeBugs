package f7519;

import java.util.List;

import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ChunkManager;
import net.minecraft.world.server.ServerWorld;

public class F7519Fix {

    public static final boolean ENABLED = Boolean.parseBoolean(System.getProperty("forge.bug7519.fix", "true"));

    public static final boolean TRIGGER = Boolean.parseBoolean(System.getProperty("forge.bug7519.trigger", "false"));

    public static void optionallyApplyFix(ChunkManager manager, ChunkHolder holder, Chunk chunk, ServerWorld world) {
        if (ENABLED) holder.func_219276_a/*getOrScheduleFuture*/(ChunkStatus.FULL, manager).thenRun(() -> finishLoadingChunk(chunk, world));
        else finishLoadingChunk(chunk, world);
    }

    public static void finishLoadingChunk(Chunk chunk, ServerWorld world) {
        world.addTileEntities(chunk.getTileEntityMap().values());
        List<Entity> list = null;
        ClassInheritanceMultiMap<Entity>[] aclassinheritancemultimap = chunk.getEntityLists();

        for (ClassInheritanceMultiMap<Entity> entities : aclassinheritancemultimap) {
            for (Entity entity : Lists.newArrayList(entities)) {
                if (!(entity instanceof PlayerEntity) && !world.addEntityIfNotDuplicate(entity)) {
                    if (list == null) {
                        list = Lists.newArrayList(entity);
                    } else {
                        list.add(entity);
                    }
                }
            }
        }

        if (list != null) {
            list.forEach(chunk::removeEntity);
        }

        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.world.ChunkEvent.Load(chunk));
    }

}
