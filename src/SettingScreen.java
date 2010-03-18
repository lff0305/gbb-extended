import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Keypad;
//import net.rim.device.api.ui.FontFamily;
import net.rim.device.api.ui.Graphics;
//import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.NumericChoiceField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.VerticalFieldManager;

public final class SettingScreen extends TransitionScreen {
	private ButtonField fgButton, bgButton, lineButton;
	private RichTextField fontText;
//	private FontFamily fontFamily[] = FontFamily.getFontFamilies();
	private NumericChoiceField pickSize;
	private NumericChoiceField pickLine;
//	private ObjectChoiceField pickFont;
	private ObjectChoiceField pickStyle;
	private ObjectChoiceField pickAnti;
	private ObjectChoiceField pickBg;
	private ModeField pannel;
//	private ObjectChoiceField pickAutoQuit;
	private CheckboxField ckbDrawLine;
	private CheckboxField ckbOpenLast;
	private CheckboxField ckbEsc;
	private CheckboxField ckbCycle;
	private CheckboxField ckbPage;
	private CheckboxField ckbTrackBall;

	private String[] imageFileName;

	private int[] fgColorID = {0,0};
	private int[] bgColorID = {0,0};;
	private int[] lineColorID ={0,0};
	private Font font;
	final String[] btTxt = {"背景颜色", "背景图片"};
	Bitmap bmp;
	private int[] bgType = {0,0};
	private int mode;
	private String[] modeS = {"日间模式预览  GBBReader\nGBB阅读器  1234567890 ABCDEFG",
							"夜间模式预览  GBBReader\nGBB阅读器  1234567890 ABCDEFG"};
	public SettingScreen(boolean doTransition) {
		super(doTransition, VERTICAL_SCROLL | VERTICAL_SCROLLBAR);   // no transition effect             
		setTitle("GBBReader - 设置");
		
		String[] styleName = {"纯文本", "粗体", "特粗体", "粗斜体", "斜体"};
		String[] antiAlias = {"无", "标准", "低分辨率", "子像素"};
		String[] bgName = {"纯色", "图片"};
        init();

		fontText = new RichTextField(modeS[mode],USE_ALL_WIDTH | NON_FOCUSABLE) {
        	public void paint(Graphics g){
        		int w = getWidth();
        		int h = getHeight();
        		if (bgType[mode] > 0 && bmp != null) 
        			g.drawBitmap(0, 0, w-1, h-1, bmp, 0, 0);
        		else {
        			g.setColor(Setting.returnColor(bgColorID[mode]));
            		g.fillRect(0, 0, w, h);
        		}
      			g.setColor(Setting.returnColor(lineColorID[mode]));
        		g.setStipple(0xCCCCCCCC);
        		g.drawLine(0, (h-2)/2, w-1, (h-2)/2);
        		g.drawLine(0, h-2, w-1, h-2);
       			g.setColor(Setting.returnColor(fgColorID[mode]));
        		super.paint(g);
        	}
        };

		FontChangeListener fontChangeListener = new FontChangeListener();
//      pickFont = new ObjectChoiceField("字体库：", fontFamily, Setting.fontFamilyIndex);
//      pickFont.setChangeListener(null);
//      pickFont.setChangeListener(fontChangeListener);
//      add(pickFont);

        pickStyle = new ObjectChoiceField("字体样式：", styleName, Setting.fontSytleIndex);
        pickStyle.setChangeListener(null);
        pickStyle.setChangeListener(fontChangeListener);
//        add(pickStyle);
 
        pickSize = new NumericChoiceField("字体高度 ：",12,39,1,Setting.fontSizeIndex);
        pickSize.setChangeListener(null);
        pickSize.setChangeListener(fontChangeListener);
//        add(pickSize);

        pickAnti = new ObjectChoiceField("反锯齿效果：", antiAlias, Setting.antiAliasIndex);
        pickAnti.setChangeListener(fontChangeListener);
//        add(pickAnti);
 
        pickLine = new NumericChoiceField("设置行距 ：",0,10,1,Setting.lineSpace);
        pickLine.setChangeListener(fontChangeListener);
//        add(pickLine);
        
        BgChangeListener bgChangeListener = new BgChangeListener();
       	pickBg = new ObjectChoiceField("背景设置", bgName, Setting.bgType);
        pickBg.setChangeListener(bgChangeListener);
        
        ButtonListener btListener = new ButtonListener();
//        HorizontalFieldManager btnHFM = new HorizontalFieldManager(USE_ALL_WIDTH | FIELD_HCENTER);
        fgButton = new ButtonField("文字颜色", ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY | FIELD_LEFT);
        fgButton.setChangeListener(btListener);
//        btnHFM.add(fgButton);
       	bgButton = new ButtonField(btTxt[Setting.bgType[mode]], ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY | FIELD_HCENTER);
        bgButton.setChangeListener(btListener);
//        btnHFM.add(bgButton);
        lineButton = new ButtonField("虚线颜色", ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY | FIELD_RIGHT);
        lineButton.setChangeListener(btListener);
        
		pannel = new ModeField();
        add(pannel);
        add(new SeparatorField());
        
        ckbDrawLine = new CheckboxField("阅读时显示虚线", Setting.drawDashLine > 0 ? true : false);
        ckbEsc = new CheckboxField("返回键转入后台", Setting.escToBackground > 0 ? true : false);
        ckbCycle = new CheckboxField("列表循环", Setting.cycle > 0 ? true : false);
        ckbPage = new CheckboxField("翻页时保留一行", Setting.pageScroll > 0 ? true : false);
        ckbOpenLast = new CheckboxField("自动打开最后阅读书籍",Setting.openLastBook > 0 ? true : false);
        ckbTrackBall = new CheckboxField("轨迹球左右翻页",Setting.useTrackBall > 0 ? true : false);
        add(ckbDrawLine);
        add(ckbPage);
        add(ckbEsc);
        add(ckbTrackBall);
        add(new SeparatorField());
        add(ckbCycle);
        add(ckbOpenLast);
        add(new SeparatorField());
        pannel.setExpand(mode + 1);
        fontText.setFont(font);
 	}

