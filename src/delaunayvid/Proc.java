package delaunayvid;

import delaunay.Delaunay;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;

public abstract class Proc {
    
Gui gui;
Random rand = new Random();
boolean seed = true;
int height, width, xdiv, ydiv, div, totalpix;  
double variance;// = 0.15;
double band;// = 1.75;
double thresh;// = 0.005;
int inc = 1;
int nodes = 0;
Color bkgdcolor = Color.black; 
Color dotcolor = Color.white; 
Color delcolor = Color.white;
Color vorcolor = Color.red;
boolean drawDelaunay = true;
boolean drawVoronoi = false; 
boolean delaunayInit = false;
boolean drawbkgd = true;
boolean extraAa = false;
boolean ignore = false;


    void getVals(Gui g){
        band = g.bandVal();
        variance = g.varValue();
        thresh = g.threshValue();
        gui = g;
    } 
    
    void setBand(double b){
        band = b;
    }
    
    void setVariance(double v){
        variance = v;
    }
    
    void setThresh(double t){
        thresh = t;
    }
    
    void setSeed(boolean b){
        seed = b;
    }
    
    void setInc(int n){
        inc = n;
    }
    
    void setDelColor(Color in){
        delcolor = in;
    }
    
    void setVorColor(Color in){
        vorcolor = in;
    }
    
    void setDelaunay(){
        drawDelaunay = true;
        drawVoronoi = false;
    }
    
    void setVoronoi(){
        drawVoronoi = true;
        drawDelaunay = false;
    }
    
    void setDelaunay(boolean in){
        drawDelaunay = in;
        drawVoronoi = !in;
    }
    
    void setVoronoi(boolean in){
        drawDelaunay = !in;
        drawVoronoi = in;
    } 
    
    double getL(byte[] in){  //3 index byte 
        return ((in[0] & 0x000000ff) + (in[1] & 0x000000ff) + (in[2] & 0x000000ff) )/255.0;
    } 
    
    int getNodes(){
        return nodes;
    }
    
    float tscale(float in, float div){     
       return 1-(float) Math.min(in/(double)div, 1);
    }
    
    BufferedImage clone(BufferedImage img){
        return new BufferedImage(img.getColorModel(), img.copyData(null), img.isAlphaPremultiplied(), null);
    }
    
    public static BufferedImage cloneScale(BufferedImage imageToScale, int width, int height) {  
        BufferedImage scaledImage = new BufferedImage(width, height, imageToScale.getType());
        Graphics2D graphics2D = scaledImage.createGraphics();
        graphics2D.drawImage(imageToScale, 0, 0, width, height, null);
        graphics2D.dispose();
        return scaledImage;
    }
    
  
/*------------------------------------------------------*/
    
    abstract void setImg(BufferedImage in);
        
    abstract void proc();   
    
    abstract void draw(Graphics g);
    
    abstract BufferedImage getImage();
    
}
