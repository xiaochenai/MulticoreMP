package GraphicKeyboard;


import javax.swing.*;

import BackGround.BackGround;
import RandomKeyStroke.RandomKeyStroke;
import VariedLengthMappingTable.VariedLengthMappingTable;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.*;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

class RadialInputButton extends JButton implements Runnable,MouseListener
{
    private double innerDist;
    private double outerDist;
    private double radians;
    private BackGround BG;
    private double radialPosition;
    private Shape hitBox;
    private static double angle =0;
    private static int anglecounter=0;
    private int Speed = 50;
    private static int aa=0;
    private String[] oldKeySet;
    private int[] UKS;
    private VariedLengthMappingTable VLMT;
    private RandomKeyStroke RKS;
    private String[] newKeySet;
    public void SetSpeed(int Speed){
                this.Speed = Speed;
    }
    public String GetString(){
        return getText();
    }
    public void para_initialization(String[] oldKeySet, String[] NewKeySet, VariedLengthMappingTable VLMT, RandomKeyStroke RKS){
    	this.oldKeySet = oldKeySet;
    	this.newKeySet = newKeySet;
    	this.VLMT = VLMT;
    	this.RKS = RKS;
    }
    public void run(){
        try{
            while(!Thread.interrupted()){
               // anglecounter;
                if(anglecounter == 720)
                    anglecounter =0;
                angle = anglecounter%720*2*Math.PI/720;
                //this.radialPosition = (this.radialPosition - angle);

                repaint();
                //TimeUnit.MICROSECONDS.sleep(Speed);
                TimeUnit.NANOSECONDS.sleep(Speed);
                //TimeUnit.MILLISECONDS.sleep(Speed);
                //System.out.println(this.radialPosition*360/(2*Math.PI));
            }
        }catch(InterruptedException e){}
    }
    public RadialInputButton(String label, BackGround BG)
    {
        super(label);
        setContentAreaFilled(false);
        setBorderPainted(false);
        this.innerDist = this.outerDist = 1;
        this.radians = 2*Math.PI;
        this.radialPosition = 0.0;
        this.hitBox = null;
        this.BG = BG;
        addMouseListener(this);
    }
    
    public void setShape(double innerDist, double outerDist, double radians, double radialPosition)
    {
        this.innerDist = innerDist;
        this.outerDist = outerDist;
        this.radians = radians;
        this.radialPosition = radialPosition;
    }

