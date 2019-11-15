package delaunayvid;

import java.awt.Graphics;
import java.awt.image.BufferedImage;


public abstract class ProcHandler {
    
    public boolean initialized = false;
    Gui gui;
    ImgProc proc;
        
    public abstract void init(BufferedImage img);
    
    public abstract void reset();
    
    public abstract void analyze(int x, int y);
    
    public abstract void draw(Graphics g);

    static float getR(int in){
        return ((in & 0xff0000) >> 16)/(float)255;
    }
    static float getG(int in){
        return ((in & 0xff00) >> 8)/(float)255;
    }
    static float getB(int in){
        return (in & 0xff)/(float)255;
    }       
}
