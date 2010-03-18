import net.rim.device.api.util.Comparator;


// 书籍信息类
public final class BookMark implements Comparator{

	public String bookName;		//无路径书名
	public String digest;		//书签摘要
	public int offset;		//记录的偏移量
	public int id;  // Record ID
	
	public String toString() {
		return digest;
	}

	public int compare(Object o1, Object o2) {
		BookMark x1 = (BookMark)o1;
		BookMark x2 = (BookMark)o2;
		if (x1.offset > x2.offset)
			return 1;
		else if (x1.offset < x2.offset)
			return -1;
		else
			return 0;
	}
}