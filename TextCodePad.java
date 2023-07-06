package tcp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.StringCharacterIterator;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.undo.UndoManager;

public class TextCodePad {

    private JFrame frame;
    private JTextArea textArea;
    private JLabel statusLabel;
    private File currentFile;
    private int zoomLevel = 100;
    private boolean isEditable = true;
    private Charset charset = StandardCharsets.UTF_8;
    private JTextArea linesTextArea;
    
    // Konstanten definieren
    private static final int WINDOW_WIDTH = 960;
    private static final int WINDOW_HEIGHT = 1280;
    private static final Font TEXT_AREA_FONT = new Font("Lucida Sans Typewriter", Font.TRUETYPE_FONT, 14);
    private static final Color TEXT_AREA_BACKGROUND_COLOR = new Color(240,248,255);
    private static final int BASE_FONT_SIZE = 14;

    public TextCodePad() {
        createFrame();
    }

    public void createFrame() {
        initializeFrame();
        initializeTextArea();
        initializeMenuBar();
        initializeStatusLabel();
        initializeToolBar();
        addCaretListenerToTextArea();
        addMouseListenerToTextArea();
        frame.setVisible(true);
    }

    private void initializeFrame() {
        frame = new JFrame();
        frame.setTitle("TextCodePad");
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setResizable(true);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    // Hinzufügen der Rückgängig/Wiederherstellen-Funktion
    private UndoManager undoManager = new UndoManager();

    private void initializeTextArea() {
        textArea = new JTextArea();
        textArea.setFont(TEXT_AREA_FONT);
        textArea.setBackground(TEXT_AREA_BACKGROUND_COLOR);
        textArea.getDocument().addUndoableEditListener(undoManager);
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateLines();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateLines();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateLines();
            }
        });

        linesTextArea = new JTextArea(" 1 ");
        linesTextArea.setEditable(false);
        linesTextArea.setFont( new Font( "Lucida Sans Typewriter", Font.TRUETYPE_FONT, 14 ));
        linesTextArea.setForeground( new Color(218,218,218 ));
        linesTextArea.setBackground(new Color(146, 145, 162));

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.getViewport().add(textArea);
        scrollPane.setRowHeaderView(linesTextArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        frame.add(scrollPane);
    }

    private void initializeMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Datei");

        fileMenu.add(createMenuItem("Neu", KeyEvent.VK_N, e -> textArea.setText("")));
        fileMenu.add(createMenuItem("Neues Fenster", KeyEvent.VK_N, InputEvent.SHIFT_DOWN_MASK, e -> createNewWindow()));
        fileMenu.add(createMenuItem("Öffnen", KeyEvent.VK_O, e -> openFile()));
        fileMenu.add(createMenuItem("Speichern", KeyEvent.VK_S, e -> saveFile()));
        fileMenu.add(createMenuItem("Speichern unter", KeyEvent.VK_S, InputEvent.SHIFT_DOWN_MASK, e -> saveFileAs()));
        
        JMenuItem closeItem = new JMenuItem("Beenden");
        closeItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });
        fileMenu.add(closeItem);


        menuBar.add(fileMenu);
        
        JMenu editMenu = new JMenu("Bearbeiten");
        JMenuItem undoItem = new JMenuItem("Rückgängig");
        undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
        undoItem.addActionListener(e -> {
            if (undoManager.canUndo()) {
                undoManager.undo();
            }
        });
        editMenu.add(undoItem);
        
        JMenuItem redoItem = new JMenuItem("Wiederherstellen");
        redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
        redoItem.addActionListener(e -> {
            if (undoManager.canRedo()) {
                undoManager.redo();
            }
        });
        editMenu.add(redoItem);
        
        JMenu zoomMenu = new JMenu("Zoom");
        editMenu.add(zoomMenu);
        
     // Erstellen Sie das Vergrößern-Menüelement und fügen Sie es zum Zoom-Menü hinzu
        JMenuItem zoomInItem = new JMenuItem("Vergrößern");
        zoomInItem.addActionListener(e -> {
            zoomLevel = Math.min(zoomLevel + 10, 550);
            setZoomLevel(zoomLevel);
        });
        zoomInItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, InputEvent.CTRL_DOWN_MASK));
        zoomInItem.addActionListener(e -> {
            Font currentFont = textArea.getFont();
            float newSize = currentFont.getSize() + 2.0f;
            textArea.setFont(currentFont.deriveFont(newSize));
        });
        zoomMenu.add(zoomInItem);

        // Erstellen Sie das Verkleinern-Menüelement und fügen Sie es zum Zoom-Menü hinzu
        JMenuItem zoomOutItem = new JMenuItem("Verkleinern");
        zoomOutItem.addActionListener(e -> {
            zoomLevel = Math.max(zoomLevel - 10, 10);
            setZoomLevel(zoomLevel);
        });
        zoomOutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK));
        zoomOutItem.addActionListener(e -> {
            Font currentFont = textArea.getFont();
            float newSize = currentFont.getSize() - 2.0f;
            if (newSize > 0) {
                textArea.setFont(currentFont.deriveFont(newSize));
            }
        });
        zoomMenu.add(zoomOutItem);
        
        JMenuItem cutItem = new JMenuItem("Ausschneiden");
        cutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
        cutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.cut();
            }
        });
        editMenu.add(cutItem);
        
        JMenuItem copyItem = new JMenuItem("Kopieren");
        copyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
        copyItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.copy();
            }
        });
        editMenu.add(copyItem);
        
        JMenuItem pasteItem = new JMenuItem("Einfügen");
        pasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
        pasteItem.addActionListener(e -> textArea.paste());
        editMenu.add(pasteItem);
        
        JMenuItem deleteItem = new JMenuItem("Löschen");
        deleteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        deleteItem.addActionListener(e -> textArea.replaceSelection(null));
        editMenu.add(deleteItem);
        
        JMenuItem selectAllItem = new JMenuItem("Alles auswählen");
        selectAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));
        selectAllItem.addActionListener(e -> textArea.selectAll());
        editMenu.add(selectAllItem);

        menuBar.add(editMenu);
        menuBar.add(new JMenu("Über"));
        
        
        frame.setJMenuBar(menuBar);
    }

    private void initializeToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        try {
            BufferedImage image1 = ImageIO.read(new File("C:/Users/Emeric-Andrej/Desktop/OpenJavaSource/jdk-11.0.2/OpenJavaSource/test/tcp/res/ico/newFile_symbol.png"));
            ImageIcon icon1 = new ImageIcon(image1.getScaledInstance(20, 20, Image.SCALE_SMOOTH));
            JLabel label1 = new JLabel(icon1);
            toolBar.add(label1);

            BufferedImage image2 = ImageIO.read(new File("C:/Users/Emeric-Andrej/Desktop/OpenJavaSource/jdk-11.0.2/OpenJavaSource/test/tcp/res/ico/save_symbol.png"));
            ImageIcon icon2 = new ImageIcon(image2.getScaledInstance(20, 20, Image.SCALE_SMOOTH));
            JLabel label2 = new JLabel(icon2);
            toolBar.add(label2);

            // Fügen Sie weitere Bilder hinzu, wie benötigt
        } catch (IOException e) {
            e.printStackTrace();
        }

        frame.add(toolBar, BorderLayout.NORTH);
    }
    
    private JMenuItem createMenuItem(String title, int keyEvent, ActionListener actionListener) {
        return createMenuItem(title, keyEvent, 0, actionListener);
    }

    private JMenuItem createMenuItem(String title, int keyEvent, int modifiers, ActionListener actionListener) {
        JMenuItem menuItem = new JMenuItem(title);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(keyEvent, InputEvent.CTRL_DOWN_MASK | modifiers));
        menuItem.addActionListener(actionListener);
        return menuItem;
    }
        
    private void updateLines() {
        int totalLines = textArea.getLineCount();
        StringBuilder linesText = new StringBuilder();
        for (int i = 1; i <= totalLines; i++) {
            linesText.append(" ").append(Integer.toHexString(i)).append(" \n");
        }
        linesTextArea.setText(linesText.toString());
    }

    private void updateStatusText() {
        int lineNumber = 0;
        int columnNumber = 1;
        int totalChars = textArea.getText().length();

        try {
            int caretPos = textArea.getCaretPosition();
            lineNumber = textArea.getLineOfOffset(caretPos);
            columnNumber = caretPos - textArea.getLineStartOffset(lineNumber);
            lineNumber += 1;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        statusLabel.setText(getStatusText(lineNumber, columnNumber, totalChars));
    }
    
    private void createNewWindow() {
        TextCodePad newPad = new TextCodePad();
        newPad.showFrame();
        newPad.frame.setTitle("Neues - TextCodePad");
    }

    private void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                String content = new String(Files.readAllBytes(Paths.get(selectedFile.getPath())));
                textArea.setText(content);
                currentFile = selectedFile;
                frame.setTitle(currentFile.getName() + " - TextCodePad");
            } catch (IOException ex) {
                showErrorDialog("Fehler beim Öffnen der Datei: " + ex.getMessage());
            }
        }
    }

    private void saveFile() {
        if (currentFile == null) {
            saveFileAs();
        } else {
            try (FileWriter writer = new FileWriter(currentFile)) { // Verwendung von try-with-resources
                writer.write(textArea.getText());
            } catch (IOException ex) {
                showErrorDialog("Fehler beim Speichern der Datei: " + ex.getMessage());
            }
        }
    }
    
    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(frame, message, "Fehler", JOptionPane.ERROR_MESSAGE);
    }

    private void saveFileAs() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showSaveDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            saveFile();
            frame.setTitle(currentFile.getName() + " - TextCodePad");
        }
    }

    private void initializeStatusLabel() {
        statusLabel = new JLabel();
        statusLabel.setText(getStatusText());
        statusLabel.setHorizontalAlignment(JLabel.RIGHT);
        frame.add(statusLabel, BorderLayout.SOUTH);
    }

    private void addCaretListenerToTextArea() {
        textArea.addCaretListener(e -> {
            int lineNumber = 0;
            int columnNumber = 1;
            int totalChars = textArea.getText().length();

            try {
                int caretPos = textArea.getCaretPosition();
                lineNumber = textArea.getLineOfOffset(caretPos);
                columnNumber = caretPos - textArea.getLineStartOffset(lineNumber);
                lineNumber += 1;
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            statusLabel.setText(getStatusText(lineNumber, columnNumber, totalChars));
        });
    }
    
    public String getCurrentFilePath() {
        if (currentFile != null) {
            return currentFile.getAbsolutePath();
        }
        return null;
    }
    
    private String getStatusText(int lineNumber, int columnNumber, int totalChars) {
        String memorySize = calcBytesToMemorySize(totalChars);
        return String.format("%d : %d : %d      |   %s   |   %d%%   |   %s   |   %s   |        Copyright © 2023 by E-Andrej S.            ", 
            lineNumber, columnNumber, totalChars, memorySize, zoomLevel, charset.displayName(), isEditable ? "Editable" : "Not Editable");
    }

    private String getStatusText() {
        return getStatusText(0, 0, 0);
    }
    
    private void setZoomLevel(int percent) {
        if (percent < 10 || percent > 550) {
            throw new IllegalArgumentException("Zoom level must be between 10% and 250%");
        }
        float newSize = BASE_FONT_SIZE * (percent / 100.0f);
        textArea.setFont(textArea.getFont().deriveFont(newSize));
        updateStatusText();
    }
    
    public void setFontSize(int size) {
        Font currentFont = textArea.getFont();
        textArea.setFont(new Font(currentFont.getFontName(), currentFont.getStyle(), size));
    }
    
    public void setFontPoint(int size) {
        Font currentFont = textArea.getFont();
        textArea.setFont(new Font(currentFont.getFontName(), currentFont.getStyle(), size));
    }
    
    public void setFontStyle(String fontName) {
        Font currentFont = textArea.getFont();
        textArea.setFont(new Font(fontName, currentFont.getStyle(), currentFont.getSize()));
    }
    
    public void setTextColor(Color color) {
        textArea.setForeground(color);
    }
    
    public void setBackgroundColor(Color color) {
        textArea.setBackground(color);
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
        statusLabel.setText(getStatusText());
    }

    public void setEditable(boolean isEditable) {
        this.isEditable = isEditable;
        textArea.setEditable(isEditable);
        statusLabel.setText(getStatusText());
    }

    private void addMouseListenerToTextArea() {
        textArea.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                JPopupMenu menu = new JPopupMenu();

                JMenuItem undoItem = new JMenuItem("Rückgängig");
                undoItem.addActionListener(ae -> {
                    if (undoManager.canUndo()) {
                        undoManager.undo();
                    }
                });
                menu.add(undoItem);

                JMenuItem redoItem = new JMenuItem("Wiederherstellen");
                redoItem.addActionListener(ae -> {
                    if (undoManager.canRedo()) {
                        undoManager.redo();
                    }
                });
                menu.add(redoItem);

                JMenuItem cutItem = new JMenuItem("Ausschneiden");
                cutItem.addActionListener(ae -> textArea.cut());
                menu.add(cutItem);

                JMenuItem copyItem = new JMenuItem("Kopieren");
                copyItem.addActionListener(ae -> textArea.copy());
                menu.add(copyItem);

                JMenuItem pasteItem = new JMenuItem("Einfügen");
                pasteItem.addActionListener(ae -> textArea.paste());
                menu.add(pasteItem);

                JMenuItem selectAllItem = new JMenuItem("Alles auswählen");
                selectAllItem.addActionListener(ae -> textArea.selectAll());
                menu.add(selectAllItem);

                menu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    });
    }

    public void showFrame() {
        frame.setVisible(true);
    }

    private static String calcBytesToMemorySize(long bytes) {
        long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);

        if (absB < 1024) {
            return bytes + " B";
        }

        long value = absB;
        StringCharacterIterator ci = new StringCharacterIterator("KMGTPE");

        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }

        value *= Long.signum(bytes);
        return String.format("%.1f %ciB", value / 1024.0, ci.current());
    }
}