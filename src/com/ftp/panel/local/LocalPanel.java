package com.ftp.panel.local;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ActionMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.table.TableStringConverter;

import com.ftp.FTPClientFrame;
import com.ftp.panel.FTPTableCellRanderer;
import com.ftp.panel.mainPanel.TableConverter;
import com.ftp.utils.DiskFile;

public class LocalPanel extends javax.swing.JPanel {
	Queue<Object[]> queue = new LinkedList<Object[]>();
	private UploadThread uploadThread = null;
	private Desktop desktop = null;
	private javax.swing.JButton createFolderButton;
	private javax.swing.JButton delButton;
	private javax.swing.JScrollPane scrollPane;
	private javax.swing.JToolBar.Separator jSeparator1;
	private javax.swing.JToolBar toolBar;
	private javax.swing.JComboBox localDiskComboBox;
	javax.swing.JTable localDiskTable;
	javax.swing.JLabel localSelFilePathLabel;
	private javax.swing.JButton renameButton;
	private javax.swing.JButton uploadButton;
	private TableRowSorter<TableModel> sorter;
	FTPClientFrame frame = null;

	public LocalPanel(FTPClientFrame client_Frame) {
		frame = client_Frame;
		if (Desktop.isDesktopSupported()) {
			desktop = Desktop.getDesktop();
		}
		initComponents();
	}

