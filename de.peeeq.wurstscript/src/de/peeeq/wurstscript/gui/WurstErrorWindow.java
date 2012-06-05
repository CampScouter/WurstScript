/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * WurstErrorWindow.java
 *
 * Created on 07.12.2011, 10:24:03
 */
package de.peeeq.wurstscript.gui;

import java.awt.Color;
import java.awt.Frame;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.utils.Utils;

/**
 *
 * @author Frotty
 */
public class WurstErrorWindow extends javax.swing.JFrame {
	private static final long serialVersionUID = -451256551943066085L;
	
	
	// Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JTextPane codeArea;
    private javax.swing.JLabel currentStatus;
    private javax.swing.JTextArea errorDetailsPanel;
    private DefaultListModel errorListModel;
//    private javax.swing.JList errorList;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JButton aboutButton;
    
    private String currentFile = "";
    private ArrayList<Integer> currentFileLineList;
   
    public About ab;
    
    /** Creates new form WurstErrorWindow */
    public WurstErrorWindow() {
    	super("Errors");
    	BufferedImage image = null;
        try {
            image = ImageIO.read(
                getClass().getResource("wurst.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        setIconImage(image);
        
        try {
			ab = new About(this, true);
		} catch (URISyntaxException e) {
			java.util.logging.Logger.getLogger(WurstStatusWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, e);
		}
        
        try {
        	UIManager.setLookAndFeel(
					UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(WurstStatusWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(WurstStatusWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(WurstStatusWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(WurstStatusWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        initComponents();
        this.setSize(800,650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Utils.setWindowToCenterOfScreen(this);
        toFront();
        setState(Frame.NORMAL);

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
    	currentStatus = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        errorListModel =  new DefaultListModel();
		final JList errorList = new JList( errorListModel);
        closeButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        codeArea = new javax.swing.JTextPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        errorDetailsPanel = new javax.swing.JTextArea();
        aboutButton = new javax.swing.JButton();
        
        errorList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				int index = errorList.getSelectedIndex();
				if (index >= 0) {
					CompileError err = (CompileError) errorListModel.get(index);
					viewErrorDetail(err);
				}
			}


		});
        
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        
        codeArea.setEditable(false);
        
        currentStatus.setFont(new java.awt.Font("Tahoma", 1, 12));
        currentStatus.setText("Status");

        jScrollPane1.setViewportView(errorList);
        jScrollPane1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        closeButton.setText("Close");
        closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                closeButtonMouseClicked(evt);
            }
        });

        jScrollPane2.setViewportView(codeArea);
        
        aboutButton.setText("About...");
        aboutButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                aboutButtonMouseClicked(evt);
            }
        });


        errorDetailsPanel.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        errorDetailsPanel.setLineWrap(true);
		errorDetailsPanel.setWrapStyleWord(true);
		errorDetailsPanel.setEditable(false);
		errorDetailsPanel.setFont(this.getFont());
		errorDetailsPanel.setBorder(new EmptyBorder(1, 1, 1, 1));
		errorDetailsPanel.setBackground(this.getBackground());
        errorDetailsPanel.setDragEnabled(true);
        
        jScrollPane3.setViewportView(errorDetailsPanel);
        jScrollPane3.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 801, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 801, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 801, Short.MAX_VALUE)
                    .addComponent(currentStatus, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 700, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(aboutButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 669, Short.MAX_VALUE)
                        .addComponent(closeButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(currentStatus)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 39, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 327, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(aboutButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(closeButton, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }

    private void closeButtonMouseClicked(java.awt.event.MouseEvent evt) {
        this.dispose();
    }
    
    private void aboutButtonMouseClicked(java.awt.event.MouseEvent evt) {
        ab.setVisible(true);
    }
    
    private void viewErrorDetail(CompileError err) {
    	setVisible(true);
		this.errorDetailsPanel.setText(err.getMessage());
		
		String fileName = err.getSource().getFile();

		try {
			if (!currentFile.equals(fileName)) {
				currentFile = fileName;
				FileReader fr = new FileReader(fileName);
				codeArea.read(fr, fileName);
			}

			String text = codeArea.getText();

			MutableAttributeSet attrs = codeArea.getInputAttributes();
			StyleConstants.setUnderline(attrs, false);
			StyleConstants.setBackground(attrs, new Color(255, 255, 255));
			// reset highlighting
			codeArea.getStyledDocument().setCharacterAttributes(0, text.length()-1, attrs , true);

			int selectionStart = err.getSource().getLeftPos();
			// select at least one character:
			int selectionEnd = err.getSource().getRightPos();
			
			if (selectionStart == selectionEnd && selectionStart > 0) {
				// select at least one char
				selectionStart --;
			}
			
			StyledDocument doc = codeArea.getStyledDocument();
			String docText = doc.getText(0, doc.getLength());
			
			
			// correct ignored chars (fix for newlines with carriage return):
			int ignoredChars = 0; 
			for (int i=0; i<selectionStart-ignoredChars; i++) {
				char docChar = docText.charAt(i);
				char textChar = text.charAt(i+ignoredChars);
				if (docChar == '\n' && textChar == '\r') {
					ignoredChars++;
				} else if (docChar != textChar) {
					System.err.println("unexpected deviation in texts: " + docChar + " != " + textChar);
					break;
				}
			}
			
			selectionStart -= ignoredChars;
			selectionEnd -= ignoredChars;
						
			selectionStart = Utils.inBorders(0, selectionStart, docText.length()-2);
			selectionEnd = Utils.inBorders(1, selectionEnd, docText.length()-1);
			

			StyleConstants.setUnderline(attrs, true);
			StyleConstants.setBackground(attrs, new Color(255, 150, 150));
			
			
			doc.setCharacterAttributes(selectionStart, selectionEnd-selectionStart, attrs, true);

			codeArea.select(selectionStart, selectionEnd);

		} catch (FileNotFoundException e) {
			codeArea.setText("Could not load file: " + fileName);
		} catch (IOException e) {
			codeArea.setText("Could not read file: " + fileName);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
//	@Override
	public void sendError(CompileError elem) {
		if (errorListModel.isEmpty()) {
			viewErrorDetail(elem);
		}
		errorListModel.addElement(elem);	
		this.requestFocus();
	}

	public void sendFinished() {
		if (errorListModel.isEmpty()) {
			dispose();
		}
	}

	

    
}
