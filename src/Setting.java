import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Ui;

public final class Setting {
	private static Font font; // 字体
	
	public static int useTrackBall;  //滚轮左右翻页 
	public static int fontSizeIndex;    //字体大小索引
	public static int fontSytleIndex;	//字体类型索引
	public static int[] fgColorIndex = {0,0};		//前景色索引
	public static int[] bgColorIndex = {0,0};		//背景色索引
	public static int[] lineColorIndex = {0,0};		//背景色索引
	public static int fullScreen;       // =1全屏幕；=0非全屏
	public static int drawDashLine;		// =1 画虚线；=0不画线
	public static int mode;		// 日夜模式  
	public static int openLastBook;     // =1打开上次读书；=0不打开
	public static int lastBookOffset;         //最近打开的书偏移量
	public static int invertColor;    //=1 反色； =0不反色
	public static int antiAliasIndex;    //反锯齿设置
	public static int lineSpace;    //行距
	public static int RecordID;
	public static int escToBackground; // esc放后台，=0不放后台，=1放后台
	public static int pageScroll; // =0 翻页是不保留一行，=1保留一行
	public static int[] bgType = {0,0}; // =0 背景纯色，=1 背景图片
	public static int cycle;  // 列表循环 =1循环
//	public static int fgColorIndex2; //图片透明度
//	public static int bgColorIndex2;
//	public static int lineColorIndex2;
//	public static int bgType2;
	public static String nullStr = "string"; // 字符串初始值
	public static String lastBookName; //最近打开的书全路径名
	public static String lastPath; //最近打开的书路径
	public static String reservedStr1; 
	public static String[] imageFileName = {nullStr, nullStr}; //背景图片全路径名
	public static String lastImgPath; //最近打开的图片路径
//	public static String imageFileName2;
	public static String reservedStr4;
	
