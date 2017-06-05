package delaunayvid2;

import delaunay.Delaunay;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.Random;


public class ImgProc {

int height, width, xdiv, ydiv, div, totalpix;  
BufferedImage img, bkgd;
Raster raster;
Random rand = new Random();
Gui gui;
Delaunay d;
boolean delaunayInit = false;
boolean seed = true;
double variance;// = 0.15;
double band;// = 1.75;
double thresh;// = 0.005;
int nodes = 0;
int inc = 1;
ArrayList list = new ArrayList();

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
              d = new Delaunay(img.getWidth(), img.getHeight());
//              d.drawDelaunay(false);
//              d.drawVoronoi(true);
              delaunayInit = true;
          }
    }
    
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
    
    BufferedImage proc(BufferedImage in){
        setImg(in);
        proc();
        return bkgd;
    }
    
    void proc(){
       Graphics g = bkgd.createGraphics();
       raster = img.getRaster();
      //  img.copyData(raster);
      g.setColor(Color.black);
      g.fillRect(0, 0, width, height);
      g.setColor(Color.white);
       
     // list.clear();
     int count = 0;
     d.reset();
      if(seed){
          rand.setSeed(99);
      }
        
        for (int y = 0; y < height; y+= inc) {
             for (int x = 0; x < width; x+= inc){ 
                 double n = getL((byte[])raster.getDataElements(x, y, null));
                 //System.out.println(n);
                 if( Math.pow( (((rand.nextDouble()*variance)+band) - n),2 ) < thresh ){
                     //list.add(new P(x, y));
                   //  g.drawLine(x, y, x, y);
                   d.addPoint(x, y);
                   
                 }
             }
        }
        nodes = count; //list.size();
       // g.dispose();
       d.draw(g);
       
    }
    
    double getL(byte[] in){  //3 index byte 

        return ((in[0] & 0x000000ff) + (in[1] & 0x000000ff) + (in[2] & 0x000000ff) )/255.0;

    }  
    BufferedImage getImage(){
        return bkgd;
    }
    
    int getNodes(){
        return nodes;
    }
    
    class P{
        int x, y;
        
        P(int x, int y){
        this.x = x; this.y = y;
        }
    
    }
    
}
