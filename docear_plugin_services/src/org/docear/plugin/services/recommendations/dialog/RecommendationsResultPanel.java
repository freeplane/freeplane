package org.docear.plugin.services.recommendations.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.docear.plugin.services.recommendations.RecommendationEntry;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;

public class RecommendationsResultPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private JTable table;

	public RecommendationsResultPanel(final List<RecommendationEntry> recommandations) {
		setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);

		table = new JTable();
		table.setModel(new DefaultTableModel(new String[] { "Document Title", "Link" }, recommandations.size()) {
			private static final long serialVersionUID = 1L;
			Class<?>[] columnTypes = new Class<?>[] { String.class, JButton.class };

			public Object getValueAt(final int row, int column) {
				if (column == 0) {
					return recommandations.get(row).getTitle();
				}
				if (column == 1) {
					JButton button = new JButton("Link");
					button.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							try {
								Controller.getCurrentController().getViewController().openDocument(recommandations.get(row).getLink());
							} catch (Exception ex) {
								LogUtils.warn("could not open link to (" + recommandations.get(row).getLink() + ")", ex);
							}
						}
					});
					return button;
				}
				return null;

			}

			public Class<?> getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}

			boolean[] columnEditables = new boolean[] { false, false };

			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});
		table.getColumnModel().getColumn(0).setPreferredWidth(250);
		table.getColumnModel().getColumn(0).setMinWidth(250);
		table.setDefaultRenderer(JButton.class, new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 1L;

			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				if (value instanceof JButton) {
					return (Component) value;
				}
				return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			}
		});
		table.addMouseListener(new MouseListener() {

			public void mouseReleased(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseClicked(MouseEvent e) {
				int row = table.rowAtPoint(e.getPoint());
				int col = table.columnAtPoint(e.getPoint());
				Object comp = table.getModel().getValueAt(row, col);
				if (comp instanceof JButton) {
					((JButton) comp).doClick();
					e.consume();
				}

			}
		});
		table.addMouseMotionListener(new MouseMotionListener() {
			private final Cursor hand = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
			
			public void mouseMoved(MouseEvent e) {
				int row = table.rowAtPoint(e.getPoint());
				int col = table.columnAtPoint(e.getPoint());
				try {
					Object comp = table.getModel().getValueAt(row, col);
					if (comp instanceof JButton) {
						table.setCursor(hand);
					}
					else {
						table.setCursor(Cursor.getDefaultCursor());
					}
				} catch (Exception ex) {
				}
				
			}
			
			public void mouseDragged(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		scrollPane.setViewportView(table);
	}

}
