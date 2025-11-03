/** UNUSED

package dev.foltz.predations.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.foltz.predations.config.ConfigHealth;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Text title) { super(title); }

    @Inject(method = "init", at = @At("TAIL"))
    private void predations$showConfigHealthPopup(CallbackInfo ci) {
        System.out.println("[Predations] TitleScreenMixin loaded!");
        if (!ConfigHealth.hasIssues()) return;
        if (ConfigHealth.popupAlreadyShown()) return;

        var list = ConfigHealth.brokenList();
        String names = String.join(", ", list);
        boolean plural = list.size() > 1;

        Text header = Text.literal("Predations config issue");
        Text body = Text.literal(
                names + " " + (plural ? "have" : "has") +
                        " malfunctioned and might not work properly in game.\nContinue?"
        );

        ConfirmScreen confirm = new ConfirmScreen(
                confirmed -> {
                    if (confirmed) {
                        ConfigHealth.markPopupShown();
                        MinecraftClient.getInstance().setScreen((TitleScreen)(Object)this);
                    } else {
                        MinecraftClient.getInstance().scheduleStop();
                    }
                },
                header,
                body
        );

        RenderSystem.recordRenderCall(() ->
                MinecraftClient.getInstance().setScreen(confirm)
        );
    }
}
**/