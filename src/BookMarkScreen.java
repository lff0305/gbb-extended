import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.util.Arrays;

public class BookMarkScreen extends MainScreen{ 
	private HistoryList bookMarkList;
	private Record bookMarkRecord ;
	public int offset;
	private String bookName;
	private BookMark compare;
	private BookMark[] bookMark ;
	private int bookSize;

	public BookMarkScreen(String book, int size) {
		super(VERTICAL_SCROLL | VERTICAL_SCROLLBAR);
		bookName = book;
		bookSize = size;
		setTitle("书签 - " + bookName);
		addMenuItem(_changeItem);
		addMenuItem(_delOneItem);
		addMenuItem(_delAllItem);
		bookMarkList = new HistoryList();
        compare = new BookMark();
		updateList(0);
        add(bookMarkList);
        offset = -1;
	}

    private MenuItem _changeItem = new MenuItem("修改书签",90,20){
    	public void run() {
    		ModifyScreen msc = new ModifyScreen("新书签名: ");
    		UiApplication.getUiApplication().pushModalScreen(msc);
    		String s = msc.value; //得到要修改的书签名
    		msc = null;
    		if (s != null && s.length() > 0) {
    	    	int i = bookMarkList.getSelectedIndex();
    	    	BookMark bm = bookMark[i];
    	    	bm.digest = s;
        		bookMarkRecord = new Record(Record.BOOKMARK_NAME);
        		bookMarkRecord.saveBookMark(bm, false);
        		bookMarkRecord = null;
        		updateList(i);
    		}
    	}
    };

    private MenuItem _delOneItem = new MenuItem("删除书签",90,20){
    	public void run() {
    		delBookMark();
    	}
    };
    
    private MenuItem _delAllItem = new MenuItem("清空书签",100,20){
    	public void run() {
        	int len = bookMark.length;
        	if (len > 0) {
        		bookMarkRecord = new Record(Record.BOOKMARK_NAME);
        		bookMarkRecord.deleteBookMark(bookName);
        		bookMarkRecord = null;
        		bookMark = null;
        	}
        	bookMarkList.set(null);
    	}
    };

	/**
	 * 读入书签记录并更新列表
	 * t - 原先被选中的index
	 */
	private void updateList(int t) {
		bookMarkRecord = new Record(Record.BOOKMARK_NAME);
		bookMark = null;
		bookMark = bookMarkRecord.loadBookMarkList(bookName);
		bookMarkRecord = null;
		//对书签数组根据偏移量排序
		if (bookMark != null) {
			int len = bookMark.length;
			if (len > 1)
				Arrays.sort(bookMark, compare);
			for (int i = 0; i < len; i++) {
				StringBuffer s = new StringBuffer("书签"); 
				String percent = InfoScreen.calProgress(bookMark[i].offset, bookSize).substring(6);
				s.append(i).append('(').append(percent).append(") - ");
				s.append(bookMark[i].digest);
				bookMark[i].digest = s.toString();
				s = null;
			}
		}
		bookMarkList.set(bookMark);
		// 在修改或者删除后还让光标保留在原位置
		if (t == bookMarkList.getSize())
			t--;
		bookMarkList.setSelectedIndex(t);
	}
	
	/**
	 * 获得选中的书签所代表的偏移量 
	 */
	private void open() {
    	int i = bookMarkList.getSelectedIndex();
    	offset = bookMark[i].offset;
    	close();
	}

	/**
	 * 删除一条书签
	 */
	private void delBookMark() {
    	int i = bookMarkList.getSelectedIndex();
		bookMarkRecord = new Record(Record.BOOKMARK_NAME);
		bookMarkRecord.deleteRecord(bookMark[i].id);
		bookMarkRecord = null;
		updateList(i);
	}

    protected boolean navigationClick(int status, int time) {
		if(!GBBMain.isTrackWheel) {
	    	open();
			return true;
    	}
		return super.navigationClick(status, time);
    }

	protected boolean keyChar(char key, int status, int time)
    {	
    	switch(key) {
    		case Characters.BACKSPACE:
    			delBookMark();
     			return true;
    		case Characters.ENTER :
    		case Characters.SPACE :
			case Characters.LATIN_SMALL_LETTER_J:
			case Characters.LATIN_SMALL_LETTER_D:
    			open();
     			return true;
    		case Characters.LATIN_SMALL_LETTER_R:
    			onClose();
     			return true;
     	}
    	return super.keyChar(key, status, time);
    }
}


