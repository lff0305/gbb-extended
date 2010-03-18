import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.i18n.DateFormat;
import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.system.Backlight;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.Clipboard;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.util.Arrays;
import net.rim.device.api.util.Comparator;
import net.rim.device.api.util.StringUtilities;

public final class TextScreen extends MainScreen {
	private String bookName;  //书名，全路径
	private String bookTitle; // 标题栏文字
	private TextBook txtBook;
	private Bitmap bmpBackground;  //背景图片
	private boolean isFullScreen = false;  //全屏标记
	private int TITLE_HEIGHT;// = 30; // title height
	private int TITLE_BG = Color.BLACK; // title background color
	private int TITLE_FG = Color.LIGHTGREY; // title font color
	private int TXT_BG;// = Color.WHITE; // book background color
	private int TXT_FG;// = Color.BLACK; // book font color
	private int LINE_FG;// = Color.LIGHTGREY;
	private int TOP_MARGIN;// = 0 ; //上边距
	private int LINE_SPACE;// = 2;  //行距
	private int MAX_W ;  //屏幕宽度
	private int MAX_H ; // 屏幕高度
	private int FONT_HEIGHT;  //字体高度
	private int LINE_PER_PAGE;  //每页显示行数
	private int IMAGE_W, IMAGE_H; //图片宽度及高度
	private final static int LEFT_MARGIN = 3;  //左边距
	private final static int RIGHT_MARGIN = 6; //右边距
	private Record bookRecord; //记录
	private Font currentFont; //当前字体
	private int mode;
	private boolean useTrackBall;
	
	private DateFormat df = SimpleDateFormat.getInstance(SimpleDateFormat.TIME_SHORT);
	   
	/**
	 * 设置默认菜单，去掉默认的close选项
	 */
	protected void makeMenu(Menu menu, int instance) { 
//		menu.add(_closeItem);
		menu.add(_jumpItem);
		menu.add(_ptItem);
		menu.add(_pbItem);
		menu.add(_fullItem);
		menu.add(_searchItem);
		menu.add(_addBookMarkItem);
		menu.add(_showBookMarkItem);
		menu.add(_copyItem);
		menu.add(_nextItem);
		menu.addSeparator();
		menu.add(_exitItem);
	}

    private MenuItem _fullItem = new MenuItem("全 屏(D)",130,20) {
    	public void run() {
    		toggleFullScreen();
    	}
    };

    private MenuItem _jumpItem = new MenuItem("跳 转(Z)",90,20){
    	public void run() {
    		pageJump(); 
    	}
    };
    private MenuItem _ptItem = new MenuItem("开 始",70,20){
    	public void run() {
  			pageTop();
    	}
    };
    private MenuItem _pbItem = new MenuItem("结 尾",75,20){
    	public void run() {
  			pageBottom();
    	}
    };
  
    private MenuItem _nextItem = new MenuItem("打开下一本",141,20){
    	public void run() {
			String nextBook = getNextBook();
			if (nextBook != null) 
				openNextBook(nextBook);			
 			else 
 				Dialog.alert("已到最后一本!");
    	}
    };

    private void search() {
		SearchScreen ss = new SearchScreen(txtBook, txtBook.getCurrentOffset());
		UiApplication.getUiApplication().pushModalScreen(ss);
		ss = null;
		}
    
	private MenuItem _searchItem = new MenuItem("查 找(V)",132,20){
    	public void run() {
    		search();
   		}
    };
    
    private MenuItem _copyItem = new MenuItem("复制页面",140,20){
    	public void run() {
    		//把当前页内容复制到剪贴板
    		Clipboard cb = Clipboard.getClipboard();	
			StringBuffer strToCopy = new StringBuffer();
			int startLine = txtBook.getCurrentLine();
   			int lastLine = getLastPageLine(startLine);

   			for (int i = startLine; i < lastLine; i++) 
   				strToCopy.append(txtBook.getLineText(i));
    		cb.put(strToCopy);
    		strToCopy = null;
    		cb = null;
    	}
    };

    private MenuItem _addBookMarkItem = new MenuItem("添加书签(A)",133,20){
    	public void run() {
    		addBookMark();
    	}
    };
    
