import net.rim.device.api.util.Comparator;


// 书籍信息类
public final class BookInfo implements Comparator {

	public String bookFullName; //全路径书名
	public String bookName;		//无路径书名
	public int bookOffset;		//记录的偏移量
	public int bookID;			//记录号
	public long time;            //上次打开时间
	
	public String toString() {
		return bookName;
	}
	
	public int compare(Object o1, Object o2) {
		BookInfo x1 = (BookInfo)o1;
		BookInfo x2 = (BookInfo)o2;
		if (x1.time > x2.time)
			return -1;
		else if (x1.time < x2.time)
			return 1;
		else
			return 0;
	}

}