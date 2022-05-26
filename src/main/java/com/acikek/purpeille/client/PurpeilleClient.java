package com.acikek.purpeille.client;

import com.acikek.purpeille.Purpeille;
import com.acikek.purpeille.block.ancient.guardian.AncientGuardian;
import com.acikek.purpeille.client.networking.AncientGuardianActivationListener;
import com.acikek.purpeille.client.particle.AncientGuardianParticle;
import com.acikek.purpeille.client.particle.ModParticleTypes;
import com.acikek.purpeille.client.render.AncientGuardianRenderer;
import com.acikek.purpeille.warpath.component.Aspect;
import com.acikek.purpeille.warpath.component.Component;
import com.acikek.purpeille.warpath.component.Revelation;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class PurpeilleClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        FabricLoader.getInstance()
                .getModContainer(Purpeille.ID)
                .ifPresent(mod -> {
                    registerPack(mod, "old", ResourcePackActivationType.NORMAL);
                    registerPack(mod, "theinar", ResourcePackActivationType.ALWAYS_ENABLED);
                });
        AncientGuardianRenderer.register();
        ModParticleTypes.register();
        AncientGuardianParticle.register();
        //ClientPlayNetworking.registerGlobalReceiver(Purpeille.id("ancient_guardian_core_removed"))
        ClientPlayNetworking.registerGlobalReceiver(AncientGuardian.ANCIENT_GUARDIAN_ACTIVATED, new AncientGuardianActivationListener());
        handleReload("revelations", Component.REVELATIONS, Revelation::read);
        handleReload("aspects", Component.ASPECTS, Aspect::read);
    }

    public static void registerPack(ModContainer mod, String key, ResourcePackActivationType type) {
        ResourceManagerHelper.registerBuiltinResourcePack(Purpeille.id(key), mod, type);
    }

    public static <T extends Component> void handleReload(String key, Map<Identifier, T> registry, Function<PacketByteBuf, T> read) {
        ClientPlayNetworking.registerGlobalReceiver(Purpeille.id(key), (client, handler, buf, responseSender) -> {
            if (client.isInSingleplayer()) {
                return;
            }
            if (buf.readBoolean()) {
                registry.clear();
            }
            registry.put(buf.readIdentifier(), read.apply(buf));
        });
    }
}