    /**
     * 添加书签
     */
    private void addBookMark() {
		BookMark bm = new BookMark();
		bm.offset = txtBook.getCurrentOffset();
		bm.bookName = bookTitle;
		bm.digest = txtBook.getLineText(txtBook.getCurrentLine()).trim(); // 用当前页面的第一行作为摘要，并删去头尾的空格
		
		Record bookMarkRecord = new Record(Record.BOOKMARK_NAME);
		bookMarkRecord.saveBookMark(bm, true);
		bookMarkRecord = null;
	}
    
    private MenuItem _showBookMarkItem = new MenuItem("打开书签(L)",134,20){
    	public void run() {
    		showBookMark();
     	}
    };

    /**
     * 打开书签
     */
    private void showBookMark() {
		BookMarkScreen bs = new BookMarkScreen(bookTitle, (int) txtBook.getBookSize());
		UiApplication.getUiApplication().pushModalScreen(bs);

		int t = bs.offset ;
		bs = null;
		if (t >= 0) {
			txtBook.jumpTo(t, TextBook.BUFFER_SIZE, false);
			txtBook.setCurrentLine(0);
			invalidate();
		}
	}

    private MenuItem _exitItem = new MenuItem("退 出(Q)",150,20){
    	public void run() {
    		quit();
    	}
    };

    /**
     * 直接退出
     */
	public void quit() {
		windowClose();
		System.exit(0);
	}

	private int maxLineX; // 虚线右端的Y坐标
	public  TextScreen(String filename) {
		super();
		MAX_W = Display.getWidth();
		MAX_H = Display.getHeight();
		bookName = filename;
		maxLineX = MAX_W - 1;
   		max_x = MAX_W - RIGHT_MARGIN;
   		max = max_x - LEFT_MARGIN ;
		txtBook = new TextBook(bookName);
		bookTitle = txtBook.getBookName();
		mode = Setting.mode > 0 ? 1 : 0;
		setPage();

		loadBackgroundImage();
		loadHistory();  //读记录
		int keytype = Keypad.getHardwareLayout();
		if (keytype == Keypad.HW_LAYOUT_REDUCED || keytype == Keypad.HW_LAYOUT_REDUCED_24) 
			isReducedKeyboard = true;
	}
	
	/**
	 * 设置页面相关参数 
	 */
	private int PB_HEIGHT; //进度条高度
	private int lineTopY; // 虚线起始Y坐标
	private int deltaY;  // 两行文字的Y距离 
	private int topY; // 文字起始Y坐标
	private int max; // 页面最大宽度，扣除左右边距
	private int min; // 不满行的最大宽度， 为max减去一个句号的宽度
	private int max_x; // 最右边字符的右边界 
	private void setPage() {
		setColor(Setting.invertColor); 
		useTrackBall = (Setting.useTrackBall > 0) ? true : false;
		isFullScreen = (Setting.fullScreen > 0) ? true : false;
		currentFont = Setting.getFont();
		setFont(currentFont);
		min = max - currentFont.getAdvance("。");
		LINE_SPACE = Setting.lineSpace;
		FONT_HEIGHT = currentFont.getHeight();
		TITLE_HEIGHT = (isFullScreen) ? 0 : FONT_HEIGHT + 1;
		PB_HEIGHT = (isFullScreen) ? 0 : 5; 
		
		int lh = FONT_HEIGHT + LINE_SPACE;
		int m = MAX_H - TITLE_HEIGHT - PB_HEIGHT;
		LINE_PER_PAGE = m / lh;
		TOP_MARGIN = (m - lh * LINE_PER_PAGE + LINE_SPACE) >> 1;
		topY = TOP_MARGIN + TITLE_HEIGHT;
		lineTopY = topY + FONT_HEIGHT - 1;
		deltaY = FONT_HEIGHT + LINE_SPACE;  
		txtBook.setPage(currentFont, LINE_PER_PAGE, max);
	}

	/**
	 * 打开设置窗口， 在设置完毕后，应用新的设置
	 */
	private void showSetting() {
//		delAutoQuitTimer(); //关闭自动退出定时
		SettingScreen setScr = new SettingScreen(false);
		UiApplication.getUiApplication().pushModalScreen(setScr);
		if (setScr.isDirty()) {
			loadBackgroundImage();
			setColor(Setting.invertColor); 
			refreshPage();
		}
		setScr = null;
//		startAutoQuitTimer(); // 启动自动退出定时
	}
	
	private void refreshPage() {
		setPage(); // 计算页面相关设置
		txtBook.jumpTo(txtBook.getCurrentOffset(), TextBook.BUFFER_SIZE, false);
		txtBook.setCurrentLine(0);
		invalidate();
	}
	
