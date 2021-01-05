package com.infinityraider.agricraft;

import com.infinityraider.agricraft.api.v1.AgriApi;
import com.infinityraider.agricraft.config.Config;
import com.infinityraider.agricraft.init.AgriBlockRegistry;
import com.infinityraider.agricraft.init.AgriItemRegistry;
import com.infinityraider.agricraft.init.AgriTileRegistry;
import com.infinityraider.agricraft.network.MessageFertilizerApplied;
import com.infinityraider.agricraft.network.MessageCompareLight;
import com.infinityraider.agricraft.network.json.MessageSyncMutationJson;
import com.infinityraider.agricraft.network.json.MessageSyncPlantJson;
import com.infinityraider.agricraft.network.json.MessageSyncSoilJson;
import com.infinityraider.agricraft.proxy.ClientProxy;
import com.infinityraider.agricraft.proxy.IProxy;
import com.infinityraider.agricraft.proxy.ServerProxy;
import com.infinityraider.agricraft.reference.Reference;
import com.infinityraider.infinitylib.InfinityMod;
import com.infinityraider.infinitylib.network.INetworkWrapper;
import net.minecraftforge.fml.common.Mod;

@Mod(Reference.MOD_ID)
public class AgriCraft extends InfinityMod<IProxy, Config> {
    public static AgriCraft instance;

    @Override
    public String getModId() {
        return Reference.MOD_ID;
    }

    @Override
    protected void onModConstructed() {
        instance = this;
    }

    @Override
    protected IProxy createClientProxy() {
        return new ClientProxy();
    }

    @Override
    protected IProxy createServerProxy() {
        return new ServerProxy();
    }

    @Override
    public AgriBlockRegistry getModBlockRegistry() {
        return AgriBlockRegistry.getInstance();
    }

    @Override
    public AgriItemRegistry getModItemRegistry() {
        return AgriItemRegistry.getInstance();
    }

    @Override
    public AgriTileRegistry getModTileRegistry() {
        return AgriTileRegistry.getInstance();
    }

    @Override
    public void registerMessages(INetworkWrapper wrapper) {
        wrapper.registerMessage(MessageFertilizerApplied.class);
        wrapper.registerMessage(MessageSyncSoilJson.class);
        wrapper.registerMessage(MessageSyncPlantJson.class);
        wrapper.registerMessage(MessageSyncMutationJson.class);
        wrapper.registerMessage(MessageCompareLight.class);
    }

    @Override
    public void initializeAPI() {
        // this will load the AgriApi class
        getLogger().info("Intializing API for " + AgriApi.MOD_ID);
    }
}
