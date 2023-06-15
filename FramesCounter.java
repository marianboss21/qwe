package celeste.Simulator.LandingModule;
/*
 * A class that contains frames used for the GUI representation
 */
public class FramesCounter {
    private double startTime = 0;
    private double frameCount = 0;
    private double frames;

    public double counerFrame() {
        if (startTime == 0) {
            startTime = System.currentTimeMillis();
        }

        frameCount++;
        double currentTime = System.currentTimeMillis();
        if (currentTime - startTime >= 1000) {
            frames = Math.round(frameCount / ((currentTime - startTime)/1000.0));
            startTime = currentTime;
            frameCount = 0;
        }
        return frames;
    }
}
