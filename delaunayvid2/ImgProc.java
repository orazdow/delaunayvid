package delaunayvid2;

import delaunay.Delaunay;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
Delaunay d = new Delaunay();
boolean delaunayInit = false;
boolean seed = true;
double variance;// = 0.15;
double band;// = 1.75;
double thresh;// = 0.005;
int nodes = 0;
int inc = 1;
ArrayList list = new ArrayList();
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
      //  img.copyData(raster);

       g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
     //  g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      // g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      // g.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
       g.setColor(Color.black);
       g.fillRect(0, 0, width, height);
       g.setColor(Color.white);

     // list.clear();
    // int count = 0;
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
                     if(gui.mode == Gui.Mode.dots){
                         g.drawLine(x, y, x, y);
                     }else{
                      d.addPoint(x, y);
                     }
                   
                 }
             }
        }
        //nodes = count; //list.size();
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
    
    class P{
        int x, y;
        
        P(int x, int y){
        this.x = x; this.y = y;
        }
    
    }
    
}