	/**
	 * 设置颜色
	 */
	private void setColor(int invert) {
		int mask = invert * 0xFFFFFFFF; 
		TXT_BG = Setting.returnBGColor() ^ mask;
		TXT_FG = Setting.returnFGColor() ^ mask;
		LINE_FG = Setting.returnLineFGColor() ^ mask;
	}
	
	/**
	 * 载入背景图片
	 */
	private void loadBackgroundImage() {
		bmpBackground = null;
		if (Setting.bgType[mode] > 0) {
			bmpBackground = Setting.loadBackgroundImage(Setting.imageFileName[mode]);
			if (bmpBackground != null) {
				IMAGE_W = Math.min(MAX_W, bmpBackground.getWidth());
				IMAGE_H = Math.min(MAX_H, bmpBackground.getHeight());
			}
			else
				Setting.bgType[mode] = 0;
		}
	}
	
	/**
	 * 显示当前时间和进度
	 */
	private void showInfo() {
		InfoScreen bid = new InfoScreen((int) getOffset(), (int)txtBook.getBookSize());
		UiApplication.getUiApplication().pushModalScreen(bid);
		bid = null;
	}
	
	/**
	 * 读入相关记录
	 */
	private void loadHistory() {
		bookRecord = new Record(Record.HISTORY_NAME);
		int id = bookRecord.findRecord(bookName);
		int offset = (id < 0) ? 0 : bookRecord.bookOffset;
		bookRecord = null;

		txtBook.jumpTo(offset, TextBook.BUFFER_SIZE, false);
		txtBook.setCurrentLine(0);
	}
	
	/**
	 * 保存阅读进度
	 */
	private void saveHistory() {
		bookRecord = new Record(Record.HISTORY_NAME);
		bookRecord.findRecord(bookName);
		bookRecord.bookName = this.bookName;
		bookRecord.bookOffset = txtBook.getCurrentOffset() ;
		bookRecord.saveBookHistory();

		Setting.fullScreen = (isFullScreen) ? 1 : 0;
		Setting.lastBookName = bookName;
		Setting.lastBookOffset = txtBook.getCurrentOffset();
		
		bookRecord.setStoreName(Record.SETTING_NAME);
		bookRecord.saveSetting();
		bookRecord = null;
	}
	
	/**
	 * 切换全屏/非全屏
	 * @param on - true表示全屏
	 */
	private void toggleFullScreen() {
		isFullScreen = !isFullScreen;
		Setting.fullScreen = (isFullScreen) ? 1 : 0;;
		setPage();
		invalidate();	
	}
	
	/**
	 * 清除背景
	 * @param g - Graphics对象
	 */
	protected void clearBackground(Graphics g){
		if (Setting.bgType[mode] == 0) {
			g.setBackgroundColor(TXT_BG);
			g.clear();
		}
		else if (Setting.bgType[mode] > 0){
			g.drawBitmap(0, TITLE_HEIGHT, IMAGE_W, IMAGE_H - TITLE_HEIGHT, bmpBackground, 0, 0);
			g.setGlobalAlpha(255);
		}
	}

	/**
	 * 画标题栏
	 * @param g - Graphics对象
	 * @param title - 标题文字
	 */
	private void drawTitle(Graphics g) {

		// 设置标题栏背景色
		g.setBackgroundColor(TITLE_BG);
		g.clear(0, 0, MAX_W, TITLE_HEIGHT);
		
		// 画标题栏文字 
		g.setColor(TITLE_FG);
		g.drawText(bookTitle + "   " + df.format(new Date()), 2, 1);
		
		// 画底部进度条
		int y = MAX_H - PB_HEIGHT ;
		long l = getOffset() * MAX_W / txtBook.getBookSize(); 
		int w = (int)l;
		g.setColor(Color.NAVY);
		g.fillRect(0, y, w, PB_HEIGHT);
		g.setColor(Color.LIGHTSKYBLUE);
		g.fillRect(w, y, MAX_W - w , PB_HEIGHT);
	}

