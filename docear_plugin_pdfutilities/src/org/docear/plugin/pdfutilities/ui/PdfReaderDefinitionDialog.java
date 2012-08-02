package org.docear.plugin.pdfutilities.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.docear.plugin.pdfutilities.PdfUtilitiesController;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.TextUtils;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class PdfReaderDefinitionDialog extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField txtPath;	
	final PdfReaderDefinitionDialog self;	

	/**
	 * Create the panel.
	 */
	public PdfReaderDefinitionDialog() {
		self = this;
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("max(250dlu;default):grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("min:grow"),},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("max(90dlu;pref)"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
				
		JPanel pnlDialogInfo = new JPanel();
		pnlDialogInfo.setBackground(Color.WHITE);
		add(pnlDialogInfo, "1, 1, 4, 2, fill, fill");
		pnlDialogInfo.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:default:grow"),}));
		
		JLabel lblHeadline = new JLabel(TextUtils.getText("docear.pdf_reader_definition.headline"));
		lblHeadline.setFont(new Font("Tahoma", Font.BOLD, 11));
		pnlDialogInfo.add(lblHeadline, "2, 2");
		
		JLabel lblHelp1 = new JLabel(TextUtils.getText("docear.pdf_reader_definition.help1"));
		lblHelp1.setFont(new Font("Tahoma", Font.ITALIC, 12));
		pnlDialogInfo.add(lblHelp1, "2, 4");
		
		JLabel lblHelp2 = new JLabel(TextUtils.getText("docear.pdf_reader_definition.help2"));
		pnlDialogInfo.add(lblHelp2, "2, 6");		
		
		txtPath = new JTextField();		
		txtPath.setText(ResourceController.getResourceController().getProperty(PdfUtilitiesController.OPEN_ON_PAGE_READER_COMMAND_KEY));
		add(txtPath, "2, 4, fill, default");
		txtPath.setColumns(10);
		
		JButton btnFilechooser = new JButton(TextUtils.getText("browse"));
		add(btnFilechooser, "4, 4");
		
		btnFilechooser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				final JFileChooser fc = new JFileChooser();	
				fc.setFileHidingEnabled(false);
				if (Compat.isWindowsOS()) {
					fc.setFileFilter(new FileNameExtensionFilter("*.exe", "exe"));
				}
				else if (Compat.isMacOsX()) {
					fc.setFileFilter(new FileNameExtensionFilter("*.app", "app"));
				}				
				int option = fc.showOpenDialog(self);
				if (option == JFileChooser.APPROVE_OPTION) {
					String readerCommand = PdfUtilitiesController.getController().buildCommandString(fc.getSelectedFile());
//					ResourceController.getResourceController().setProperty(PdfUtilitiesController.OPEN_ON_PAGE_READER_COMMAND_KEY, readerCommand);
					txtPath.setText(readerCommand);
				}
			}
		});
		
		Font font = new Font("Dialog", Font.PLAIN, 12);
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, TextUtils.getText("docear.pdf_reader_definition.samples"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(panel, "2, 6, 3, 1, fill, fill");
		panel.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("max(250dlu;default):grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),}));
		JLabel sampleLabel1 = new JLabel("C:\\Program Files\\Tracker Software\\PDF Viewer\\PDFXCview.exe*/A*page=$PAGE&nameddest=$TITLE*$FILE");
		panel.add(sampleLabel1, "1, 1, 3, 1, fill, fill");
		sampleLabel1.setFont(font);
		
		JLabel sampleLabel2 = new JLabel("wine*/home/stefan/.wine/drive_c/Program Files/Tracker Software/PDF Viewer/PDFXCview.exe*/A*page=$PAGE&nameddest=$TITLE*$FILE");
		panel.add(sampleLabel2, "1, 3, 3, 1, fill, fill");
		sampleLabel2.setFont(font);
		
		JLabel sampleLabel3 = new JLabel("evince*-i*$PAGE*$FILE");
		panel.add(sampleLabel3, "1, 5, 3, 1, fill, fill");
		sampleLabel3.setFont(font);
	}

	public String getReaderCommand() {
		return txtPath.getText();
	}

}