	private static int[] style = {Font.PLAIN, Font.BOLD, Font.EXTRA_BOLD, Font.BOLD | Font.ITALIC , Font.ITALIC};
	private static int[] anti = {Font.ANTIALIAS_NONE, Font.ANTIALIAS_STANDARD, Font.ANTIALIAS_LOW_RES, Font.ANTIALIAS_SUBPIXEL};
	public static int[] colorList = {	
		Color.BLACK,
		Color.WHITE,
		Color.ANTIQUEWHITE,
		Color.NAVAJOWHITE,
		Color.SILVER,
		Color.SNOW,
		Color.WHEAT,
		Color.WHITESMOKE,
		Color.GHOSTWHITE,

		Color.LIGHTGREY,
		Color.DARKGRAY,
		Color.DARKSLATEGRAY,
		Color.DIMGRAY,
		Color.GRAY,
		Color.LIGHTSLATEGRAY,
		Color.SLATEGRAY,

		Color.DARKRED,
		Color.INDIANRED,
		Color.MEDIUMVIOLETRED,
		Color.ORANGERED,
		Color.PALEVIOLETRED,
		Color.RED,
		Color.MISTYROSE,
		Color.TOMATO,

		Color.GREEN,
		Color.DARKGREEN,
		Color.DARKSEAGREEN,
		Color.DARKOLIVEGREEN,
		Color.FORESTGREEN,
		Color.LIGHTGREEN,
		Color.LIGHTSEAGREEN,
		Color.LAWNGREEN,
		Color.PALEGREEN,
		Color.LIMEGREEN,
		Color.SPRINGGREEN,
		Color.MEDIUMSEAGREEN,
		Color.MEDIUMSPRINGGREEN,
		Color.SEAGREEN,
		Color.YELLOWGREEN,

		Color.ALICEBLUE,
		Color.BLUE,
		Color.CADETBLUE,
		Color.CORNFLOWERBLUE,
		Color.DARKBLUE,
		Color.DARKSLATEBLUE,
		Color.DEEPSKYBLUE,
		Color.LIGHTBLUE,
		Color.LIGHTSKYBLUE,
		Color.LIGHTSTEELBLUE,
		Color.MEDIUMBLUE,
		Color.MEDIUMSLATEBLUE,
		Color.MIDNIGHTBLUE,
		Color.POWDERBLUE,
		Color.ROYALBLUE,
		Color.SKYBLUE,
		Color.NAVY,
		Color.STEELBLUE,
		Color.SLATEBLUE,
		
		Color.GREENYELLOW,
		Color.LIGHTGOLDENRODYELLOW,
		Color.LIGHTYELLOW,
		Color.YELLOW,
		
		Color.CYAN,
		Color.DARKCYAN,
		Color.LIGHTCYAN,
	
		Color.BROWN,
		Color.ROSYBROWN,
		Color.SADDLEBROWN,
		Color.SANDYBROWN,

		Color.DEEPPINK,
		Color.HOTPINK,
		Color.LIGHTPINK,
		Color.PINK,

		Color.ORANGE,
		Color.DARKORANGE,

		Color.PURPLE,
		Color.MEDIUMPURPLE,


		Color.AQUA,
		Color.AQUAMARINE,
		Color.AZURE,
		Color.BEIGE,
		Color.BISQUE,
		Color.BLANCHEDALMOND,
		Color.BLUEVIOLET,
		Color.BURLYWOOD,
		Color.CHARTREUSE,
		Color.CHOCOLATE,
		Color.CORAL,
		Color.CORNSILK,
		Color.CRIMSON,
		Color.DARKGOLDENROD,

		Color.DARKKHAKI,
		Color.DARKMAGENTA,
		Color.DARKORCHID,
		Color.DARKSALMON,
		Color.DARKTURQUOISE,
		Color.DARKVIOLET,
		Color.FIREBRICK,
		Color.FLORALWHITE,
		Color.FUCHSIA,
		Color.GAINSBORO,
		Color.GOLD,
		Color.GOLDENROD,
		Color.HONEYDEW,
		Color.INDIGO,
		Color.IVORY,
		Color.KHAKI,
		Color.LAVENDER,
		Color.LAVENDERBLUSH,
		Color.LEMONCHIFFON,
		Color.LIGHTCORAL,
		Color.LIGHTSALMON,
		Color.LIME,
		Color.LINEN,
		Color.MAGENTA,
		Color.MAROON,
		Color.MEDIUMAQUAMARINE,
		Color.MEDIUMORCHID,
		Color.MEDIUMTURQUOISE,
		Color.MINTCREAM,
		Color.MOCCASIN,
		Color.OLDLACE,
		Color.OLIVE,
		Color.OLIVEDRAB,
		Color.ORCHID,
		Color.PALEGOLDENROD,
		Color.PALETURQUOISE,
		Color.PAPAYAWHIP,
		Color.PEACHPUFF,
		Color.PERU,
		Color.PLUM,
		Color.SALMON,
		Color.SEASHELL,
		Color.SIENNA,
		Color.TAN,
		Color.TEAL,
		Color.THISTLE,
		Color.TURQUOISE,
		Color.VIOLET
	};

	public static void init() {
//		fontFamilyIndex = 0;
		fontSizeIndex = 8;
//		fontSytleIndex = 0;
//		fgColorIndex = 0;
		fgColorIndex[1] = 1;
		bgColorIndex[0] = 1;
		drawDashLine = 1;
//		bgColorIndex2 = 0;
//		openLastBook = 0;
//		lastBookOffset = 0;
		fullScreen = 1;
//		invertColor = 0;
		lineSpace = 1;
//		antiAliasIndex = 0;
//		escToBackground = 0;
//		pageScroll = 0;
		lineColorIndex[0] = 9;
		lineColorIndex[1] = 9;
//		bgType = 0;
//		reservedInt4 = 0;
//		reservedInt5 = 0;
//		reservedInt6 = 0;
//		reservedInt7 = 0;
		cycle = 1;
		lastBookName = nullStr;
		lastPath = nullStr;
		reservedStr1 = nullStr;
		imageFileName[0] = nullStr;
		lastImgPath = nullStr;
		imageFileName[1] = nullStr;
		reservedStr4 = nullStr;
		
		RecordID = -1;
	}

//	public  static Font getFont(int familyID, int styleID, int sizeID, int antiAliasMode) {
	public  static Font getFont(int styleID, int sizeID, int antiAliasMode) {
		int sty = style[styleID];
		int aam = anti[antiAliasMode];
		int height = sizeID + 12;
//		Font f = fontFamily[familyID].getFont(sty, height, Ui.UNITS_px); 
		Font f = Font.getDefault(); 
		return f.derive(sty,height, Ui.UNITS_px, aam, 0);
	}

