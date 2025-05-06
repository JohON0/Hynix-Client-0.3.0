package io.hynix.utils.attackdev;

import java.util.ArrayList;

public class TickScheduler {
    static ArrayList<Scheluder> scheluders = new ArrayList<>();

    public static void schelude(Runnable action, int ticks) {
        scheluders.add(new Scheluder(action, ticks));
    }

    public static void update() {

        scheluders.removeIf(scheluder -> scheluder.wait_tick == -1);

        for(Scheluder scheluder : scheluders) {
            scheluder.update();
        }
    }

    static class Scheluder {
        private int wait_tick;
        private Runnable action;
        public Scheluder(Runnable action, int wait_tick) {
            this.action = action;
            this.wait_tick = wait_tick;
        }

        public void execute() {
            action.run();
        }

        public void update() {
            this.wait_tick --;

            if(wait_tick == 0) {
                execute();
            }
        }
    }
}