	protected void paint(Graphics g){
		drawPage(g, txtBook.getCurrentLine());
		if (!isFullScreen) {
			drawTitle(g);
		}
	}

//	private int optimize;
	/**
	 * 从指定行开始填充一页
	 * @param g - Graphis对象
	 * @param start - 本页内第一行的行号
	 * @return - 本页所用行数
	 */
    private void drawPage(Graphics g, int start) {
		clearBackground(g);
		g.setDrawingStyle(DrawStyle.TOP | DrawStyle.LEFT, true);
		int lastLine = getLastPageLine(start);
		
		if (Setting.drawDashLine > 0) { //画虚线 
			g.setColor(LINE_FG);  //设虚线颜色
			g.setStipple(0xCCCCCCCC);  //设线型为虚线 
			int ly = lineTopY;  // 虚线的Y坐标
			for (int i = start; i < lastLine ; i++) {
				g.drawLine(0, ly, maxLineX, ly);
				ly += deltaY;
			}
		}
		int ty = topY;
   		g.setColor(TXT_FG);
		for (int i = start; i < lastLine ; i++) {
		   		//显示文字
   			String txtLine = txtBook.getLineText(i); // 根据编码转为字符串
   			int w = currentFont.getAdvance(txtLine); //得到整行宽度像素值
   			if (w < min || w >= max) 
   				g.drawText(txtLine, LEFT_MARGIN, ty); // 不是满行则直接画该行
   			else { 
//   	   			Dialog.alert(txtLine+ String.valueOf(w));
	   			int len = txtLine.length(); 
	   			int leftPixel = max - w;
	   			int cs = leftPixel / (len  - 1); //字符间距
	   			leftPixel %= (len  - 1); // 扣除字符间距后多出的像素
   				int x0 = LEFT_MARGIN;
				for (int t = 0; t < len; t++) { 
	   				int dx = g.drawText(txtLine.charAt(t), x0, ty, 0, 50); //一个字符宽度不应该超过50
	   				if (t < leftPixel) // 根据多余left_w像素，对前left_w个字符插入间距cs+1
	   					dx++; 
		   				x0 += cs + dx;
	   			}
   			}
   			txtLine = null;
   			ty += deltaY;  
		}
		g.setGlobalAlpha(255);
    }

    /////////////打开下本书/////////////////////////
    /**
     * 已经到书尾了，打开下一本
     */
   private String[] filter = {"*.txt","*.TXT","*.Txt"};
   private String getNextBook() {
 		Vector v = new Vector();
    	int i = bookName.lastIndexOf('/') + 1;
    	String path = bookName.substring(0, i);
		String bookFilename = bookName.substring(i);
    	//列出当前书所在目录中符合后缀要求的文件
    	try {
			int len = filter.length;
			FileConnection fc = (FileConnection)Connector.open(path);
			for (int t = 0; t < len; t++) {
				Enumeration list = fc.list(filter[t], false);
				while (list.hasMoreElements())
					v.addElement(list.nextElement());
				list = null;
			}
		} catch (IOException e) {
			return null;
		}
		String nextBook;
		if (v.size() == 0)
			nextBook = null;
		else {
			//对文件列表进行排序
			String[] books = new String[v.size()];
			v.copyInto(books);
			v.removeAllElements();
			v = null;
			Arrays.sort(books, new bookCompare());
			//找到列表中的下一本书，返回文件名
			i = Arrays.getIndex(books, bookFilename);
			if (i < books.length - 1) 
				nextBook = path + books[i + 1];
			else 
				nextBook = null;
			books = null;
		}
		return nextBook;
   }

    final class bookCompare implements Comparator {
	   	public int compare(Object o1, Object o2) {
	   		String x1 = (String)o1;
	   		String x2 = (String)o2;
   			return StringUtilities.compareToIgnoreCase(x1, x2);
	   	}
   }

    /**
     * 打开书
     * @param nextBook - 书的文件名
     */
    private void openNextBook(String nextBook) {
		saveHistory();
		bookName = nextBook;
		txtBook.clear();
		txtBook = null;
		txtBook = new TextBook(bookName);
		txtBook.setPage(currentFont, LINE_PER_PAGE, MAX_W - LEFT_MARGIN - RIGHT_MARGIN);
		bookTitle = txtBook.getBookName();
		loadHistory();  //读记录
		invalidate();
    }
    /////////////打开下本书结束/////////////////////////
   
