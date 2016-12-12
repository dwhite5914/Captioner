/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stanley.captioner;

import edu.cmu.sphinx.api.Configuration;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import javax.swing.JFileChooser;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author User
 */
public class MainFrame extends javax.swing.JFrame
{
    private File lastAddDirectory = null;
    private File lastOutputDirectory = null;
    private List<File> tempFiles = new ArrayList<File>();

    /**
     * Creates new form Captioner
     */
    public MainFrame()
    {
        initSettings();
        initComponents();
        initListeners();
    }

    public void caption()
    {
        Converter converter = new Converter();

        Configuration config = new Configuration();
        config.setAcousticModelPath(
                "resource:/edu/cmu/sphinx/models/en-us/en-us");
        config.setDictionaryPath(
                "resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
        config.setLanguageModelPath(
                "resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");

        for (int i = 0; i < videoTable.getRowCount(); i++)
        {
            String videoInPath = (String) videoTable.getValueAt(i, 0);
            File videoIn = new File(videoInPath);
            String videoInDirPath = videoIn.getParent();
            String audioOutPath = FilenameUtils.concat(
                    videoInDirPath,
                    FilenameUtils.getBaseName(videoInPath) + ".wav");
            File audioOut = new File(audioOutPath);
            tempFiles.add(audioOut);
            String outputPath = outputDirectoryField.getText();
            String textOutPath = FilenameUtils.concat(
                    outputPath, FilenameUtils.getBaseName(videoInPath) + ".txt");
            File textOut = new File(textOutPath);
            String videoOutPath = FilenameUtils.concat(
                    outputPath, FilenameUtils.getBaseName(videoInPath) + ".mp4");
            File videoOut = new File(videoOutPath);
            System.out.println(videoInPath);
            System.out.println(audioOutPath);
            System.out.println(outputPath);
            System.out.println(textOutPath);
            System.out.println(videoOutPath);
            converter.videoToAudio(videoIn, audioOut);
            Transcriber transcriber = new Transcriber(
                    audioOut, config, textOut, videoIn,
                    quickTestCheckBox.isSelected());
            transcriber.start();

            //Converter.imagesFromVideo(videoIn);
            //Converter.videoFromImages(videoIn, videoOut);
        }
    }

    private Object[] getVideoInfo(File file)
    {
        String path = file.getAbsolutePath();
        Converter converter = new Converter();
        FFprobe ffprobe = null;
        try
        {
            ffprobe = new FFprobe(converter.getFFprobePath());
        }
        catch (IOException e)
        {
            System.out.println("Failed to find ffprobe.");
        }

        FFmpegProbeResult probeResult = null;
        try
        {
            probeResult = ffprobe.probe(path);
        }
        catch (IOException e)
        {
            System.out.println("Failed to probe video file.");
        }

        FFmpegFormat format = probeResult.getFormat();
        FFmpegStream stream = probeResult.getStreams().get(0);

        String type = FilenameUtils.getExtension(path).toUpperCase();
        String size = NumberFormat.getNumberInstance(Locale.US).format(file.length() / 1000) + " KB";

        long millis = stream.duration_ts * 1000;
        TimeZone tz = TimeZone.getTimeZone("UTC");
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        df.setTimeZone(tz);
        String duration = df.format(new Date(millis));

        return new Object[]
        {
            format.filename,
            type,
            size,
            duration,
            stream.codec_name,
            stream.width,
            stream.height
        };
    }

    private void initSettings()
    {
        setResizable(false);
    }

    private void initListeners()
    {
        videoTable.getModel().addTableModelListener(new TableModelListener()
        {
            @Override
            public void tableChanged(TableModelEvent e)
            {
                if (e.getType() == TableModelEvent.INSERT
                        || e.getType() == TableModelEvent.DELETE)
                {
                    if (videoTable.getRowCount() > 0)
                    {
                        clearButton.setEnabled(true);
                        if (outputDirectoryField.getText() != null
                                && outputDirectoryField.getText().length() > 0)
                        {
                            convertButton.setEnabled(true);
                        }
                        else
                        {
                            convertButton.setEnabled(false);
                        }
                    }
                    else
                    {
                        clearButton.setEnabled(false);
                        convertButton.setEnabled(false);
                    }
                }
            }
        });

        videoTable.getSelectionModel()
                .addListSelectionListener(new ListSelectionListener()
                {
                    @Override
                    public void valueChanged(ListSelectionEvent e)
                    {
                        if (videoTable.getSelectedRowCount() > 0)
                        {
                            removeButton.setEnabled(true);
                        }
                        else
                        {
                            removeButton.setEnabled(false);
                        }
                    }
                });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        videoScroller = new javax.swing.JScrollPane();
        videoTable = new javax.swing.JTable();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        clearButton = new javax.swing.JButton();
        convertButton = new javax.swing.JButton();
        outputDirectoryButton = new javax.swing.JButton();
        outputDirectoryField = new javax.swing.JTextField();
        outputFormatCombo = new javax.swing.JComboBox<>();
        quickTestCheckBox = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Automatic Video Captioner");

        videoScroller.setBackground(new java.awt.Color(255, 255, 255));
        videoScroller.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        videoTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {

            },
            new String []
            {
                "File Name", "Type", "Size", "Length", "Codec", "Width", "Height"
            }
        )
        {
            boolean[] canEdit = new boolean []
            {
                false, false, false, false, true, true, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return canEdit [columnIndex];
            }
        });
        videoTable.setFillsViewportHeight(true);
        videoTable.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        videoTable.setShowHorizontalLines(false);
        videoTable.setShowVerticalLines(false);
        videoScroller.setViewportView(videoTable);

        addButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/add_icon.png"))); // NOI18N
        addButton.setText("Add");
        addButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                addButtonActionPerformed(evt);
            }
        });

        removeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/remove_icon.png"))); // NOI18N
        removeButton.setText("Remove");
        removeButton.setEnabled(false);
        removeButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                removeButtonActionPerformed(evt);
            }
        });

        clearButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/clear_icon.png"))); // NOI18N
        clearButton.setText("Clear");
        clearButton.setEnabled(false);
        clearButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                clearButtonActionPerformed(evt);
            }
        });

        convertButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/convert_icon.png"))); // NOI18N
        convertButton.setText("Convert");
        convertButton.setEnabled(false);
        convertButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                convertButtonActionPerformed(evt);
            }
        });

        outputDirectoryButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/output_icon.png"))); // NOI18N
        outputDirectoryButton.setText("Output Directory");
        outputDirectoryButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                outputDirectoryButtonActionPerformed(evt);
            }
        });

        outputDirectoryField.setEditable(false);

        outputFormatCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "MP4", "AVI" }));

        quickTestCheckBox.setText("Quick Test");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(videoScroller)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(outputDirectoryButton)
                        .addGap(18, 18, 18)
                        .addComponent(outputDirectoryField, javax.swing.GroupLayout.PREFERRED_SIZE, 529, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(outputFormatCombo, 0, 80, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(addButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(removeButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(clearButton))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(convertButton)
                                .addGap(60, 60, 60)
                                .addComponent(quickTestCheckBox)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addButton)
                    .addComponent(removeButton)
                    .addComponent(clearButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(videoScroller, javax.swing.GroupLayout.PREFERRED_SIZE, 335, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(outputDirectoryField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(outputFormatCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(outputDirectoryButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(convertButton)
                    .addComponent(quickTestCheckBox))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_addButtonActionPerformed
    {//GEN-HEADEREND:event_addButtonActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "MP4 VIDEOS", "mp4", "mpeg");
        fileChooser.setFileFilter(filter);
        if (lastAddDirectory != null)
        {
            fileChooser.setCurrentDirectory(lastAddDirectory);
        }
        int returnValue = fileChooser.showOpenDialog(this);

        if (returnValue == JFileChooser.APPROVE_OPTION)
        {
            lastAddDirectory = fileChooser.getCurrentDirectory();

            DefaultTableModel model = (DefaultTableModel) videoTable.getModel();
            File files[] = fileChooser.getSelectedFiles();
            for (File file : files)
            {
                boolean alreadyExists = false;
                for (int i = model.getRowCount() - 1; i >= 0; i--)
                {
                    String path = (String) model.getValueAt(i, 0);
                    if (file.getAbsolutePath().equals(path))
                    {
                        alreadyExists = true;
                    }
                }

                if (!alreadyExists)
                {
                    model.addRow(getVideoInfo(file));
                }
            }
        }
    }//GEN-LAST:event_addButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_removeButtonActionPerformed
    {//GEN-HEADEREND:event_removeButtonActionPerformed
        int selectedRows[] = videoTable.getSelectedRows();
        if (selectedRows.length > 0)
        {
            DefaultTableModel model = (DefaultTableModel) videoTable.getModel();
            if (selectedRows.length > 0)
            {
                for (int i = selectedRows.length - 1; i >= 0; i--)
                {
                    model.removeRow(selectedRows[i]);
                }
            }
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_clearButtonActionPerformed
    {//GEN-HEADEREND:event_clearButtonActionPerformed
        DefaultTableModel model = (DefaultTableModel) videoTable.getModel();
        for (int i = model.getRowCount() - 1; i >= 0; i--)
        {
            model.removeRow(i);
        }
    }//GEN-LAST:event_clearButtonActionPerformed

    private void outputDirectoryButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_outputDirectoryButtonActionPerformed
    {//GEN-HEADEREND:event_outputDirectoryButtonActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (lastOutputDirectory != null)
        {
            fileChooser.setCurrentDirectory(lastOutputDirectory);
        }

        int returnValue = fileChooser.showOpenDialog(this);

        if (returnValue == JFileChooser.APPROVE_OPTION)
        {
            lastOutputDirectory = fileChooser.getCurrentDirectory();

            File file = fileChooser.getSelectedFile();
            outputDirectoryField.setText(file.getAbsolutePath());

            if (videoTable.getRowCount() > 0
                    && outputDirectoryField.getText().length() > 0)
            {
                convertButton.setEnabled(true);
            }
        }
    }//GEN-LAST:event_outputDirectoryButtonActionPerformed

    private void convertButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_convertButtonActionPerformed
    {//GEN-HEADEREND:event_convertButtonActionPerformed
        caption();
    }//GEN-LAST:event_convertButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[])
    {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try
        {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
            {
                if ("Nimbus".equals(info.getName()))
                {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        }
        catch (ClassNotFoundException ex)
        {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (InstantiationException ex)
        {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (IllegalAccessException ex)
        {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (javax.swing.UnsupportedLookAndFeelException ex)
        {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                new MainFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton clearButton;
    private javax.swing.JButton convertButton;
    private javax.swing.JButton outputDirectoryButton;
    private javax.swing.JTextField outputDirectoryField;
    private javax.swing.JComboBox<String> outputFormatCombo;
    private javax.swing.JCheckBox quickTestCheckBox;
    private javax.swing.JButton removeButton;
    private javax.swing.JScrollPane videoScroller;
    private javax.swing.JTable videoTable;
    // End of variables declaration//GEN-END:variables
}
