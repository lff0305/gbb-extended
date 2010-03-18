
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.util.Arrays;

public final class TextBook {
	// 与编码格式有关
	private final static String ENC_UTF16BE = "UTF-16BE";
	private final static String ENC_UTF8 = "UTF-8";
	private final static String ENC_GB2312 = "GB2312";
	private final static int ID_UTF8 = 3; 
	private final static int ID_UTF16LE = 2; 
	private final static int ID_UTF16BE = 1; 
	private final static int ID_GB2312 = 0; 
	public final static int BUFFER_SIZE = 4096 ; // 缓冲区大小
	private int formatID;
	
	
	//与文件操作有关的变量
	private String fullBookName;  // 全路径书名
	private String bookName;  // 无路径书名
	private long bookSize; // size of the book
	private FileConnection fconn;
    private InputStream di;	
    private String bookFormat; // 文件格式
	public boolean EOF; //当前缓冲区已经到达文件末尾 
	public boolean BOF; //当前缓冲区已经到达文件起始 
    //////////////////////////
    
    //这些变量与缓冲区操作及当前窗口第一行有关
    private int[] line_offset; //每行偏移量
    private short[] line_width;  //每行长度
    private byte[] bufferTxt; 
	public  int line_number;  //总行数
	private int currentOffset; // 当前文件指针，只在locate()及read()中被修改
	private int currentLine;  //当前显示页面第一行在缓冲区内所处行数 
	private int bufferHeadOffset; // 缓冲区首对应的文件偏移量
    private int BufferLen; // 缓冲区实际长度

	///////////////////////////
	
	//与排版有关的变量
	private Font fontCurrent; //当前画布所用字体
	private int gbWidth; //中文字宽度
	private int pageWidth; // 屏幕可画宽度
	private int LINE_PER_PAGE; // 每页行数
	//////////////////////////
	
