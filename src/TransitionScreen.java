import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.container.MainScreen;

public class TransitionScreen extends MainScreen
{
	public boolean doTransition;
	public boolean sliding;
	public boolean slide_out_screen;
    private int i, STEP;
    private int _timerId;
    public Bitmap imgPrev, imgCurrent;
    public Graphics gPrev,gCurrent;
    public static int MAX_W;
    public static int MAX_H;
    private Runnable run_animation= new Runnable() {
   		public void run() 
   		{
   			nextStep();
   		}
    };

	/** 
	 * class constructor 
	 * @param doTransition
	 * if True, the screen will do sliding transition,  else it acts as MainScreen
	 */
	public TransitionScreen(boolean doTransition, long style){
		super(style);
		this.doTransition = doTransition;
//		sliding = false;
//		slide_out_screen = false;

		if (doTransition) {
			imgPrev = new Bitmap(MAX_W,MAX_H);
			gPrev = new Graphics(imgPrev);
				
			imgCurrent = new Bitmap(MAX_W,MAX_H);
			gCurrent = new Graphics(imgCurrent);
		}
 	}
	
	public TransitionScreen(Bitmap bPrev, Bitmap bCurrent){
		super();

		imgPrev = bPrev;
		imgCurrent = bCurrent;
		
//		sliding = false;
		doTransition = true;
		slide_out_screen = true;
	}

	public boolean onClose()
	{
		if (doTransition && !slide_out_screen) {
				MyScreenShot(gCurrent);
				TransitionScreen scr = new TransitionScreen(imgPrev, imgCurrent);
				UiApplication.getUiApplication().pushScreen(scr);
		}
		return super.onClose();
	}
	/**
	 * Start timer to draw sliding effect
	 */
    protected void startTransition() {
        i = 0;
        sliding = true;
        int FRAMES_PER_SECOND = 40;
      	double SECOND = 0.15;

		STEP =(int)( MAX_W /(FRAMES_PER_SECOND * SECOND));
    	_timerId = UiApplication.getUiApplication().invokeLater(
        		run_animation, 
        		1000 / FRAMES_PER_SECOND, 
        		true);
    }
    
    private void nextStep() {
    	i += STEP;
        if (i >= MAX_W ) {
        	i = 0;
        	UiApplication.getUiApplication().cancelInvokeLater(_timerId);
        	if (slide_out_screen)
        		close();
        	sliding = false;
        }
        invalidate();
   }
    
    /**
	 * Override onUiEngineAttached().
	 * When this event occurs, if need sliding, snapshot the prev & current 
	 * screen to imgPrev & imgCurrent
	 * @param attached : attached - True if screen is going to be displayed
    protected void onUiEngineAttached(boolean attached)	{
    	if (doTransition && attached) {
			sliding = false;

			if(!slide_out_screen){
				((TransitionScreen)getScreenBelow()).MyScreenShot(gPrev);
				MyScreenShot(gCurrent);
			}
			startTransition();
		}
		else
			super.onUiEngineAttached(attached);
	}
	 */

	protected void onDisplay() {
		if (doTransition ) {
			sliding = false;
			if(!slide_out_screen){
				((TransitionScreen)getScreenBelow()).MyScreenShot(gPrev);
				MyScreenShot(gCurrent);
			}
			startTransition();
		}
		super.onDisplay();
	}

	/**
	 * Get the snapshot of the screen.
	 * @param g : graphics - Graphics context used for painting.
	 */
	public void MyScreenShot(Graphics g){
		super.paintBackground(g);
		super.paint(g);
	}
	
	/**
	 * Override the paint() method.
	 * If need sliding , then draw two bitmaps of current and previous screen,
	 * else call super.paint().
	 * @param g : graphics - Graphics context used for painting.
	 */
	protected void paint(Graphics graphics){
		if (sliding) 
			drawTransition(graphics);
		else
			super.paint(graphics);
	}
	
	/**
	 * On timer event, draw two bitmap at moving position
	 * @param graphics : graphics - Graphics context used for painting.
	 */
	public void drawTransition(Graphics graphics) { 
		int a = i + 1;
		int b = MAX_W - a;
		int h = MAX_H - 1;
	    if (!slide_out_screen) { 
	    	graphics.drawBitmap(0, 0, b , h, imgPrev, i, 0);
	    	graphics.drawBitmap(b, 0, a, h, imgCurrent, 0, 0);
	    }
	    else {
	    	graphics.drawBitmap(0, 0, a, h, imgPrev, b, 0);
	    	graphics.drawBitmap(a, 0, b, h, imgCurrent, 0, 0);
	    }
	}
	
    protected boolean navigationMovement(int dx,int dy,int status,int time) {
    	if (!slide_out_screen && !sliding && (KeypadListener.STATUS_FOUR_WAY & status) > 0 && dx < 0) {  
    		onClose(); // ×ó¹öÂÖÓÃÀ´ÍË³ö
   			return true;
    	}
    	return super.navigationMovement(dx, dy, status, time);
    }

	protected boolean keyChar(char key, int status, int time){
		if (!slide_out_screen && !sliding) {
		   	if (key == Characters.LATIN_SMALL_LETTER_R) {
	   			onClose();
	   			return true;
		   	}
		   	else 
		   		return super.keyChar(key, status, time);
		}
		return true;
	}

}