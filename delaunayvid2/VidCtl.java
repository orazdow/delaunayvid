package delaunayvid2;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.MediaToolAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IAddStreamEvent;
import com.xuggle.mediatool.event.IAudioSamplesEvent;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import java.awt.image.BufferedImage;
import java.io.File;


public class VidCtl extends MediaToolAdapter implements Runnable{
    
    IMediaReader reader;
    IMediaWriter writer;
    boolean image1st = false;
    long count = 0;
    long next = 0;
    long totalframes;
    long framerate = 33;
    boolean go = false;
    boolean ff = false;
    boolean converting = false;
    BufferedImage img;
    Gui g;
    File infile, outfile;
    long currentframe = 0;
    
    VidCtl(File infile, Gui g){
        this.infile = infile;
        reader = ToolFactory.makeReader(infile.toString());
        reader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
        reader.addListener(this);
        this.g = g;
        getFrame(true);
    }
    
    void setWriter(File file){
     converting = true; 
     stop();
     rewind();
     outfile = file;   
     writer = ToolFactory.makeWriter(outfile.toString(), reader); 
     addListener(writer);
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
        if(converting)
        super.onVideoPicture(event);
    }
    
    @Override
    public void onAudioSamples(IAudioSamplesEvent event){ //System.out.println("a");
        if(converting)
        super.onAudioSamples(event);
    }
    
    @Override
    public void onAddStream(IAddStreamEvent event){
       totalframes = event.getSource().getContainer().getStream(0).getNumFrames();
       framerate = Math.round(1000/event.getSource().getContainer().getStream(0).getFrameRate().getDouble());
       
    }
    
    
    boolean getFrame(boolean stop){
        boolean rtn = true;
        while(count == next){ 
            if(reader.readPacket() != null){ go = false; break;
               // if(stop){rtn = false; }
            }
        }
        count = next;
        return rtn;
    }
    
    void getFrame2(){
        if(reader.readPacket() != null){
            go = false;
        }
    }
    
    void rewind(){
        reader = ToolFactory.makeReader(infile.toString());
        reader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
        reader.addListener(this);
        currentframe = 0;
        if(!converting)
        getFrame(true);
    }
    
    void stop(){
        go = false;
    }
    
    
    void play() throws InterruptedException{
        
        while(go){
              //if(!go){ wait();}
                getFrame(converting);
           //  getFrame2();
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
