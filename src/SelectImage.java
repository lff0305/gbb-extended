import net.rim.device.api.system.Characters;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

public final class SelectImage extends PopupScreen {
	private FileSelField _olf;       //Lists fields and directories.
	private LabelField pathField;//

	public SelectImage(String startPath){
		super(new VerticalFieldManager(NO_VERTICAL_SCROLL), DEFAULT_CLOSE);   // no transition effect        
        
        String[] extentions = {"jpg","JPG","jpeg","Jpg"};
        _olf = new FileSelField(extentions, startPath);
    	Setting.lastImgPath = Setting.isNull(_olf._currentPath);
        pathField= new LabelField((_olf._currentPath == null) ? "/" : "/" + _olf._currentPath, NON_FOCUSABLE);
        
        VerticalFieldManager vfm = new VerticalFieldManager(VERTICAL_SCROLL | VERTICAL_SCROLLBAR) {
            protected void sublayout(int width, int height) {
            	height = Display.getHeight() / 2;
                super.sublayout(width, height);
                setExtent(width, height);
            }
        };
        vfm.add(_olf);
        add(pathField);
        add(new SeparatorField());
        add(vfm);
	}
	
	public String getFile() {
        return _olf._selectedFile;
    }    
	
    //Handles a user picking an entry in the ObjectListField.
    private void doSelection() {
    	boolean fileSelected = _olf.fileSelected();
		Setting.lastImgPath = Setting.isNull(_olf._currentPath);
    	if (fileSelected) 
    		close();
	}
    
    //Handle trackball clicks.  
    protected boolean navigationClick(int status, int time) {
    	doSelection();
		pathField.setText((_olf._currentPath == null) ? "/" : "/" + _olf._currentPath);
    	return true;
    }
    
    public boolean keyChar(char c, int status, int time) {
    	//Close this screen if escape is selected.
    	switch(c) {
    		case Characters.ENTER:
    		case Characters.SPACE:
			case Characters.LATIN_SMALL_LETTER_J:
			case Characters.LATIN_SMALL_LETTER_D:
    			return navigationClick(status, time);
    	}
    	return super.keyChar(c, status, time);
    }
    
// class GBBMainScreen ends
}
