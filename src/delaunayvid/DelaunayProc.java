package delaunayvid;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import delaunay.Triangle;
import delaunay.Delaunay;
import java.awt.Color;


public class DelaunayProc extends ProcHandler{
    
    Delaunay d;  

    public DelaunayProc(Gui gui, ImgProc proc){
        this.gui = gui; 
        this.proc = proc;
        d = new Delaunay();
    }
    

    @Override
    public void init(BufferedImage img) {
        d.setSize(img.getWidth(), img.getHeight());
        initialized = true;
    }
    
    @Override
    public void reset(){
        d.reset();
    }

    @Override
    public void analyze(int x, int y) {
        if(gui.doubleRez){
            d.addPoint(x*2, y*2);
        }else{d.addPoint(x, y);}
    }

    @Override
    public void draw(Graphics g) {
        for(Triangle t : d.triangles.triangles.values()){
            d.setNeighbors(t);           
            if(!t.boundary && proc.drawDelaunay){
                if(proc.ignore){ 
                  //  g.setColor(Color.getHSBColor(tscale(t.r,trilim),1, tscale(t.r,100))); 
                  int c = proc.delcolor.getRGB();
                  g.setColor(new Color(getR(c), getG(c), getB(c), tscale(t.r,proc.trilim)));
                }else{
                    g.setColor(proc.delcolor); 
                }
                g.drawPolygon(new int[] {(int)t.a.x, (int)t.b.x, (int)t.c.x}, new int[] {(int)t.a.y, (int)t.b.y, (int)t.c.y}, 3);             
            }
            if(proc.drawVoronoi){
                g.setColor(proc.vorcolor);
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

    private float tscale(float in, float div){     
       return 1-(float) Math.min(in/(double)div, 1);
    }    
}
