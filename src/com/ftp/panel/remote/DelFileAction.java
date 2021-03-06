package com.ftp.panel.remote;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JOptionPane;

import com.ftp.panel.local.LocalPanel;
import sun.net.ftp.FtpClient;
import com.ftp.utils.FtpFile;
import sun.net.ftp.FtpProtocolException;

/**
 * FTP面板的删除按钮的动作处理器
 */
class DelFileAction extends AbstractAction {
	private FtpPanel ftpPanel;

	/**
	 * 删除动作处理器的构造方法
	 *
	 * @param ftpPanel
	 *            - FTP资源管理面板
	 * @param name
	 *            - 动作名称
	 * @param icon
	 *            - 图标
	 */
	public DelFileAction(FtpPanel ftpPanel, String name, Icon icon) {
		super(name, icon);
		this.ftpPanel = ftpPanel;
	}

	public void actionPerformed(ActionEvent e) {
		// 获取显示FTP资源列表的表格组件当前选择的所有行
		final int[] selRows = ftpPanel.ftpDiskTable.getSelectedRows();
		if (selRows.length < 1)
			return;
		int confirmDialog = JOptionPane.showConfirmDialog(ftpPanel, "确定要删除吗？",null,JOptionPane.YES_NO_OPTION);
		if (confirmDialog == JOptionPane.YES_OPTION) {
			Runnable runnable = new Runnable() {

				/**
				 * 删除服务器文件的方法
				 * @param file - 文件名称
				 */
				private void delFile(FtpFile file) {
					FtpClient ftpClient = ftpPanel.ftpClient; // 获取ftpClient实例
					try {
						if (file.isFile()) { // 如果删除的是文件
							ftpClient.deleteFile(file.getName()); // 发送删除文件的命令
							ftpClient.getLastReplyCode(); // 接收返回编码
						} else if (file.isDirectory()) { // 如果删除的是文件夹
							ftpClient.changeDirectory(file.getName()); // 进入到该文件夹

							InputStream inputStream=ftpClient.list(ftpClient.getWorkingDirectory());
							byte[]names=new byte[2048];
							int bufsize=0;
							bufsize=inputStream.read(names, 0, names.length);
							int i=0,j=0;
							while(i<bufsize){
								//字符模式为10，二进制模式为13
//								if (names[i]==10) {
								if (names[i]==13) {
									//文件名在数据中开始做坐标为j,i-j为文件名的长度，文件名在数据中的结束下标为i-1
									String fileMessage = new String(names,j,i-j);
									if(fileMessage.length() == 0){
										System.out.println("fileMessage.length() == 0");
										break;
									}
									//按照空格将fileMessage截为数组后获取相关信息
									// 正则表达式  \s表示空格，｛1，｝表示1一个以上
									if(!fileMessage.split("\\s+")[8].equals(".") && !fileMessage.split("\\s+")[8].equals("..")){
										/**文件大小*/
										String sizeOrDir="";
										if (fileMessage.startsWith("d")) {//如果是目录
											sizeOrDir="<DIR>";
										}else if (fileMessage.startsWith("-")) {//如果是文件
											sizeOrDir=fileMessage.split("\\s+")[4];
										}
										/**文件名*/
										String fileName=fileMessage.split("\\s+")[8];
										/**文件日期*/
										String dateStr =fileMessage.split("\\s+")[5] +fileMessage.split("\\s+")[6] +fileMessage.split("\\s+")[7];
										FtpFile ftpFile = new FtpFile();
										// 将FTP目录信息初始化到FTP文件对象中
										ftpFile.setLastDate(dateStr);
										ftpFile.setSize(sizeOrDir);
										ftpFile.setName(fileName);
										ftpFile.setPath(file.getAbsolutePath());
										// 递归删除文件或文件夹
										delFile(ftpFile);
									}
									//j=i+1;//上一次位置为字符模式
									j=i+2;//上一次位置为二进制模式
								}
								i=i+1;
							}
							ftpClient.changeToParentDirectory(); // 返回上层文件夹
							ftpClient.removeDirectory(file.getName()); // 发送删除文件夹指令
							ftpClient.getLastReplyCode(); // 接收返回码
						}
					} catch (Exception ex) {
						Logger.getLogger(LocalPanel.class.getName()).log(
								Level.SEVERE, null, ex);
					}
				}

				/**
				 * 线程的主体方法
				 */
				public void run() {
					// 遍历显示FTP资源的表格的所有选择行
					for (int i = 0; i < selRows.length; i++) {
						// 获取每行的第一个单元值，并转换为FtpFile类型
						final FtpFile file = (FtpFile) ftpPanel.ftpDiskTable
								.getValueAt(selRows[i], 0);
						if (file != null) {
							delFile(file); // 调用删除文件的递归方法
							try {
								// 向服务器发删除文件夹的方法
								ftpPanel.ftpClient.removeDirectory(file.getName());
								// 读取FTP服务器的返回码
								ftpPanel.ftpClient.getLastReplyCode();
							} catch (IOException | FtpProtocolException e) {
								e.printStackTrace();
							}
						}
					}
					// 刷新FTP服务器资源列表
					DelFileAction.this.ftpPanel.refreshCurrentFolder();
					JOptionPane.showMessageDialog(ftpPanel, "删除成功。");
				}
			};
			new Thread(runnable).start();
		}
	}
}