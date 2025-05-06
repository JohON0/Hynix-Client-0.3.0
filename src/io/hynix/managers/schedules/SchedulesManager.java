package io.hynix.managers.schedules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.hynix.managers.schedules.impl.*;
import io.hynix.utils.client.IMinecraft;

public class SchedulesManager
        implements IMinecraft {
    private final List<Schedule> schedules = new ArrayList<>();

    public SchedulesManager() {
        this.schedules.addAll(Arrays.asList(new AirDropSchedule(), new ScroogeSchedule(), new SecretMerchantSchedule(), new MascotSchedule(), new CompetitionSchedule()));
    }

    public List<Schedule> getSchedules() {
        return this.schedules;
    }
}