	/**
	 * 根据记录设置屏幕
	 */
	private void init() {
		mode = (Setting.mode > 0) ? 1 : 0;
		imageFileName = new String[2];
		for(int i = 0; i < 2; i++) {
			imageFileName[i] = Setting.imageFileName[i];
			fgColorID[i] = Setting.fgColorIndex[i];
		    bgColorID[i] = Setting.bgColorIndex[i];
			lineColorID[i] = Setting.lineColorIndex[i];	
			bgType[i] = Setting.bgType[i];
		}
	    font = Setting.getFont();
//        fontText.setFont(font);
       	loadImg();
 	}
	
	/**
	 * 加载图片
	 */
	private void loadImg() {
		bmp = null;
        if (bgType[mode] > 0)
        	bmp = Setting.loadBackgroundImage(imageFileName[mode]);
	}
	
	public boolean onSavePrompt() {
		return true; // call onClose() when true
	}

	private void saveSetting() {
//		Setting.fontFamilyIndex = pickFont.getSelectedIndex();
		Setting.fontSizeIndex = pickSize.getSelectedIndex();
		Setting.fontSytleIndex = pickStyle.getSelectedIndex();
		Setting.antiAliasIndex = pickAnti.getSelectedIndex();
		Setting.lineSpace = pickLine.getSelectedIndex();
		Setting.fgColorIndex = fgColorID;
		Setting.bgColorIndex = bgColorID;
		Setting.lineColorIndex = lineColorID;
		Setting.drawDashLine = ckbDrawLine.getChecked() ? 1 : 0;
		Setting.openLastBook = ckbOpenLast.getChecked() ? 1 : 0;
		Setting.escToBackground = ckbEsc.getChecked() ? 1 : 0;
		Setting.useTrackBall = ckbTrackBall.getChecked() ? 1 : 0;
		Setting.pageScroll = ckbPage.getChecked() ? 1 : 0;
		Setting.cycle = ckbCycle.getChecked() ? 1 : 0;
		Setting.imageFileName[0] = imageFileName[0];
		Setting.imageFileName[1] = imageFileName[1];
		//如果没设置过图片文件, 则背景模式应为纯色
		Setting.bgType[0] = (imageFileName[0].equals(Setting.nullStr)) ? 0 : bgType[0];
		Setting.bgType[1] = (imageFileName[1].equals(Setting.nullStr)) ? 0 : bgType[1];
		Record setRecord = new Record(Record.SETTING_NAME);
		setRecord.saveSetting();
		setRecord = null;
	}

