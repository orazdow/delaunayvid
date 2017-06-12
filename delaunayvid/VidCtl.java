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
        getFrame();
    }
    
    void setWriter(File file){
     converting = true; 
//     stop();
//     rewind();
     g.updateFrameNum(0, totalframes);
     outfile = file;   
     writer = ToolFactory.makeWriter(outfile.toString(), reader); 
     addListener(writer);
    }
    
    void rmvWriter(){
        removeListener(writer);
        outfile = null;
        g.convmsg.setText("Done");
        g.updateFrameNum(totalframes, totalframes);
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
    public void onAudioSamples(IAudioSamplesEvent event){ 
        if(converting)
        super.onAudioSamples(event);
    }
    
    @Override
    public void onAddStream(IAddStreamEvent event){
       totalframes = event.getSource().getContainer().getStream(0).getNumFrames();
       framerate = Math.round(1000/event.getSource().getContainer().getStream(0).getFrameRate().getDouble());
       
    }
    
    
    void getFrame(){
        while(count == next){ 
            if(reader.readPacket() != null){ 
                
                if(converting){
                go = false; 
                converting = false;
                rmvWriter();
                break;
                }
            }
        }
        count = next;
    }
    
    
    void rewind(){
        reader = ToolFactory.makeReader(infile.toString());
        reader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
        reader.addListener(this);
        currentframe = 0;
        if(!converting)
        getFrame();
    }
    
    void stop(){
        go = false;
    }
    
    
    void play() throws InterruptedException{
        
        while(go){
                getFrame();
                if(!ff){
                Thread.sleep(framerate);
                }else{
               // Thread.sleep(1);
                }

        }   
        
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
