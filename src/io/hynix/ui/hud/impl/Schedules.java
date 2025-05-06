package io.hynix.ui.hud.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.hynix.events.impl.EventRender2D;
import io.hynix.events.impl.EventUpdate;
import io.hynix.ui.clickgui.ClickGui;
import io.hynix.ui.hud.updater.ElementRenderer;
import io.hynix.ui.hud.updater.ElementUpdater;
import io.hynix.managers.schedules.Schedule;
import io.hynix.managers.schedules.SchedulesManager;
import io.hynix.managers.schedules.TimeType;
import io.hynix.managers.theme.Theme;
import io.hynix.utils.johon0.animations.easing.CompactAnimation;
import io.hynix.utils.johon0.animations.easing.Easing;
import io.hynix.managers.drag.Dragging;
import io.hynix.utils.johon0.render.render2d.RenderUtils;
import io.hynix.utils.johon0.render.other.Scissor;
import io.hynix.utils.text.font.ClientFonts;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.*;
import java.util.ArrayList;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class Schedules implements ElementRenderer, ElementUpdater {
    final Dragging dragging;
    float width;
    float height;
    private final CompactAnimation widthAnimation = new CompactAnimation(Easing.EASE_OUT_QUART, 100);
    private final CompactAnimation heightAnimation = new CompactAnimation(Easing.EASE_OUT_QUART, 100);
    final SchedulesManager schedulesManager = new SchedulesManager();
    final TimeZone timeZone = TimeZone.getTimeZone("Europe/Moscow");
    List<Schedule> activeSchedules = new ArrayList<>();
    private static final int MINUTES_IN_DAY = 1440;
    boolean sorted = false;

    @Override
    public void update(EventUpdate e) {
        activeSchedules = schedulesManager.getSchedules();
        if (!sorted) {
            this.activeSchedules.sort(Comparator.comparingInt(schedule -> (int) -ClientFonts.tenacityBold[14].getWidth(schedule.getName())));
            sorted = true;
        }
    }

    @Override
    public void render(EventRender2D eventRender2D) {
        MatrixStack ms = eventRender2D.getMatrixStack();

        float posX = dragging.getX();
        float posY = dragging.getY();
        float fontSize = 6.5f;
        float padding = 5;
        String name = "Schedules";
        RenderUtils.drawShadow(posX,posY, (float) width, height, 5, ClickGui.backgroundColor);
        RenderUtils.drawRoundedRect(posX,posY, (float) width, height, 4, ClickGui.backgroundColor);
        int textColor = ClickGui.textcolor;

        ClientFonts.tenacity[18].drawString(ms, name, posX + 10, posY + padding + 3.5f, textColor);

        float maxWidth = ClientFonts.tenacityBold[14].getWidth(name) + padding * 2;
        float localHeight = fontSize + padding * 2 ;
        ClientFonts.icons_nur[20].drawString(ms, "G", posX + width - padding - 10, posY + 6.5f, Theme.rectColor);
        posY += fontSize + padding + 2;
        posY += 6f;
        Scissor.push();
        Scissor.setFromComponentCoordinates(posX, posY - 5, width, height+ 5);
        for (Schedule schedule : activeSchedules) {
            String nameText = schedule.getName();
            String timeString = getTimeString(schedule);

            float nameWidth = ClientFonts.tenacityBold[14].getWidth(nameText);
            float bindWidth = ClientFonts.tenacityBold[14].getWidth(timeString);

            float localWidth = nameWidth + bindWidth + padding * 3;

            ClientFonts.tenacityBold[14].drawString(ms, nameText, posX + padding - 0.5f, posY + 2f, textColor );
            ClientFonts.tenacityBold[14].drawString(ms, timeString, posX + width - padding - bindWidth + 1, posY + 2f, textColor);

            if (localWidth > maxWidth) {
                maxWidth = localWidth;
            }

            posY += (fontSize + padding - 3);
            localHeight += (fontSize + padding - 3);
        }
        Scissor.unset();
        Scissor.pop();
        widthAnimation.run(Math.max(maxWidth, 70));
        heightAnimation.run( (localHeight + 5.5f));
        width = (float) widthAnimation.getValue();
        height = (float) heightAnimation.getValue();
        dragging.setWidth(width);
        dragging.setHeight(height);
    }

    private String formatTime(Calendar calendar, int minutes) {
        int hours = minutes / 60;
        int secondsLeft = 59 - calendar.get(Calendar.SECOND);

        if ((minutes %= 60) > 0) {
            --minutes;
        }
        String formated;
        if (hours == 0) {
            formated = minutes + "м " + secondsLeft + "с";
        } else {
            formated = hours + "ч " + minutes + "м " + secondsLeft + "с";
        }
        return formated;
    }

    private int calculateTimeDifference(int[] times, int minutes) {
        int index = Arrays.binarySearch(times, minutes);

        if (index < 0) {
            index = -index - 1;
        }

        if (index >= times.length) {
            return times[0] + MINUTES_IN_DAY - minutes;
        }

        return times[index] - minutes;
    }

    private String getTimeString(Schedule schedule, Calendar calendar) {
        int minutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
        int[] timeArray = Arrays.stream(schedule.getTimes()).mapToInt(TimeType::getMinutesSinceMidnight).toArray();
        int timeDifference = calculateTimeDifference(timeArray, minutes);
        return formatTime(calendar, timeDifference);
    }

    public String getTimeString(Schedule schedule) {
        return getTimeString(schedule, Calendar.getInstance(timeZone));
    }

}

