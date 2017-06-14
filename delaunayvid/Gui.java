package delaunayvid;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
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
    JRadioButton dots, delaunay, voronoi;
    ButtonGroup radiogroup;
    File infile;
    VidCtl vid;
    ImgProc p;
    JLabel bandval, varval, threshval, nodecount, framenum, convmsg;
    int nodes;
    boolean seed = true;
    Display display = null;
    Thread t = null;
    boolean threadstarted = false;
    BufferedImage img, bkgd;
    boolean procview = false;
    boolean dividegain = false;
    double gaindiv = 8;
    boolean colorpanelopen = false;
    ColorPanel cpanel;
    public enum Mode{
        dots,delaunay,voronoi
    }
    public Mode mode;
    
    
    Gui(int w, int h){
        
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        setSize(w,h);
        
        mode = Mode.delaunay;
                
        chooser = new JFileChooser();
        
        add(Box.createVerticalStrut(30));
        add(new JLabel("Band"));
        band = new JSlider(0, 3000);
        band.addChangeListener(this);
        add(band);
        // band.setValue(1);
        bandval = new JLabel(String.valueOf(bandVal()));
        add(bandval);
        
      
        add(Box.createVerticalStrut(10));
        add(new JLabel("Variance"));
        variance = new JSlider(0,2000);
        variance.addChangeListener(this);
        add(variance);
        //variance.setValue(1);
        varval = new JLabel(String.valueOf(varValue()));
        add(varval);
        
        add(Box.createVerticalStrut(10));
        add(new JLabel("Gain"));
        thresh = new JSlider(1,1500);
        thresh.addChangeListener(this);
        add(thresh);
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

            }
        });
        checks.add(skipCheck); 
        
        dots = new JRadioButton("Dots");
        delaunay = new JRadioButton("Delaunay");
        voronoi = new JRadioButton("Voronoi");
        radiogroup = new ButtonGroup();
        radiogroup.add(dots);
        radiogroup.add(delaunay);
        radiogroup.add(voronoi);
        delaunay.setSelected(true);
        dots.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mode = Mode.dots;
                if(procview)
                stoppedUpdate();
            }
        });
        delaunay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mode = Mode.delaunay;
                p.setDelaunay();
                if(procview)
                stoppedUpdate();
            }
        });
        voronoi.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mode = Mode.voronoi;
                p.setVoronoi();
                if(procview)
                stoppedUpdate();
            }
        });
        checks.add(dots);
        checks.add(delaunay);
        checks.add(voronoi);
                
        JPanel buildrow = new JPanel(new FlowLayout());
        add(buildrow);
        
        
        viewdots = new JCheckBox("Process");
        viewdots.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(viewdots.isSelected()){
                    procview = true;
                    if(vid != null)
                    vid.ff = true;
                    bkgd = p.bkgd; 
                    if(bkgd == null){
                        bkgd = img;
                    }
                    if(vid != null && !vid.go){ 
                    display.update(imgProc(bkgd));
                    }
                }else{
                    procview = false; 
                    if(vid != null)
                    vid.ff = false;
                    if(vid != null && !vid.go)
                    display.update(p.bkgd);
                }
            }
        });
        //viewdots.setSelected(true);
        buildrow.add(viewdots, FlowLayout.LEFT);
        
        final JCheckBox gaindiv = new JCheckBox("Low Gain");
        gaindiv.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dividegain = gaindiv.isSelected();
                threshfunc(thresh.getValue());
                if(vid != null){
                    if(!vid.go && procview){
                        stoppedUpdate();
                    }
                }
            }
        });
       // gaindiv.setSelected(true);
        buildrow.add(gaindiv);
        
        JButton outfile = new JButton("Select output file");
        outfile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectSaveFile();
            }
        });
        buildrow.add(outfile);
        
        JButton setcolors = new JButton("Set Colors");
        setcolors.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openColorFrame();
            }
        });
        buildrow.add(setcolors);
        
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
                        if(!procview)
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
                    if(!vid.converting){ 
                   if(vid.go){ 
                      stop(); 
                     }
                     vid.rewind();
                    }
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
                 vid.getFrame();
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
        
    //    add(Box.createVerticalStrut(20));   
        JPanel framemsg = new JPanel(new FlowLayout());
        add(framemsg);
        
        framenum = new JLabel();
        framemsg.add(framenum);
        
        convmsg = new JLabel();
        framemsg.add(convmsg);
        
        p = new ImgProc();
        p.getVals(this);
               
        
        thresh.setValue(90);               
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
    
    void selectSaveFile(){
        if(vid == null){return;}
        stop(); 
        int rtn = chooser.showSaveDialog(this);
        if(rtn == JFileChooser.APPROVE_OPTION){

            File save = chooser.getSelectedFile();
            if(vid != null){
                vid.rewind();
                vid.setWriter(save);
                convmsg.setText("Convert Mode...");
            }
        }
    }
    
    BufferedImage imgProc(BufferedImage img){
            p.setImg(img);
            p.proc();
            return p.getImage();
    }
    
    void stoppedUpdate(){
        bkgd = p.bkgd; 
        if(bkgd == null){bkgd = img;}                      
        if(vid != null && !vid.go){ 
         display.update(imgProc(bkgd));
        }             
                  
    }
    
    void openColorFrame(){
        cpanel = new ColorPanel();
    }
    
    void updateFrameNum(long current, long total){
        framenum.setText("Frame: "+current+" / "+total);
    }
            
    double bandVal(){
        return band.getValue()/1000.0;
    }
    
    double varValue(){
        return variance.getValue()/1000.0;
    }
    
    double threshValue(){
        return dividegain ? thresh.getValue()/(gaindiv*1000000.0) : thresh.getValue()/1000000.0;
    }
    
    @Override
    public void stateChanged(ChangeEvent e){
       if(e.getSource() != null){
       JSlider source = (JSlider)e.getSource();    
       if(source == band){
           double b = source.getValue()/1000.0;
           bandval.setText(String.valueOf(b));
           p.setBand(b);        
       }
       if(source == variance){
           double v = source.getValue()/1000.0;
           varval.setText(String.valueOf(v));
           p.setVariance(v);      
       }
       if(source == thresh){
           threshfunc(source.getValue());
       }
        if(procview && !vid.go){
            if(mode == Mode.dots || !source.getValueIsAdjusting()){
                stoppedUpdate();
            }
        }
        
       }

    }
    
    void threshfunc(int val){
            double t = val/1000000.0;
           if(dividegain){ t/=gaindiv;}
           threshval.setText(String.valueOf(t));
           p.setThresh(t); 
    }
    
    void initDisplay(BufferedImage img){
        display = new Display(img);
    }
    
    void updateImg(BufferedImage img){
        this.img = img; 
        if(procview){
            display.update(imgProc(img));
        }else{
            display.update(img);
        }    
    }

    
    class ColorPanel extends JFrame{

        JRadioButton dot, del, vor, bkgd;
        ButtonGroup group = new ButtonGroup();
        JCheckBox drawbkgd = new JCheckBox("Draw Background");
        JCheckBox extraAa = new JCheckBox("Extra AntiAliasing");
        
        ColorPanel(){
            Gui.this.colorpanelopen = true;
            setSize(400, 200);
            setLayout(new FlowLayout());
            JPanel panel = new JPanel();
            add(panel);
            JButton delbutton = new JButton("Set");
            delbutton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(dot.isSelected()){
                        Gui.this.p.dotcolor = JColorChooser.showDialog(null, "Dot Color", Gui.this.p.dotcolor);
                    }else if(del.isSelected()){
                        Gui.this.p.setDelColor(JColorChooser.showDialog(null, "Delaunay Color", Gui.this.p.delcolor));
                    }
                    else if(vor.isSelected()){
                         Gui.this.p.setVorColor(JColorChooser.showDialog(null, "Voronoi Color", Gui.this.p.vorcolor));
                    }else if(bkgd.isSelected()){
                        Gui.this.p.bkgdcolor = JColorChooser.showDialog(null, "Background Color", Gui.this.p.bkgdcolor);
                    }
                }
            });
            panel.add(delbutton);
            
            dot = new JRadioButton("Dots");
            del = new JRadioButton("Delaunay");
            vor = new JRadioButton("Voronoi");
            bkgd = new JRadioButton("Background");
            group.add(dot);
            group.add(del);
            group.add(vor);
            group.add(bkgd);
           // del.setSelected(true);
            panel.add(dot);
            panel.add(del);
            panel.add(vor);
            panel.add(bkgd);
            
            JPanel p2 = new JPanel();
            add(p2);
            p2.add(drawbkgd);
            drawbkgd.setSelected(true);
            p2.add(Box.createHorizontalStrut(240));
            drawbkgd.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    p.drawbkgd = drawbkgd.isSelected();
                }
            });
            JPanel p3 = new JPanel();
            add(p3);
            p3.add(extraAa);
            p3.add(Box.createHorizontalStrut(240));
            extraAa.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    p.extraAa = extraAa.isSelected();
                }
            });            
            setVisible(true);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
            addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                Gui.this.colorpanelopen = false;
                dispose();
            }
            });

        }
        
        
    }
    
    public class Display extends JFrame{
     
        ImageIcon icon;
        boolean on = true;
            
        Display(BufferedImage img){
        Gui.this.img = img;
        setSize(img.getWidth(), img.getHeight());
        icon = new ImageIcon(img);
        JLabel label = new JLabel(icon);
        add(label); 
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent event) {
            if(!Gui.this.vid.converting){
                Gui.this.stop(); 
                }
                on = false;
                dispose();
        }
        });
        setVisible(true);     
        }
        
        void update(BufferedImage img){
            if(on){
            icon.setImage(img);
            repaint();
            }
            
        }
    }
    
}