	/**
	 * ���沼�����ʼ������
	 */
	private void initComponents() {
		ActionMap actionMap = getActionMap();
		actionMap.put("delAction", new DelFileAction(this, "ɾ��", null));
		actionMap.put("renameAction", new RenameAction(this, "������", null));
		actionMap.put("createFolderAction", new CreateFolderAction(this, "�½��ļ���", null));
		actionMap.put("uploadAction", new UploadAction(this, "�ϴ�", null));
		actionMap.put("refreshAction", new RefreshAction(this, "ˢ��", null));

		java.awt.GridBagConstraints gridBagConstraints;

		toolBar = new javax.swing.JToolBar();
		delButton = new javax.swing.JButton();
		renameButton = new javax.swing.JButton();
		createFolderButton = new javax.swing.JButton();
		uploadButton = new javax.swing.JButton();
		jSeparator1 = new javax.swing.JToolBar.Separator();
		localDiskComboBox = new javax.swing.JComboBox();
		localDiskComboBox.setPreferredSize(new Dimension(100, 25));
		scrollPane = new javax.swing.JScrollPane();
		localDiskTable = new javax.swing.JTable();
		localDiskTable.setDragEnabled(true);
		localSelFilePathLabel = new javax.swing.JLabel();
		/**
		 *  �����б߿����һ�����⣬ʹ�����ָ����λ�ú�Ĭ��������ı���ɫ���ɵ�ǰ���ȷ������
		 *  TitledBorder.CENTER: �������ı����ڱ߿��ߵ����ġ�
		 *  TitledBorder.ABOVE_TOP: ���������ڱ߿򶥶��ߵ��ϲ���
		 */
		setBorder(javax.swing.BorderFactory.createTitledBorder(null, "����",
				javax.swing.border.TitledBorder.CENTER,
				javax.swing.border.TitledBorder.ABOVE_TOP));
		setLayout(new java.awt.GridBagLayout());

		toolBar.setRollover(true);
		toolBar.setFloatable(false);

		delButton.setText("ɾ��");
		delButton.setFocusable(false);
		delButton.setAction(actionMap.get("delAction"));
		toolBar.add(delButton);

		renameButton.setText("������");
		renameButton.setFocusable(false);
		renameButton.setAction(actionMap.get("renameAction"));
		toolBar.add(renameButton);

		createFolderButton.setText("���ļ���");
		createFolderButton.setFocusable(false);
		createFolderButton.setAction(actionMap.get("createFolderAction"));
		toolBar.add(createFolderButton);

		uploadButton.setText("�ϴ�");
		uploadButton.setFocusable(false);
		uploadButton.setAction(actionMap.get("uploadAction"));
		toolBar.add(uploadButton);

		JButton refreshButton = new JButton();
		refreshButton.setText("ˢ��");
		refreshButton.setFocusable(false);
		refreshButton.setAction(actionMap.get("refreshAction"));
		toolBar.add(refreshButton);
		toolBar.add(jSeparator1);
		
		//File.listRoots():�г����õ��ļ�ϵͳ����
		localDiskComboBox.setModel(new DefaultComboBoxModel(File.listRoots())); 
		localDiskComboBox.addItemListener(new java.awt.event.ItemListener() {
			public void itemStateChanged(java.awt.event.ItemEvent evt) {
				localDiskComboBoxItemStateChanged(evt);
			}
		});
		toolBar.add(localDiskComboBox);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		add(toolBar, gridBagConstraints);
		localDiskTable.setModel(new LocalTableModel());
		localDiskTable.setShowHorizontalLines(false);
		localDiskTable.setShowVerticalLines(false);
		localDiskTable.getTableHeader().setReorderingAllowed(false);
		localDiskTable.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				localDiskTableMouseClicked(evt);
			}
		});
		scrollPane.setViewportView(localDiskTable);
		scrollPane.getViewport().setBackground(Color.WHITE);
		//������Ⱦ������Դ��FTP��Դ����������Ⱦ��
		localDiskTable.getColumnModel().getColumn(0).setCellRenderer(FTPTableCellRanderer.getCellRanderer());
		//RowSorter ��һ��ʵ�֣���ʹ�� TableModel �ṩ����͹��˲�����
		sorter = new TableRowSorter<TableModel>(localDiskTable.getModel());
		TableStringConverter converter = new TableConverter();
		//���ø���ֵ��ģ��ת��Ϊ�ַ����Ķ���
		sorter.setStringConverter(converter);
		//���� RowSorter��RowSorter �����ṩ�� JTable ������͹��ˡ� 
		localDiskTable.setRowSorter(sorter);
		sorter.toggleSortOrder(0);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		add(scrollPane, gridBagConstraints);

		localSelFilePathLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		add(localSelFilePathLabel, gridBagConstraints);
	}

	/**
	 * ���ش�������ѡ����ѡ��ı��¼������������¼�����������
	 */
	private void localDiskComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {
		if (evt.getStateChange() == ItemEvent.SELECTED) {
			Object item = evt.getItem(); // ��ȡѡ��������б��ѡ��
			if (item instanceof File) { // �����ѡ����File���ʵ������
				File selDisk = (File) item; // ����ѡ��ת����File��
				// ����listLocalFiles()��������ʾ��File��ָ���Ĵ����ļ��б�
				listLocalFiles(selDisk);
			}
		}
	}

	/**
	 * ˢ��ָ���ļ��еķ���
	 */
	void refreshFolder(File file) {
		listLocalFiles(file);
	}

	/**
	 * ˢ�±��ص�ǰ�ļ��еķ���
	 */
	public void refreshCurrentFolder() {
		final File file = getCurrentFolder(); // ��ȡ��ǰ�ļ���
		Runnable runnable = new Runnable() { // �����µ��߳�
			public void run() {
				listLocalFiles(file); // ���ص�ǰ�ļ��е��б������
			}
		};
		//���� runnable �� run ������ EventQueue ��ָ���߳��ϱ����á�
		SwingUtilities.invokeLater(runnable); // ���¼��߳��е��ø��̶߳���
	}

	/**
	 * ��ȡ��ǰ�ļ���
	 */
	public File getCurrentFolder() {
		// ʹ��·����ǩ��·��������ǰ�ļ��ж���
		File file = new File(localSelFilePathLabel.getText());
		// ������ѡ�����ļ��У���ѡ����ļ������ǵ��ϼ��ļ���
		if (localDiskTable.getSelectedRow() > 1 && file.getParentFile() != null)
			file = file.getParentFile(); // ��ȡ���ϼ��ļ���
		return file; //  �����ļ��ж���
	}

	/**
	 * ���ش����ļ��ı�񵥻���˫���¼�������
	 */
	private void localDiskTableMouseClicked(java.awt.event.MouseEvent evt) {
		int selectedRow = localDiskTable.getSelectedRow(); // ��ȡѡ��ı���к�
		if (selectedRow < 0)
			return;
		// ��ȡ�����ѡ��ĵ�ǰ�еĵ�һ���ֶε�ֵ
		Object value = localDiskTable.getValueAt(selectedRow, 0);
		if (value instanceof DiskFile) { //  �����ֵ��DiskFile��ʵ������
			DiskFile selFile = (DiskFile) value;
			// ����״̬���ı����ļ�·��
			localSelFilePathLabel.setText(selFile.getAbsolutePath());
			if (evt.getClickCount() >= 2) { //  �����˫�����
				if (selFile.isDirectory()) { // ����ѡ������ļ���
					listLocalFiles(selFile); // ��ʾ���ļ��е������б�
				} else if (desktop != null) { // ��������ļ���
					try {
						desktop.open(selFile); // ��������ϵͳ����򿪸��ļ�
					} catch (IOException ex) {
						Logger.getLogger(FTPClientFrame.class.getName()).log(
								Level.SEVERE, null, ex);
					}
				}
			}
		} else { // ���ѡ��ı�����ݲ���DiskFile���ʵ��
			// �ж�ѡ����ǲ���..ѡ��
			if (evt.getClickCount() >= 2 && value.equals("..")) {
				// ������ǰѡ���ļ�����ʱ�ļ�
				File tempFile = new File((localSelFilePathLabel.getText()));
				// ��ʾѡ����ļ����ϼ�Ŀ¼�б�
				listLocalFiles(tempFile.getParentFile());
			}
		}
	}

	/**
	 * ��ȡ�����ļ������ķ���
	 */
	private void listLocalFiles(File selDisk) {
		if (selDisk == null || selDisk.isFile()) {
			return;
		}
		localSelFilePathLabel.setText(selDisk.getAbsolutePath());
		File[] listFiles = selDisk.listFiles(); // ��ȡ�����ļ��б�
		// ��ȡ��������ģ��
		DefaultTableModel model = (DefaultTableModel) localDiskTable.getModel();
		model.setRowCount(0); //  ���ģ�͵�����
		model.addRow(new Object[] { ".", "<DIR>", "" }); // ����.ѡ��
		model.addRow(new Object[] { "..", "<DIR>", "" }); // ����..ѡ��
		if (listFiles == null) {
			JOptionPane.showMessageDialog(this, "�ô����޷�����");
			return;
		}
		// �������̸��ļ��е����ݣ���ӵ������
		for (File file : listFiles) {
			File diskFile = new DiskFile(file); // �����ļ�����
			String length = file.length() + "B "; // ��ȡ�ļ���С
			if (file.length() > 1000 * 1000 * 1000) { // �����ļ�G��λ
				length = file.length() / 1000000000 + "G ";
			}
			if (file.length() > 1000 * 1000) { // �����ļ�M��λ
				length = file.length() / 1000000 + "M ";
			}
			if (file.length() > 1000) {
				length = file.length() / 1000 + "K "; // �����ļ�K��λ
			}
			if (file.isDirectory()) { // ��ʾ�ļ��б�־
				length = "<DIR>";
			}
			// ��ȡ�ļ�������޸�����
			String modifDate = new Date(file.lastModified()).toLocaleString();
			if (!file.canRead()) {
				length = "δ֪";
				modifDate = "δ֪";
			}
			// �������ļ�����Ϣ��ӵ���������ģ����
			model.addRow(new Object[] { diskFile, length, modifDate });
		}
		localDiskTable.clearSelection(); // ȡ������ѡ����
	}

	/**
	 * ֹͣ�ļ��ϴ��̵߳ķ���
	 */
	public void stopUploadThread() {
		if (uploadThread != null)
			uploadThread.stopThread();
	}

	public javax.swing.JComboBox getLocalDiskComboBox() {
		return localDiskComboBox;
	}

	/**
	 * ����FTP���ӣ��������ϴ������̵߳ķ�����
	 */
	public void setFtpClient(String server, int port, String userStr,
			String passStr) {
		if (uploadThread != null)
			uploadThread.stopThread();
		uploadThread = new UploadThread(this, server, port, userStr, passStr);
		uploadThread.start();
	}

	public Queue<Object[]> getQueue() {
		return queue;
	}
}