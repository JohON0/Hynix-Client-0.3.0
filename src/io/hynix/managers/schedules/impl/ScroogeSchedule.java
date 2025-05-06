package io.hynix.managers.schedules.impl;

import io.hynix.managers.schedules.Schedule;
import io.hynix.managers.schedules.TimeType;

public class ScroogeSchedule
        extends Schedule {
    @Override
    public String getName() {
        return "Скрудж";
    }

    @Override
    public TimeType[] getTimes() {
        return new TimeType[]{TimeType.FIFTEEN_HALF};
    }
}
