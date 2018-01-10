package delaunayvid;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;


public abstract class ProcHandler {
    
    public boolean initialized = false;
    
    public abstract void init(BufferedImage img);
    
    public abstract void analyze(int x, int y);
    
    public abstract void draw(Graphics2D g);
    
}