	public boolean onClose() {
		if ((fgColorID[0] != Setting.fgColorIndex[0]) ||
			(bgColorID[0] != Setting.bgColorIndex[0])	||
			(lineColorID[0] != Setting.lineColorIndex[0]) ||	
			(lineColorID[1] != Setting.lineColorIndex[1]) ||	
			(!imageFileName[0].equals(Setting.imageFileName[0])) ||
			(fgColorID[1] != Setting.fgColorIndex[1]) ||
			(bgColorID[1] != Setting.bgColorIndex[1])	||
			(bgType[0] != Setting.bgType[0])	||
			(bgType[1] != Setting.bgType[1])	||
			(!imageFileName[1].equals(Setting.imageFileName[1])))
			setDirty(true);
		if (!isDirty())
			return super.onClose();
		switch(Dialog.ask(Dialog.D_SAVE, "是否保存设置?")) {
			case Dialog.SAVE:
				saveSetting(); //保存后退出
				super.onClose();
				break;
			case Dialog.DISCARD:
				super.onClose();
				break;
		}
		return true;
	}
	protected boolean navigationMovement(int dx,int dy,int status,int time) {
    	if (dx < 0 && !fgButton.isFocus() && !bgButton.isFocus() && !lineButton.isFocus())
   			return super.navigationMovement(dx, dy, status, time);
    	else
    		return super.navigationMovement(dx, dy, 0, time);
    } 

	class FontChangeListener implements FieldChangeListener {
	    public void fieldChanged(Field field, int context) {
	       	int size = pickSize.getSelectedIndex(); 
//	   		int family = pickFont.getSelectedIndex();
	   		int style = pickStyle.getSelectedIndex();
	   		int aam = pickAnti.getSelectedIndex();
//        	font = Setting.getFont(family, style , size, aam);
        	font = Setting.getFont(style , size, aam);
        	fontText.setFont(font);
        	invalidate(); 
	    }
	}

	class BgChangeListener implements FieldChangeListener {
	    public void fieldChanged(Field field, int context) {
    		bgType[mode] = pickBg.getSelectedIndex();
	   		bgButton.setLabel(btTxt[bgType[mode] ]);
	   		loadImg();
	   		invalidate();
	    }
	}

	class ButtonListener implements FieldChangeListener {
        public void fieldChanged(Field field, int context) {
        	if (field == (Field)bgButton && pickBg.getSelectedIndex() > 0){
           		String lastPath = Setting.lastImgPath;
           		String startPath = (lastPath.equals(Setting.nullStr)) ? null : lastPath;
           		SelectImage si = new SelectImage(startPath);
           		UiApplication.getUiApplication().pushModalScreen(si);
           		String fs = si.getFile();
           		if (fs != null ) {
           			String is = "file:///" + fs;
       				imageFileName[mode] = is;
       				loadImg();
       				invalidate();
           		}
           		si = null;
           	}
        	else {
        		int id ;
        		if (field == (Field)fgButton)
        			id = fgColorID[mode];
        		else if (field == (Field)bgButton)
        			id = bgColorID[mode];
        		else
        			id = lineColorID[mode];
	        	ColorSelScreen cs = new ColorSelScreen(id);
	        	UiApplication.getUiApplication().pushModalScreen(cs);
	        	int color = cs.selColor;
	        	cs = null;
	    		if (color >= 0) {
	        		if (field == (Field)fgButton) {
	       				fgColorID[mode] = color;
        			}
	        		else if (field == (Field)bgButton) {
        				bgColorID[mode] = color;
	        		}
	        		else {
        				lineColorID[mode] = color;
	        		}
        			invalidate(); 
	    		}
        	}
        }
    };
    
