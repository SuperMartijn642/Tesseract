package com.supermartijn642.tesseract.screen;

import com.google.common.collect.Iterables;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.ScreenUtils;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

/**
 * Created 5/20/2021 by SuperMartijn642
 */
public class PlayerRenderer {

    private static final Field profileCache = ObfuscationReflectionHelper.findField(TileEntitySkull.class, "field_184298_j");
    private static final Field sessionService = ObfuscationReflectionHelper.findField(TileEntitySkull.class, "field_184299_k");

    // TODO: this should probably be cleared after a certain time
    private static final Map<UUID,GameProfile> PLAYER_PROFILE_MAP = new HashMap<>();
    private static final HashSet<UUID> FETCH_QUEUE = new HashSet<>();

    public static void renderPlayerHead(UUID player, int x, int y, int width, int height){
        ScreenUtils.bindTexture(getPlayerSkin(player));
        ScreenUtils.drawTexture(x, y, width, height, 1 / 8f, 1 / 8f, 1 / 8f, 1 / 8f);
    }

    public static String getPlayerUsername(UUID player){
        return PLAYER_PROFILE_MAP.containsKey(player) ? PLAYER_PROFILE_MAP.get(player).getName() : null;
    }

    public static ResourceLocation getPlayerSkin(UUID player){
        if(PLAYER_PROFILE_MAP.containsKey(player)){
            GameProfile profile = PLAYER_PROFILE_MAP.get(player);
            SkinManager skinManager = ClientUtils.getMinecraft().getSkinManager();
            Map<MinecraftProfileTexture.Type,MinecraftProfileTexture> map = skinManager.loadSkinFromCache(profile);
            if(map.containsKey(MinecraftProfileTexture.Type.SKIN))
                return skinManager.loadSkin(map.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
        }else if(!updateFetchQueue(player, null, null))
            fetchPlayerProfile(player);
        return DefaultPlayerSkin.getDefaultSkin(player);
    }

    private static void fetchPlayerProfile(final UUID player){
        updateFetchQueue(null, player, null);
        new Thread(() -> {
            boolean success = false;
            try{
                InputStream inputStream = new URL("https://api.mojang.com/user/profiles/" + player + "/names").openStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                // No tools to just read an array I guess
                StringBuilder builder = new StringBuilder();
                String s;
                while((s = reader.readLine()) != null)
                    builder.append(s);
                if(builder.length() > 0){
                    JsonArray array = ((JsonObject)Streams.parse(new JsonReader(new StringReader("{\"array\":" + builder + "}")))).getAsJsonArray("array");
                    String name = array.get(0).getAsJsonObject().get("name").getAsString();
                    GameProfile profile = updateGameProfile(new GameProfile(player, name));
                    if(profile != null){
                        PLAYER_PROFILE_MAP.put(player, profile);
                        success = true;
                    }
                }
            }catch(Exception ignore){}
            if(!success){
                try{
                    Thread.sleep(120000);
                }catch(Exception e2){
                    e2.printStackTrace();
                }
            }
            updateFetchQueue(null, null, player);
        }, "Tesseract - UUID to username").start();
    }

    private static synchronized boolean updateFetchQueue(UUID contains, UUID add, UUID remove){
        if(add != null)
            FETCH_QUEUE.add(add);
        if(remove != null)
            FETCH_QUEUE.remove(remove);
        return contains != null && FETCH_QUEUE.contains(contains);
    }

    @Nullable
    private static GameProfile updateGameProfile(@Nullable GameProfile input){
        if(input != null && input.getName() != null && !input.getName().isEmpty()){
            if(!input.isComplete() || !input.getProperties().containsKey("textures")){
                PlayerProfileCache profileCache = getProfileCache();
                MinecraftSessionService sessionService = getSessionService();
                if(profileCache != null && sessionService != null){
                    GameProfile gameprofile = profileCache.getGameProfileForUsername(input.getName());
                    if(gameprofile != null){
                        Property property = Iterables.getFirst(gameprofile.getProperties().get("textures"), null);
                        if(property == null)
                            gameprofile = sessionService.fillProfileProperties(gameprofile, true);
                        return gameprofile;
                    }
                }
            }
        }
        return null;
    }

    private static PlayerProfileCache getProfileCache(){
        try{
            return (PlayerProfileCache)profileCache.get(null);
        }catch(IllegalAccessException e){
            e.printStackTrace();
        }
        return null;
    }

    private static MinecraftSessionService getSessionService(){
        try{
            return (MinecraftSessionService)sessionService.get(null);
        }catch(IllegalAccessException e){
            e.printStackTrace();
        }
        return null;
    }
}
