package io.hynix.waveycapes.config;

import io.hynix.waveycapes.CapeMovement;
import io.hynix.waveycapes.CapeStyle;
import io.hynix.waveycapes.WindMode;

public class Config {
    public int configVersion = 2;
    public WindMode windMode = WindMode.NONE;
    public CapeStyle capeStyle = CapeStyle.SMOOTH;
    public CapeMovement capeMovement = CapeMovement.BASIC_SIMULATION;
    public int gravity = 25;
    public int heightMultiplier = 6;
    public int straveMultiplier = 2;
}
