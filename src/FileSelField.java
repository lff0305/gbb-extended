import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;
import net.rim.device.api.util.Arrays;
import net.rim.device.api.util.Comparator;
import net.rim.device.api.util.StringUtilities;

public final class FileSelField extends MyObjectListField implements ListFieldCallback
{
	public String _currentPath;		//The current path;
	public String _selectedFile;		//The selected file;
	private String[] _extensions;       //File extensions to filter by.
	private Bitmap smallicon = Bitmap.getBitmapResource("smallicon.png");    
	private FileName comparor; 

	public FileSelField(String[] extensions, String path){
		super(ELLIPSIS);
		comparor = new FileName();
		_extensions = extensions;
		updateList(path);
//		_selectedFile;// = null;
	}
	
	protected int moveFocus(int amount, int status, int time) {
     	 invalidate();
     	 return (super.moveFocus(amount, status, time));
   }

	public boolean fileSelected() {
		String thePath = buildPath();
		if (thePath == null)
			//Only update the screen if a directory was selected.
			updateList(thePath);
		else if (!thePath.equals("*?*"))
			//Only update the screen if a directory was selected.
			//A second check is required here to avoid a NullPointerException.
			updateList(thePath);
		else 
			return true; //选中文件
		return false;
	}
	
	private boolean isDir(String path) {
//		return (path.lastIndexOf('/') == path.length() - 1);
		int i = path.length();
		return (path.charAt(--i) == '/');
	}
	
    private String buildPath() {
		String newPath = (String)get(this, this.getSelectedIndex());
    	
    	if (newPath.equals(".."))
    	{
    		//Go up a directory.
    		//Remove the trailing '/';
    		newPath = _currentPath.substring(0, _currentPath.length() - 2);
    		
    		//Remove everything after the last '/' (the current directory).
			//If a '/' is not found, the user is opening the file system roots.
			//Return null to cause the screen to display the file system roots.
			int lastSlash = newPath.lastIndexOf('/');
			if (lastSlash == -1)
				newPath = null;
			else
				newPath = newPath.substring(0, lastSlash + 1);
    	}
    	else if (isDir(newPath)) {
    		//If the path ends with /, a directory was selected.
    		//Prefix the _currentPath if it is not null (not in the root directory).
    		if (_currentPath != null)
    			newPath = _currentPath + newPath;
    	}
    	else { //A file was selected.
    		_selectedFile = _currentPath + newPath;
    		//Return *?* to stop the screen update process.
    		newPath = "*?*";
    	}
    	return newPath;
    }

    //Updates the entries in the ObjectListField.
	public  void updateList(String path) {
    	//Read all files and directories in the path.
		Object fileArray[] = readFiles(path);
    	//Update the field with the new files.
    	set(fileArray);
    }
 
	// 返回当前目录下的内容
	private Enumeration listDir(String path) {
		Enumeration list;
		try	{
    		FileConnection fc = (FileConnection)Connector.open("file:///" + path);
    		list = fc.list();
    		fc.close();
    		fc = null;
     	}catch (Exception ex) { 
     		_currentPath = null;
       		list = FileSystemRegistry.listRoots(); //如果目录不存在或为空，则返回根目录
       	}
     	return list;
	}
	
    //Reads all of the files and directories in a given path.
    private Object[] readFiles(String path) {
        Vector filesVector = new Vector();
        _currentPath = path;
        
        
        Enumeration fileEnum = listDir(path);
        while (fileEnum.hasMoreElements()) {
              	//Use the file extension filter, if there is one.
          	if (_extensions == null) 
           		filesVector.addElement((Object)fileEnum.nextElement());
           	else { //添加目录或者符合后缀要求的文件
           		String currentFile = (String)fileEnum.nextElement();
           		if (isDir(currentFile) || matchExtension(currentFile))
           			filesVector.addElement((Object)currentFile);
           		currentFile = null;
           	}
  		}
        return sortFile(filesVector);
    }
    
    // 检查是否符合给定的文件后缀过滤要求
    private boolean matchExtension(String fileName) {
		for (int count = _extensions.length - 1; count >= 0; --count) 
			if (fileName.indexOf(_extensions[count]) != -1) 
				//There was a match, add the file and stop looping.
				return true;
		return false;
    }
    
    // 把向量按目录在前,文件在后并排序
    private final Object[] sortFile(Vector v) {
    	int len = v.size();

    	// 转为数组
        Object[] fa = new Object[len];
        v.copyInto(fa);
        
        //数组排序
        Arrays.sort(fa, comparor);

        //排序后合并
        int inc = (_currentPath == null) ? 0 : 1;
        Object[] fileArray = new Object[len + inc];
        if (_currentPath != null) // 如果不是根目录，则加上".."
        	fileArray[0] = "..";
       	System.arraycopy(fa, 0, fileArray, inc, len);
       	fa = null;
    	return fileArray;
    }

    public void drawListRow(ListField list, Graphics graphics, int index, int y, int w) {
    	// get file/dir name
        String row = (String)get(list,index);
        
        // get Row height
       	int dy = (list.getRowHeight() - 20) / 2 + y;
       	int icon_pos;
    	if (isDir(row))  // folder
    		icon_pos = (index == list.getSelectedIndex()) ? 39 : 19;
        else  //文件或者上级目录
        	icon_pos = (row.equals("..")) ? 0 : 60;
		graphics.drawBitmap(2, dy, 20, 20, smallicon, 0,icon_pos);
    	graphics.drawText(row, 24 , y, 0, w); // files
    	row = null;
    }
    
    // 用于文件名排序 , 排序原则：目录在前，文件在后
    final class FileName implements Comparator {
	   	public int compare(Object o1, Object o2) {
	   		String x1 = (String)o1;
	   		String x2 = (String)o2;
	   		boolean d1 = isDir(x1); 
	   		boolean d2 = isDir(x2);
	   		if ((d1 && d2) || (!d1 && !d2)) 
	   			return StringUtilities.compareToIgnoreCase(x1, x2);
	   		else if (d1 && !d2) 
	   			return -1;
	   		else 
	   			return 1;
	   	}
   }
 ////// class FileSelField ends //////////////////////////////////    
}

