package org.docear.plugin.core.mindmap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.swing.JOptionPane;

import org.docear.plugin.core.features.DocearMapModelController;
import org.docear.plugin.core.features.DocearMapModelExtension;
import org.docear.plugin.core.listeners.ISplmmMapsConvertListener;
import org.docear.plugin.core.listeners.SplmmMapsConvertEvent;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.url.UrlManager;
import org.freeplane.features.url.mindmapmode.IMapConverter;
import org.freeplane.features.url.mindmapmode.MFileManager;
import org.freeplane.features.url.mindmapmode.MapConversionException;

public class MapConverter implements IMapConverter {
	
	private final static HashSet<ISplmmMapsConvertListener> mapsConvertedListener = new HashSet<ISplmmMapsConvertListener>();
	private final static HashSet<MapModel> mapsQuestionedInCurrentSession = new HashSet<MapModel>();
	
		
	public static void addMapsConvertedListener(ISplmmMapsConvertListener listener) {
		mapsConvertedListener.add(listener);
	}
	
	public static void removeMapsConvertedListener(ISplmmMapsConvertListener listener) {
		mapsConvertedListener.remove(listener);
	}
	
	public static void fireConvertMapsEvent(MindmapUpdateController mindmapUpdateController) {
		for (ISplmmMapsConvertListener listener : mapsConvertedListener) {
			SplmmMapsConvertEvent event = new SplmmMapsConvertEvent(mindmapUpdateController);
			listener.mapsConvert(event);
		}
		
	}
	
	public static boolean convert(final List<MapModel> maps){
		try{
			if(maps == null || maps.size() <= 0) return false;
			currentlyConverting = true;
			MindmapUpdateController mindmapUpdateController = new MindmapUpdateController();
			mindmapUpdateController.addMindmapUpdater(new AnnotationModelUpdater(TextUtils.getText("MapConverter.0"))); //$NON-NLS-1$		
			fireConvertMapsEvent(mindmapUpdateController);
			//mindmapUpdateController.addMindmapUpdater(new MindmapLinkTypeUpdater("Converting hyperlinks...."));
			if(!mindmapUpdateController.updateMindmapsInList(maps)){				
				for(MapModel map : maps){
					DocearMapModelExtension docearMapModel = DocearMapModelController.getModel(map);
					if(docearMapModel != null){
						docearMapModel.setIncompatible(true);
					}
					else{
						DocearMapModelController.setModelWithCurrentVersion(map).setIncompatible(true);
					}
				}				
				currentlyConverting = false;
				return false;
			}
			currentlyConverting = false;
			return true;
		}
		catch(Exception e){
			LogUtils.warn(e);
			currentlyConverting = false;
			return false;
		}
		finally{
			System.gc();
		}
	}

	public static boolean currentlyConverting; 
	
	public void convert(NodeModel root) throws MapConversionException {
		ArrayList<MapModel> maps = new ArrayList<MapModel>();
		if(isAlreadyQuestioned(root.getMap())) {
			return;
		}		
		maps.add(root.getMap());
		showConversionDialog(maps);
	}
	
	public boolean showConversionDialog(final List<MapModel> maps) {
		boolean converted = false;		
		for(MapModel map : maps){
			DocearMapModelExtension docearMapModel = DocearMapModelController.getModel(map);
			if(docearMapModel == null){
				DocearMapModelController.setModelWithCurrentVersion(map);
			}			
		}
		
		int result = UITools.showConfirmDialog(null, getMessage(maps), getTitle(maps), JOptionPane.OK_CANCEL_OPTION);		
		if(result == JOptionPane.OK_OPTION){
			converted = convert(maps);			
		}
		else{			
			for(MapModel map : maps){
				DocearMapModelExtension docearMapModel = DocearMapModelController.getModel(map);
				if(docearMapModel != null){
					docearMapModel.setIncompatible(true);
				}
				else{
					DocearMapModelController.setModelWithCurrentVersion(map).setIncompatible(true);
				}
			}			
		}
		mapsQuestionedInCurrentSession.addAll(maps);
		return converted;
	}
	
	private String getMessage(final List<MapModel> mapsToConvert){
		if(mapsToConvert.size() > 1){
			return mapsToConvert.size() + TextUtils.getText("DocearMapConverterListener.0")+TextUtils.getText("update_splmm_to_docear_explanation"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		else if (mapsToConvert.size() == 1){
			return mapsToConvert.get(0).getTitle() + TextUtils.getText("DocearMapConverterListener.2")+TextUtils.getText("update_splmm_to_docear_explanation"); //$NON-NLS-1$ //$NON-NLS-2$
		}		
		return ""; //$NON-NLS-1$
	}
	
	private String getTitle(final List<MapModel> mapsToConvert){
		if(mapsToConvert.size() > 1){
			return mapsToConvert.size() + TextUtils.getText("DocearMapConverterListener.5"); //$NON-NLS-1$
		}
		else if (mapsToConvert.size() == 1){
			return mapsToConvert.get(0).getTitle() + TextUtils.getText("DocearMapConverterListener.6"); //$NON-NLS-1$
		}
		return ""; //$NON-NLS-1$
	}
	
	private boolean isAlreadyQuestioned(MapModel map){
		for(MapModel alreadyQuestioned : mapsQuestionedInCurrentSession){
			if(alreadyQuestioned.getFile().equals(map.getFile())){
				return true;
			}
		}
		return false;
	}
}
