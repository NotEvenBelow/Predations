package dev.foltz.predations.client.gui;

import dev.foltz.predations.rirtualNetwork.ModPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class LifeRitualScreen extends Screen {
    private TextFieldWidget usernameField;

    public LifeRitualScreen() {
        super(Text.literal("Life Ritual"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.usernameField = new TextFieldWidget(this.textRenderer, centerX - 100, centerY - 20, 200, 20, Text.literal("Username"));
        this.usernameField.setMaxLength(16);
        this.usernameField.setFocused(true);
        this.addDrawableChild(this.usernameField);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Use"), button -> {
            String targetName = this.usernameField.getText();
            if (!targetName.isBlank()) {
                sendLifeRitualPacket(targetName);
                this.close();
            }
        }).dimensions(centerX - 105, centerY + 10, 100, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), button -> {
            this.close();
        }).dimensions(centerX + 5, centerY + 10, 100, 20).build());
    }

    private void sendLifeRitualPacket(String targetName) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(targetName);
        ClientPlayNetworking.send(ModPackets.LIFE_RITUAL_PACKET_ID, buf);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 40, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}