package PC;

    import javax.swing.JPanel;  
    import java.awt.Toolkit;  
    import java.awt.Image; 
    import java.awt.Graphics; 
    import java.awt.Graphics2D; 
      
    public class ImagePanel extends JPanel {  
        
     /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		//Initializing the class Image  
		Image background;  
      
		//Setting up GUI  
        public ImagePanel(/*FilePanel buttons*/) {  
           
	         //Constructing the class "Toolkit" which will be used to manipulate our images.  
	         Toolkit kit = Toolkit.getDefaultToolkit();  
	           
	         //Getting the "background.jpg" image we have in the folder  
	         background = kit.getImage("images/bg4.jpg");  
        }  
          
        //Manipulate Images with JAVA2D API. . creating a paintComponent method.  
         public void paintComponent(Graphics comp) {  
            
	         //Constructing the class Graphics2D. Create 2D by casting the "comp" to Graphics2D  
	         Graphics2D comp2D = (Graphics2D)comp;  
	           
	         //creating a graphics2d using the images in the folder and place it in a specific coordinates.  
	         comp2D.drawImage(background, 0, 0, 630, 330, this);  
         }  
    }  