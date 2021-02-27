package f7519;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.IntNBT;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.FMLNetworkConstants;

@Mod("f7519")
public class F7519
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Random random = new Random();

    public F7519() {
        LOGGER.info("[Forge7519] Running Forge7519 (Trigger={},Fix={})", F7519Fix.TRIGGER, F7519Fix.ENABLED);

        MinecraftForge.EVENT_BUS.register(this);

        Supplier<String> SERVER_ONLY = () -> FMLNetworkConstants.IGNORESERVERONLY;
        BiPredicate<String, Boolean> ALWAYS = (s, b) -> true;
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, ()->Pair.of(SERVER_ONLY, ALWAYS));
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
                LOGGER.error("[Forge7519] Deadlocked for EntityJoinWorldEvent at " + new ChunkPos(entity.getPosition()));
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
