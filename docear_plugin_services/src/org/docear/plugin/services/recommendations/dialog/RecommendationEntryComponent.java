package org.docear.plugin.services.recommendations.dialog;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import org.docear.plugin.services.recommendations.RecommendationEntry;
import org.freeplane.core.util.TextUtils;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class RecommendationEntryComponent extends JPanel {

	private static final long serialVersionUID = 1L;
	public static final int OPEN_RECOMMENDATION = 1;
	public static final int IMPORT_RECOMMENDATION = 2;
	private Set<ActionListener> actionListeners = new HashSet<ActionListener>();

	public RecommendationEntryComponent(final RecommendationEntry recommendation) {
		setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("default:grow"),
				ColumnSpec.decode("50px"),},
			new RowSpec[] {
				RowSpec.decode("50px"),}));
		
		final JLabel lblOpenButton = new JLabel(recommendation.getTitle());
//		final JTextField lblOpenButton = new JTextField(recommendation.getTitle());		
//		lblOpenButton.setBorder( null );
//		lblOpenButton.setOpaque( false );
//		lblOpenButton.setEditable( false ); 
		lblOpenButton.setIcon(new ImageIcon(RecommendationEntryComponent.class.getResource("/icons/document-open-remote_32x32.png")));
		lblOpenButton.setToolTipText(TextUtils.getText("recommendation.preview.tooltip"));
		lblOpenButton.setBorder(new BevelBorder(BevelBorder.RAISED, SystemColor.control, null, null, null));
		lblOpenButton.setMinimumSize(new Dimension(200, 50));
		lblOpenButton.setPreferredSize(new Dimension(200, 50));
		//javax.swing.plaf.ColorUIResource[r=10,g=36,b=106]
		final Color background = lblOpenButton.getBackground();
		final Color selectionBackground = new Color(140, 180, 240);
		lblOpenButton.addMouseListener(new MouseListener() {
						
			public void mouseReleased(MouseEvent e) {}
			
			public void mousePressed(MouseEvent e) {}
			
			public void mouseExited(MouseEvent e) {
				setCursor(Cursor.getDefaultCursor());
				setBackground(background);
			}
			
			public void mouseEntered(MouseEvent e) {
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				setBackground(selectionBackground);
			}
			
			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1) {
					fireActionEvent(new ActionEvent(lblOpenButton, RecommendationEntryComponent.OPEN_RECOMMENDATION, "OPEN_RECOMMENDATION"));
					e.consume();
				}
			}			
		});
		add(lblOpenButton, "1, 1");
		
		JLabel lblImportButton = new JLabel("");
		lblImportButton.setToolTipText(TextUtils.getText("recommendation.import.tooltip"));
		lblImportButton.setHorizontalAlignment(SwingConstants.CENTER);
		lblImportButton.setIcon(new ImageIcon(RecommendationEntryComponent.class.getResource("/icons/document-import_32x32.png")));
		lblImportButton.setBorder(new BevelBorder(BevelBorder.RAISED, SystemColor.control, null, null, null));
		lblImportButton.setMinimumSize(new Dimension(50, 50));
		lblImportButton.setPreferredSize(new Dimension(50, 50));
		lblImportButton.addMouseListener(new MouseListener() {
			
			public void mouseReleased(MouseEvent e) {}
			
			public void mousePressed(MouseEvent e) {}
			
			public void mouseExited(MouseEvent e) {
				setCursor(Cursor.getDefaultCursor());	
				setBackground(background);
			}
			
			public void mouseEntered(MouseEvent e) {
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				setBackground(selectionBackground);
			}
			
			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1) {
					fireActionEvent(new ActionEvent(recommendation, RecommendationEntryComponent.IMPORT_RECOMMENDATION, "IMPORT_RECOMMENDATION"));
					e.consume();
				}
			}			
		});
		add(lblImportButton, "2, 1");
	}

	public void addActionListener(ActionListener actionListener) {
		actionListeners.add(actionListener);
		
	}
	
	private void fireActionEvent(ActionEvent actionEvent) {
		for(ActionListener listener : actionListeners ) {
			listener.actionPerformed(actionEvent);
		}
		
	}

}
