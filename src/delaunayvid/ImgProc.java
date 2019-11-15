package delaunayvid;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;


public class ImgProc extends Proc{
    
    BufferedImage a_img, draw_img;
    WritableRaster a_raster, d_raster;
    Graphics2D g;
    int trilim = 20;
    ProcHandler del;
    Analyzer an = new Analyzer();
    boolean writeAnalyzer = false;
    
    ImgProc(Gui gui){
        this.gui = gui;
        del = new DelaunayProc(gui, this);
    }
 

    @Override
    void setImg(BufferedImage img){
        a_img = clone(img);
        width = img.getWidth();
        height = img.getHeight();

        draw_img =  gui.doubleRez ? cloneScale(img, width*2, height*2) : img;
        g = draw_img.createGraphics();
        if(writeAnalyzer){
            d_raster = draw_img.getRaster();
            an.setWriteRaster(d_raster);
        }
        
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        if(extraAa)
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
       // g.setStroke(new BasicStroke(3));
        if(!del.initialized){
            del.init(img);
        }
    }
        
    
    @Override
    void proc(){
        
        a_raster = a_img.getRaster();        
        del.reset(); 
        if(seed){ rand.setSeed(99); }
        
        if(drawbkgd){
            g.setColor(bkgdcolor);
            if(gui.doubleRez){
                g.fillRect(0, 0, width*2, height*2);
            }else{ g.fillRect(0, 0, width, height); }
        }
        g.setColor(dotcolor);
        g.setStroke(new BasicStroke(thickness));
        
        for (int y = 0; y < height; y+= inc){
            for (int x = 0; x < width; x+= inc){
             //   an.analyze(x, y, a_raster);
              //  d_raster.setPixel(x, y, new int[]{0,255,255});
                double n = getL((byte[])a_raster.getDataElements(x, y, null));
                if( Math.pow( (((rand.nextDouble()*variance)+band) - n),2 ) < thresh ){
                    if(gui.mode == Gui.Mode.dots){
                        if(gui.doubleRez){
                        g.drawLine(x*2, y*2, x*2, y*2);
                        }else{ g.drawLine(x, y, x, y); }
                     }else{
                        del.analyze(x, y);
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
        del.draw(g);
    }
    
    @Override
    BufferedImage getImage(){
        return draw_img;
    }
    
    
}
