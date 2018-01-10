package delaunayvid;

import delaunay.Delaunay;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;

public abstract class Proc {
    
Gui gui;
Delaunay d = new Delaunay();  
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
        d.setDelaunayColor(in);
    }
    
    void setVorColor(Color in){
        vorcolor = in;
        d.setVoronoiColor(in);
    }
    
    void setDelaunay(){
        drawDelaunay = true;
        drawVoronoi = false;
        d.setDrawDelaunay(true);
        d.setDrawVoronoi(false);
    }
    
    void setVoronoi(){
        drawVoronoi = true;
        drawDelaunay = false;
        d.setDrawDelaunay(false);
        d.setDrawVoronoi(true);
    }
    
    void setDelaunay(boolean in){
        drawDelaunay = in;
        drawVoronoi = !in;
        d.setDrawDelaunay(drawDelaunay);
        d.setDrawVoronoi(drawVoronoi);
    }
    
    void setVoronoi(boolean in){
        drawDelaunay = !in;
        drawVoronoi = in;
        d.setDrawDelaunay(drawDelaunay);
        d.setDrawVoronoi(drawVoronoi);
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
    
    static float getR(int in){
        return ((in & 0xff0000) >> 16)/(float)255;
    }
    static float getG(int in){
        return ((in & 0xff00) >> 8)/(float)255;
    }
    static float getB(int in){
        return (in & 0xff)/(float)255;
    }        
//     BufferedImage cloneScale(BufferedImage imageToScale, int width, int height) {  
//         WritableRaster a = imageToScale.getData().createCompatibleWritableRaster(width, height);
//         ColorModel c = imageToScale.getColorModel();
//         BufferedImage scaledImage = new BufferedImage(c, a, c.isAlphaPremultiplied(), null); 
//         return scaledImage;
//    }     
/*------------------------------------------------------*/
    
    abstract void setImg(BufferedImage in);
        
    abstract void proc();   
    
    abstract void draw(Graphics g);
    
    abstract BufferedImage getImage();
    
}
