package io.hynix.units.impl.display;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;


import io.hynix.HynixMain;
import io.hynix.events.impl.EventPreRender3D;
import io.hynix.events.impl.EventRender2D;
import io.hynix.events.impl.EventUpdate;
import io.hynix.managers.theme.Theme;
import io.hynix.managers.friend.FriendManager;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.impl.combat.BotRemover;
import io.hynix.units.settings.impl.BooleanSetting;
import io.hynix.units.settings.impl.ColorSetting;
import io.hynix.units.settings.impl.ModeListSetting;
import io.hynix.units.settings.impl.ModeSetting;
import io.hynix.utils.client.ClientUtils;
import io.hynix.utils.johon0.animations.AnimationUtils;
import io.hynix.utils.johon0.animations.Direction;
import io.hynix.utils.johon0.animations.impl.DecelerateAnimation;
import io.hynix.utils.johon0.math.MathUtils;
import io.hynix.utils.johon0.math.Vector4i;
import io.hynix.utils.johon0.render.color.ColorUtils;
import io.hynix.utils.johon0.render.font.Fonts;
import io.hynix.utils.johon0.render.render2d.ProjectionUtils;
import io.hynix.utils.johon0.render.render2d.RectUtils;
import io.hynix.utils.johon0.render.render2d.RenderUtils;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.PotionSpriteUploader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectUtils;
import net.minecraft.scoreboard.Score;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.*;
import org.lwjgl.opengl.GL11;

import java.util.*;

import static com.mojang.blaze3d.platform.GlStateManager.GL_QUADS;
import static com.mojang.blaze3d.systems.RenderSystem.depthMask;
import static net.minecraft.client.renderer.WorldRenderer.frustum;
import static net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_COLOR_TEX;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslatef;

@UnitRegister(name = "ESP", category = Category.Display, desc = "ESP на Entity")
public class ESP extends Unit {
    public ModeListSetting remove = new ModeListSetting("Убрать", new BooleanSetting("Боксы", false), new BooleanSetting("Полоску хп", false), new BooleanSetting("Зачарования", false), new BooleanSetting("Список эффектов", false));
    public ModeListSetting targets = new ModeListSetting("Отображать",
            new BooleanSetting("Себя", true),
            new BooleanSetting("Игроки", true),
            new BooleanSetting("Предметы", false)
    );

    public BooleanSetting targetEsp = new BooleanSetting("TargetESP", true);
    public BooleanSetting konfetkaabayudna = new BooleanSetting("Конфетка абаюдна", true);

    private final AnimationUtils alpha = new DecelerateAnimation(600, 255);
    private LivingEntity currentTarget;
    private long lastTime = System.currentTimeMillis();

    float healthAnimation = 0.0f;

    public ESP() {
        addSettings(targets, remove,konfetkaabayudna, targetEsp);
    }

    float length;

    private final HashMap<Entity, Vector4f> positions = new HashMap<>();

    public ColorSetting color = new ColorSetting("Color", -1);

    @Subscribe
    private void onUpdate(EventUpdate e) {
        boolean bl = (HynixMain.getInstance().getModuleManager().getAttackAura().isEnabled());
        if (HynixMain.getInstance().getModuleManager().getAttackAura().target != null) {
            currentTarget = HynixMain.getInstance().getModuleManager().getAttackAura().target;
        }

        this.alpha.setDirection(bl && HynixMain.getInstance().getModuleManager().getAttackAura().target != null ? Direction.FORWARDS : Direction.BACKWARDS);
    }

    @Subscribe
    public void onRender(EventPreRender3D e) {
        if (this.alpha.finished(Direction.BACKWARDS)) {
            return;
        }
    }

