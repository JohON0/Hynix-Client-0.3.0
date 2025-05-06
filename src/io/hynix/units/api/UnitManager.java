package io.hynix.units.api;

import com.google.common.eventbus.Subscribe;

import io.hynix.HynixMain;
import io.hynix.events.impl.EventKey;
import io.hynix.units.impl.combat.*;
import io.hynix.units.impl.display.*;
import io.hynix.units.impl.miscellaneous.*;
import io.hynix.units.impl.traversal.*;
import io.hynix.utils.johon0.render.font.Font;
import io.hynix.utils.text.font.ClientFonts;
import lombok.Getter;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Getter
public class UnitManager {
    private final List<Unit> modules = new CopyOnWriteArrayList<>();

    private NoServerDesync noServerDesync;
    private CustomViewItem viewModel;
    private Hud hud;
    private AspectRatio aspectRatio;
    private AutoGappleEat autoGapple;
    private Notifications notifications;
    private AutoSprint autoSprint;
    private NoVelocity velocity;
    private Remover noRender;
    private Timer timer;
    private ElytraHelper elytrahelper;
    private AutoPotionUse autopotion;
    private TriggerBot triggerbot;
    private AutoArmor autoArmor;
    private ItemScroller itemScroller;
    private ClickFriend clickfriend;
    private ESP esp;
    private FTHelper FTHelper;
    private ItemCooldown itemCooldown;
    private ClickPearl clickPearl;
    private AutoSwapItem autoSwap;
    private HitBox entityBox;
    private AntiPush antiPush;
    private FreeCamera freeCam;
    private ChestStealer chestStealer;
    private Fly fly;
    private TargetStrafe targetStrafe;
    private Sounds clientTune;
    private AuctionHelper auctionHelper;
    private AutoTotem autoTotem;
    private AutoCrystal autoExplosion;
    private AttackAura attackAura;
    private BotRemover antiBot;
    private Crosshair crosshair;
    private Strafe strafe;
    private Predictions predictions;
    private NoEntityTrace noEntityTrace;
    private NoClip noClip;
    private BlockESP blockESP;
    private NameProtect nameProtect;
    private NoInteract noInteract;
    private Tracers tracers;
    private Unhook selfDestruct;
    private BetterMinecraft betterMinecraft;
    private SeeInvisibles seeInvisibles;
    private Jesus jesus;
    private Speed speed;
    private WaterSpeed waterSpeed;
    private NoFriendDamage noFriendHurt;
    private NoSlowDown noSlowDown;
    private ClickGui clickGui;
    private TntTimer tntTimer;
    private WorldEditor worldTweaks;
    private Arrows arrows;
    private RWJoiner rwJoiner;
    private AutoTpAccept autoTpAccept;
    private FullBright fullBright;
    private AutoDuel autoDuel;
    private FlyingParticles fireFly;
    private Trails trails;
    private JumpCircle jumpCircle;
    private Particles particles;
    private BabyBoy babyBoy;
    private NoJumpDelay noJumpDelay;
    private ElytraBooster elytraBooster;
    private Animations animations;

    public void init() {
        registerAll(
                noJumpDelay = new NoJumpDelay(), itemScroller = new ItemScroller(), babyBoy = new BabyBoy(),
                fireFly = new FlyingParticles(), trails = new Trails(), jumpCircle = new JumpCircle(),
                fullBright = new FullBright(), autoDuel = new AutoDuel(), hud = new Hud(), arrows = new Arrows(),
                autoTpAccept = new AutoTpAccept(), rwJoiner = new RWJoiner(), tntTimer = new TntTimer(), clickGui = new ClickGui(),
                noServerDesync = new NoServerDesync(), autoArmor = new AutoArmor(), worldTweaks = new WorldEditor(),
                particles = new Particles(), noSlowDown = new NoSlowDown(),velocity = new NoVelocity(), noRender = new Remover(),
                noFriendHurt = new NoFriendDamage(), waterSpeed = new WaterSpeed(), jesus = new Jesus(),
                speed = new Speed(), autoGapple = new AutoGappleEat(), autoSprint = new AutoSprint(), notifications = new Notifications(),
                seeInvisibles = new SeeInvisibles(), elytrahelper = new ElytraHelper(), autopotion = new AutoPotionUse(),
                noClip = new NoClip(), triggerbot = new TriggerBot(), clickfriend = new ClickFriend(),
                esp = new ESP(), FTHelper = new FTHelper(), entityBox = new HitBox(), antiPush = new AntiPush(),
                freeCam = new FreeCamera(), chestStealer = new ChestStealer(),
                fly = new Fly(), clientTune = new Sounds(), autoExplosion = new AutoCrystal(),
                antiBot = new BotRemover(), crosshair = new Crosshair(), autoTotem = new AutoTotem(), itemCooldown = new ItemCooldown(),
                attackAura = new AttackAura(autopotion), clickPearl = new ClickPearl(itemCooldown),
                autoSwap = new AutoSwapItem(), targetStrafe = new TargetStrafe(attackAura),
                strafe = new Strafe(targetStrafe, attackAura), viewModel = new CustomViewItem(attackAura),
                predictions = new Predictions(), noEntityTrace = new NoEntityTrace(), betterMinecraft = new BetterMinecraft(),
                blockESP = new BlockESP(), timer = new Timer(), nameProtect = new NameProtect(),
                noInteract = new NoInteract(), tracers = new Tracers(), selfDestruct = new Unhook(),
                new DeathCoords(), new LongJump(), new RWHelper(), new RPSpoofer(), elytraBooster = new ElytraBooster(),
                 new AutoMyst(), aspectRatio = new AspectRatio(), animations = new Animations(), auctionHelper = new AuctionHelper(),
                new DragonFly(), new TapeMouse(), new GuiMove(), new AutoFarmClan(), new AutoEat(), new TargetESP()
        );

        sortModulesByWidth();


        HynixMain.getInstance().getEventBus().register(this);
    }

    private void registerAll(Unit... modules) {
        this.modules.addAll(List.of(modules));
    }

    public void sortModulesByWidth() {
        try {
            modules.sort(Comparator.comparingDouble(module ->
                    ClientFonts.tenacity[14].getWidth(module.getClass().getName())
            ).reversed());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public List<Unit> get(final Category category) {
        return modules.stream().filter(module -> module.getCategory() == category).collect(Collectors.toList());
    }
    public List<Unit> getSorted(Font font, float size) {
        return modules.stream().sorted((f1, f2) -> Float.compare(ClientFonts.tenacityBold[12].getWidth(f2.getName()), ClientFonts.tenacityBold[12].getWidth(f1.getName()))).toList();
    }
    public int countEnabledModules() {
        return (int) modules.stream().filter(Unit::isEnabled).count();
    }

    @Subscribe
    private void onKey(EventKey e) {
        if (selfDestruct.unhooked) return;
        modules.stream().filter(module -> module.getBind() == e.getKey()).forEach(Unit::toggle);
    }

}
