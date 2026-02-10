package cn.kuzuanpa.kubicdivers.client.gui;

import cn.kuzuanpa.kubicdivers.KubicDiversMod;
import cn.kuzuanpa.kubicdivers.common.mission.PlayerManager;
import cn.kuzuanpa.kubicdivers.network.UpdateLoadoutPacket;
import cn.kuzuanpa.kubicdivers.stratagem.common.StratagemPlayerManager;
import cn.kuzuanpa.kubicdivers.stratagem.common.stratagem.IStratagem;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class StrategemSelectionScreen extends Screen {
    private enum ContentCategory { STRATAGEMS, MAIN_WEAPON, SUB_WEAPON, ARMOR, BOOSTER }

    private ContentCategory getCategoryForSlot(int type) {
        return switch (type) {
            case 0 -> ContentCategory.ARMOR;
            case 1 -> ContentCategory.MAIN_WEAPON;
            case 2 -> ContentCategory.SUB_WEAPON;
            case 3 -> ContentCategory.BOOSTER;
            default -> ContentCategory.STRATAGEMS;
        };
    }
    private static final int COLOR_YELLOW = 0xFFE800;
    private static final int COLOR_CYAN = 0x00FFFF;
    private static final int slotSize = 40;
    private static final int gap = 10;
    private static final int stratagems = 4;
    boolean hoveringRemoteItems = false, localPlayerReady = false;
    ItemStack hoveredEquip;
    IStratagem hoveredStratagem;
    int hoveredSlot = -1;

    private final Player localPlayer;
    private final List<IStratagem> availableStratagems;
    private final List<IStratagem> selectedStratagems = new ArrayList<>();
    private final List<ItemStack> selectedEquips = new ArrayList<>();

    private int activeSlot = 4;
    private double leftScrollAmount = 0;
    private double rightScrollAmount = 0;

    private int remotePanelX, remotePanelY, remotePanelW, remotePanelH, localPanelX, localPanelY, localPanelW, localPanelH, localPanelSlotH, localPanelSelectorY, localPanelSelectorH, localPanelAvailListW, localPanelDetailX, localPanelDetailW;

    public StrategemSelectionScreen() {
        super(Component.literal("Stratagem Selection"));
        this.localPlayer = Minecraft.getInstance().player;
        this.availableStratagems = StratagemPlayerManager.getUnlockedStratagem(localPlayer);
        for(int i=0; i<stratagems; i++) selectedStratagems.add(null);
        for(int i=0; i<4; i++) selectedEquips.add(null);
    }
    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        super.init();

        this.remotePanelW = 114;
        this.remotePanelH = this.height- gap*2;
        this.remotePanelX = this.width - remotePanelW - gap;
        this.remotePanelY = gap ;

        this.localPanelW = this.width - remotePanelW - gap*3;
        this.localPanelH = this.height - gap*2;
        this.localPanelX = gap;
        this.localPanelY = gap;

        localPanelSlotH = 100;
        localPanelSelectorY = localPanelY + localPanelSlotH + gap;
        localPanelSelectorH = localPanelH - localPanelSlotH - gap;

        localPanelDetailW = 80;
        localPanelDetailX = localPanelX + localPanelW - localPanelDetailW;
        localPanelAvailListW = localPanelW - localPanelX - localPanelDetailW - gap;
        int btnWidth = 100;
        this.addRenderableWidget(Button.builder(Component.literal("DEPLOY"),
                button -> {
            localPlayerReady = ! localPlayerReady;
            this.sendLoadoutToServer();
        }).bounds(this.width - btnWidth - 20, this.height - 40, btnWidth, 20).build());
    }

    @Override
    public void render(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gui);
        gui.fillGradient(0, 0, this.width, this.height, 0x11000000, 0x16000000);

        hoveredEquip = null;
        hoveredStratagem = null;
        hoveredSlot = -1;
        hoveringRemoteItems = false;
        renderRemotePanel(gui, mouseX, mouseY);
        renderLocalPanel(gui,mouseX, mouseY);
        super.render(gui, mouseX, mouseY, partialTick);
    }
    private void renderRemotePanel(GuiGraphics gui, int mx, int my) {
        gui.enableScissor(remotePanelX, remotePanelY, remotePanelX + remotePanelW, remotePanelY + remotePanelH);
        gui.pose().pushPose();

        gui.pose().translate(0, -leftScrollAmount, 0);
        List<UpdateLoadoutPacket> teammates = PlayerManager.getDisplayTeammates(localPlayer);
        int itemH = 80;
        int gap = 4;
        int slotSize = 24;

        int stratagemSlotSize = 96/stratagems;
        int stratagemSlotGap = 16/stratagems;
        int currentY = remotePanelY;

        for (UpdateLoadoutPacket mate : teammates) {
            //Don't render when out of screen
            if (currentY + itemH < remotePanelY + leftScrollAmount || currentY > remotePanelY + remotePanelH + leftScrollAmount) {
                currentY += itemH + gap;
                continue;
            }
            gui.fill(remotePanelX, currentY, remotePanelX + remotePanelW, currentY + itemH, 0x60333333);

            gui.drawString(font, mate.playerName(), remotePanelX + remotePanelW /2 - font.width(mate.playerName())/2, currentY + gap, 0xFFE800, false);

            for (int i = 0; i < 4; i++) {
                int x = remotePanelX + gap + i * (gap + slotSize);
                int y = currentY + gap + 10;

                ItemStack stack = i == 0 ? mate.armor(): i == 1 ?mate.primaryWeapon(): i == 2 ?mate.secondaryWeapon(): mate.enhance();
                drawEquipmentSlot(gui, x, y, slotSize, stack, -1, mx, my, true);
            }

            for (int i = 0; i < stratagems; i++) {
                int x = remotePanelX + gap + i * (stratagemSlotGap + stratagemSlotSize);
                int y = currentY + gap + 10 + slotSize + stratagemSlotGap ;

                if(i >= mate.stratagems().size()){
                    drawStratagemSlot(gui, x, y, stratagemSlotSize, null, -1, mx, my, true);
                    continue;
                }
                IStratagem stratagem = mate.stratagems().get(i);
                drawStratagemSlot(gui, x, y, stratagemSlotSize, stratagem, -1, mx, my, true);
            }

            String ready = "READY";
            if(mate.isReady())gui.drawString(font, ready, remotePanelX + remotePanelW /2 - font.width(ready)/2, currentY + itemH - 7 -gap, 0xFFE800, false);

            currentY += itemH + gap;
        }
        gui.pose().popPose();

        drawScrollBar(gui, remotePanelX + remotePanelW - 2, remotePanelY, remotePanelH, teammates.size() * (itemH + gap), (int)leftScrollAmount);

        gui.disableScissor();
    }

    private void renderLocalPanel(GuiGraphics gui, int mx, int my) {
        renderSelectSlots(gui, mx, my);
        renderAvailObjectLists(gui, mx, my);
        renderDetails(gui, mx, my);
    }
    private void renderSelectSlots(GuiGraphics gui, int mx, int my) {
        drawEquipmentSlot(gui, localPanelX                     , localPanelY, slotSize, selectedEquips.get(0), 0, mx, my, false);
        drawEquipmentSlot(gui, localPanelX +  slotSize + gap   , localPanelY, slotSize, selectedEquips.get(1), 1, mx, my, false);
        drawEquipmentSlot(gui, localPanelX + (slotSize + gap)*2, localPanelY, slotSize, selectedEquips.get(2), 2, mx, my, false);
        drawEquipmentSlot(gui, localPanelX + (slotSize + gap)*3, localPanelY, slotSize, selectedEquips.get(3), 3, mx, my, false);

        int stratagemSlotY = localPanelY + slotSize + gap;
        int stratagemSlotSize = 160/stratagems;
        int stratagemSlotGap = 40/stratagems;
        for (int i = 0; i < stratagems; i++) {
            IStratagem current = selectedStratagems.get(i);
            drawStratagemSlot(gui, localPanelX + (stratagemSlotSize + stratagemSlotGap) * i, stratagemSlotY, stratagemSlotSize, current, 4+i, mx, my, false);
        }
    }

    private void drawEquipmentSlot(GuiGraphics gui, int x, int y, int size, ItemStack stack, int id, int mx, int my, boolean isRemote) {
        boolean isHovered = (mx >= x && mx <= x + size && my >= y && my <= y + size);

        drawSlot(gui, x, y, size, id, mx, my, isHovered);
        if(stack == null) return;

        gui.pose().pushPose();
        gui.pose().translate(x + size/5F, y + size/5F, 0);
        gui.pose().scale(size/28F, size/28F, size/28F);
        gui.renderFakeItem(stack, 0, 0);
        gui.renderItemDecorations(font, stack, 0, 0);
        gui.pose().popPose();

        if(isHovered){
            hoveredEquip = stack;
            hoveringRemoteItems = isRemote;
        }
    }
    private void drawStratagemSlot(GuiGraphics gui, int x, int y, int size, IStratagem stratagem, int id, int mx, int my, boolean isRemote) {
        boolean isHovered = (mx >= x && mx <= x + size && my >= y && my <= y + size);
        drawSlot(gui, x, y, size, id, mx, my, isHovered);

        if (stratagem == null || stratagem.getIcon() == null) return;
        int iconSize = (int) (size * 0.8F);
        RenderSystem.enableBlend();
        gui.pose().pushPose();
        gui.pose().translate(x + (size - iconSize)/ 2F, y + (size - iconSize)/ 2F, 0);
        gui.blit(stratagem.getIcon(), 0, 0, 0, 0, iconSize,iconSize,iconSize,iconSize);
        gui.pose().popPose();
        if(isHovered){
            hoveredStratagem = stratagem;
            hoveringRemoteItems = isRemote;
        }
    }
    private void drawSlot(GuiGraphics gui, int x, int y, int size, int id, int mx, int my, boolean isHovered){
        boolean isActive = (this.activeSlot == id);

        if(isHovered)hoveredSlot = id;
        int borderColor = isActive ? COLOR_YELLOW : (isHovered ? COLOR_CYAN : 0xFF404040);
        gui.fill(x, y, x + size, y + size, 0x80000000);
        gui.renderOutline(x, y, size, size, borderColor);
    }
    private void renderAvailObjectLists(GuiGraphics gui, int mx, int my) {

        gui.enableScissor(localPanelX, localPanelSelectorY, localPanelX + localPanelAvailListW, localPanelSelectorY + localPanelSelectorH);
        gui.fillGradient(localPanelX, localPanelSelectorY, localPanelX + localPanelAvailListW, localPanelSelectorY + localPanelSelectorH, 0x22000000, 0x22000000);


        gui.pose().pushPose();
        gui.pose().translate(0, -rightScrollAmount, 0);

        int iconSize = 32;
        int gap = 4;
        int cols = Math.max(1, (int)Math.floor(localPanelAvailListW * 1F / (iconSize + gap)));

        ContentCategory currentCategory = getCategoryForSlot(this.activeSlot);
        if (currentCategory == ContentCategory.STRATAGEMS) {
            renderAvailStratagemLists(gui, mx, my, iconSize, gap, cols);
        } else {
            renderAvailEquipLists(gui,mx,my,iconSize, gap,cols, currentCategory);
        }

        gui.pose().popPose();
        gui.disableScissor();

        drawScrollBar(gui, localPanelX + localPanelAvailListW, localPanelSelectorY, localPanelSelectorH - gap, availableStratagems.size() / cols * iconSize, (int)rightScrollAmount);
    }
    private void renderAvailStratagemLists(GuiGraphics gui, int mx, int my, int iconSize, int gap, int cols){
        int idx = 0;
        for (IStratagem s : availableStratagems) {
            int col = idx % cols;
            int row = idx / cols;

            int drawX = localPanelX + col * (iconSize + gap);
            int drawY = localPanelSelectorY + row * (iconSize + gap);

            if (drawY + iconSize < localPanelSelectorY + rightScrollAmount || drawY > localPanelSelectorY + localPanelSelectorH + rightScrollAmount) {
                idx++;
                continue;
            }


            if (s.getIcon() != null) {
                RenderSystem.enableBlend();
                gui.blit(s.getIcon(), drawX, drawY, 0, 0, iconSize, iconSize, iconSize, iconSize);
            }

            boolean hovered = mx >= drawX && mx <= drawX + iconSize &&
                    my >= (drawY - rightScrollAmount) && my <= (drawY + iconSize - rightScrollAmount) &&
                    my >= localPanelSelectorY && my <= localPanelSelectorY + localPanelSelectorH;

            if (hovered) {
                this.hoveredStratagem = s;
                gui.renderOutline(drawX, drawY, iconSize, iconSize, 0xFF00FFFF);
            }
            idx++;
        }
    }
    private void renderAvailEquipLists(GuiGraphics gui, int mx, int my, int iconSize, int gap, int cols, ContentCategory category){
        List<ItemStack> equipment = getAvailableEquipment(category);
        int idx = 0;
        for (ItemStack stack : equipment) {
            int col = idx % cols;
            int row = idx / cols;
            int drawX = localPanelX + col * (iconSize + gap);
            int drawY = localPanelSelectorY + row * (iconSize + gap);

            if (drawY + iconSize < localPanelSelectorY + rightScrollAmount || drawY > localPanelSelectorY + localPanelSelectorH + rightScrollAmount) {
                idx++;
                continue;
            }

            boolean hovered = mx >= drawX && mx <= drawX + iconSize &&
                    my >= (drawY - rightScrollAmount) && my <= (drawY + iconSize - rightScrollAmount) &&
                    my >= localPanelSelectorY && my <= localPanelSelectorY + localPanelSelectorH;

            gui.fill(drawX, drawY, drawX + iconSize, drawY + iconSize, hovered ? 0x80FFFFFF : 0x40000000);

            gui.renderFakeItem(stack, drawX + 8, drawY + 8);

            gui.renderItemDecorations(font, stack, drawX + 8, drawY + 8);

            if (hovered) {
                this.hoveredEquip = stack;
                gui.renderOutline(drawX, drawY, iconSize, iconSize, 0xFF00FFFF);
            }

            idx++;
        }
    }

    private void renderDetails(GuiGraphics gui, int mx, int my) {
        IStratagem displayStratagem = this.hoveredStratagem != null ? this.hoveredStratagem : null;
        ItemStack displayStack = this.hoveredEquip != null ? this.hoveredEquip : ItemStack.EMPTY;

        if (!displayStack.isEmpty()) {
            gui.pose().pushPose();
            gui.pose().translate(localPanelDetailX, localPanelSelectorY, 100);
            gui.pose().scale(2.0f, 2.0f, 2.0f);
            gui.renderFakeItem(displayStack, 0, 0);
            gui.pose().popPose();

            gui.drawString(font, displayStack.getHoverName().getString().toUpperCase(), localPanelDetailX, localPanelSelectorY + 10, 0xFFE800, false);

            gui.drawString(font,  "DAMAGE: 55", localPanelDetailX, localPanelSelectorY + 20, 0xFFE800, false);
            gui.drawString(font,  "CAPACITY: 55", localPanelDetailX, localPanelSelectorY + 20, 0xFFE800, false);
        } else if (displayStratagem != null) {
            if (displayStratagem.getIcon() != null) {
                int iconSize = 32;
                RenderSystem.enableBlend();
                gui.blit(displayStratagem.getIcon(), localPanelDetailX, localPanelSelectorY, 0, 0, iconSize, iconSize, iconSize, iconSize);
            }
            gui.drawString(font, displayStratagem.getName().toUpperCase(), localPanelDetailX, localPanelSelectorY +10, 0xFFE800, false);
            gui.drawString(font, "CALL-IN TIME: 0s", localPanelDetailX, localPanelSelectorY +20, 0xAAAAAA, false);
            gui.drawString(font, "USES: UNLIMITED", localPanelDetailX, localPanelSelectorY +30, 0xAAAAAA, false);
            gui.drawString(font, "COOLDOWN: " + displayStratagem.getCooldown() + "s", localPanelDetailX, localPanelSelectorY +40, 0xAAAAAA, false);
        }
    }

    private List<ItemStack> getAvailableEquipment(ContentCategory category) {
        List<ItemStack> items = new ArrayList<>();
        if (category == ContentCategory.MAIN_WEAPON) {
            items.add(new ItemStack(Items.GOLDEN_SWORD));
            items.add(new ItemStack(Items.DAMAGED_ANVIL));
        }
        if (category == ContentCategory.SUB_WEAPON) {
            items.add(new ItemStack(Items.SADDLE));
            items.add(new ItemStack(Items.GLOW_ITEM_FRAME));
        } else if (category == ContentCategory.ARMOR) {
            items.add(new ItemStack(Items.DIAMOND_CHESTPLATE));
        }
        return items;
    }
    private void drawScrollBar(GuiGraphics gui, int x, int y, int height, int contentHeight, int scroll) {
        if (contentHeight <= height) return;
        int barHeight = (int) ((float) height / contentHeight * height);
        int barY = (int) ((float) scroll / (contentHeight - height) * (height - barHeight));
        gui.fill(x, y + barY, x + 2, y + barY + barHeight, 0xFF888888);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (mouseX >= localPanelX + localPanelW) {
            int contentHeight = PlayerManager.getDisplayTeammates(localPlayer).size() * 85;
            double maxScroll = Math.max(0, contentHeight - remotePanelH);

            this.leftScrollAmount = Mth.clamp(this.leftScrollAmount - delta * 20, 0, maxScroll);
            return true;
        }

        int iconSize = 36;
        int cols = localPanelAvailListW / iconSize;
        int rows = (int) Math.ceil((double) availableStratagems.size() / cols);
        int contentHeight = rows * iconSize;
        double maxScroll = Math.max(0, contentHeight - localPanelSelectorH);

        this.rightScrollAmount = Mth.clamp(this.rightScrollAmount - delta * 20, 0, maxScroll);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if(hoveredSlot > -1) onSlotClick();
        if(hoveringRemoteItems)return false;
        if (this.hoveredStratagem != null && this.activeSlot >= 4) onStratagemClick();
        if (this.hoveredEquip != null && this.activeSlot < 4) onEquipClick();
        return true;
    }

    public void onSlotClick(){
        this.activeSlot = hoveredSlot;
    }
    boolean allowDuplicateStratagem = false;
    public void onStratagemClick(){
        if(allowDuplicateStratagem || !this.selectedStratagems.contains(this.hoveredStratagem)){
            this.selectedStratagems.set(this.activeSlot - 4, this.hoveredStratagem);
            this.sendLoadoutToServer();
        }
    }
    public void onEquipClick(){
        this.selectedEquips.set(this.activeSlot, this.hoveredEquip);
        this.sendLoadoutToServer();
    }

    @Override
    public void onClose() {
        for(int i=0; i<stratagems; i++) selectedStratagems.add(null);
        for(int i=0; i<4; i++) selectedEquips.add(null);
        localPlayerReady = false;
        sendLoadoutToServer();
        super.onClose();
    }

    private void sendLoadoutToServer() {
        KubicDiversMod.NETWORK_CHANNEL.sendToServer(new UpdateLoadoutPacket(localPlayer.getScoreboardName(), selectedEquips.get(0), selectedEquips.get(1), selectedEquips.get(2), selectedEquips.get(3), this.selectedStratagems, localPlayerReady));
    }
}