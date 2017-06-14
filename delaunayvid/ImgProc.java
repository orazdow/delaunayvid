package delaunayvid;

import delaunay.Delaunay;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.Random;


public class ImgProc {

int height, width, xdiv, ydiv, div, totalpix;  
BufferedImage img, bkgd;
Raster raster;
Random rand = new Random();
Gui gui;
Delaunay d = new Delaunay();
boolean delaunayInit = false;
boolean seed = true;
double variance;// = 0.15;
double band;// = 1.75;
double thresh;// = 0.005;
int nodes = 0;
int inc = 1;
Color bkgdcolor = Color.black; 
Color dotcolor = Color.white; 
Color delcolor = Color.white;
Color vorcolor = Color.red;
boolean drawbkgd = true;
boolean extraAa = false;
Graphics2D g;

    ImgProc(){
        
    }
    ImgProc(BufferedImage in){
        setImg(in);
    }
    
    void setImg(BufferedImage in){
          img = in;
          width = img.getWidth();
          height = img.getHeight();
          bkgd = new BufferedImage(img.getColorModel(), img.copyData(null), img.isAlphaPremultiplied(), null);
          if(!delaunayInit){
             d.setSize(img.getWidth(), img.getHeight());
//              d.drawDelaunay(false);
//              d.drawVoronoi(true);
//                d.setDelaunayColor(Color.red);
//                d.setVoronoiColor(Color.white);
              delaunayInit = true;
          }
    }
    
    void getVals(Gui g){
        band = g.bandVal();
        variance = g.varValue();
        thresh = g.threshValue();
        gui = g;
    }
    
//    void updateColors(Gui g){
//        
//    }
    
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
    
    BufferedImage proc(BufferedImage in){
        setImg(in);
        proc();
        return bkgd;
    }
    
    void setDelaunay(){
        d.setDrawDelaunay(true);
        d.setDrawVoronoi(false);
    }
    void setVoronoi(){
        d.setDrawDelaunay(false);
        d.setDrawVoronoi(true);
    }
    
    void proc(){
       
       raster = bkgd.getRaster();
       g = img.createGraphics();

       g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
       
       if(extraAa)
       g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

       g.setColor(bkgdcolor);
       if(drawbkgd)
       g.fillRect(0, 0, width, height);
       g.setColor(dotcolor);

      d.reset();
      if(seed){
          rand.setSeed(99);
      }
        
        for (int y = 0; y < height; y+= inc) {
             for (int x = 0; x < width; x+= inc){ 
                 double n = getL((byte[])raster.getDataElements(x, y, null));
                 if( Math.pow( (((rand.nextDouble()*variance)+band) - n),2 ) < thresh ){
                     if(gui.mode == Gui.Mode.dots){
                         g.drawLine(x, y, x, y);
                     }else{
                      d.addPoint(x, y);
                     }
                   
                 }
             }
        }

        if(gui.mode == Gui.Mode.dots){
         g.dispose();
        }else{
         d.draw(g);
        }
       
    }
    
    double getL(byte[] in){  //3 index byte 

        return ((in[0] & 0x000000ff) + (in[1] & 0x000000ff) + (in[2] & 0x000000ff) )/255.0;

    }  
    BufferedImage getImage(){
        return img;
    }
    
    
    int getNodes(){
        return nodes;
    }
    
    
}
