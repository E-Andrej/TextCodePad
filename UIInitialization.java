package tcp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.nio.charset.Charset;
import java.text.StringCharacterIterator;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Element;

public class UIInitialization {
	
	public JFrame frame;
	private ImageIcon image;
	public JTextArea textArea;
	private JScrollPane scrollPane;
	private JMenuBar menuBar;
	private JLabel statusBar;
	
	public int WINDOW_WIDTH  = 966;
	public int WINDOW_HEIGHT = 1228;
	
	public UIInitialization() {
		
	}
	
	public void initializeFrame() {
        frame = new JFrame();
       
        image = new ImageIcon( "src/tcp/res/icon.png" );
		
		// Skalieren Sie das Bild auf die gewünschte Größe
		Image scaledImage = image.getImage().getScaledInstance(WINDOW_WIDTH, WINDOW_HEIGHT, Image.SCALE_SMOOTH);
		
		frame.setIconImage( scaledImage );
        
        frame.setTitle("TextCodePad");
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setResizable(true);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        initializeTextArea();
        initializeMenuBar();
        initializeStatusBar();
       
		frame.setVisible( true );
    }
	
	private void initializeTextArea() {
		textArea = new JTextArea();
		
		// Setzen Sie den Schriftart
		textArea.setFont(new Font("Lucida Sans Typewriter", Font.TRUETYPE_FONT, 14));
		
		// Setzen Sie die Hintergrundfarbe
		textArea.setBackground(new Color(240, 248, 255));
		
		// Erstellen Sie eine JScrollPane und fügen Sie die JTextArea hinzu
		scrollPane = new JScrollPane(textArea);
		
		// Stellen Sie sicher, dass die Scrollbars immer angezeigt werden
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		// Fügen Sie die JScrollPane zum Frame hinzu
		frame.add(scrollPane);
	}
	
	private void initializeMenuBar() {
		menuBar = new JMenuBar();
		
		// Erstellen Sie die Menüs
		JMenu fileMenu = new JMenu("Datei");
		JMenu editMenu = new JMenu("Bearbeiten");
		
		// Erstellen Sie die MenuItems für das Datei-Menü
		JMenuItem newItem = new JMenuItem("Neu");
		JMenuItem newFileItem = new JMenuItem("Neue Datei");
		JMenuItem openItem = new JMenuItem("Öffnen");
		JMenuItem saveItem = new JMenuItem("Speichern");
		JMenuItem saveAsItem = new JMenuItem("Speichern als");
		JMenuItem closeItem = new JMenuItem("Schließen");
		
		// Fügen Sie die MenuItems zum Datei-Menü hinzu
		fileMenu.add(newItem);
		fileMenu.add(newFileItem);
		fileMenu.add(openItem);
		fileMenu.add(saveItem);
		fileMenu.add(saveAsItem);
		fileMenu.add(closeItem);
		
		// Fügen Sie die Menüs zur MenuBar hinzu
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		
		// Fügen Sie die MenuBar zum Frame hinzu
		frame.setJMenuBar(menuBar);
	}
	
	public void initializeStatusBar() {
	    statusBar = new JLabel();
	    statusBar.setHorizontalAlignment(SwingConstants.RIGHT);
	    updateStatusBar();

	    textArea.addCaretListener(new CaretListener() {
	        @Override
	        public void caretUpdate(CaretEvent e) {
	            updateStatusBar();
	        }
	    });

	    frame.add(statusBar, BorderLayout.SOUTH);
	}
	
	private void updateStatusBar() {
	    int pos = textArea.getCaretPosition();
	    Element map = textArea.getDocument().getDefaultRootElement();

	    int lineNumber = map.getElementIndex(pos);
	    int columnNumber = pos - map.getElement(lineNumber).getStartOffset();
	    int totalChars = textArea.getText().length();

	    String memorySize = calcBytesToMemorySize(totalChars);
	    String charset = Charset.defaultCharset().displayName();

	    statusBar.setText("  " + (lineNumber + 1) + " : " + columnNumber + " : " + totalChars + "   |   " + memorySize + "   |   100%   |   " + charset + "   |   ");
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