	/**
	 * 向下翻行
	 * @param lines - 需要向下滚动的行数 
	 */
    private void pageDown(int lines) {
		if (txtBook.EOF && (txtBook.line_number - txtBook.getCurrentLine() <= LINE_PER_PAGE)) {
			//弹出对话框 
			String nextBook = getNextBook();
			if (nextBook != null) {
				String prompt = "打开: " + nextBook.substring(nextBook.lastIndexOf('/')+1);
				if (Dialog.ask(Dialog.D_YES_NO, prompt) == Dialog.YES) 
					openNextBook(nextBook);					
			}
 			else 
				return ;
		}
		else {
			txtBook.scrollDown(lines);
			invalidate();	
		}
	}

	/**
	 * 向上翻行
	 * @param lines - 需要向上滚动的行数 
	 */
    private void pageUp(int lines) {
		if (txtBook.getCurrentLine() == 0 && txtBook.BOF)
			return;
		txtBook.scrollUp(lines);
		invalidate();
	}
	
	/**
	 * 跳转到文件结尾
	 */
    private void pageBottom() {
		txtBook.jumpToBottom();
		invalidate();
	}

	/**
	 * 跳转到文件起始
	 */
    private void pageTop() {
		txtBook.jumpTo(0, TextBook.BUFFER_SIZE, false);
		txtBook.setCurrentLine(0);
		invalidate();
	}

    private int getLastPageLine(int start) {
    	int lastLine = txtBook.getCurrentLine() + LINE_PER_PAGE;
		int ln = txtBook.line_number;
		lastLine = (lastLine > ln ) ? ln : lastLine;
		return lastLine;
    }
    /**
     * 计算当前进度 
     * @return - 当前进度=当前页面最后一个字的偏移量
     */
    private long getOffset() {
    	int pageLastLine = getLastPageLine(txtBook.getCurrentLine());
    	long offset;
		if (pageLastLine == txtBook.line_number && txtBook.BOF && txtBook.EOF)
			offset = txtBook.getBookSize(); // 小文件, 一页内就能显示下
		else {
	    	if (pageLastLine == txtBook.line_number)
	    		pageLastLine--;
			offset = txtBook.getLineOffset(pageLastLine);
		}
		return offset;
    }
    
	/**
	 * 从给定偏移量跳转
	 * @param value - 偏移量
	 */
    private void pageJump() {
		JumpScreen js = new JumpScreen((int) getOffset(), (int) txtBook.getBookSize());
        UiApplication.getUiApplication().pushModalScreen(js);
		int v = js.getValue();
		js = null;
		if (v >= 0) {
			if (!(txtBook.BOF && txtBook.EOF)) {
				long  x =  v * txtBook.getBookSize() / 10000;
				txtBook.jumpTo((int)x, TextBook.BUFFER_SIZE, true);
			}
			txtBook.setCurrentLine(0);
			invalidate();
		}
	}
	
    private boolean doScroll(int keycode) {
		int pn = LINE_PER_PAGE - Setting.pageScroll;
		char key = Keypad.map(keycode);
		switch(key) {
		case Characters.LATIN_SMALL_LETTER_D :
			if (isReducedKeyboard)
   				pageUp(pn);
			else
				toggleFullScreen();
			return true;
		case Characters.LATIN_SMALL_LETTER_J :
			if (isReducedKeyboard)
   				pageDown(pn);
			else
				toggleFullScreen();
			return true;
		case Characters.SPACE: 
		case Characters.ENTER: 
		case Characters.LATIN_SMALL_LETTER_F:
		case Characters.LATIN_SMALL_LETTER_K:
			//下一页
			pageDown(pn);
			return true;
		case Characters.LATIN_SMALL_LETTER_H:
		case Characters.LATIN_SMALL_LETTER_S:
				//上一页 
			pageUp(pn);
			return true;
		case Characters.LATIN_SMALL_LETTER_E:
		case Characters.LATIN_SMALL_LETTER_U:
			// 上一行
			pageUp(1);
			return true;
		case Characters.LATIN_SMALL_LETTER_X:
		case Characters.LATIN_SMALL_LETTER_N:
			// 下一行
			pageDown(1);
			return true;
		}
		return false;
    }
    
    protected boolean keyDown(int keycode, int time) {
    	if (doScroll(keycode))
    		return true;
   		return super.keyDown(keycode, time);
    }
    
    protected boolean keyRepeat(int keycode, int time) {
    	doScroll(keycode);
		return super.keyRepeat(keycode, time);
    }
    
