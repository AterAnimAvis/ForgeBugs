package f7519;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.IntNBT;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

@Mod("create-entity-on-load")
public class F7519
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Random random = new Random();

    public F7519() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (!F7519Fix.TRIGGER) return;
        if (event.getWorld().isRemote) return;

        Entity entity = event.getEntity();
        if (entity.hasNoGravity()) return;
        if (!entity.getClass().equals(ItemEntity.class)) return;

        AtomicBoolean finished = new AtomicBoolean(false);
        Thread watchdog = new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {}
            if (!finished.get()) {
                LOGGER.error("...[Forge7519] Deadlocked for EntityJoinWorldEvent at " + new ChunkPos(entity.getPosition()));
            }
        });
        watchdog.setDaemon(true);
        watchdog.start();

        ItemStack stack = (((ItemEntity)entity).getItem()).copy();
        stack.setTagInfo("RandomInt", IntNBT.valueOf(random.nextInt()));

        ItemEntity item = new ItemEntity(event.getWorld(), entity.getPosX(), entity.getPosY(), entity.getPosZ(), stack);
        item.setMotion(entity.getMotion());
        item.setNoGravity(true);
        item.setPickupDelay(20);
        event.getWorld().addEntity(item);
        finished.set(true);
    }

}
