package delaunayvid;

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
import static javax.swing.JFileChooser.SAVE_DIALOG;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Gui extends JFrame implements ChangeListener{
    JSlider band;
    JSlider variance;
    JSlider thresh;
    JCheckBox seedCheck, skipCheck, viewdots, displaytoggle;
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
    boolean doubleRez = false;
    ColorPanel cpanel;
    public enum Mode{
        dots,delaunay,voronoi
    }
    public Mode mode;
    
    
    Gui(int w, int h){
        
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        setSize(w,h);
        
        mode = Mode.delaunay;
                
        chooser = new JFileChooser() {
        @Override
        public void approveSelection() {
            File f = getSelectedFile();
            if (f.exists() && getDialogType() == SAVE_DIALOG) {
                int result = JOptionPane.showConfirmDialog(this,
                        "The file exists, overwrite?", "Existing file",
                        JOptionPane.YES_NO_CANCEL_OPTION);
                switch (result) {
                    case JOptionPane.YES_OPTION:
                        super.approveSelection();
                        return;
                    case JOptionPane.NO_OPTION:
                        return;
                    case JOptionPane.CLOSED_OPTION:
                        return;
                    case JOptionPane.CANCEL_OPTION:
                        cancelSelection();
                        return;
                }
            }
            super.approveSelection();
        }
    };
        
      //  chooser.setCurrentDirectory(new File(""));
        
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
                    p.setImg(img); 
                    bkgd = p.a_img; 
                    if(vid != null && !vid.go){ 
                    display.update(imgProc(bkgd));
                    }
                }else{
                    procview = false; 
                    if(vid != null)
                    vid.ff = false;
                    if(vid != null && !vid.go)
                    display.update(p.a_img);
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
       
        buildrow.add(gaindiv);
        
        displaytoggle = new JCheckBox("display");
        displaytoggle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(display != null){
                if(displaytoggle.isSelected()){
                    display.setVisible(true);
                    display.on = true;
                }else{
                    display.dispose();
                    display.on = false;
                }
                }
            }
        });
        buildrow.add(displaytoggle);
        

        JButton setcolors = new JButton("Set Colors");
        setcolors.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openColorFrame();
            }
        });
        buildrow.add(setcolors);
        

        JPanel filectls = new JPanel(new FlowLayout());
        add(filectls);   
        
        openFile = new JButton("Open File");
        openFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              openFile();
            }
        });
        filectls.add(openFile, FlowLayout.LEFT);
        
        JButton outfile = new JButton("Select output file");
        outfile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectSaveFile();
            }
        });
        
        filectls.add(outfile);
        
        final JCheckBox doubleRezCheck = new JCheckBox("2x res");
        doubleRezCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {              
                if(vid == null || !vid.converting)
                    doubleRez = doubleRezCheck.isSelected();
            }
        });
        filectls.add(doubleRezCheck);
               

    //    add(Box.createVerticalStrut(20));   
        JPanel vidctls = new JPanel(new FlowLayout());
        add(vidctls);
        
        
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
        
        p = new ImgProc(this);
        p.getVals(this);
               
        cpanel = new ColorPanel();  
        
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
             if(convmsg.getText().equalsIgnoreCase("Done")){
                 convmsg.setText("");
             }
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
    

    void stoppedUpdate(){
        bkgd = p.a_img;
        if(bkgd == null){bkgd = img;}                      
        if(vid != null && !vid.go){ 
         display.update(imgProc(bkgd));
        }             
                  
    }
    
    void openColorFrame(){
        cpanel.setVisible(true);
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
    
    BufferedImage updateImg(BufferedImage _img){
        img = procview? imgProc(_img) : _img;
        display.update(img);
        return img;    
    }
    
    BufferedImage imgProc(BufferedImage img){
            p.setImg(img);
            p.proc();
            return p.getImage();
    }
    
    
    class ColorPanel extends JFrame{

        JRadioButton dot, del, vor, bkgd;
        ButtonGroup group = new ButtonGroup();
        JCheckBox drawbkgd = new JCheckBox("Draw Background");
        JLabel sys = new JLabel();
        JCheckBox trifade = new JCheckBox("fade triangles larger than:");
        JSlider trilim = new JSlider();
        JSlider lineThickness = new JSlider(10, 40, 10);
        JLabel thickNum = new JLabel("1.0");
        JCheckBox rainbowMode = new JCheckBox("Rainbow triangle fade");
        
        ColorPanel(){
            sys.setText("jre: "+System.getProperty("java.version"));
            Gui.this.colorpanelopen = true;
            setSize(400, 400);
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
            del.setSelected(true);
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
                    if(procview && !vid.go){
                        stoppedUpdate();
                    }
                }
            
            });
            
//            JPanel p3 = new JPanel();
//            add(p3);
//            p3.add(extraAa);
//            p3.add(Box.createHorizontalStrut(240));
//            extraAa.addActionListener(new ActionListener() {
//                @Override
//                public void actionPerformed(ActionEvent e) {
//                    p.extraAa = extraAa.isSelected();
//                }
//            });     
            
            JPanel p4 = new JPanel();
            add(p4);
            p4.add(new JLabel("Line Thickness"));
            p4.add(Box.createVerticalStrut(40));
            p4.add(lineThickness);
            p4.add(thickNum);
            
            lineThickness.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    float t = lineThickness.getValue()/10f;
                    p.thickness = t;
                    thickNum.setText(String.valueOf(t)); 
                    if(!(((JSlider)e.getSource()).getValueIsAdjusting()) && procview && !vid.go){
                            stoppedUpdate();                   
                    }
                    
                }
            });
            
            trifade.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    p.trifade = trifade.isSelected();
                    if(procview && !vid.go){
                        stoppedUpdate();
                    }
                }
            });
            
            JPanel p5 = new JPanel();
            add(p5);
            p5.add(Box.createHorizontalStrut(240));
            p5.add(trifade);
//            p5.add(Box.createHorizontalStrut(240));

//            p5.add(Box.createVerticalStrut(40));
            
            trilim = new JSlider(1, 50, 20);
            trilim.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    p.trilim = trilim.getValue();
                    if(!(((JSlider)e.getSource()).getValueIsAdjusting()) && procview && !vid.go){
                        stoppedUpdate();                   
                    }
                }
            });
            p5.add(trilim);
            p5.add(Box.createHorizontalStrut(240));


            JPanel p6 = new JPanel();
            add(p6);
            p6.add(Box.createHorizontalStrut(12));
            p6.add(rainbowMode);
            rainbowMode.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    p.rainbowMode = rainbowMode.isSelected();
                    if(procview && !vid.go){
                        stoppedUpdate();
                    }
                }
            });
            p6.add(Box.createHorizontalStrut(240));
            
            
            JPanel p7 = new JPanel();
            add(p7);
            p7.add(sys);
            
            // setVisible(true);
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
                displaytoggle.setSelected(false);
                dispose();
        }
        @Override
        public void windowOpened(WindowEvent e){
             displaytoggle.setSelected(true);
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