    @Subscribe
    public void onDisplay(EventRender2D e) {
        if (mc.world == null || e.getType() != EventRender2D.Type.PRE) {
            return;
        }

        positions.clear();

        Vector4i colors = new Vector4i(Theme.rectColor, Theme.rectColor, Theme.mainRectColor, Theme.mainRectColor);
        Vector4i friendColors = new Vector4i(ColorUtils.getColor1(ColorUtils.rgb(144, 238, 144), ColorUtils.rgb(0, 139, 0), 0, 1), ColorUtils.getColor1(ColorUtils.rgb(144, 238, 144), ColorUtils.rgb(0, 139, 0), 90, 1), ColorUtils.getColor1(ColorUtils.rgb(144, 238, 144), ColorUtils.rgb(0, 139, 0), 180, 1), ColorUtils.getColor1(ColorUtils.rgb(144, 238, 144), ColorUtils.rgb(0, 139, 0), 270, 1));

        for (Entity entity : mc.world.getAllEntities()) {
            if (!isValid(entity)) continue;
            if (!(entity instanceof PlayerEntity && entity != mc.player && targets.is("Игроки").getValue()
                    || entity instanceof ItemEntity && targets.is("Предметы").getValue()
                    || entity == mc.player && targets.is("Себя").getValue() && !(mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON) && !HynixMain.getInstance().getModuleManager().getBabyBoy().isEnabled()
            )) continue;

            double x = MathUtils.interpolate(entity.getPosX(), entity.lastTickPosX, e.getPartialTicks());
            double y = MathUtils.interpolate(entity.getPosY(), entity.lastTickPosY, e.getPartialTicks());
            double z = MathUtils.interpolate(entity.getPosZ(), entity.lastTickPosZ, e.getPartialTicks());

            Vector3d size = new Vector3d(entity.getBoundingBox().maxX - entity.getBoundingBox().minX, entity.getBoundingBox().maxY - entity.getBoundingBox().minY, entity.getBoundingBox().maxZ - entity.getBoundingBox().minZ);

            AxisAlignedBB aabb = new AxisAlignedBB(x - size.x / 2f, y, z - size.z / 2f, x + size.x / 2f, y + size.y, z + size.z / 2f);

            Vector4f position = null;
            for (int i = 0; i < 8; i++) {
                Vector2f vector = ProjectionUtils.project(i % 2 == 0 ? aabb.minX : aabb.maxX, (i / 2) % 2 == 0 ? aabb.minY : aabb.maxY, (i / 4) % 2 == 0 ? aabb.minZ : aabb.maxZ);

                if (position == null) {
                    position = new Vector4f(vector.x, vector.y, 1, 1.0f);
                } else {
                    position.x = Math.min(vector.x, position.x);
                    position.y = Math.min(vector.y, position.y);
                    position.z = Math.max(vector.x, position.z);
                    position.w = Math.max(vector.y, position.w);
                }
            }
            if (konfetkaabayudna.getValue()) {
                float height = (position.w - position.y),
                        width = (position.z - position.x);
                RenderUtils.drawImage(new ResourceLocation("hynix/images/abaydna.png"),position.x, position.y, width,height, -1);

            }
            positions.put(entity, position);
        }


        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(7425);

        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        for (Map.Entry<Entity, Vector4f> entry : positions.entrySet()) {
            Vector4f position = entry.getValue();
            if (entry.getKey() instanceof LivingEntity entity) {
                if (!remove.is("Боксы").getValue()) {
                    RenderUtils.drawBox(position.x - 0.5f, position.y - 0.5f, position.z + 0.5f, position.w + 0.5f, 2, ColorUtils.rgba(0, 0, 0, 128));
                    RenderUtils.drawBox(position.x, position.y, position.z, position.w, 1, FriendManager.isFriend(entity.getName().getString()) ? friendColors : colors);
                }

                float hpOffset = 3f;
                float out = 0.5f;
                if (!remove.is("Полоску хп").getValue()) {
                    String header = mc.ingameGUI.getTabList().header == null ? " " : mc.ingameGUI.getTabList().header.getString().toLowerCase();
                    Score score = mc.world.getScoreboard().getOrCreateScore(entity.getScoreboardName(), mc.world.getScoreboard().getObjectiveInDisplaySlot(2));

                    float hp = entity.getHealth();
                    float maxHp = entity.getMaxHealth();

                    if (mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP.contains("funtime") && (header.contains("анархия") || header.contains("гриферский"))) {
                        hp = score.getScorePoints();
                        maxHp = 20;
                    }

                    RenderUtils.drawRectBuilding(position.x - hpOffset - out, position.y - out, position.x - hpOffset + 1 + out, position.w + out, ColorUtils.rgba(0, 0, 0, 128));
                    RenderUtils.drawRectBuilding(position.x - hpOffset, position.y, position.x - hpOffset + 1, position.w, ColorUtils.rgba(0, 0, 0, 128));
                    RenderUtils.drawMCVerticalBuilding(position.x - hpOffset, position.y + (position.w - position.y) * (1 - MathHelper.clamp(hp / maxHp, 0, 1)), position.x - hpOffset + 1, position.w, FriendManager.isFriend(entity.getName().getString()) ? friendColors.w : colors.w, FriendManager.isFriend(entity.getName().getString()) ? friendColors.x : colors.x);
                }
            }
        }
        Tessellator.getInstance().draw();
        RenderSystem.shadeModel(7424);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();

        for (Map.Entry<Entity, Vector4f> entry : positions.entrySet()) {
            Entity entity = entry.getKey();

            if (entity instanceof LivingEntity living) {
                Score score = mc.world.getScoreboard().getOrCreateScore(living.getScoreboardName(), mc.world.getScoreboard().getObjectiveInDisplaySlot(2));
                float hp = living.getHealth();
                float maxHp = living.getMaxHealth();

                String header = mc.ingameGUI.getTabList().header == null ? " " : mc.ingameGUI.getTabList().header.getString().toLowerCase();


                if (mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP.contains("funtime") && (header.contains("анархия") || header.contains("гриферский"))) {
                    hp = score.getScorePoints();
                    maxHp = 20;
                }

                Vector4f position = entry.getValue();
                float width = position.z - position.x;

                GL11.glPushMatrix();

                String friendPrefix = FriendManager.isFriend(entity.getName().getString()) ? TextFormatting.GREEN + "[F] " : "";
                String creativePrefix = "";
                if (entity instanceof PlayerEntity && ((PlayerEntity) entity).isCreative()) {
                    creativePrefix = TextFormatting.GRAY + " [" + TextFormatting.RED + "GM" + TextFormatting.GRAY + "]";
                } else {
                    if (mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP.contains("funtime") && (header.contains("анархия") || header.contains("гриферский"))) {
                        creativePrefix = TextFormatting.GRAY + " [" + TextFormatting.RED + (int) hp + TextFormatting.GRAY + "]";
                    } else {
                        creativePrefix = TextFormatting.GRAY + " [" + TextFormatting.RED + ((int) hp + (int) living.getAbsorptionAmount()) + TextFormatting.GRAY + "]";
                    }
                }



                healthAnimation = MathUtils.fast(healthAnimation, MathHelper.clamp(hp / maxHp, 0, 1), 10);
                PlayerEntity player = (PlayerEntity)entity;
                ItemStack stack;
                String nameS;
                String itemName;

                stack = player.getHeldItemOffhand();
                nameS = "";
                itemName = stack.getDisplayName().getString();
                stack.getDisplayName().getString();
                if (stack.getItem() == Items.PLAYER_HEAD || stack.getItem() == Items.TOTEM_OF_UNDYING) {
                    CompoundNBT tag = stack.getTag();
                    String gata;
                    CompoundNBT display;
                    ListNBT lore;
                    String firstLore;
                    int levelIndex;
                    if (tag != null && tag.contains("display", 10)) {
                        display = tag.getCompound("display");
                        if (display.contains("Lore", 9)) {
                            lore = display.getList("Lore", 8);
                            if (!lore.isEmpty()) {
                                firstLore = lore.getString(0);
                                levelIndex = firstLore.indexOf("Уровень");
                                if (levelIndex != -1) {
                                    gata = firstLore.substring(levelIndex + "Уровень".length()).trim();
                                    if (gata.contains("1/3")) {
                                        nameS = " 1/3]";
                                    } else if (gata.contains("2/3")) {
                                        nameS = " 2/3]";
                                    } else if (gata.contains("MAX")) {
                                        nameS = " MAX]";
                                    } else {
                                        nameS = "";
                                    }
                                }
                            }
                        }
                    }

                    if (itemName.contains("Сфера")) {
                        if (itemName.contains("Афина")) itemName =  TextFormatting.GRAY + " [" + TextFormatting.RED + "Сфера Афина" + TextFormatting.RESET;
                        else if (itemName.contains("Панекея")) itemName =  TextFormatting.GRAY + " [" + TextFormatting.RED + "Сфера Панакея" + TextFormatting.RESET;
                        else if (itemName.contains("Магмы")) itemName =  TextFormatting.GRAY + " [" + TextFormatting.RED + "Сфера Магмы" + TextFormatting.RESET;
                        else if (itemName.contains("Теургия")) itemName =  TextFormatting.GRAY + " [" + TextFormatting.RED + "Сфера Теургия" + TextFormatting.RESET;
                        else if (itemName.contains("Иасо")) itemName =  TextFormatting.GRAY + " [" + TextFormatting.RED + "Сфера Иасо" + TextFormatting.RESET;
                        else if (itemName.contains("Скифа")) itemName =  TextFormatting.GRAY + " [" + TextFormatting.RED + "Сфера Скифа" + TextFormatting.RESET;
                        else if (itemName.contains("Абанты")) itemName =  TextFormatting.GRAY + " [" + TextFormatting.RED + "Сфера Абанты" + TextFormatting.RESET;
                        else if (itemName.contains("Филона")) itemName =  TextFormatting.GRAY + " [" + TextFormatting.RED + "Сфера Филона" + TextFormatting.RESET;
                        else if (itemName.contains("Сорана")) itemName =  TextFormatting.GRAY + " [" + TextFormatting.RED + "Сфера Сорана" + TextFormatting.RESET;
                        else if (itemName.contains("Эпиона")) itemName =  TextFormatting.GRAY + " [" + TextFormatting.RED + "Сфера Эпиона" + TextFormatting.RESET;
                        else if (itemName.contains("Пандо")) itemName =  TextFormatting.GRAY + " [" + TextFormatting.RED + "Сфера Пандоры" + TextFormatting.RESET;
                        else if (itemName.contains("Аполл")) itemName =  TextFormatting.GRAY + " [" + TextFormatting.RED + "Сфера Аполлона" + TextFormatting.RESET;
                        else if (itemName.contains("Тит")) itemName =  TextFormatting.GRAY + " [" + TextFormatting.RED + "Сфера Титана" + TextFormatting.RESET;
                        else if (itemName.contains("Осир")) itemName =  TextFormatting.GRAY + " [" + TextFormatting.RED + "Сфера Осириса" + TextFormatting.RESET;
                        else if (itemName.contains("Андро")) itemName =  TextFormatting.GRAY + " [" + TextFormatting.RED + "Сфера Андромеда" + TextFormatting.RESET;
                        else if (itemName.contains("Хим")) itemName =  TextFormatting.GRAY + " [" + TextFormatting.RED + "Сфера Химеры" + TextFormatting.RESET;
                        else if (itemName.contains("Астр")) itemName =  TextFormatting.GRAY + " [" + TextFormatting.RED + "Сфера Астрея" + TextFormatting.RESET;
                    } else if (itemName.contains("Талисман")) {
                        display = tag.getCompound("display");
                        lore = display.getList("Lore", 8);
                        firstLore = lore.getString(0);
                        levelIndex = firstLore.indexOf("Уровень");
                        if (levelIndex != -1) {
                            gata = firstLore.substring(levelIndex + "Уровень".length()).trim();
                            if (gata.contains("1/3")) {nameS = " 1/3";}
                            else if (gata.contains("2/3")) nameS = "2/3";
                            else if (gata.contains("MAX")) nameS = "MAX";
                            else nameS = "";
                        }

                        if (itemName.contains("Фугу")) {
                            itemName = "[FUGU]";
                        } else if (itemName.contains("Эгида")) {
                            itemName = "[EGIDA]";
                        } else if (itemName.contains("Крайта")) {
                            itemName = "[KRAITA]";
                        } else if (itemName.contains("Лекаря")) {
                            itemName = "[LEKARYA]";
                        } else if (itemName.contains("Манеса")) {
                            itemName = "[MANESA]";
                        } else if (itemName.contains("Кобры")) {
                            itemName = "[KOBRA]";
                        } else if (itemName.contains("Диониса")) {
                            itemName = "[DIONISA]";
                        } else if (itemName.contains("Гефеста")) {
                            itemName = "[GEFESTA]";
                        } else if (itemName.contains("Хауберка")) {
                            itemName = "[HAUBERKA]";
                        } else if (itemName.contains("Крушителя")) {
                            itemName = "[KRUSH ";
                        } else if (itemName.contains("Грани")) {
                            itemName = "[GRANI ";
                        } else if (itemName.contains("Дедала")) {
                            itemName = "[DEDALA ";
                        } else if (itemName.contains("Тритона")) {
                            itemName = "[TRITONA ";
                        } else if (itemName.contains("Гармонии")) {
                            itemName = "[GARMONII ";
                        } else if (itemName.contains("Феникса")) {
                            itemName = "[FENIXA ";
                        } else if (itemName.contains("Ехидны")) {
                            itemName = "[EHIDNA ";
                        } else if (itemName.contains("Карателя")) {
                            itemName = TextFormatting.GRAY + "[" + TextFormatting.RESET + "KARATEL" + TextFormatting.GRAY + "] ";
                        }
                    }
                }

                TextComponent name = (TextComponent) ITextComponent.getTextComponentOrEmpty(friendPrefix);
                int colorRect = FriendManager.isFriend(entity.getName().getString()) ? ColorUtils.rgba(66, 163, 60, 160) : ColorUtils.rgba(10, 10, 10, 160);
                name.append(FriendManager.isFriend(entity.getName().getString()) ?
                        (HynixMain.getInstance().getModuleManager().getNameProtect().isEnabled() ? ITextComponent.getTextComponentOrEmpty(TextFormatting.RED + "protected") : entity.getDisplayName())
                        : entity.getDisplayName());
                name.appendString(creativePrefix);
                if (!ClientUtils.isConnectedToServer("funtime")) {
                    if (itemName.contains("Cфера") || itemName.toLowerCase().contains("сфера")) {
                        name.appendString(TextFormatting.GRAY + " [" + TextFormatting.RED + itemName + TextFormatting.GRAY + "]");

                    }
                    if (itemName.contains("Талисман") || itemName.toLowerCase().contains("талисман")) {
                        name.appendString(TextFormatting.GRAY + " [" + TextFormatting.RED + itemName + TextFormatting.GRAY + "]");
                    }
                } else {
                    if (itemName.contains("Сфера")) {
                        name.appendString(itemName + nameS);

                    } else if (itemName.contains("Талисман")) {
                        name.appendString(itemName + nameS);
                    }
                }

                glCenteredScale(position.x + width / 2f - length / 2f - 4, position.y - 9, length + 8, 13, 0.5f);

                length = mc.fontRenderer.getStringPropertyWidth(name);
                float x1 = position.x + width / 2f - length / 2f - 4;
                float y1 = position.y - 15.5f;
                RectUtils.getInstance().drawRoundedRectShadowed(e.getMatrixStack(), x1, y1, x1 + length + 8, y1 + 13, 0, 2, colorRect, colorRect, colorRect, colorRect, false, false, true, false);
                mc.fontRenderer.func_243246_a(e.getMatrixStack(), name, position.x + width / 2f - length / 2f, position.y - 12.5f, -1);

                GL11.glPopMatrix();
                if (!remove.is("Список эффектов").getValue()) {
                    drawPotions(e.getMatrixStack(), living, position.z + 2, position.y);
                }
                drawItems(e.getMatrixStack(), living, (int) (position.x + width / 2f), (int) (position.y - 14.5f));
            } else if (entity instanceof ItemEntity item) {
                Vector4f position = entry.getValue();
                float width = position.z - position.x;
                ITextComponent displayName = new StringTextComponent((item.getItem().getDisplayName().getString() + (item.getItem().getCount() < 1 ? "" : " x" + item.getItem().getCount())));

                float length = mc.fontRenderer.getStringPropertyWidth(displayName);
                GL11.glPushMatrix();

                glCenteredScale(position.x + width / 2f - length / 2f, position.y - 7, length, 10, 0.5f);
                mc.fontRenderer.func_243248_b(e.getMatrixStack(), displayName, position.x + width / 2f - length / 2f, position.y - 7, -1);
                GL11.glPopMatrix();
            }
        }

        if (this.alpha.finished(Direction.BACKWARDS)) {
            return;
        }
    }

