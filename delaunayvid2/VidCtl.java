package delaunayvid2;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaToolAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IAddStreamEvent;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import java.awt.image.BufferedImage;
import java.io.File;


public class VidCtl extends MediaToolAdapter implements Runnable{
    
    IMediaReader reader;
    boolean image1st = false;
    long count = 0;
    long next = 0;
    long totalframes;
    long framerate = 33;
    boolean go = true;
    boolean ff = false;
    BufferedImage img;
    Gui g;
    File infile;
    long currentframe = 0;
    
    VidCtl(File infile, Gui g){
        this.infile = infile;
        reader = ToolFactory.makeReader(infile.toString());
        reader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
        reader.addListener(this);
        this.g = g;
        getFrame(true);
    }
    
    @Override
    public void onVideoPicture(IVideoPictureEvent event){
            next++; 
            img = event.getImage(); 
            if(!image1st){
               g.initDisplay(img);
               image1st = true;
            }
            g.updateImg(img);
            currentframe++;
            if(currentframe > totalframes){
                currentframe = 0;
            }
            g.updateFrameNum(currentframe, totalframes);
        
      //  super.onVideoPicture(event);
    }
    
    @Override
    public void onAddStream(IAddStreamEvent event){
       totalframes = event.getSource().getContainer().getStream(0).getNumFrames();
       framerate = Math.round(1000/event.getSource().getContainer().getStream(0).getFrameRate().getDouble());
       
    }
    
    
    boolean getFrame(boolean stop){
        boolean rtn = true;
        while(count == next){ 
            if(reader.readPacket() != null){
                if(stop){rtn = false;}
            }
        }
        count = next;
        return rtn;
    }
    
    void rewind(){
        reader = ToolFactory.makeReader(infile.toString());
        reader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
        reader.addListener(this);
        getFrame(true);
    }
    
    void stop(){
        go = false;
    }
    
    
    void play() throws InterruptedException{
        
        while(go){
              //if(!go){ wait();}
                getFrame(false);
                if(!ff){
                Thread.sleep(framerate);
                }else{
               // Thread.sleep(1);
                }

        }   
        ff = false;
        
    }
    

    @Override
    public void run() {
        try {
            play();
        } catch (InterruptedException ex) {
            System.err.println(ex.getMessage());
        }

    }
    
}