    class ModeField extends VerticalFieldManager{
	   	 private boolean day , night, font;
	   	 private LabelField dayTitle, nightTitle, fontTitle;
	   	 private HorizontalFieldManager bgHM, btnHM;
	   	 private VerticalFieldManager fontVM, dayVM, nightVM;
	   	 final String fs1 ="版面设置";
	   	 final String ds1 = "日间模式颜色";
	   	 final String ns1 = "夜间模式颜色";
	   	 public ModeField() {    
	   		 super(FIELD_HCENTER | USE_ALL_WIDTH);
	   		 fontTitle = new TitleField(fs1);
	   		 dayTitle = new TitleField(ds1);
	   		 nightTitle = new TitleField(ns1);
	   		 fontVM = new VerticalFieldManager();
	   		 dayVM = new VerticalFieldManager();
	   		 nightVM = new VerticalFieldManager();
	   		 btnHM = new HorizontalFieldManager(USE_ALL_WIDTH | NO_HORIZONTAL_SCROLL);
	   		 bgHM = new HorizontalFieldManager();
	   		 bgHM.add(pickBg);
	   		 btnHM.add(fgButton);
	   		 btnHM.add(bgButton);
	   		 btnHM.add(lineButton);
	   		 add(fontTitle);
	   		 add(fontVM);
	         add(new SeparatorField());
	   		 add(dayTitle);
	   		 add(dayVM);
	         add(new SeparatorField());
	   		 add(nightTitle);
	   		 add(nightVM);
	   	 }
	   	 
	   	 private void deleteFont() {
	   		 fontVM.deleteAll();
	   		 fontTitle.setText(fs1);
	   		 font = false;
	   	 }

	   	 private void addFont() {
	   		 fontVM.add(pickStyle);
	   		 fontVM.add(pickSize);
	   		 fontVM.add(pickAnti);
	   		 fontVM.add(pickLine);
	   		 font = true;
	   	 }

	   	 private void deleteDay() {
   			dayVM.deleteAll();
   			day = false;
	   	 }

	   	 private void addDay() {
	   		 dayVM.add(bgHM);
	   		 dayVM.add(btnHM);
	   		 day = true;
	   		 night = false;
	   		 mode = 0;
	   		 loadImg();
	   		 fontText.setText(modeS[0]);
	   		 dayVM.add(fontText);
	   		 invalidate();
	   	 }

	   	 private void deleteNight() {
	   		nightVM.deleteAll();
			night = false;
	   	 }
	   	
	   	 private void addNight() {
	   		nightVM.add(bgHM);
	   		nightVM.add(btnHM);
			night = true;
			day = false;
			mode = 1;
			loadImg();
			fontText.setText(modeS[1]);
			nightVM.add(fontText);
			invalidate();
		 }

	   	private void setExpand(int flag) {
	   		boolean pNight = night;
	   		boolean pDay = day;
	   		if (flag == 0) {
	   			if (font) 
	   				deleteFont();
	   			else  
	   				addFont();
	   			return;
	   		}
	   		if (night)
	   			deleteNight();
	   		if (day)
	   			deleteDay();
	   		if (flag == 1 && !pDay) {
   				addDay();
   				pickBg.setSelectedIndex(bgType[mode]);
	   		}
	   		else if (flag == 2 && !pNight) {
   				addNight();
   				pickBg.setSelectedIndex(bgType[mode]);
	   		}
	   	 }
	   	 
	   	private boolean doExpand() {
	   		if (dayTitle.isFocus()) { 
	   			 setExpand(1);
	   			 return true;
	   		}
	   		else if (nightTitle.isFocus()) {
	   			setExpand(2);
	   			return true;
	   		}
	   		else if (fontTitle.isFocus()) {
	   			setExpand(0);
	   			return true;
	   		}
	   		else 
	   			return false;
	   	}
	   	 
	   	 protected boolean navigationClick(int status, int time) {
	   		 if (doExpand())
	   		 	return true;
	   		 return super.navigationClick(status, time);
	   	 }
	   	 
	     protected boolean keyDown(int keycode, int time) {
	    	 char key = Keypad.map(keycode);
			 if (key == Characters.ENTER || key == Characters.SPACE) {
		   		 if (doExpand())
		   		 	return true;
			 }
			 return super.keyDown(keycode, time);
	     }

    };
}
//默认的LabelField很奇怪，没法实现全宽度且居中的效果，莫名其妙，只好自己写一个
final class TitleField extends LabelField {
	int MAX_W;
	public TitleField(String title){
		super(title, FOCUSABLE | HIGHLIGHT_FOCUS | FIELD_HCENTER | USE_ALL_WIDTH | DrawStyle.LEFT | DrawStyle.TOP);
		MAX_W = Display.getWidth();
	}
	public int getPreferredWidth(){
		return MAX_W;
	}
	
	protected void paint(Graphics g){
		String text = getText();
		int w = getFont().getAdvance(text);
		int left = (MAX_W - w) >> 1;
		g.drawText(text, left, 0);
	}
}; 

	