    public boolean isInView(Entity ent) {

        if (mc.getRenderViewEntity() == null) {
            return false;
        }
        frustum.setCameraPosition(mc.getRenderManager().info.getProjectedView().x, mc.getRenderManager().info.getProjectedView().y, mc.getRenderManager().info.getProjectedView().z);
        return frustum.isBoundingBoxInFrustum(ent.getBoundingBox()) || ent.ignoreFrustumCheck;
    }
    int index = 0;
    private void drawPotions(MatrixStack matrixStack, LivingEntity entity, float posX, float posY) {
        for (Iterator var8 = entity.getActivePotionEffects().iterator(); var8.hasNext(); ++index) {
            EffectInstance effectInstance = (EffectInstance)var8.next();

            int amp = effectInstance.getAmplifier() + 1;
            String ampStr = "";

            if (amp >= 1 && amp <= 9) {
                ampStr = " " + amp;
            }


            String text = EffectUtils.getPotionDurationString(effectInstance, 1) + " - " + I18n.format(effectInstance.getEffectName(), new Object[0]) + ampStr;
            PotionSpriteUploader potionspriteuploader = mc.getPotionSpriteUploader();
            Effect effect = effectInstance.getPotion();
            float iconSize = (float) (8);
            TextureAtlasSprite textureatlassprite = potionspriteuploader.getSprite(effect);
            mc.getTextureManager().bindTexture(textureatlassprite.getAtlasTexture().getTextureLocation());
            DisplayEffectsScreen.blit(matrixStack, (float) (posX),  (int)posY - 0.5f, 10, 8, 8, textureatlassprite);

            Fonts.montserrat.drawTextWithOutline(matrixStack, text, posX + iconSize, posY, ColorUtils.setAlpha(-1, (int) (255)), 6, 0.02f);
            posY += Fonts.montserrat.getHeight(6) + 1;
        }
    }

