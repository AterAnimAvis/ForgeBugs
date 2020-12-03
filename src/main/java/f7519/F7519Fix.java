package f7519;

import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ChunkManager;
import net.minecraft.world.server.ServerWorld;

import java.util.List;

public class F7519Fix {

    public static final boolean ENABLED = Boolean.parseBoolean(System.getProperty("forge.bug7519.fix", "true"));

    public static final boolean TRIGGER = Boolean.parseBoolean(System.getProperty("forge.bug7519.trigger", "false"));

    public static void optionallyApplyFix(ChunkManager manager, ChunkHolder holder, Chunk chunk, ServerWorld world) {
        if (ENABLED) holder.func_219276_a(ChunkStatus.FULL, manager).thenRun(() -> finishLoadingChunk(chunk, world));
        else finishLoadingChunk(chunk, world);
    }

    public static void finishLoadingChunk(Chunk chunk, ServerWorld world) {
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
