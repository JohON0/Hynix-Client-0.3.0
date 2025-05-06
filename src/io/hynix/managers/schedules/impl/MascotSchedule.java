package io.hynix.managers.schedules.impl;

import io.hynix.managers.schedules.Schedule;
import io.hynix.managers.schedules.TimeType;

public class MascotSchedule
        extends Schedule {
    @Override
    public String getName() {
        return "Талисман";
    }

    @Override
    public TimeType[] getTimes() {
        return new TimeType[]{TimeType.NINETEEN_HALF};
    }
}