    private void drawItems(MatrixStack matrixStack, LivingEntity entity, int posX, int posY) {
        int size = 8;
        int padding = 6;

        float fontHeight = Fonts.consolas.getHeight(6);

        List<ItemStack> items = new ArrayList<>();

        ItemStack mainStack = entity.getHeldItemMainhand();

        if (!mainStack.isEmpty()) {
            items.add(mainStack);
        }

        for (ItemStack itemStack : entity.getArmorInventoryList()) {
            if (itemStack.isEmpty()) continue;
            items.add(itemStack);
        }

        ItemStack offStack = entity.getHeldItemOffhand();

        if (!offStack.isEmpty()) {
            items.add(offStack);
        }

        posX -= (items.size() * (size + padding)) / 2f;

        for (ItemStack itemStack : items) {
            if (itemStack.isEmpty()) continue;

            GL11.glPushMatrix();

            glCenteredScale(posX, posY - 5, size / 2f, size / 2f, 0.5f);

            mc.getItemRenderer().renderItemAndEffectIntoGUI(itemStack, posX, posY - 5);
            mc.getItemRenderer().renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, posX, posY - 5, null);

            GL11.glPopMatrix();

            if (itemStack.isEnchanted() && !remove.is("Зачарования").getValue()) {
                int ePosY = (int) (posY - fontHeight);

                Map<Enchantment, Integer> enchantmentsMap = EnchantmentHelper.getEnchantments(itemStack);

                for (Enchantment enchantment : enchantmentsMap.keySet()) {
                    int level = enchantmentsMap.get(enchantment);

                    if (level < 1 || !enchantment.canApply(itemStack)) continue;

                    IFormattableTextComponent iformattabletextcomponent = new TranslationTextComponent(enchantment.getName());

                    String enchText = iformattabletextcomponent.getString().substring(0, 2) + level;

                    Fonts.consolas.drawText(matrixStack, enchText, posX, ePosY - 5, -1, 6, 0.05f);

                    ePosY -= (int) fontHeight;
                }
            }

            posX += size + padding;
        }
    }

    public boolean isValid(Entity e) {
        if (BotRemover.isBot(e)) return false;

        return isInView(e);
    }

    public static void drawMcRect(
            double left,
            double top,
            double right,
            double bottom,
            int color) {
        if (left < right) {
            double i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            double j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();

        bufferbuilder.pos(left, bottom, 1.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(right, bottom, 1.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(right, top, 1.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(left, top, 1.0F).color(f, f1, f2, f3).endVertex();

    }

    public void glCenteredScale(final float x, final float y, final float w, final float h, final float f) {
        glTranslatef(x + w / 2, y + h / 2, 0);
        glScalef(f, f, 1);
        glTranslatef(-x - w / 2, -y - h / 2, 0);
    }

    public double getScale(Vector3d position, double size) {
        Vector3d cam = mc.getRenderManager().info.getProjectedView();
        double distance = cam.distanceTo(position);
        double fov = mc.gameRenderer.getFOVModifier(mc.getRenderManager().info, mc.getRenderPartialTicks(), true);

        return Math.max(10f, 1000 / distance) * (size / 30f) / (fov == 70 ? 1 : fov / 70.0f);
    }

    public void drawImageMarker(EventRender2D e) {
        if (this.currentTarget != null && this.currentTarget != mc.player) {
            double sin = Math.sin(System.currentTimeMillis() / 1000.0);
            double distance = mc.player.getDistance(currentTarget);
            float maxSize = (float) getScale(currentTarget.getPositionVec(), 10);
            float size = Math.max(maxSize - (float)distance, 20.0F);
            Vector3d interpolated = currentTarget.getPositon(e.getPartialTicks());
            Vector2f pos = ProjectionUtils.project(interpolated.x, interpolated.y + currentTarget.getHeight() / 2f, interpolated.z);
            GlStateManager.pushMatrix();
            GlStateManager.translatef(pos.x, pos.y, 0);
            GlStateManager.rotatef((float) sin * 360, 0, 0, 1);
            GlStateManager.translatef(-pos.x, -pos.y, 0);

            if (pos != null) {
                RenderUtils.drawImageAlpha(new ResourceLocation("hynix/images/modules/target.png"), pos.x - size / 2f, pos.y - size / 2f, size, size, new Vector4i(
                        ColorUtils.setAlpha(Theme.mainRectColor, (int)(alpha.getOutput())),
                        ColorUtils.setAlpha(Theme.mainRectColor, (int) (alpha.getOutput())),
                        ColorUtils.setAlpha(Theme.mainRectColor, (int)(alpha.getOutput())),
                        ColorUtils.setAlpha(Theme.mainRectColor, (int)(alpha.getOutput()))
                ));
                GlStateManager.popMatrix();
            }
        }
    }

    public void drawSoulsMarker(MatrixStack stack, EventPreRender3D e) {
        if (this.currentTarget != null && this.currentTarget != mc.player) {
            MatrixStack ms = stack;
            ms.push();

            RenderSystem.pushMatrix();
            RenderSystem.disableLighting();
            depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.shadeModel(7425);
            RenderSystem.disableCull();
            RenderSystem.disableAlphaTest();
            RenderSystem.blendFuncSeparate(770, 1, 0, 1);

            double x = currentTarget.getPosX();
            double y = currentTarget.getPosY() + (currentTarget.getHeight() / 2f);
            double z = currentTarget.getPosZ();
            double radius = 0.35 + currentTarget.getWidth() / 2;
            float speed = 20;
            float size = 0.6f;
            double distance = 24;
            int length = 24;
            int color = ColorUtils.multAlpha(Theme.rectColor, 1);
            int alpha = 1;
            ActiveRenderInfo camera = mc.getRenderManager().info;
            ms.translate(-mc.getRenderManager().info.getProjectedView().getX(), -mc.getRenderManager().info.getProjectedView().getY(), -mc.getRenderManager().info.getProjectedView().getZ());
            Vector3d interpolated = MathUtils.interpolate(currentTarget.getPositionVec(), new Vector3d(currentTarget.lastTickPosX, currentTarget.lastTickPosY, currentTarget.lastTickPosZ), e.getPartialTicks());
            interpolated.y += 0.25 + currentTarget.getHeight() / 2;

            ms.translate(interpolated.x + 0.2, interpolated.y, interpolated.z);

            RectUtils.bindTexture(new ResourceLocation("hynix/images/glow.png"));

            for (int i = 0; i < length; i++) {
                Quaternion r = camera.getRotation().copy();
                double angle = 0.05f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);
                double s = Math.sin(angle * (Math.PI / 2)) * radius;
                double c = Math.cos(angle * (Math.PI / 2)) * radius;
                double o = Math.cos(angle * (Math.PI / 3)) * radius;
                buffer.begin(GL_QUADS, POSITION_COLOR_TEX);

                ms.translate(-s, o, -c);
                ms.translate(-size / 2f, -size / 2f, 0);
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);

                buffer.pos(ms.getLast().getMatrix(), 0, -size, 0).color(ColorUtils.reAlphaInt(color, (int) (alpha * this.alpha.getOutput()))).tex(0, 0).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, -size, 0).color(ColorUtils.reAlphaInt(color, (int) (alpha * this.alpha.getOutput()))).tex(0, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, 0, 0).color(ColorUtils.reAlphaInt(color, (int) (alpha * this.alpha.getOutput()))).tex(1, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), 0, 0, 0).color(ColorUtils.reAlphaInt(color, (int) (alpha * this.alpha.getOutput()))).tex(1, 0).endVertex();

                tessellator.draw();

                ms.translate(-size / 2f, -size / 2f, 0);
                r.conjugate();
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);

                ms.translate(s, -o, c);
            }

            for (int i = 0; i < length; i++) {
                Quaternion r = camera.getRotation().copy();
                double angle = 0.05f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);
                double s = Math.sin(angle * (Math.PI / 2)) * radius;
                double c = Math.cos(angle * (Math.PI / 2)) * radius;
                double o = Math.sin(angle * (Math.PI / 3)) * radius;
                buffer.begin(GL_QUADS, POSITION_COLOR_TEX);

                ms.translate(s, o, c);
                ms.translate(-size / 2f, -size / 2f, 0);
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);

                buffer.pos(ms.getLast().getMatrix(), 0, -size, 0).color(ColorUtils.reAlphaInt(color, (int) (alpha * this.alpha.getOutput()))).tex(0, 0).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, -size, 0).color(ColorUtils.reAlphaInt(color, (int) (alpha * this.alpha.getOutput()))).tex(0, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, 0, 0).color(ColorUtils.reAlphaInt(color, (int) (alpha * this.alpha.getOutput()))).tex(1, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), 0, 0, 0).color(ColorUtils.reAlphaInt(color, (int) (alpha * this.alpha.getOutput()))).tex(1, 0).endVertex();

                tessellator.draw();

                ms.translate(-size / 2f, -size / 2f, 0);
                r.conjugate();
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);

                ms.translate(-s, -o, -c);
            }

            ms.translate(-x, -y, -z);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.enableAlphaTest();
            depthMask(true);
            RenderSystem.popMatrix();
            ms.pop();
        }
    }

    public void drawCircleMarker(MatrixStack stack, EventPreRender3D e) {
        if (this.currentTarget != null && this.currentTarget != mc.player) {
            MatrixStack ms = stack;
            ms.push();
            RenderSystem.pushMatrix();
            RenderSystem.disableLighting();
            depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.shadeModel(7425);
            RenderSystem.disableCull();
            RenderSystem.disableAlphaTest();
            RenderSystem.blendFuncSeparate(770, 1, 0, 1);
            double radius = 0.4 + currentTarget.getWidth() / 2;
            float speed = 30;
            float size = 0.3f;
            double distance = 155;
            int lenght = (int) (distance + currentTarget.getWidth());
            ActiveRenderInfo camera = mc.getRenderManager().info;
            ms.translate(-mc.getRenderManager().info.getProjectedView().getX(),
                    -mc.getRenderManager().info.getProjectedView().getY(),
                    -mc.getRenderManager().info.getProjectedView().getZ());

            Vector3d interpolated = MathUtils.interpolate(currentTarget.getPositionVec(), new Vector3d(currentTarget.lastTickPosX, currentTarget.lastTickPosY, currentTarget.lastTickPosZ), e.getPartialTicks());
            ms.translate(interpolated.x + 0.15, interpolated.y + 0.2 + currentTarget.getHeight() / 2, interpolated.z);
            RectUtils.bindTexture(new ResourceLocation("hynix/images/glow.png"));
            for (int j = 0; j < 1; j++) {
                for (int i = 0; i < lenght; i++) {
                    Quaternion r = camera.getRotation().copy();
                    buffer.begin(GL_QUADS, POSITION_COLOR_TEX);

                    double angle = 0.1f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);

                    double s = Math.sin(angle + j * (Math.PI / 1.5)) * radius;
                    double c = Math.cos(angle + j * (Math.PI / 1.5)) * radius;

                    double yOffset = Math.sin(System.currentTimeMillis() * 0.003 + j) * 0.8;

                    ms.translate(0, yOffset, 0);

                    ms.translate(s, 0, -c);

                    ms.translate(-size / 2f, -size / 2f, 0);
                    ms.rotate(r);
                    ms.translate(size / 2f, size / 2f, 0);
                    int color = ColorUtils.getColor(i);
                    int alpha = (int) (1 * this.alpha.getOutput());
                    buffer.pos(ms.getLast().getMatrix(), 0, -size, 0).color(ColorUtils.reAlphaInt(color, alpha)).tex(0, 0).endVertex();
                    buffer.pos(ms.getLast().getMatrix(), -size, -size, 0).color(ColorUtils.reAlphaInt(color, alpha)).tex(0, 1).endVertex();
                    buffer.pos(ms.getLast().getMatrix(), -size, 0, 0).color(ColorUtils.reAlphaInt(color, alpha)).tex(1, 1).endVertex();
                    buffer.pos(ms.getLast().getMatrix(), 0, 0, 0).color(ColorUtils.reAlphaInt(color, alpha)).tex(1, 0).endVertex();
                    tessellator.draw();

                    ms.translate(-size / 2f, -size / 2f, 0);
                    r.conjugate();
                    ms.rotate(r);
                    ms.translate(size / 2f, size / 2f, 0);
                    ms.translate(-s, 0, c);
                    ms.translate(0, -yOffset, 0);
                }
            }

            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.enableAlphaTest();
            depthMask(true);
            RenderSystem.popMatrix();
            ms.pop();
        }
    }
}
