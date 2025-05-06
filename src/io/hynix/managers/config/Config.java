package io.hynix.managers.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.hynix.HynixMain;
import io.hynix.units.settings.api.Setting;
import io.hynix.units.settings.impl.*;
import io.hynix.utils.client.IMinecraft;
import lombok.Getter;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.util.function.Consumer;

@Getter
public class Config implements IMinecraft {
    private final File file;
    private final String name;

    public Config(String name) {
        this.name = name;
        this.file = new File(new File(Minecraft.getInstance().gameDir, "\\saves\\files\\configs"), name + ".cfg");
    }

    public void loadConfig(JsonObject jsonObject) {
        if (jsonObject == null) {
            return;
        }

        if (jsonObject.has("modules")) {
            loadFunctionSettings(jsonObject.getAsJsonObject("modules"));
        }
    }

    private void loadFunctionSettings(JsonObject functionsObject) {
        HynixMain.getInstance().getModuleManager().getModules().forEach(module -> {
            JsonObject moduleObject = functionsObject.getAsJsonObject(module.getName().toLowerCase());
            if (moduleObject == null) {
                return;
            }

            module.setEnabled(false,true);
            loadSettingFromJson(moduleObject, "bind", value -> module.setBind(value.getAsInt()));
            loadSettingFromJson(moduleObject, "state", value -> module.setEnabled(value.getAsBoolean(),true));
            module.getSettings().forEach(setting -> loadIndividualSetting(moduleObject, setting));
        });
    }

    private void loadIndividualSetting(JsonObject moduleObject, Setting<?> setting) {
        JsonElement settingElement = moduleObject.get(setting.getName());

        if (settingElement == null || settingElement.isJsonNull()) {
            return;
        }

        if (setting instanceof SliderSetting) {
            ((SliderSetting) setting).setValue(settingElement.getAsFloat());
        } if (setting instanceof BooleanSetting) {
            ((BooleanSetting) setting).setValue(settingElement.getAsBoolean());
        } if (setting instanceof ColorSetting) {
            ((ColorSetting) setting).setValue(settingElement.getAsInt());
        } if (setting instanceof ModeSetting) {
            ((ModeSetting) setting).setValue(settingElement.getAsString());
        } if (setting instanceof BindSetting) {
            ((BindSetting) setting).setValue(settingElement.getAsInt());
        } if (setting instanceof StringSetting) {
            ((StringSetting) setting).setValue(settingElement.getAsString());
        } if (setting instanceof ModeListSetting) {
            loadModeListSetting((ModeListSetting) setting, moduleObject);
        }
    }

    private void loadModeListSetting(ModeListSetting setting, JsonObject moduleObject) {
        JsonObject elements = moduleObject.getAsJsonObject(setting.getName());
        setting.getValue().forEach(option -> {
            JsonElement optionElement = elements.get(option.getName());
            if (optionElement != null && !optionElement.isJsonNull()) {

                option.setValue(optionElement.getAsBoolean());
            }
        });
    }

    private void loadSettingFromJson(JsonObject jsonObject, String key, Consumer<JsonElement> consumer) {
        JsonElement element = jsonObject.get(key);
        if (element != null && !element.isJsonNull()) {
            consumer.accept(element);
        }
    }



    public JsonElement saveConfig() {
        JsonObject functionsObject = new JsonObject();

        saveFunctionSettings(functionsObject);

        JsonObject newObject = new JsonObject();
        newObject.add("modules", functionsObject);

        return newObject;
    }

    private void saveFunctionSettings(JsonObject functionsObject) {
        HynixMain.getInstance().getModuleManager().getModules().forEach(module -> {
            JsonObject moduleObject = new JsonObject();

            moduleObject.addProperty("bind", module.getBind());
            moduleObject.addProperty("state", module.isEnabled());

            module.getSettings().forEach(setting -> saveIndividualSetting(moduleObject, setting));

            functionsObject.add(module.getName().toLowerCase(), moduleObject);
        });
    }

    private void saveIndividualSetting(JsonObject moduleObject, Setting<?> setting) {
        if (setting instanceof BooleanSetting) {
            moduleObject.addProperty(setting.getName(), ((BooleanSetting) setting).getValue());
        } if (setting instanceof SliderSetting) {
            moduleObject.addProperty(setting.getName(), ((SliderSetting) setting).getValue());
        } if (setting instanceof ModeSetting) {
            moduleObject.addProperty(setting.getName(), ((ModeSetting) setting).getValue());
        } if (setting instanceof ColorSetting) {
            moduleObject.addProperty(setting.getName(), ((ColorSetting) setting).getValue());
        } if (setting instanceof BindSetting) {
            moduleObject.addProperty(setting.getName(), ((BindSetting) setting).getValue());
        } if (setting instanceof StringSetting) {
            moduleObject.addProperty(setting.getName(), ((StringSetting) setting).getValue());
        } if (setting instanceof ModeListSetting) {
            saveModeListSetting(moduleObject, (ModeListSetting) setting);

        }
    }

    private void saveModeListSetting(JsonObject moduleObject, ModeListSetting setting) {
        JsonObject elements = new JsonObject();
        setting.getValue().forEach(option -> elements.addProperty(option.getName(), option.getValue()));
        moduleObject.add(setting.getName(), elements);
    }
}
