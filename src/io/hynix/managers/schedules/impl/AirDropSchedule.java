package io.hynix.managers.schedules.impl;

import io.hynix.managers.schedules.Schedule;
import io.hynix.managers.schedules.TimeType;

public class AirDropSchedule
        extends Schedule {
    @Override
    public String getName() {
        return "Аир дроп";
    }

    @Override
    public TimeType[] getTimes() {
        return new TimeType[]{TimeType.NINE, TimeType.ELEVEN, TimeType.THIRTEEN, TimeType.FIFTEEN, TimeType.SEVENTEEN, TimeType.NINETEEN, TimeType.TWENTY_ONE, TimeType.TWENTY_THREE, TimeType.ONE};
    }
}