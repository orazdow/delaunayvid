package delaunayvid;

import delaunay.Triangle;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;


public class ImgProc extends Proc{
    
    BufferedImage a_img, draw_img;
    Raster a_raster;
    Graphics2D g;
    int trilim = 20;
 

    @Override
    void setImg(BufferedImage img){
        a_img = clone(img);
        width = img.getWidth();
        height = img.getHeight();

        draw_img =  gui.doubleRez ? cloneScale(img, width*2, height*2) : img;
        g = draw_img.createGraphics();
        
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        if(extraAa)
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
       // g.setStroke(new BasicStroke(3));
        if(!delaunayInit){
           d.setSize(img.getWidth(), img.getHeight());
           delaunayInit = true;
        }
    }
        
    
    @Override
    void proc(){
        
        a_raster = a_img.getRaster();        
        d.reset(); 
        if(seed){ rand.setSeed(99); }
        
        if(drawbkgd){
            g.setColor(bkgdcolor);
            if(gui.doubleRez){
                g.fillRect(0, 0, width*2, height*2);
            }else{ g.fillRect(0, 0, width, height); }
        }
        g.setColor(dotcolor);
        
        for (int y = 0; y < height; y+= inc){
            for (int x = 0; x < width; x+= inc){
                double n = getL((byte[])a_raster.getDataElements(x, y, null));
                if( Math.pow( (((rand.nextDouble()*variance)+band) - n),2 ) < thresh ){
                    if(gui.mode == Gui.Mode.dots){
                        if(gui.doubleRez){
                        g.drawLine(x*2, y*2, x*2, y*2);
                        }else{ g.drawLine(x, y, x, y); }
                     }else{
                        if(gui.doubleRez){
                        d.addPoint(x*2, y*2);
                        }else{ d.addPoint(x, y); }
                     }
                }
            }
        }
        
        if(gui.mode == Gui.Mode.dots){
            g.dispose();
        }else{
            draw(g);
        }        
    }   
    
    @Override
    void draw(Graphics g){       
        
        for(Triangle t : d.triangles.triangles.values()){
            d.setNeighbors(t);           
            if(!t.boundary && drawDelaunay){
                if(ignore){
                    g.setColor(Color.getHSBColor(tscale(t.r,trilim),1, tscale(t.r,100))); 
//                  int c = delcolor.getRGB();
//                  g.setColor(new Color(getR(c), getG(c), getB(c), tscale(t.r,trilim)));
                }else{
                    g.setColor(delcolor); 
                }
                g.drawPolygon(new int[] {(int)t.a.x, (int)t.b.x, (int)t.c.x}, new int[] {(int)t.a.y, (int)t.b.y, (int)t.c.y}, 3);             
            }
            
            if(drawVoronoi){
                g.setColor(vorcolor);
                try
                {                   
                    g.drawLine((int)t.center.x, (int)t.center.y, (int)t.va.center.x, (int)t.va.center.y);
                    g.drawLine((int)t.center.x, (int)t.center.y, (int)t.vb.center.x, (int)t.vb.center.y);
                    g.drawLine((int)t.center.x, (int)t.center.y, (int)t.vc.center.x, (int)t.vc.center.y);                 
                }
                catch(NullPointerException e){}
            }
            
        }
        g.dispose();
    }
    
    @Override
    BufferedImage getImage(){
        return draw_img;
    }
    
    
}
