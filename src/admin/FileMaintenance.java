package admin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class FileMaintenance {
    public static void main(String[] args) {
        // Ito yung frame pre
        JFrame frame = new JFrame("My First File Explorer");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // making sure that it only closes this window
        frame.setSize(600, 500);
        frame.setLayout(new BorderLayout());

        // Nav Bar 
        JPanel topPanel = new JPanel(new BorderLayout());
        JTextField pathField = new JTextField("C:/"); 
        JButton goButton = new JButton("Open Folder");
        topPanel.add(pathField, BorderLayout.CENTER);
        topPanel.add(goButton, BorderLayout.EAST);

        // Ito ata yung listahan mismo
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> fileList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(fileList);

        // Buttons for Add, Edit, Remove (CRUD)
        JPanel bottomPanel = new JPanel(new GridLayout(1, 3));
        JButton addButton = new JButton("Add File");
        JButton editButton = new JButton("Rename");
        JButton removeButton = new JButton("Delete");
        bottomPanel.add(addButton);
        bottomPanel.add(editButton);
        bottomPanel.add(removeButton);

        // Taga open ng folder
        goButton.addActionListener(e -> {
            File folder = new File(pathField.getText());
            if (folder.exists() && folder.isDirectory()) {
                listModel.clear(); 
                String[] files = folder.list(); 
                if (files != null) {
                    for (String name : files) listModel.addElement(name);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Folder not found!");
            }
        });

        // Create File
        addButton.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(frame, "New File Name (with extension):");
            if (name != null && !name.isEmpty()) {
                try {
                    File newFile = new File(pathField.getText(), name);
                    if (newFile.createNewFile()) {
                        goButton.doClick(); // Refresh listahan
                    } else {
                        JOptionPane.showMessageDialog(frame, "File already exists!");
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Error creating file!");
                }
            }
        });

        // Update/Rename
        editButton.addActionListener(e -> {
            String selected = fileList.getSelectedValue();
            if (selected != null) {
                String newName = JOptionPane.showInputDialog(frame, "Rename to:", selected);
                if (newName != null && !newName.isEmpty()) {
                    File oldFile = new File(pathField.getText(), selected);
                    File newFile = new File(pathField.getText(), newName);
                    if (oldFile.renameTo(newFile)) {
                        goButton.doClick(); // Refresh
                    }
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Pili ka muna ng file, pre!");
            }
        });

        // Delete
        removeButton.addActionListener(e -> {
            String selected = fileList.getSelectedValue();
            if (selected != null) {
                int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete " + selected + "?");
                if (confirm == JOptionPane.YES_OPTION) {
                    File file = new File(pathField.getText(), selected);
                    if (file.delete()) {
                        goButton.doClick(); //Nag rerefresh
                    } else {
                        JOptionPane.showMessageDialog(frame, "Cannot delete file!");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Select a file to remove!");
            }
        });

        // Opening ng file 
        fileList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Double click para bumukas
                    try {
                        File fileToOpen = new File(pathField.getText(), fileList.getSelectedValue());
                        Desktop.getDesktop().open(fileToOpen);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(frame, "Cannot open this file!");
                    }
                }
            }
        });

        // Add lahat sa frame
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null); // I-Center sa screen
        frame.setVisible(true);
    }
}