    private boolean isReducedKeyboard; // 判断是否是ST键盘
    protected boolean keyChar(char key, int status, int time) {	
		switch(key) {
    		case Characters.LATIN_SMALL_LETTER_Z:
    			// 页面跳转
     			pageJump(); 
     			return true;
    		case Characters.LATIN_SMALL_LETTER_O:
    			// 设置
     			showSetting(); 
     			return true;
    		case Characters.LATIN_SMALL_LETTER_C:
    			//显示时间和进度
    			showInfo();
     			return true;
      		case Characters.DIGIT_ZERO:
       			Setting.invertColor = (~Setting.invertColor) & 0x00000001;
    			setColor(Setting.invertColor); 
       			invalidate();
     			return true;
    		case Characters.LATIN_SMALL_LETTER_A:
    			//添加书签
    			addBookMark();   
     			return true;
    		case Characters.LATIN_SMALL_LETTER_L:
    			//打开书签
    			showBookMark();   
     			return true;
    		case Characters.LATIN_SMALL_LETTER_V:
    			//打开搜索
    			search();   
     			return true;
    		case Characters.LATIN_SMALL_LETTER_G: //为ST键盘设计G切换全屏
    			if (isReducedKeyboard)
    				toggleFullScreen();
    			return true;
     		case Characters.LATIN_SMALL_LETTER_M:
    			//打开搜索
    			HelpScreen hs = new HelpScreen(false);   
    			UiApplication.getUiApplication().pushModalScreen(hs);
    			hs = null;
     			return true;
    		case Characters.LATIN_SMALL_LETTER_I:
    			Setting.FontSizeDec(); //字体减小
    			refreshPage() ;
    			return true;
    		case Characters.LATIN_SMALL_LETTER_Y :
    			Setting.FontSizeInc(); //字体增大
    			refreshPage() ;
    			return true;
    		case Characters.LATIN_SMALL_LETTER_T:
    			//关闭窗口，返回上级界面
    			if (mode == 0)
    				mode = 1;
    			else
    				mode = 0;
    			Setting.mode = mode;
    			loadBackgroundImage();
    			refreshPage();
     			return true;
    		case Characters.LATIN_SMALL_LETTER_R:
    			//关闭窗口，返回上级界面
    			windowClose();
     			return true;
    		case Characters.LATIN_SMALL_LETTER_P:
    			//切换背光常亮
    			if (!GBBMain.isLightOn) {
    				GBBMain.isLightOn = true;
    				Dialog.alert("背光长亮(2分钟)开启");
    				Backlight.setTimeout(120);
    			}
    			else {
    				Dialog.alert("背光长亮关闭");
    				RestoreBackLight();    			
    			}
    			return true;
    		case Characters.ESCAPE : // 按返回键时把程序转入后台
    			if (Setting.escToBackground > 0) {
    				saveHistory();
    				UiApplication.getUiApplication().requestBackground();
    			}
    			else 
    				windowClose();
    			return true;
    		case Characters.LATIN_SMALL_LETTER_Q:
    			//退出程序
    			quit();   
    			break;
		}
		return super.keyChar(key, status, time);
     }
    
    private void RestoreBackLight() {
		GBBMain.isLightOn = false;
    	int defaultTime = Backlight.getTimeoutDefault();
    	Backlight.setTimeout(defaultTime);
    }
    
    protected boolean navigationMovement(int dx,int dy,int status,int time) {
//    	enableBackLight();
    	int pn = LINE_PER_PAGE - Setting.pageScroll;
    	if (dx > 0 && useTrackBall) {
			pageDown(pn);
			return true;
    	} else if (dx < 0 && useTrackBall) {
			pageUp(pn);
			return true;
    	} else if (dy > 0) {
    		if (GBBMain.isTrackWheel)
   				pageDown(pn);
    		else 
    			pageDown(dy);
			return true;
    	} else if (dy < 0) {
    		if (GBBMain.isTrackWheel)
   				pageUp(pn);
    		else
    			pageUp(-dy);
			return true;
    	}
    	return super.navigationMovement(dx, dy, status, time);
    }

    protected boolean navigationClick(int status, int time) {
    	if(!GBBMain.isTrackWheel) {
			showInfo();
			return true;
    	}
		return super.navigationClick(status, time);
    }

    private void windowClose() {
		saveHistory();
		RestoreBackLight();
		txtBook.clear();
		txtBook = null;
		close();
    }
    
////TextScreen Class ends////////////////////////////////////////
}