	/**
	 * 返回字体
	 * @return
	 */
	public static Font getFont() {
		int aam = anti[antiAliasIndex];
//		fontEffect = Font.COLORED_OUTLINE_EFFECT;
//		fontEffect = Font.DROP_SHADOW_RIGHT_EFFECT;
//		fontEffect = Font.EMBOSSED_EFFECT;
//		fontEffect = Font.ENGRAVED_EFFECT; 
		int sty = style[fontSytleIndex];
		int height = fontSizeIndex + 12;
//		Font f = fontFamily[fontFamilyIndex].getFont(sty, height, Ui.UNITS_px); 
		Font f = Font.getDefault();
		font =  f.derive(sty,height, Ui.UNITS_px, aam, 0); //最后一个参数0是glyph effect, 但是没有文档
		return font;
	}
	
	public static void FontSizeInc() {
		if (fontSizeIndex < 28)
			fontSizeIndex++;
	}
	
	public static void FontSizeDec() {
		if (fontSizeIndex > 0)
			fontSizeIndex--;
	}

	public static int returnColor(int id) {
		return colorList[id];
	}

	/**
	 * 返回前景颜色
	 * @return
	 */
	public static int returnFGColor() {
		return colorList[fgColorIndex[mode]];
	}

	/**
	 * 返回背景颜色
	 * @return
	 */
	public static int returnBGColor() {
		return colorList[bgColorIndex[mode]];
	}

	/**
	 * 返回虚线颜色
	 * @return
	 */
	public static int returnLineFGColor() {
		return colorList[lineColorIndex[mode]];
	}

	/**
	 * 如果字符串为空则置为string,否则写入RMS会出错
	 * @param s - 要检查的字符串
	 * @return - 检查并处理后的字符串
	 */
	public static String isNull(String s) {
		if (s == null || s.length() < 1)
			return nullStr;
		else
			return s;
	}
	
	/**
	 * 检查文件是否还存在
	 * @param bookName - 文件名
	 * @return - true:文件存在, false:文件不存在
	 */
	public static boolean bookExists(String bookName) {
		boolean result = false;
		FileConnection fconn;
		try {
			fconn = (FileConnection)Connector.open(bookName);
		    if (fconn.exists())  
		    	result = true;
	    	fconn.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	fconn = null;
    	return result;
	}

	/**
	 * 载入背景图片
	 */
	public static Bitmap loadBackgroundImage(String imgFile) {
		Bitmap bmp = null;;
		byte[] imgBuffer = null;
		FileConnection fconn = null;
		try {
			fconn = (FileConnection)Connector.open(imgFile);
			if ((fconn != null) && fconn.exists()) {
		        int size = (int) fconn.fileSize();
		        imgBuffer = new byte[size];
		        InputStream s;	
			    s = fconn.openInputStream();	
				s.read(imgBuffer);
			    s.close();
			    s = null;
			    fconn.close();
				EncodedImage image = EncodedImage.createEncodedImage(imgBuffer, 0, imgBuffer.length, "image/jpeg");
				bmp = image.getBitmap();
				imgBuffer = null;
				image = null;
			}
		} catch (IOException e) {
			bmp = null;
		}
	    fconn = null;
		return bmp;
	}

}
