import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.UiApplication;
//import net.rim.device.api.system.Bitmap;
//import net.rim.device.api.ui.container.VerticalFieldManager;
//import net.rim.device.api.ui.decor.Background;
//import net.rim.device.api.ui.decor.BackgroundFactory;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;

public final class GBBMainScreen extends TransitionScreen {
	private MainList mainList;
	
	public GBBMainScreen(){
		super(false, NO_VERTICAL_SCROLL);   // no transition effect             
		setTitle("GBBReader");
		
        // 添加背景图片， 4.6适用
//		Bitmap bmpBg = Bitmap.getBitmapResource("rabbit.jpg");
//		Background _bg = BackgroundFactory.createBitmapBackground(bmpBg);
//		VerticalFieldManager _vfm = (VerticalFieldManager)getMainManager();
//		_vfm.setBackground(_bg);
		
		// add list
		mainList = new MainList();
		add(mainList);
   	}
	
	public boolean onClose() {
		Record rec = new Record(Record.SETTING_NAME);;
		rec.saveSetting();
		rec = null;

		System.exit(0);
		return true;
	}
	
	protected boolean keyChar(char key, int status, int time)
    {
    	switch(key) {
    		case Characters.ESCAPE:
    		case Characters.LATIN_SMALL_LETTER_Q:
    			onClose();
    			return true;
    		case Characters.LATIN_SMALL_LETTER_O:
    			openSetting();
				return true;
    		case Characters.LATIN_SMALL_LETTER_R:
				return true;
     		case Characters.ENTER :
    		case Characters.SPACE :
			case Characters.LATIN_SMALL_LETTER_J:
			case Characters.LATIN_SMALL_LETTER_D:
				doSelection(); 
				return true;
      	}
		return super.keyChar(key, status, time);
     } 
    
    protected boolean navigationMovement(int dx,int dy,int status,int time) {
		if(!GBBMain.isTrackWheel) {
    		if (dx > 0) {  // 右滚轮也用来选择
    			doSelection();
    			return true;
    		}
    		else if (dx < 0)
    			return true;
    	}
		return super.navigationMovement(dx, dy, status, time);
    }

    protected boolean navigationClick(int status, int time) {
		if(!GBBMain.isTrackWheel) {
			doSelection();
			return true;
    	}
		return super.navigationClick(status, time);
    }

    /**
     * 打开设置窗口
     */
    private void openSetting() {
		SettingScreen setScr = new SettingScreen(true);
		UiApplication.getUiApplication().pushModalScreen(setScr);
		setScr = null;
    }
    
    private void doSelection() {
    	switch(mainList.getSelectedIndex()) {
	    	case 0:
	    		HistoryScreen hisScr = new HistoryScreen();
	    		UiApplication.getUiApplication().pushModalScreen(hisScr);
	    		hisScr = null;
	    		break;
	    	case 1:
	    		String startPath = (Setting.lastPath.equals(Setting.nullStr) ||
	    							Setting.lastPath.length() < 1) ? null : Setting.lastPath;
	    		OpenBookScreen obScr = new OpenBookScreen(startPath);
	    		UiApplication.getUiApplication().pushModalScreen(obScr);
	    		obScr = null;
	    		break;
	    	case 2:
	    		openSetting();
	    		break;
	    	case 3:
	    		HelpScreen hlpScr = new HelpScreen(true);
	    		UiApplication.getUiApplication().pushModalScreen(hlpScr);
	    		hlpScr = null;
	    		break;
	    	case 4:
	    		AboutScreen abtScr = new AboutScreen();
	    		UiApplication.getUiApplication().pushModalScreen(abtScr);
	    		abtScr = null;
	    		break;
    	}
    }

// class GBBMainScreen ends
}

// MainList可以实现背景图片及半透明效果
class MainList extends MyObjectListField implements ListFieldCallback {
	private Bitmap icon = Bitmap.getBitmapResource("totalicon.png");
	private Font f;
	private Bitmap bmpArrow = Bitmap.getBitmapResource("arrow.png");
	private int wicon,hicon,hf;
    
    public  MainList() { 
		super(HCENTER);
        setRowHeight((GBBMain.deviceName >= 89) ? 48 : 40);
        f = getFont().derive(Font.BOLD);
        hf = f.getHeight();
        setFont(f);
		String[] items = {"最近阅读",
				  "选择书籍",
				  "设      置",
				  "帮      助",
        		  "关      于"};		
		set(items);
        wicon = bmpArrow.getWidth();
        hicon = bmpArrow.getHeight();

 	}

	//Handles moving the focus within this field.
    public int moveFocus(int amount, int status, int time) {
        invalidate();
        return super.moveFocus(amount,status,time);
    }

    /** override this method to avoid default drawHighlightRegion
	 * 
	 */
	protected void drawFocus(Graphics graphics, boolean on) {
		// do nothing here
		// normally, here will call drawHighlightRegion, but to implements
		// transparent effect, i called drawHighlightRegion at drawListRow
	}

	public void drawListRow(ListField list, Graphics g, int index, int y, int w) {
		// get item text
		int icon_size = 36;

		Object obj = this.get(list,index);
        String row = obj.toString();

        g.setGlobalAlpha(255);
        int hr = getRowHeight(); // row height
        
        //draw icon
        int x = 2;
        int starty = y + ((hr - icon_size) >> 1);
        g.drawBitmap(x, starty, icon_size, icon_size, icon, 0, index * icon_size);
        
        //draw text
        x = icon_size + 3;
        starty = y + ((hr - hf) >> 1);
    	g.drawText(row,x, starty, 0, w); // files
    	
    	//draw arrow
        x = w - wicon -3;
        starty = y + ((hr - hicon) >> 1);
        g.drawBitmap(x, starty, wicon, hicon, bmpArrow, 0, 0);
        
        //draw bottom line
        int c = g.getColor();
        g.setColor(Color.GRAY);
        int bottom_y = (index + 1) * hr - 1;
        g.drawLine(0, bottom_y, w,bottom_y);
        g.setColor(c);
        
		if (index == getSelectedIndex()) { //draw high light row
        	g.setGlobalAlpha(155);
			drawHighlightRegion(g, HIGHLIGHT_SELECT , true,	0, y, w, hr);
 		}
    }
	
    // end of class
}
