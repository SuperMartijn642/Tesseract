package com.supermartijn642.tesseract.mixin;

import com.supermartijn642.tesseract.manager.TesseractSaveHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProgressListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created 21/02/2023 by SuperMartijn642
 */
@Mixin(ServerLevel.class)
public class ServerLevelMixin {

    @Inject(
        method = "save",
        at = @At("TAIL")
    )
    private void save(ProgressListener progressListener, boolean bl, boolean bl2, CallbackInfo ci){
        if(!bl2)
            //noinspection DataFlowIssue
            TesseractSaveHandler.save((ServerLevel)(Object)this);
    }
}