	public TextBook(String fileName) {
		setFullBookName(fileName);
		byte[] headOfBook = new byte[3];
		try {
		    fconn = (FileConnection)Connector.open(fileName);
	        bookSize = fconn.fileSize();
	        InputStream s;	
		    s = fconn.openInputStream();	
			s.read(headOfBook); //读文件头三个字节，判断文件编码格式
		    s.close();
		    s = null;
		    fconn.close();
		    fconn = null;
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
		// 判断编码格式
		if ((headOfBook[0] == -2) && (headOfBook[1] == -1)) {
			bookFormat =  ENC_UTF16BE;
			formatID = ID_UTF16BE;
		}
		else if ((headOfBook[0] == -1) && (headOfBook[1] == -2)) {
			bookFormat =  ENC_UTF16BE;
			formatID = ID_UTF16LE;
		}
		else if ((headOfBook[0] == -17) && (headOfBook[1] == -69) &&(headOfBook[2] == -65))
		{
			bookFormat = ENC_UTF8;  // UTF8 with BOM
			formatID = ID_UTF8;
		}
		else {
			bookFormat = ENC_GB2312;
			formatID = ID_GB2312;
		}
		
		line_offset = new int[1024]; //初始化行偏移量数组，姑且假定4K数据不会分成1024行
		line_width = new short[1024];  //初始化行长度数组
//		line_offset[0] = 0;
//		line_number = 0;
//		currentLine = 0;
//		currentOffset = 0;
		BOF = true;
//		EOF = false;
	}

	public void setPage(Font f, int line, int width) {
		fontCurrent = f;
		gbWidth = f.getAdvance('中');
		LINE_PER_PAGE = line;
		pageWidth = width;
	}

	/**
	 * 设置当前页面第一行对应于缓冲区内的行数
	 * @param line - 行号
	 */
	public void setCurrentLine(int line) {
		currentLine = line;
	}
	
	/**
	 * 返回当前页面第一行对应于缓冲区内的行数
	 * @return - 当前页面第一行对应于缓冲区内的行数
	 */
	public int getCurrentLine() {
		return currentLine;
	}
	
	/**
	 * 设置书名
	 * @param fileName 全路径书名
	 */
	public void setFullBookName(String fileName) {
		fullBookName = fileName;
		int i = fileName.lastIndexOf('/') + 1;
		int t = fileName.lastIndexOf('.');
		bookName = fileName.substring(i, t);
	}

	/**
	 * 获取无路径书名
	 * @return - 无路径书名
	 */
	public String getBookName() {
		return bookName;
	}

	/**
	 * 获取文件大小
	 * @return - 文件大小
	 */
	public long getBookSize() {
		return bookSize;
	}

	public long getLineOffset(int line) {
		return bufferHeadOffset + line_offset[line] + line_width[line];
	}
	
	private void Error() {
		Dialog.alert("文件访问错误!");
	}
	
	/**
	 * 打开文件
	 */
	private void open() {
        try {
		    fconn = (FileConnection)Connector.open(fullBookName);
		    di = fconn.openInputStream();
        } catch (IOException ioe) {
//            ioe.printStackTrace();
        	Error();
        }
    }

    /**
     * 关闭文件
     */
	private void close() {
        if (fconn != null)
	    	try {
	        	fconn.close();
	        	if (di != null)
	        		di.close();
	            di = null;
	            fconn = null;
	        } catch (IOException ioe) {
//	            ioe.printStackTrace();
	        	Error();
	        }
    }
    
    /**
     * 关闭时清理缓冲区及行索引数组 
     */
    public void clear() {
    	close();
    	line_offset = null;
    	line_width = null;
    	bufferTxt = null;
    }

    /**
     * 得到当前页面第一行在文件中的偏移量 
     * @return
     */
    public int getCurrentOffset() {
    	return line_offset[currentLine] + bufferHeadOffset;
    }
    
    /**
     * 返回数据块中第一个回车的位置 
     * @param b - 数据块
     * @param tail - 数据块尾部
     * @return - 第一个回车的位置
     */
    /*
    private int firstReturn(byte[] b, int tail) {
        int t = 0; 
        int bLen = tail;
        while (t < bLen) {
        	if (b[t] == 10)  //遇到换行
           		break;
            t++;
        }
        if (t == bLen) { //没有一个回车符,异常情况
//        	System.out.println("No return in buffer, STRANGE!!");
        	return 0;
        }
    	return t;
    }*/
 
    /**
     * 定位到文档偏移处
     * @param i int 偏移的位置
     */
    private void locate(int i) {
    	if (i >= bookSize)
    		return;
    	if (i <= 0 )  
            i = 0;
    	BOF = ( i == 0) ? true : false;
        close();
        open();
        currentOffset = i;
        try {
			di.skip(i);
		} catch (IOException e) {
//			e.printStackTrace();
        	Error();
		}
    }

    /**
     * 从当前文件偏移位置读出len个byte
     * @param len - 指定读取长度
     * @return - 读取的数据块  
     */
    private byte[] read(int LENGTH) {
        byte b[];

        int buf_len = (int) Math.min(bookSize - currentOffset, LENGTH);
        EOF = (buf_len < LENGTH) ? true : false;
        b = new byte[buf_len];
        try {
            di.read(b);
            currentOffset += b.length;
        } catch (IOException ioe) {
        	Error();
            return null;
        }
        if (formatID == ID_UTF16LE) { //如果是UTF16LE,则处理缓冲区，使之成为UTF16BE
          	int len = b.length;
           	for (int i = 0; i < len; i+=2) {
           		byte c = b[i];
          		b[i] = b[i+1];
           		b[i+1] = c;
           	}
        }
        return b;
    }
    
    /**
     * 从指定偏移量出开始读缓冲区，处理头尾的回车，返回处理后的缓冲区第一行行号
     * @param start - 指定偏移量
     * @param length - 期望读入的数据长度
     * @param flag - true表示要删除头部回车
     */
  	  public void jumpTo(int start, int length, boolean flag) {
  		  	bufferTxt = null;
  		  	if (formatID == ID_UTF16LE || formatID == ID_UTF16BE)
  		  		start = start & 0xFFFFFFFE;
  		  	locate(start);
  		  	int offset = currentOffset;
  		    byte[] b = read(length); 
  		    
  		    //如果不是读到文件头，则 删除缓冲区中第一个回车以前的字符，包括回车本身
  		    int t = 0;
  		    int s = 0;
  		    
  		    byte ret = 10;
  		    if (flag && !BOF) {
// 		    	t = firstReturn(b, b.length);
  		    	t = Arrays.getIndex(b, ret); //得到第一个回车的位置
  		    	if (t > 0)
  		    		s = t + 1;
  		    }
  		    bufferHeadOffset  = offset + s; 

  		    int len = b.length - s;
  		    int m = 0; // 如果是UTF16格式，则缓冲区大小不变，因为UTF16每个字符都是2字节
  		    if (formatID == ID_UTF8) // 如果是utf8格式，则缓冲区加2，以免建立索引时溢出或缺字
  		    	m = 2;
  		    else if (formatID == ID_GB2312) // 如果是GB2312格式，则缓冲区加1
  		    	m = 1;
  		    BufferLen = m + len;
  		    bufferTxt = new byte[BufferLen];
  		    System.arraycopy(b, s, bufferTxt, 0, len);
  		    createIndex();
  		    b = null;
  	  }

  	   /**
       * 从start指定的起始行向上滚动lines指定的行数 
       * @param start - 指定的起始行(当前缓冲区内)
       * @param window - 窗口内可显示的行数 
       * @param lines - 向下滚动的行数 
       * @return - 滚动后的首行行号，如果需要追加数据，则返回行号对应于新的缓冲区
       */
      public void scrollUp(int lines) {
       	//判断是否需要读入新的数据
      	if (currentLine < lines ) { 
      		if (!BOF) {//当前缓冲区是否已经达到文件首 
      	    	int page_bottom_line = Math.min(line_number - 1, currentLine + LINE_PER_PAGE - 1);
      	    	int last = bufferHeadOffset + line_offset[page_bottom_line];
      	    	int offset = (last < BUFFER_SIZE) ? 0 : last - BUFFER_SIZE;
      			int len = last - offset;
      	    	jumpTo(offset, len , true);
      			currentLine = line_number - (LINE_PER_PAGE + lines) + 1;
      		}
      		else
      			currentLine = 0; // 当前行以上的行数目小于要上滚的行数且 缓冲区已经到达文件首，返回0
      	}
      	else
      		currentLine = currentLine - lines;
      }

    /**
     * 从start指定的起始行向下滚动lines指定的行数 
     * @param start - 指定的起始行(当前缓冲区内)
     * @param window - 窗口内可显示的行数 
     * @param lines - 向下滚动的行数 
     * @return - 滚动后的首行行号，如果需要追加数据，则返回行号对应于新的缓冲区
     */
    public void scrollDown(int lines) {
    	//判断是否需要读入新的数据
    	if (currentLine + LINE_PER_PAGE + lines >= line_number) {
    		//从缓冲区读数据 
    		if (!EOF ) {//判断当前缓冲区是否已经达到文件末尾 
    			jumpTo(bufferHeadOffset + line_offset[currentLine], BUFFER_SIZE, false);
    			createIndex();
    			currentLine = lines;
    		}
    		else { // 到达文件尾
    			int last_line = Math.min(line_number , currentLine + LINE_PER_PAGE);
    			int rest = line_number - last_line;
    			currentLine = (rest <= lines) ? currentLine + rest : currentLine + lines ;
     		}
    	}
    	else
    		currentLine = currentLine + lines;
    }

     /**
     * 跳转到文件结尾
     * @return - 最后一页的首行号
     */
    public void jumpToBottom() {
    	int offset; 
    	if (EOF) // 如果缓冲区已经是文件尾，直接返回行号
    		currentLine =Math.max(0, line_number - LINE_PER_PAGE);
    	else {
    		offset = (int) ((bookSize < BUFFER_SIZE) ? 0 : (bookSize - BUFFER_SIZE));
    		jumpTo(offset,  BUFFER_SIZE, false);
    		EOF = true;
            currentLine = line_number - LINE_PER_PAGE ;
    	}
    }

    private void createIndex() {
    	switch (formatID) {
	    	case ID_GB2312 :
	    		createIndexGB2312();
	    		break;
	    	case ID_UTF16LE :
	    	case ID_UTF16BE :
				createIndexUTF16BE();
	    		break;
	    	case ID_UTF8: 
	    		createIndexUTF8();
	    		break;
    	}
    }
    
     /**
     * 建立UTF8编码每行的偏移量及行长度(字节数)索引
     */
    private void createIndexUTF8() {
        if (bufferTxt == null) 
            return; 

        line_number = 0; // 初始时总行数=0
    	byte[] localBufferTxt = bufferTxt;
		Font f = fontCurrent;
		int pw = pageWidth;

    	int c = 0; // 遍历缓冲区指针
        int w = 0; // 行像素宽度 
        short l = 0; // 行占用字节数
        int nextW; // 增加下一个字符后的行宽度
        int len = BufferLen - 2 ;
        
		while (c < len) {
	      	int c1 = localBufferTxt[c] & 0x00ff ;
	       	if (c1 == 0x0A) { //\r 换行
	       		c++;
                l++;
	            addIndex(c, l);
	           	w = l = 0;
	           	continue;
	       	} 
	       	else if (c1 == 0x0D) { //\r 换行
	       		c++;
                l++;
	           	continue;
	       	}
	       	else if (c1 < 0x80) { //ascii 0xxxxxxx
	       		char x = (char)c1;
      			int inc = 1;
       			if (isLetter(x)) {
       				int t = getWord(c);
       				inc += (t - c);
       				nextW = w + getStringWidth(c, t);
       			}
       			else
       				nextW = w + f.getAdvance(x);
	           	if (nextW > pw) { //当前行无法显示完整
	               	//设置行索引
	           		addIndex(c, l);
	                w = l = 0;
	                continue; 
	           	} 
	           	else {
	               	c += inc;
	                l += inc;
	                w = nextW; 
	           	}
	       	}
		   	else {  //非ascii符号
		   		int b;
		   		if (c1 < 0xE0) { //双字节 110xxxxx 10xxxxxx 
		   			int c2 = localBufferTxt[c+1] & 0x00ff;
		   			b = ((c1 & 0x1F) << 6) | (c2 & 0x3F) ;
  	       			nextW = w + f.getAdvance((char)b) ; // 非CJK统一汉字
	           		if (nextW > pw) { //当前行无法显示完整
	          	        addIndex(c, l);
	           			w = l = 0;
	           			continue;
	                } 
	           		else {
	                    c += 2;
	                    l += 2;
	                    w = nextW;
	                }
		   		}
		   		else { //1110xxxx 10xxxxxx 10xxxxxx
		   			int c2 = localBufferTxt[c+1] & 0x00ff;
		   			int c3 = localBufferTxt[c+2] & 0x00ff;
		   			b = (c1 & 0x1F) << 12;
		   			b |= (c2 & 0x3F) << 6;
		   			b |= (c3 & 0x3F);	       			
		   			if (b >= 0x4E00 && b <= 0x9FA5)
		           		// CJK统一汉字在Unicode中分布于0x4E00 - 0x9FA5之间，假定一般小说不用冷僻字 
    	       			nextW = w + gbWidth;  
    	       		else
    	       			nextW = w + f.getAdvance((char)b) ; // 非CJK统一汉字
 	           		if (nextW > pw) { //当前行无法显示完整
	          	        addIndex(c, l);
	           			w = l = 0;
	           			continue;
	                } 
	           		else {
	                    c += 3;
	                    l += 3;
	                    w = nextW;
	                }
		   		}
	   		}
		}
       	addIndex(c,l);
       	localBufferTxt = null;
    }

    /**
     * 建立UTF16BE编码每行的偏移量及行长度(字节数)索引
     * @throws UnsupportedEncodingException 
     */
    private void createIndexUTF16BE(){
        if (bufferTxt == null) {
            return; 
        } 
    	line_number = 0; // 初始时总行数=0
    	byte[] localBufferTxt = bufferTxt;
		Font f = fontCurrent;
		int pw = pageWidth;

    	int c = 0; // 遍历缓冲区指针
        int w = 0; // 行像素宽度 
        short l = 0; // 行占用字节数
        int nextW = 0; // 增加下一个字符后的行宽度
        int len = BufferLen - 1;

        while (c < len) {
	      	int c1 = localBufferTxt[c] & 0x00ff ;
	      	int c2 = localBufferTxt[c+1] & 0x00ff ;
	      	int b = (c1 << 8) | c2;
	       	if (b == 10) { //\r 换行
	       		c += 2;
                l += 2;
	            addIndex(c, l);
	           	w = l = 0;
	           	continue;
	       	}
	       	else {
	       		int inc = 2;
	       		if (c1 >= 0x004E && c1 <= 0x009F) // CJK统一汉字在Unicode中分布于0x4E00 - 0x9FA5之间，假定一般小说不用冷僻字 
	       			nextW = w + gbWidth;  

	       		else {
	       			char x = (char)b;
	       			if (isLetter(x)) { //字母
	       				int t = getWordUTF16(c);
	       				inc += (t - c);
	       				nextW = w + getStringWidthUTF16(c, t);
	       			}
	       			else
	       				nextW = w + f.getAdvance(x) ; // 非CJK统一汉字
	       		}
           		if (nextW > pw) { //当前行无法显示完整
          	        addIndex(c, l);
           			w = l = 0;
           			continue;
                } 
           		else {
                    c += inc;
                    l += inc;
                    w = nextW;
                }
	       	} 
		}
   		addIndex(c, l);
    	localBufferTxt = null;
    }

    /**
     * 建立GB2312编码每行的偏移量及行长度(字节数)索引
     */
    private void createIndexGB2312() {
        if (bufferTxt == null) {
            return; 
        } 
    	line_number = 0; // 初始时总行数=0
    	
    	byte[] localBufferTxt = bufferTxt;
        int len = BufferLen - 1;
		Font f = fontCurrent;
		int pw = pageWidth;
		int gw = gbWidth;

		// GBK 符号A1-A9, 常用的在A1-A3
		// GBK2 B0-F7
		// GBK3 81 - A0
		// GBK4 AA - FE
    	int c = 0; // 遍历缓冲区指针
        int w = 0; // 行像素宽度 
        short l = 0; // 行占用字节数
        int nextW = 0; // 增加下一个字符后的行宽度
	    while (c < len) {
	    	int b = localBufferTxt[c] & 0x00ff;
	       	if (b == 10) { // 0x0A 换行
	       		c++;  
	       		l++;
	           //设置行索引
	       		addIndex(c, l);
	            w = l = 0;
	            continue;
	       	}
	            //字符处理
	       	if (b > 0x0080) { //汉字
	          	if (b > 0x00A0 && b < 0x00A4) {// 只单独判断GB2312 A1 - A3区
	          		char sym = GB2312SymToUnicode(localBufferTxt[c], localBufferTxt[c+1]);
	          		nextW = w + f.getAdvance(sym);  //计算标点符号的宽度
	          	}
	          	else
	          		nextW = w + gw ; // 中文
	          	if (nextW > pw) { //当前行无法显示完整
	        	//设置行索引
	                addIndex(c, l);
	        		w = l = 0;
	        		continue;
	            } 
	        	else {
	        		c += 2;
	                l += 2;
	                w = nextW;
	            }
	       	} else { //ascii
	       		if (b == 13) { //不计算0x0D
	            	c++;
	            	l++;
	            	continue;
	            }
       			char x = (char)b;
       			int inc = 1;
       			if (isLetter(x)) {
       				int t = getWord(c);
       				inc += (t - c);
       				nextW = w + getStringWidth(c, t);
       			}
       			else
       				nextW = w + f.getAdvance(x);
	           	if (nextW > pw) { //当前行无法显示完整
	               	//设置行索引
	           		addIndex(c, l);
	                w = l = 0;
	                continue; 
	           	} 
	           	else {
	               	c += inc;
	                l += inc;
	                w = nextW; 
	           	}
	       	}
	    }
       	addIndex(c, l );
    	localBufferTxt = null;
    }
   
    /**
     * 判断是否为字母
     * @param x - 字符
     * @return - true:是字母
     */
    private static boolean isLetter(char x) {
		return (x >= 'a' && x <= 'z') || (x >= 'A' && x <= 'Z');
    }
    
    private static boolean isContinue(char x) {
    	return (x == '\'') || (x == '.') || (x == ',') || (x == '"')|| (x == '@') || (x == '-');
    }
    
    /**
     * 取出一个英文单词, 用于GB2312及UTF8或者ascii编码
     * @param start - 缓冲区内开始位置
     * @return - 单词结束位置
     */
    private int getWord(int start) {
     	int i = start;
    	int len = BufferLen;
//    	int w = 0;
//    	while(i < len && w < pageWidth) {
    	while(i < len) {
    		char x = (char)bufferTxt[i];
    		if (isLetter(x) || isContinue(x)) {
//        		w += fontCurrent.getAdvance(x);
     			i++;
    		}
    		else
    			return i - 1;
    	}
    	return Math.min(len - 1, i);
    }
    
    /**
     * 得到一段ascii字符的长度
     * @param start 缓冲区开始位置
     * @param end 缓冲区结束位置
     * @return 字符总长度
     */
    private int getStringWidth(int start, int end) {
    	int w = 0;
    	for(int i = start; i <= end; i++) {
    		char b = (char)bufferTxt[i];
    		w += fontCurrent.getAdvance(b);
    	}
    	return w;
    }

    /**
     * 取出一个英文单词, 用于UTF16编码
     * @param start - 缓冲区内开始位置
     * @return - 单词结束位置
     */
        
	private int getWordUTF16(int start) {
     	int i = start;
    	int len = BufferLen - 1;
//    	int w = 0;
//    	while(i < len && w < pageWidth) {
    	while(i < len) {
    		char x = (char) (((bufferTxt[i] & 0x00ff) << 8) | (bufferTxt[i+1] & 0x00ff));
    		if (isLetter(x) || isContinue(x)) {
//        		w += fontCurrent.getAdvance(x);
     			i += 2;
    		}
    		else
    			return i - 2;
    	}
    	return Math.min(len - 1, i);
    }
    
    /**
     * 得到一段UTF16中英文字符的长度
     * @param start 缓冲区开始位置
     * @param end 缓冲区结束位置
     * @return 字符总长度
     */
   private int getStringWidthUTF16(int start, int end) {
    	int w = 0;
    	for(int i = start; i <= end; i+=2) {
    		char b = (char) (((bufferTxt[i] & 0x00ff) << 8) | (bufferTxt[i+1] & 0x00ff));
    		w += fontCurrent.getAdvance(b);
    	}
    	return w;
    }
    

     /**
     * 返回指定行的字符串
     * @param line - 行号
     * @return - 返回的字符串
     */
    public String getLineText(int line){
    	try {
			String txtLine = new String(bufferTxt, line_offset[line], line_width[line], bookFormat);
			return txtLine;
    	} catch (UnsupportedEncodingException e) {
    		return null;
    	}
    }
    
    /**
     * 添加索引
     * @param offset - 缓冲区中一行末尾指针
     * @param num - 一行长度字节数 
     */
    private void addIndex(int offset, short num) {
        line_offset[line_number] = offset - num;
        line_width[line_number++] = num;
    }
    
    private static char[] symbol_table;
    private static int symbolLen;
    /**
     * 根据当前字体建立GB2312标点符号转Unicode字符表
     */
    public static void initGB2312SymbolTable(){
     	byte[] code = new byte[600];
    	int c = 0;
    	for (int i = 0x00A1; i< 0x00A4; i++)
    		for(int j = 0x00A1; j < 0x00FF; j++) {
    			code[c++] = (byte) i;
    			code[c++] = (byte) j;
    		}
    	String cs = null; 
    	try {
			cs = new String(code, 0, c, ENC_GB2312);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		code = null;
		symbol_table = cs.toCharArray();
		symbolLen = cs.length();
		cs = null;
   }
    
    /**
     * GB2312标点符号转为UNICODE字符
     * @param i 第一字节
     * @param j 第二字节
     * @return  Unicode字符
     */
    private static char GB2312SymToUnicode(byte i, byte j) {
    	int a = ((i  - 0xA1) & 0xff) << 1;
    	int b = j & 0xff - 0xA1;
//   	c = a * 94 + b = a*64 + a*32 - a*2 + b
		int c = (a << 5) + (a << 4) - a + b;
		if (c >= 0 && c < symbolLen)
			return symbol_table[c];
		else
			return '?';
    }
    
    /****************以下与搜索相关的部分************************/

    private int lastOffset;
    private byte[] sBuf;

    /**
     * 搜索字符串
     * @param key - 搜索的关键词字符串
     * @param startOffset - 开始搜索的文件偏移量
     * @return - 大于0代表搜索到的匹配位置,即文件偏移量；=-1表示未搜索到
     */
    public int search(String keyString, int startOffset) {
    	byte[] key;// = null;
    	try {
			key = keyString.getBytes(bookFormat); //把要查找的字符串转换为和原始格式一致的数组 
		} catch (UnsupportedEncodingException e) {
        	Error();
//			e.printStackTrace();
			return -1;
		}
	    int SEARCH_BUFFER_SIZE = 16384;
	    sBuf = new byte[SEARCH_BUFFER_SIZE];
		SEARCH_EOF = false;
		int start = startOffset;
		while (!SEARCH_EOF) {
			searchLocate(start);
			searchRead(SEARCH_BUFFER_SIZE); 
			// 删除缓冲区中最后一个回车及其后的字符
			getLastReturn();
			int offsetInBuffer = searchBuf(key);
			if (offsetInBuffer > 0) {
				// 搜索成功，跳转
				sBuf = null;
				return searchOffset + offsetInBuffer;
			}
			start += lastOffset;
		}
		sBuf = null;
		return -1;
    }
   
    /**
     * 在缓冲区内搜索
     * @param key - 关键字数组
     * @return : 匹配位置的缓冲区内偏移量, -1表示没有匹配 
     */
 // 如果文本内没有回车，查找有可能不准确，特别是跨缓冲区的情况
 // 一种缓解的办法是增大缓冲区，但是考虑到一般小说不会没有回车，因此这种算法应该适用绝大部分情况 
    private int searchBuf(byte[] key) {
    	int offset = 0;
    	byte first = key[0];
    	int keyLen = key.length;
    	int end = lastOffset - keyLen; // 如果文本内没有回车，查找有可能不准确，特别是跨缓冲区的情况
    	while (offset < end) {
    		offset = getFirst(first, offset);
    		if (offset < 0)
    			return -1;
    		else if (Arrays.equals(sBuf, offset, key, 0, keyLen)) 
    			return offset;
    		else
    			offset++;
    	}
    	return -1;
    }
    
    /**
     * 搜索缓冲区内符合第一个关键字字节的位置
     * @param k - 关键字的第一个字节
     * @return >0代表缓冲区内偏移量, =-1表示未匹配
     */
    private int getFirst(byte k, int startOffset) {
    	for(int i = startOffset; i < lastOffset; i++) {
    		if (k == sBuf[i])
    			return i;
    	}
    	return -1;
    }
    
    private boolean SEARCH_EOF;
    private int searchOffset;
    
    /**
     * 跳转到文件指定偏移量
     * @param i - 偏移量
     */
    private void searchLocate(int i) {
    	if (i >= bookSize)
    		return;
    	else if (i <= 0 )  
            i = 0;
        searchOffset = i;
        close();
        open();
        try {
			di.skip(i);
		} catch (IOException e) {
//			e.printStackTrace();
        	Error();
		}
    }
    
    /**
     * 从当前文件偏移位置读出len个byte
     * @param len - 指定读取长度
     * @return - 读取的数据块  
     */
    private int sBufLen;
    private void searchRead(int LENGTH) {
        try {
        	sBufLen = di.read(sBuf);
            SEARCH_EOF = (sBufLen < LENGTH) ? true : false;
            close();
        } catch (IOException ioe) {
        	Error();
//			ioe.printStackTrace();
            return;
        }
    }

    /**
     * 返回搜索缓冲区中最后一个回车的位置 
     * @return - 最后一个回车的位置
     */
    private int getLastReturn() {
        int t = sBufLen - 1;
        while (t > 0) {
        	byte c = sBuf[t];
            if (c == 10)  //遇到换行
            	break;
            t--;
        }
        if (t == 0)  //没有一个回车符
        	t = sBufLen ;
        lastOffset = t;
        return t;
    }
 /**************************Class End************************************/
 }
