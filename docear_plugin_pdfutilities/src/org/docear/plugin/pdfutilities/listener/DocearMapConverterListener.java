package org.docear.plugin.pdfutilities.listener;

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.docear.plugin.core.features.DocearMapModelController;
import org.docear.plugin.pdfutilities.util.MapConverter;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.IMapSelectionListener;
import org.freeplane.features.map.MapModel;

public class DocearMapConverterListener implements IMapSelectionListener,  WindowFocusListener{
	
	private List<MapModel> mapsToConvert = new ArrayList<MapModel>();	
	private List<MapModel> mapsQuestionedInCurrentSession = new ArrayList<MapModel>();	
	private boolean startup = true;
	public static boolean currentlyConverting = false;
	
	public void afterMapChange(MapModel oldMap, MapModel newMap) {		
	}

	public void afterMapClose(MapModel oldMap) {		
	}

	public void beforeMapChange(MapModel oldMap, MapModel newMap) {
		if(newMap == null || DocearMapModelController.getModel(newMap) != null || newMap.getFile() == null) return;
		if(this.isAlreadyQuestioned(newMap)) return;
		if(startup){
			mapsToConvert.add(newMap);
		}
		else{			
			mapsToConvert.add(newMap);			
			showConversionDialog();
		}
	}

	public void windowGainedFocus(WindowEvent e) {
		if(startup){
			startup = false;
			mapsQuestionedInCurrentSession.addAll(mapsToConvert);
			if(this.mapsToConvert.size() > 0){
				showConversionDialog();
			}			
		}
	}

	public void windowLostFocus(WindowEvent e) {		
	}
	
	private void showConversionDialog() {
		int result = UITools.showConfirmDialog(null, getMessage(), getTitle(), JOptionPane.OK_CANCEL_OPTION);
		if(result == JOptionPane.OK_OPTION){	
			DocearMapConverterListener.currentlyConverting = true;
			MapConverter.convert(mapsToConvert);
			DocearMapConverterListener.currentlyConverting = false;
		}			
		mapsQuestionedInCurrentSession.addAll(mapsToConvert);
		mapsToConvert.clear();
	}	
	
	private boolean isAlreadyQuestioned(MapModel map){
		for(MapModel alreadyQuestioned : this.mapsQuestionedInCurrentSession){
			if(alreadyQuestioned.getFile().equals(map.getFile())){
				return true;
			}
		}
		return false;
	}
	
	private String getMessage(){
		if(this.mapsToConvert.size() > 1){
			return mapsToConvert.size() + TextUtils.getText("DocearMapConverterListener.0")+TextUtils.getText("update_splmm_to_docear_explanation"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		else if (this.mapsToConvert.size() == 1){
			return mapsToConvert.get(0).getTitle() + TextUtils.getText("DocearMapConverterListener.2")+TextUtils.getText("update_splmm_to_docear_explanation"); //$NON-NLS-1$ //$NON-NLS-2$
		}		
		return ""; //$NON-NLS-1$
	}
	
	private String getTitle(){
		if(this.mapsToConvert.size() > 1){
			return mapsToConvert.size() + TextUtils.getText("DocearMapConverterListener.5"); //$NON-NLS-1$
		}
		else if (this.mapsToConvert.size() == 1){
			return mapsToConvert.get(0).getTitle() + TextUtils.getText("DocearMapConverterListener.6"); //$NON-NLS-1$
		}
		return ""; //$NON-NLS-1$
	}

}
