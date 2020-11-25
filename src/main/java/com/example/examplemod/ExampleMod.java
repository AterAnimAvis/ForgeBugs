package com.example.examplemod;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("create-entity-on-load")
public class ExampleMod
{
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public ExampleMod() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        if (entity.hasNoGravity()) return;
        if (!entity.getClass().equals(ItemEntity.class)) return;

        LOGGER.warn("<<< Adding New Entity");
        ItemStack stack = ((ItemEntity)entity).getItem();
        ItemEntity item = new ItemEntity(event.getWorld(), entity.getPosX(), entity.getPosY(), entity.getPosZ(), stack);
        item.setNoGravity(true);
        item.setPickupDelay(20);
        event.getWorld().addEntity(item);
        LOGGER.warn("<<< Finished Adding New Entity");
    }

}
