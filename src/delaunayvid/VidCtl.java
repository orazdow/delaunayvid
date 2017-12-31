package delaunayvid;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.MediaToolAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IAddStreamEvent;
import com.xuggle.mediatool.event.IAudioSamplesEvent;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.mediatool.event.VideoPictureEvent;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IStreamCoder;
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
    Integer newWidth = null, newHeight = null;
    ScaleListener scaleListener;
    
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
        g.updateFrameNum(0, totalframes);
        outfile = file;   
        writer = ToolFactory.makeWriter(outfile.toString(), reader); 
        scaleListener = new ScaleListener(newWidth, newHeight);
        writer.addListener(scaleListener);
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
            
            img = g.updateImg(img);
            currentframe++;
            if(currentframe > totalframes){
                currentframe = 0;
            }
            g.updateFrameNum(currentframe, totalframes);
            
        if(converting){
            if(g.doubleRez){
                if(!g.procview){ img = Proc.cloneScale(img, newWidth, newHeight); }
                IVideoPictureEvent e = new VideoPictureEvent(event.getSource(), img, event.getTimeStamp(), event.getTimeUnit(), event.getStreamIndex());
                super.onVideoPicture(e);
            }else{
                super.onVideoPicture(event);
            }
        }
    }
    
    @Override
    public void onAudioSamples(IAudioSamplesEvent event){ 
        if(converting)
        super.onAudioSamples(event);
    }
    
    @Override
    public void onAddStream(IAddStreamEvent event){
        newWidth = event.getSource().getContainer().getStream(0).getStreamCoder().getWidth();
        newHeight = event.getSource().getContainer().getStream(0).getStreamCoder().getHeight();
        if(g.doubleRez){ newWidth *= 2; newHeight *= 2; }
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
    

public class ScaleListener extends MediaToolAdapter {
	private int width, height;
 
	public ScaleListener(Integer width, Integer height) {
		this.width = width;
		this.height = height;
	}
 
	@Override
	public void onAddStream(IAddStreamEvent event) {
		int streamIndex = event.getStreamIndex();
		IStreamCoder streamCoder = event.getSource().getContainer().getStream(streamIndex).getStreamCoder();
		if (streamCoder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO) {
		} else if (streamCoder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
			streamCoder.setWidth(width);
			streamCoder.setHeight(height);
		}
		super.onAddStream(event);              
	}
 
}
    
}
