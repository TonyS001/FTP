package com.ftp.panel.mainPanel;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ActionMap;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.table.TableStringConverter;

import com.ftp.FTPClientFrame;
import com.ftp.panel.FTPTableCellRanderer;
import sun.net.ftp.FtpClient;
import com.ftp.utils.FtpFile;
import sun.net.ftp.FtpProtocolException;

public class FtpPanel extends javax.swing.JPanel{
    FtpClient ftpClient;
    private javax.swing.JButton createFolderButton;
    private javax.swing.JButton delButton;
    private javax.swing.JButton downButton;
    javax.swing.JTable ftpDiskTable;
    private javax.swing.JLabel ftpSelFilePathLabel;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JButton refreshButton;
    private javax.swing.JButton renameButton;
    FTPClientFrame frame = null;
    Queue<Object[]> queue = new LinkedList<Object[]>();
    private DownThread thread;

    public FtpPanel(FTPClientFrame client_Frame) {
        frame = client_Frame;
        initComponents();
    }

    private void initComponents() {
        ActionMap actionMap = getActionMap();
        actionMap.put("createFolderAction", new CreateFolderAction(this, "�����ļ���", null));
        actionMap.put("delAction", new DelFileAction(this, "ɾ��", null));
        actionMap.put("refreshAction", new RefreshAction(this, "ˢ��", null));
        actionMap.put("renameAction", new RenameAction(this, "������", null));
        actionMap.put("downAction", new DownAction(this, "����", null));

        java.awt.GridBagConstraints gridBagConstraints;

        toolBar = new javax.swing.JToolBar();
        delButton = new javax.swing.JButton();
        renameButton = new javax.swing.JButton();
        createFolderButton = new javax.swing.JButton();
        downButton = new javax.swing.JButton();
        refreshButton = new javax.swing.JButton();
        scrollPane = new JScrollPane();
        ftpDiskTable = new JTable();
        ftpDiskTable.setDragEnabled(true);
        ftpSelFilePathLabel = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Զ��",
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.ABOVE_TOP));
        setLayout(new java.awt.GridBagLayout());

        toolBar.setRollover(true);
        toolBar.setFloatable(false);

        delButton.setText("ɾ��");
        delButton.setFocusable(false);
        delButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        delButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
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

        downButton.setText("����");
        downButton.setFocusable(false);
        downButton.setAction(actionMap.get("downAction"));
        toolBar.add(downButton);

        refreshButton.setText("ˢ��");
        refreshButton.setFocusable(false);
        refreshButton.setAction(actionMap.get("refreshAction"));
        toolBar.add(refreshButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(toolBar, gridBagConstraints);

        ftpDiskTable.setModel(new FtpTableModel());
        ftpDiskTable.setShowHorizontalLines(false);
        ftpDiskTable.setShowVerticalLines(false);
        ftpDiskTable.getTableHeader().setReorderingAllowed(false);
        ftpDiskTable.setDoubleBuffered(true);
        ftpDiskTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ftpDiskTableMouseClicked(evt);
            }
        });
        scrollPane.setViewportView(ftpDiskTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        //������Ⱦ������Դ��FTP��Դ����������Ⱦ��
        ftpDiskTable.getColumnModel().getColumn(0).setCellRenderer(FTPTableCellRanderer.getCellRanderer());
        //RowSorter ��һ��ʵ�֣���ʹ�� TableModel �ṩ����͹��˲�����
        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(ftpDiskTable.getModel());
        TableStringConverter converter = new TableConverter();
        //���ø���ֵ��ģ��ת��Ϊ�ַ����Ķ���
        sorter.setStringConverter(converter);
        //���� RowSorter��RowSorter �����ṩ�� JTable ������͹��ˡ�
        ftpDiskTable.setRowSorter(sorter);
        /**
         * �ߵ�ָ���е�����˳�򡣵��ô˷���ʱ���������ṩ������Ϊ��
         * ͨ�������ָ�����Ѿ�����Ҫ�����У���˷����������Ϊ���򣨻򽫽����Ϊ���򣩣�
         * ����ʹָ���г�Ϊ��Ҫ�����У���ʹ����������˳�����ָ���в���������˷���û���κ�Ч����
         */
        sorter.toggleSortOrder(0);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(scrollPane, gridBagConstraints);

        ftpSelFilePathLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(ftpSelFilePathLabel, gridBagConstraints);
    }

    /**
     * ��񵥻���˫���¼��Ĵ�������
     */
    private void ftpDiskTableMouseClicked(java.awt.event.MouseEvent evt) {
        int selectedRow = ftpDiskTable.getSelectedRow();
        Object value = ftpDiskTable.getValueAt(selectedRow, 0);
        if (value instanceof FtpFile) {
            FtpFile selFile = (FtpFile) value;
            ftpSelFilePathLabel.setText(selFile.getAbsolutePath());
            if (evt.getClickCount() >= 2) { //˫�����
                if (selFile.isDirectory()) try {
                    ftpClient.changeDirectory(selFile.getAbsolutePath());
                    InputStream list = ftpClient.list(null);
                    ByteArrayOutputStream temp = clone(list);
                    list.close();
                    ftpClient.completePending();
                    list = new ByteArrayInputStream(temp.toByteArray());
                    listFtpFiles(list); //���ý�������
                } catch (IOException | FtpProtocolException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * ��ȡFTP�ļ������ķ���
     * @param list
     *            ��ȡFTP��������Դ�б��������
     */
    public synchronized void listFtpFiles(final InputStream list) {
        // ��ȡ��������ģ��
        final DefaultTableModel model = (DefaultTableModel) ftpDiskTable
                .getModel();
        model.setRowCount(0);
        // ����һ���߳���
        Runnable runnable = new Runnable() {
            public synchronized void run() {
                ftpDiskTable.clearSelection();
                try {
                    String pwd = getPwd(); // ��ȡFTP�������ĵ�ǰ�ļ���
                    model.addRow(new Object[] { new FtpFile("..", pwd, true), "", "" }); // ��ӡ�..������
                    byte[]names=new byte[2048];
                    int bufsize=0;
                    bufsize=list.read(names, 0, names.length);
                    int i=0,j=0;
                    while(i<bufsize){
                        //�ַ�ģʽΪ10��������ģʽΪ13
                        if (names[i]==13) {
                            //�ļ����������п�ʼ������Ϊj,i-jΪ�ļ����ĳ��ȣ��ļ����������еĽ����±�Ϊi-1
                            String fileMessage = new String(names,j,i-j);
                            if(fileMessage.length() == 0){
                                System.out.println("fileMessage.length() == 0");
                                break;
                            }
                            //���տո�fileMessage��Ϊ������ȡ�����Ϣ
                            // ������ʽ  \s��ʾ�ո񣬣�1������ʾ1һ������
                            if(!fileMessage.split("\\s+")[8].equals(".") && !fileMessage.split("\\s+")[8].equals("..")){
                                /**�ļ���С*/
                                String sizeOrDir="";
                                if (fileMessage.startsWith("d")) {//�����Ŀ¼
                                    sizeOrDir="<DIR>";
                                }else if (fileMessage.startsWith("-")) {//������ļ�
                                    sizeOrDir=fileMessage.split("\\s+")[4];
                                }
                                /**�ļ���*/
                                String fileName=fileMessage.split("\\s+")[8];
                                /**�ļ�����*/
                                String dateStr =fileMessage.split("\\s+")[5] +" "+fileMessage.split("\\s+")[6]+" " +fileMessage.split("\\s+")[7];

                                FtpFile ftpFile = new FtpFile();
                                // ��FTPĿ¼��Ϣ��ʼ����FTP�ļ�������
                                ftpFile.setLastDate(dateStr);
                                ftpFile.setSize(sizeOrDir);
                                ftpFile.setName(fileName);
                                ftpFile.setPath(pwd);
                                // ���ļ���Ϣ��ӵ������
                                model.addRow(new Object[] { ftpFile, ftpFile.getSize(),
                                        dateStr });
                            }

							//j=i+1;//��һ��λ��Ϊ�ַ�ģʽ
                            j=i+2;//��һ��λ��Ϊ������ģʽ
                        }
                        i=i+1;
                    }
                    list.close();

                } catch (IOException ex) {
                    Logger.getLogger(FTPClientFrame.class.getName()).log(
                            Level.SEVERE, null, ex);
                }
            }
        };
        if (SwingUtilities.isEventDispatchThread()) // �����̶߳���
            runnable.run();
        else
            SwingUtilities.invokeLater(runnable);
    }

    /**
     * ����FTP���ӣ����������ض����̵߳ķ���
     */
    public void setFtpClient(FtpClient ftpClient) {
        this.ftpClient = ftpClient;
        startDownThread();
    }

    /**
     * ˢ��FTP��Դ�������ĵ�ǰ�ļ���
     */
    public void refreshCurrentFolder() {
        try {
            //��ȡ�������ļ��б�
            InputStream list = ftpClient.list(null);
            ByteArrayOutputStream temp = clone(list);
            list.close();
            ftpClient.completePending();
            list = new ByteArrayInputStream(temp.toByteArray());
            listFtpFiles(list);
        } catch (IOException | FtpProtocolException e) {
            e.printStackTrace();
        }
    }

    /**
     * ��ʼ���ض����߳�
     */
    private void startDownThread() {
        if (thread != null)
            thread.stopThread();
        thread = new DownThread(this);
        thread.start();
    }

    /**
     * ֹͣ���ض����߳�
     */
    public void stopDownThread() {
        if (thread != null) {
            thread.stopThread();
            ftpClient = null;
        }
    }

    public String getPwd() {
        String pwd = null;
        try {
            pwd = ftpClient.getWorkingDirectory();
        } catch (IOException | FtpProtocolException e) {
            e.printStackTrace();
        }
        return pwd;
    }

    public Queue<Object[]> getQueue() {
        return queue;
    }

    /**
     * ���FTP��Դ������ݵķ���
     */
    public void clearTable() {
        FtpTableModel model = (FtpTableModel) ftpDiskTable.getModel();
        model.setRowCount(0);
    }

    private static ByteArrayOutputStream clone(InputStream input){
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = input.read(buffer)) > -1 ) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            return baos;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
