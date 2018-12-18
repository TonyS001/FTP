package com.ftp;

import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FTPmain {
    //Ӧ�ó������
    public static void main(String args[]) {
        //���� runnable �� run ������ EventQueue ��ָ���߳��ϱ����á�
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    //ʹ�� LookAndFeel �������õ�ǰ��Ĭ����ۡ�
                    UIManager.setLookAndFeel(new NimbusLookAndFeel());//����һ���ǳ�Ư�������
                    FTPClientFrame client_Frame = new FTPClientFrame();
                    client_Frame.setVisible(true);
                } catch (Exception ex) {
                    Logger.getLogger(FTPClientFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
}