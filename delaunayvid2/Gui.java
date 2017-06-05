package delaunayvid2;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Gui extends JFrame implements ChangeListener{
    JSlider band;
    JSlider variance;
    JSlider thresh;
    JCheckBox seedCheck, skipCheck, viewdots;
    JButton openFile, nextframe, play, stop, back, seek, build;
    JFileChooser chooser;
    File infile;
    VidCtl vid;
    ImgProc p;
    JLabel bandval, varval, threshval, nodecount;
    int nodes;
    boolean seed = true;
    Display display = null;
    Thread t = null;
    boolean threadstarted = false;
    BufferedImage img, pimg;
    boolean dotview = false;
    
    Gui(int w, int h){
        
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        setSize(w,h);
        
        chooser = new JFileChooser();
        
        add(Box.createVerticalStrut(30));
        add(new JLabel("band"));
        band = new JSlider(0, 3000);
        band.addChangeListener(this);
        add(band);
        // band.setValue(1);
        bandval = new JLabel(String.valueOf(bandVal()));
        add(bandval);
        
      
        add(Box.createVerticalStrut(10));
        add(new JLabel("variance"));
        variance = new JSlider(0,2000);
        variance.addChangeListener(this);
        add(variance);
        //variance.setValue(1);
        varval = new JLabel(String.valueOf(varValue()));
        add(varval);
        
        add(Box.createVerticalStrut(10));
        add(new JLabel("threshold"));
        thresh = new JSlider(1,1500);
        thresh.addChangeListener(this);
        add(thresh);
        //thresh.setValue(1);
        threshval = new JLabel(String.valueOf(threshValue()));
        add(threshval);
        
        add(Box.createVerticalStrut(20));
        
        JPanel checks = new JPanel(new FlowLayout());
        add(checks);
        
        seedCheck = new JCheckBox("Seed Noise");
        seedCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                seed = !seed;
                p.setSeed(seed);
            }
        });
        seedCheck.setSelected(true);
        checks.add(seedCheck);
        
        skipCheck = new JCheckBox("Skip Pixels"); 
        skipCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(skipCheck.isSelected()){
                    p.setInc(2);
                }else{
                    p.setInc(1);
                }
             //   p.proc();
             //   updateNodes();
              //  d.repaint();
            }
        });
        checks.add(skipCheck); 
        
       // add(Box.createVerticalStrut(20));
        nodecount = new JLabel("Node Count: "+nodes);
        checks.add(nodecount);
        
        
        JPanel buildrow = new JPanel(new FlowLayout());
        add(buildrow);
        
        viewdots = new JCheckBox("View Dots");
        viewdots.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(viewdots.isSelected()){
                    dotview = true;
                }else{
                    dotview = false;
                }
            }
        });
        //viewdots.setSelected(true);
        buildrow.add(viewdots);
        
        build = new JButton("Build");
        build.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                    if(vid != null){
//                       p.setImg(img);
//                       p.proc();
//                       display.update(p.getImage());
                       display.update(imgProc(img));
                       updateNodes();
                    }
            }
        });
        buildrow.add(build);
        
    //    add(Box.createVerticalStrut(20));   
        JPanel vidctls = new JPanel(new FlowLayout());
        add(vidctls);
        
        openFile = new JButton("Open File");
        openFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              openFile();
            }
        });
        vidctls.add(openFile, FlowLayout.LEFT);
        
        
        play = new JButton("Play");
        play.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(vid != null){
                    if(t == null || !t.isAlive()){
                    t = new Thread(vid);
                    t.start();
                    vid.go = true;
                    }else{
                        vid.ff = false;
                    }
                }
            }
        });
        vidctls.add(play);
        
       
        back = new JButton("Back");
        back.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                 if(vid != null){
                   if(vid.go){  stop(); }
                     vid.rewind();
                 }
            }
        });
        vidctls.add(back);
        
        stop = new JButton("Stop");
        stop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                 if(vid != null){
                    stop();
                 }
            }
        });
        vidctls.add(stop);   
        
        nextframe = new JButton("Frame");
        nextframe.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
             if(vid != null){
                 if(vid.go){vid.stop();}
                 vid.getFrame(true);
             }
            }
        });
        vidctls.add(nextframe);

        seek = new JButton("FF");
        seek.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(vid != null){
                    if(t == null || !t.isAlive()){
                    t = new Thread(vid);
                    vid.ff = true;
                    t.start();
                    vid.go = true;
                    }else{
                        vid.ff = true;
                    }
                }
            }
        });
        vidctls.add(seek);  
        
        p = new ImgProc();
        p.getVals(this);
        
                       
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        setVisible(true);       
    }
    
    void stop(){
        vid.go = false;
        try {
           if(t != null) 
           t.join();
        } catch (InterruptedException ex) {
            System.err.println(ex.getMessage());
        }
    }
    
     
    void openFile(){ 
         int rtn = chooser.showOpenDialog(this); 
         if (rtn == JFileChooser.APPROVE_OPTION){
             infile = chooser.getSelectedFile();
             if(display != null){
               stop();
               display.dispose();
             }
             vid = new VidCtl(infile, this);
           //  t = new Thread(vid);
         }
         
    }
    
    BufferedImage imgProc(BufferedImage img){
            p.setImg(img);
            p.proc();
            return p.getImage();
    }
    

    
    void updateNodes(){
       nodecount.setText("Node Count: "+String.valueOf(p.getNodes()));
    }
        
    double bandVal(){
        return band.getValue()/1000.0;
    }
    
    double varValue(){
        return variance.getValue()/1000.0;
    }
    
    double threshValue(){
        return thresh.getValue()/1000000.0;
    }
    
    @Override
    public void stateChanged(ChangeEvent e){
       if(e.getSource() != null){
       JSlider source = (JSlider)e.getSource();    
       if(source == band){
           double b = source.getValue()/1000.0;
           bandval.setText(String.valueOf(b));
           p.setBand(b);
//           p.proc();
//           d.repaint();         
       }
       if(source == variance){
           double v = source.getValue()/1000.0;
           varval.setText(String.valueOf(v));
           p.setVariance(v);
//           p.proc();
//           d.repaint();         
       }
       if(source == thresh){
           double t = source.getValue()/1000000.0;
           threshval.setText(String.valueOf(t));
           p.setThresh(t);
//           p.proc();
//           d.repaint();         
       }
       }
       updateNodes();
       
    }
    
 
    
    void initDisplay(BufferedImage img){
        display = new Display(img);
    }
    
    void updateImg(BufferedImage img){
        this.img = img;
        if(dotview){
            display.update(imgProc(img));
            updateNodes();
        }else{
            display.update(img);
        }    
    }
    
    public class Display extends JFrame{
     
        ImageIcon icon;
            
        Display(BufferedImage img){
        Gui.this.img = img;
        pimg = new BufferedImage(img.getColorModel(), img.copyData(null), img.isAlphaPremultiplied(), null);
        setSize(img.getWidth(), img.getHeight());
        icon = new ImageIcon(img);
        JLabel label = new JLabel(icon);
        add(label); 
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent event) {
          Gui.this.stop();
          dispose();
        }
        });
        setVisible(true);     
        }
        
        void update(BufferedImage img){
            icon.setImage(img);
            repaint();
        }
    }
    
}