    public void paintComponent(Graphics g)
    {
        long start = System.currentTimeMillis();
        // Turn on antialiasing


        Graphics2D g2 = (Graphics2D) g;
        Random rnd = new Random();

        g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));

        // Construct shape
        Arc2D.Double arc = new Arc2D.Double();
        GeneralPath shape = new GeneralPath();
        shape.moveTo(0,0);
        shape.lineTo(0,outerDist-innerDist);
        arc.setArcByCenter(0, outerDist, innerDist, 90, -Math.toDegrees(radians), Arc2D.OPEN);
        shape.append(arc, true);
        shape.lineTo(Math.sin(radians)*outerDist, outerDist-Math.cos(radians)*outerDist);
        arc.setArcByCenter(0, outerDist, outerDist, 90-Math.toDegrees(radians), Math.toDegrees(radians), Arc2D.OPEN);
        shape.append(arc, true);
        shape.closePath();

        Color blue1 = new Color(83, 122, 158);
        Color blue2 = new Color(112, 163, 208);
        g2.setPaint(new GradientPaint(0, 2, blue2, 0, getBounds().height-2*2, blue1.darker().darker()));


        // Rotate/translate it into place
        AffineTransform at = new AffineTransform();
        at.translate(getBounds().width/2.0,getBounds().height/2.0-outerDist);
        at.rotate(radialPosition+angle, 0, outerDist);
        shape.transform(at);
        hitBox = shape;


        if(getModel().isRollover()){
            g2.fill(shape);

        }
        if(getModel().isPressed()){

            //anglecounter= 200;
            repaint();
        }
        if(!getText().equals("") )
        {
            //System.out.println(a);
            //g2.fill(shape);
            g2.setPaint(Color.BLACK);


        }
        else
        {
            g2.setPaint(Color.BLACK);
        }

        // Paint text
        double ringWidth = outerDist - innerDist;
       // g2.drawString(getText().substring(0,1), (int)(Math.sin(radialPosition+radians/2)*(outerDist-ringWidth/2)+getBounds().width/2-2),
                //(int)(-Math.cos(radialPosition+radians/2)*(outerDist-ringWidth/2)+getBounds().height/2));
        DrawString(g2,getText(),ringWidth);

         if(aa==1)   {
            System.out.println("repaint : " + (System.currentTimeMillis()-start));
             aa=0;
         }

    }

    private void DrawString(Graphics2D g2, String CharacterSet,double ringWidth){
        double RingIndex = radialPosition/radians;
        //System.out.println(RingIndex);
        switch((int)RingIndex){
            case 0:
               for(int index=0;index<CharacterSet.length();index++){
                   if(index<3){
                       g2.drawString(CharacterSet.substring(index,index+1),
                               (int)(Math.sin(radialPosition+radians/2  + angle)*(outerDist-ringWidth/2)+getBounds().width/2-20+index*15),
                               (int)(-Math.cos(radialPosition+radians/2  + angle)*(outerDist-ringWidth/2)+getBounds().height/2-20));
                   }
                   else if(index<7){
                       g2.drawString(CharacterSet.substring(index,index+1),
                               (int)(Math.sin(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().width/2-20+(index-3)*15),
                               (int)(-Math.cos(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().height/2-35));
                   }
                   else if(index<11){
                       g2.drawString(CharacterSet.substring(index,index+1),
                               (int)(Math.sin(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().width/2-20+(index-7)*15),
                               (int)(-Math.cos(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().height/2-50));
                   }
               }
                break;
            case 1:
                for(int index=0;index<CharacterSet.length();index++){
                if(index<3){
                    g2.drawString(CharacterSet.substring(index,index+1),
                            (int)(Math.sin(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().width/2-10+index*15),
                            (int)(-Math.cos(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().height/2+20));
                }
                else if(index<7){
                    g2.drawString(CharacterSet.substring(index,index+1),
                            (int)(Math.sin(radialPosition+radians/2 + angle )*(outerDist-ringWidth/2)+getBounds().width/2-10+(index-3)*15),
                            (int)(-Math.cos(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().height/2+5));
                }
                else if(index<11){
                    g2.drawString(CharacterSet.substring(index,index+1),
                            (int)(Math.sin(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().width/2-10+(index-7)*15),
                            (int)(-Math.cos(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().height/2-10));
                }
            }
                break;
            case 2:
                for(int index=0;index<CharacterSet.length();index++){
                    if(index<3){
                        g2.drawString(CharacterSet.substring(index,index+1),
                                (int)(Math.sin(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().width/2-5+index*15),
                                (int)(-Math.cos(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().height/2+20));
                    }
                    else if(index<7){
                        g2.drawString(CharacterSet.substring(index,index+1),
                                (int)(Math.sin(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().width/2-5+(index-3)*15),
                                (int)(-Math.cos(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().height/2+5));
                    }
                    else if(index<11){
                        g2.drawString(CharacterSet.substring(index,index+1),
                                (int)(Math.sin(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().width/2-5+(index-7)*15),
                                (int)(-Math.cos(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().height/2-10));
                    }
                }
                break;
            case 3:
                for(int index=0;index<CharacterSet.length();index++){
                    if(index<3){
                        g2.drawString(CharacterSet.substring(index,index+1),
                                (int)(Math.sin(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().width/2-10+index*15),
                                (int)(-Math.cos(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().height/2+35));
                    }
                    else if(index<7){
                        g2.drawString(CharacterSet.substring(index,index+1),
                                (int)(Math.sin(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().width/2-10+(index-3)*15),
                                (int)(-Math.cos(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().height/2+20));
                    }
                    else if(index<11){
                        g2.drawString(CharacterSet.substring(index,index+1),
                                (int)(Math.sin(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().width/2-10+(index-7)*15),
                                (int)(-Math.cos(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().height/2+5));
                    }
                }
                break;
            case 4:
                for(int index=0;index<CharacterSet.length();index++){
                    if(index<3){
                        g2.drawString(CharacterSet.substring(index,index+1),
                                (int)(Math.sin(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().width/2-20+index*15),
                                (int)(-Math.cos(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().height/2+40));
                    }
                    else if(index<7){
                        g2.drawString(CharacterSet.substring(index,index+1),
                                (int)(Math.sin(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().width/2-20+(index-3)*15),
                                (int)(-Math.cos(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().height/2+25));
                    }
                    else if(index<11){
                        g2.drawString(CharacterSet.substring(index,index+1),
                                (int)(Math.sin(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().width/2-20+(index-7)*15),
                                (int)(-Math.cos(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().height/2+5));
                    }
                }
                break;
            case 5:
                for(int index=0;index<CharacterSet.length();index++){
                    if(index<3){
                        g2.drawString(CharacterSet.substring(index,index+1),
                                (int)(Math.sin(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().width/2-30+index*15),
                                (int)(-Math.cos(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().height/2+30));
                    }
                    else if(index<7){
                        g2.drawString(CharacterSet.substring(index,index+1),
                                (int)(Math.sin(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().width/2-30+(index-3)*15),
                                (int)(-Math.cos(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().height/2+15));
                    }
                    else if(index<11){
                        g2.drawString(CharacterSet.substring(index,index+1),
                                (int)(Math.sin(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().width/2-30+(index-7)*15),
                                (int)(-Math.cos(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().height/2));
                    }
                }
                break;
            case 6:
                for(int index=0;index<CharacterSet.length();index++){
                    if(index<3){
                        g2.drawString(CharacterSet.substring(index,index+1),
                                (int)(Math.sin(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().width/2-30+index*15),
                                (int)(-Math.cos(radialPosition+radians/2+ angle )*(outerDist-ringWidth/2)+getBounds().height/2+20));
                    }
                    else if(index<7){
                        g2.drawString(CharacterSet.substring(index,index+1),
                                (int)(Math.sin(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().width/2-30+(index-3)*15),
                                (int)(-Math.cos(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().height/2+5));
                    }
                    else if(index<11){
                        g2.drawString(CharacterSet.substring(index,index+1),
                                (int)(Math.sin(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().width/2-30+(index-7)*15),
                                (int)(-Math.cos(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().height/2-10));
                    }
                }
                break;
            case 7:
                for(int index=0;index<CharacterSet.length();index++){
                    if(index<3){
                        g2.drawString(CharacterSet.substring(index,index+1),
                                (int)(Math.sin(radialPosition+radians/2+ angle )*(outerDist-ringWidth/2)+getBounds().width/2-30+index*15),
                                (int)(-Math.cos(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().height/2+20));
                    }
                    else if(index<7){
                        g2.drawString(CharacterSet.substring(index,index+1),
                                (int)(Math.sin(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().width/2-30+(index-3)*15),
                                (int)(-Math.cos(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().height/2+5));
                    }
                    else if(index<11){
                        g2.drawString(CharacterSet.substring(index,index+1),
                                (int)(Math.sin(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().width/2-30+(index-7)*15),
                                (int)(-Math.cos(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().height/2-10));
                    }
                }
                break;
            case 8:
                for(int index=0;index<CharacterSet.length();index++){
                    if(index<3){
                        g2.drawString(CharacterSet.substring(index,index+1),
                                (int)(Math.sin(radialPosition+radians/2+ angle )*(outerDist-ringWidth/2)+getBounds().width/2-30+index*15),
                                (int)(-Math.cos(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().height/2));
                    }
                    else if(index<7){
                        g2.drawString(CharacterSet.substring(index,index+1),
                                (int)(Math.sin(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().width/2-30+(index-3)*15),
                                (int)(-Math.cos(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().height/2-15));
                    }
                    else if(index<11){
                        g2.drawString(CharacterSet.substring(index,index+1),
                                (int)(Math.sin(radialPosition+radians/2 + angle)*(outerDist-ringWidth/2)+getBounds().width/2-30+(index-7)*15),
                                (int)(-Math.cos(radialPosition+radians/2+ angle )*(outerDist-ringWidth/2)+getBounds().height/2-30));
                    }
                }



        }
    }

    public boolean contains(int x, int y)
    {
        if(hitBox != null)
            return hitBox.contains(x,y);
        else
            return false;
    }
    public void mousePressed(MouseEvent e){
    	try {
			this.BG.CheckFriendDeviceStatuBuffer();
		} catch (NoSuchAlgorithmException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
    	this.BG.Background_Key_Pressed_transit(getText());
        System.out.println("TIME : " + System.currentTimeMillis()) ;
        long start = System.currentTimeMillis();
        try {
			boolean Local = this.BG.Insert_RKS();
			if(Local == true){
				//send something to Android, tell it insert in PC, denote -1
			}else {
				//send something to Android, tell it insert in Android denote -2
			}
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
       // this.BG.UpdateRKS(getText());
        aa=1;





        //anglecounter= 90;
        repaint();
        System.out.println(System.currentTimeMillis()-start);
        System.out.println(getText());
        System.out.println("--------------------------------");
       // SetSpeed(10);

    }
    public void mouseReleased(MouseEvent e){


    }
    public void mouseEntered(MouseEvent e){

        //SetSpeed(1000000);


    }
    public void mouseExited(MouseEvent e){
           //this.SetSpeed(1000);

    }
    public void mouseClicked(MouseEvent e){
    	
    }
}


class CenterButton extends JButton
{
    protected double diameter;
    private BackGround BG;
    public CenterButton(String label, BackGround BG)
    {
        super(label);
        setContentAreaFilled(false);
        setBorderPainted(false);
        diameter = 0;
        this.BG = BG;
    }

    public void setDiameter(double diameter)
    {
        if(diameter < 0)
            diameter = 0;
        this.diameter = diameter;
    }

    public void paintComponent(Graphics g)
    {   
        // Turn on antialiasing
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));

        // Set up some colors
        GradientPaint borderPaint, fillPaint;
        Color gray1 = new Color(229, 229, 229);
        Color gray2 = new Color(235, 235, 235);

        // Center the graphics context
        double translationXY = getBounds().width/2.0-diameter/2;
        g2.translate(translationXY, translationXY);

        // Draw the button
        int pushOffset = 4;
        if(getModel().isArmed())
        {
            borderPaint = new GradientPaint(0, 0, Color.GRAY, 0, (int)diameter, Color.WHITE);
            fillPaint = new GradientPaint(0, 1, gray1, 0, (int)diameter-2, gray2);
            pushOffset = 6;
        }
        else
        {
            borderPaint = new GradientPaint(0, 0, Color.WHITE, 0, (int)diameter, Color.GRAY);
            fillPaint = new GradientPaint(0, 1, gray2, 0, (int)diameter-2, gray1);
        }
        g2.setPaint(borderPaint); 
        g2.fill(new Ellipse2D.Double(0, 0, diameter, diameter));
        g2.setPaint(fillPaint); 
        g2.fill(new Ellipse2D.Double(1, 1, diameter-2, diameter-2));

        g2.setPaint(Color.BLACK); 
        int strWidth = g.getFontMetrics().stringWidth(getText());
        g2.drawString(getText(), (int)(diameter/2-strWidth/2), (int)(diameter/2+pushOffset));

    }

    public boolean contains(int x, int y)
    {
        int center = getBounds().width/2;
        double x2 = Math.pow(center-x, 2);
        double y2 = Math.pow(center-y, 2);
        return (Math.sqrt(x2+y2) <= diameter/2);
    }
}

class RadialBackground extends JPanel implements Runnable
{
    //BORDER is the margin border
    public static final int BORDER = 8;
    protected int rings;
    protected int sectionsPerRing;
    private static double angle = Math.PI/360;
    private static int anglecounter=0;
    public void run(){
        try{
            while(!Thread.interrupted()){
                anglecounter++;

                angle = anglecounter%360*2*Math.PI/360;
                repaint();
                TimeUnit.MILLISECONDS.sleep(50);
            }
        }catch(InterruptedException e){}
    }

    public RadialBackground(int rings, int sectionsPerRing)
    {
        super();
        this.rings = rings;
        this.sectionsPerRing = sectionsPerRing;
    }

    public void paintComponent(Graphics g)
    {
        // Turn on antialiasing
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));

        // Make some quick calculations
        int size = getSize().width;
        double center = size/2.0;
        double radius = center - BORDER;
        double ringWidth = radius/(rings+1);
        double sectionRadians = Math.PI * 2 / sectionsPerRing;

        // Set up a few colors
        Color gray1 = new Color(217, 217, 217);
        Color gray2 = new Color(229, 229, 229);

        // Paint background
        g2.setPaint(new GradientPaint(0, 0, Color.GRAY, 0, size, Color.WHITE));
        g2.fill(new Ellipse2D.Double(0, 0, size, size));
        g2.setPaint(new GradientPaint(0, BORDER, gray2, 0, size-2*BORDER, gray1));
        g2.fill(new Ellipse2D.Double(BORDER, BORDER, size-2*BORDER, size-2*BORDER));

        // Draw sections
        g2.setPaint(new Color(200, 200, 200));
        for(int i = 0; i < sectionsPerRing; ++i)
            g2.draw(new Line2D.Double(center, center, Math.sin((sectionRadians)*i+angle)*radius+center, -Math.cos((sectionRadians)*i+angle)*radius+center));

        // Draw rings
        for(int j = 2; j <= rings; ++j)
            g2.draw(new Ellipse2D.Double(center-j*ringWidth, center-j*ringWidth, 2*ringWidth*j, 2*ringWidth*j));
        g2.drawString("aaaa",0,0);
    }
}

