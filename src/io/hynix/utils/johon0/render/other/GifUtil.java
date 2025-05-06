package io.hynix.utils.johon0.render.other;

public class GifUtil {
    public int getFrame(int totalFrames, int frameDelay, boolean countFromZero) {
        long currentTime = System.currentTimeMillis();
        int frameIndex = (int) (currentTime / frameDelay % totalFrames);
        return countFromZero ? frameIndex : frameIndex + 1;
    }
}
