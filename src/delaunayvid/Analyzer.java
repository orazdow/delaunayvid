package delaunayvid;

import java.awt.image.WritableRaster;
import java.awt.Color;

public class Analyzer {
    
    boolean writePixels = true;
    double outval = 0;
    int outvals[] = new int[]{0,0,0};
  //  int ioutvals[] = new int[]{255, 255, 255};
    WritableRaster writeraster;
    float rcoef = 1, gcoef = 1, bcoef = 1;
    int red = new Color(193, 141, 149).getRGB();//Color.RED.getRGB();
    
    void setWriteRaster(WritableRaster r){
        this.writeraster = r;
    }
    
    void analyze(int x, int y, WritableRaster raster){
       // int val = getH((byte[])raster.getDataElements(x, y, null));
        outval = pow(cDiff((byte[])raster.getDataElements(x, y, null), red), 2);
     //   outval = cDiff((byte[])raster.getDataElements(x, y, null), red);
   //   outval = getL((byte[])raster.getDataElements(x, y, null));
     // int val = shift(getL((byte[])raster.getDataElements(x, y, null)),170);
        if(writePixels){
          //  setVals(outval/3.0, outval/3.0, outval/3.0);
         //   setVals(5,10,50);
         //   writePixel(writeraster, x, y, new double[]{outval*85,outval*85,outval*85});
        }
    }

    private void setVals(int r, int g, int b){
        outvals[0] = r;
        outvals[1] = g;
        outvals[2] = b;
    } 
    
    private void writePixel(WritableRaster raster, int x, int y, double[] vals){
        raster.setPixel(x, y, vals);
    } 
    private void writePixel(WritableRaster raster, int x, int y, int[] vals){
        raster.setPixel(x, y, vals);
    }     
//    private int getL(byte[] in){  //3 index byte 
//        return ((int)((in[0] & 0x000000ff)*rcoef) + (int)((in[1] & 0x000000ff)*gcoef) + (int)((in[2] & 0x000000ff)*bcoef) )/3;
//    }
    double getL(byte[] in){  //3 index byte 
//        return ((in[0] & 0x000000ff) + (in[1] & 0x000000ff) + (in[2] & 0x000000ff) )/3.0;
    return ((in[0] & 0x000000ff) + (in[1] & 0x000000ff) + (in[2] & 0x000000ff) )/255.0;
    } 
    
    int cDiff(byte[] in, int r, int g, int b){
        return Math.abs((int)in[0] - r)+ Math.abs((int)in[1] - g)+ Math.abs((int)in[2] - b);
    }
    
    double cDiff(byte[] in, int rgb){
        int r = ((rgb & 0xff0000) >> 16);
        int g = ((rgb & 0xff00) >> 8);
        int b = (rgb & 0xff) ;
        return Math.sqrt(((int)in[0] - r)*((int)in[0] - r)+((int)in[1] - g)*((int)in[1] - g)+((int)in[2] - b)*((int)in[2] - b))/255.0;
     //   return (int)((Math.abs((int)in[0] - r)+Math.abs((int)in[1] - g)+Math.abs((int)in[2] - b))/3.0);

    }    
    int getH(byte[] in){
        return getHue((int)in[0], (int)in[1], (int)in[2]);
    }
    double pow(double in, int pow){
        return Math.pow(in/3.0, pow)*3*pow;
    }
    int shift(int in, int shift){
        return (in+shift)%255;
    }
    private int getHue(int red, int green, int blue) {

    float min = Math.min(Math.min(red, green), blue);
    float max = Math.max(Math.max(red, green), blue);

    if (min == max) {
        return 0;
    }

    float hue = 0f;
    if (max == red) {
        hue = (green - blue) / (max - min);

    } else if (max == green) {
        hue = 2f + (blue - red) / (max - min);

    } else {
        hue = 4f + (red - green) / (max - min);
    }

    hue = hue * 60;
    if (hue < 0) hue = hue + 360;

    return Math.round(hue);
}
//    private void getL(byte[] in){  //3 index byte 
//        return ((double)(in[0] & 0x000000ff) + (double)(in[1] & 0x000000ff) + (double)(in[2] & 0x000000ff) )/255.0;
//    } 
    
}
