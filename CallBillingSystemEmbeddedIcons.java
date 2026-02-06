import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

import javax.swing.table.DefaultTableCellRenderer;

public class CallBillingSystemEmbeddedIcons extends JFrame {

    private static final String APP_OWNER = "ChimwemweSiyingwa_BIT24126947";

    // Table and data
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final List<CDR> cdrList = new ArrayList<>();

    // UI components
    private final JTextField searchField = new JTextField(22);
    private final JLabel statusLabel = new JLabel("No records loaded.");
    private boolean darkTheme = false;

    // Color scheme - green, blue, grey
    private final Color PRIMARY_GREEN = new Color(0, 128, 0);
    private final Color SECONDARY_BLUE = new Color(70, 130, 180);
    private final Color LIGHT_GREY = new Color(220, 220, 220);
    private final Color MEDIUM_GREY = new Color(180, 180, 180);
    private final Color DARK_GREY = new Color(100, 100, 100);
    private final Color LIGHT_TEXT = Color.WHITE;
    private final Color DARK_TEXT = DARK_GREY;

    // Date formats
    private final DateTimeFormatter[] acceptedDateFormats = new DateTimeFormatter[]{
            DateTimeFormatter.ofPattern("yyyy-MM-dd:HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd:hh:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
    };

    public CallBillingSystemEmbeddedIcons() {
        super("CBS – " + APP_OWNER);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 660);
        setLocationRelativeTo(null);

        setPreferredLookAndFeel();
        applyThemeColors();

        // Top toolbar
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setBorder(new EmptyBorder(6, 6, 6, 6));
        toolbar.setBackground(LIGHT_GREY);

        // Buttons with embedded icons
        JButton btnLoad = createToolButton(drawIcon("folder", 28, 28, PRIMARY_GREEN), "Load CDRs", e -> actionLoad());
        JButton btnLinear = createToolButton(drawIcon("search", 28, 28, SECONDARY_BLUE), "Linear Search", e -> actionLinearSearchDialog());
        JButton btnBinary = createToolButton(drawIcon("search2", 28, 28, SECONDARY_BLUE), "Binary Search", e -> actionBinarySearchDialog());
        JButton btnSort = createToolButton(drawIcon("sort", 28, 28, PRIMARY_GREEN), "Sort by Duration", e -> actionSortByDuration());
        JButton btnExport = createToolButton(drawIcon("export", 28, 28, PRIMARY_GREEN), "Export CSV", e -> actionExportCSV());
        JButton btnTheme = createToolButton(drawIcon("theme", 28, 28, DARK_GREY), "Toggle Theme", e -> toggleTheme());
        JButton btnExit = createToolButton(drawIcon("exit", 28, 28, new Color(200, 60, 60)), "Exit", e -> System.exit(0));

        // Add toolbar components
        toolbar.add(btnLoad);
        toolbar.addSeparator(new Dimension(10, 0));
        toolbar.add(new JLabel("Search: "));
        toolbar.add(searchField);
        toolbar.add(btnLinear);
        toolbar.add(btnBinary);
        toolbar.addSeparator(new Dimension(10, 0));
        toolbar.add(btnSort);
        toolbar.add(btnExport);
        toolbar.addSeparator();
        toolbar.add(btnTheme);
        toolbar.add(btnExit);

        add(toolbar, BorderLayout.NORTH);

        // Table model and table
        String[] cols = {"Call ID", "Sender", "Receiver", "Timestamp", "Duration (ms)"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setRowHeight(26);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setupColumnWidths();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBackground(LIGHT_GREY);
        add(scrollPane, BorderLayout.CENTER);

        // Status bar
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, MEDIUM_GREY), new EmptyBorder(6, 10, 6, 10)));
        statusBar.setBackground(LIGHT_GREY);
        statusLabel.setText("Ready");
        statusLabel.setForeground(DARK_TEXT);
        statusBar.add(statusLabel, BorderLayout.WEST);
        add(statusBar, BorderLayout.SOUTH);

        // Menu bar
        setJMenuBar(buildMenuBar());

        // Listeners
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            private void doSearch() {
                String q = searchField.getText().trim();
                if (q.isEmpty()) {
                    table.clearSelection();
                    updateStatus(String.format("%d records", cdrList.size()));
                    return;
                }
                int idx = linearSearchIndex(q);
                if (idx >= 0) {
                    int viewRow = table.convertRowIndexToView(idx);
                    table.getSelectionModel().setSelectionInterval(viewRow, viewRow);
                    table.scrollRectToVisible(table.getCellRect(viewRow, 0, true));
                    updateStatus("Found (linear): " + q);
                } else {
                    table.clearSelection();
                    updateStatus("No match: " + q);
                }
            }
            public void insertUpdate(DocumentEvent e) { doSearch(); }
            public void removeUpdate(DocumentEvent e) { doSearch(); }
            public void changedUpdate(DocumentEvent e) { doSearch(); }
        });

        // Double click row to view details
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
                    showCDRDetails(cdrList.get(modelRow));
                }
            }
        });
    }

    private void applyThemeColors() {
        if (darkTheme) {
            applyDarkTheme();
        } else {
            applyLightTheme();
        }
    }

    private void applyLightTheme() {
        // Background colors
        UIManager.put("control", LIGHT_GREY);
        UIManager.put("nimbusLightBackground", LIGHT_GREY);
        UIManager.put("nimbusBase", PRIMARY_GREEN);
        UIManager.put("nimbusFocus", SECONDARY_BLUE);
        
        // Text colors
        UIManager.put("text", DARK_TEXT);
        UIManager.put("textText", DARK_TEXT);
        UIManager.put("textForeground", DARK_TEXT);
        UIManager.put("Label.foreground", DARK_TEXT);
        UIManager.put("TextField.foreground", DARK_TEXT);
        UIManager.put("TextArea.foreground", DARK_TEXT);
        UIManager.put("Table.foreground", DARK_TEXT);
        UIManager.put("Menu.foreground", DARK_TEXT);
        UIManager.put("MenuItem.foreground", DARK_TEXT);
        
        // Component colors
        UIManager.put("Table.background", Color.WHITE);
        UIManager.put("Table.gridColor", MEDIUM_GREY);
        UIManager.put("Table.selectionBackground", SECONDARY_BLUE);
        UIManager.put("Table.selectionForeground", LIGHT_TEXT);
        UIManager.put("TextField.background", Color.WHITE);
        UIManager.put("TextField.caretForeground", SECONDARY_BLUE);
        UIManager.put("MenuBar.background", LIGHT_GREY);
        UIManager.put("Menu.background", LIGHT_GREY);
        UIManager.put("MenuItem.background", LIGHT_GREY);
        UIManager.put("MenuItem.selectionBackground", SECONDARY_BLUE);
        UIManager.put("MenuItem.selectionForeground", LIGHT_TEXT);
        UIManager.put("Button.background", LIGHT_GREY);
        UIManager.put("Button.foreground", DARK_TEXT);
        UIManager.put("Button.select", SECONDARY_BLUE);
        UIManager.put("ScrollPane.background", LIGHT_GREY);
        
        // Update component colors
        statusLabel.setForeground(DARK_TEXT);
        searchField.setForeground(DARK_TEXT);
        searchField.setBackground(Color.WHITE);
    }

    private void applyDarkTheme() {
        Color darkBg = new Color(45, 45, 45);
        Color darkerBg = new Color(30, 30, 30);
        Color darkText = LIGHT_TEXT;
        Color darkHighlight = new Color(75, 110, 175);
        
        // Background colors
        UIManager.put("control", darkBg);
        UIManager.put("info", darkerBg);
        UIManager.put("nimbusBase", new Color(18, 30, 49));
        UIManager.put("nimbusFocus", darkHighlight);
        UIManager.put("nimbusLightBackground", darkBg);
        
        // Text colors
        UIManager.put("text", darkText);
        UIManager.put("textText", darkText);
        UIManager.put("textForeground", darkText);
        UIManager.put("Label.foreground", darkText);
        UIManager.put("TextField.foreground", darkText);
        UIManager.put("TextArea.foreground", darkText);
        UIManager.put("Table.foreground", darkText);
        UIManager.put("Menu.foreground", darkText);
        UIManager.put("MenuItem.foreground", darkText);
        
        // Component colors
        UIManager.put("Table.background", darkerBg);
        UIManager.put("Table.gridColor", new Color(80, 80, 80));
        UIManager.put("Table.selectionBackground", darkHighlight);
        UIManager.put("Table.selectionForeground", darkText);
        UIManager.put("TextField.background", darkerBg);
        UIManager.put("TextField.caretForeground", SECONDARY_BLUE);
        UIManager.put("MenuBar.background", darkBg);
        UIManager.put("Menu.background", darkBg);
        UIManager.put("MenuItem.background", darkBg);
        UIManager.put("MenuItem.selectionBackground", darkHighlight);
        UIManager.put("MenuItem.selectionForeground", darkText);
        UIManager.put("Button.background", darkBg);
        UIManager.put("Button.foreground", darkText);
        UIManager.put("Button.select", darkHighlight);
        UIManager.put("ScrollPane.background", darkBg);
        
        // Update component colors
        statusLabel.setForeground(darkText);
        searchField.setForeground(darkText);
        searchField.setBackground(darkerBg);
    }

    private void toggleTheme() {
        darkTheme = !darkTheme;
        applyThemeColors();
        SwingUtilities.updateComponentTreeUI(this);
        updateStatus(darkTheme ? "Dark theme enabled" : "Light theme enabled");
    }

    private void setupColumnWidths() {
        table.getColumnModel().getColumn(0).setPreferredWidth(120);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(360);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);
        // right align duration
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(4).setCellRenderer(r);
    }

    private JButton createToolButton(Icon icon, String tooltip, ActionListener act) {
        JButton b = new JButton(icon);
        b.setToolTipText(tooltip);
        b.setFocusable(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.addActionListener(act);
        b.setMargin(new Insets(4, 6, 4, 6));
        b.setBackground(LIGHT_GREY);
        b.setForeground(DARK_TEXT);
        return b;
    }

    private JMenuBar buildMenuBar() {
        JMenuBar mb = new JMenuBar();
        mb.setBackground(LIGHT_GREY);

        JMenu file = new JMenu("File");
        file.setForeground(DARK_TEXT);
        JMenuItem load = new JMenuItem("Load CDRs...");
        load.setIcon(drawIcon("folder", 16, 16, PRIMARY_GREEN));
        load.addActionListener(e -> actionLoad());
        JMenuItem export = new JMenuItem("Export CSV");
        export.setIcon(drawIcon("export", 16, 16, PRIMARY_GREEN));
        export.addActionListener(e -> actionExportCSV());
        JMenuItem exit = new JMenuItem("Exit");
        exit.setIcon(drawIcon("exit", 16, 16, new Color(200, 60, 60)));
        exit.addActionListener(e -> System.exit(0));
        file.add(load);
        file.add(export);
        file.addSeparator();
        file.add(exit);

        JMenu search = new JMenu("Search");
        search.setForeground(DARK_TEXT);
        JMenuItem lin = new JMenuItem("Linear Search by Call ID");
        lin.addActionListener(e -> actionLinearSearchDialog());
        JMenuItem bin = new JMenuItem("Binary Search by Call ID");
        bin.addActionListener(e -> actionBinarySearchDialog());
        search.add(lin);
        search.add(bin);

        JMenu sort = new JMenu("Sort");
        sort.setForeground(DARK_TEXT);
        JMenuItem sortDur = new JMenuItem("Selection Sort by Duration (asc)");
        sortDur.addActionListener(e -> actionSortByDuration());
        sort.add(sortDur);

        JMenu help = new JMenu("Help");
        help.setForeground(DARK_TEXT);
        JMenuItem about = new JMenuItem("About");
        about.setIcon(drawIcon("info", 16, 16, SECONDARY_BLUE));
        about.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Call Billing System\nDeveloper: " + APP_OWNER + "\nVersion: 1.0\nGreen/Blue/Grey Theme",
                "About", JOptionPane.INFORMATION_MESSAGE));
        JMenuItem themeToggle = new JMenuItem("Toggle Theme");
        themeToggle.addActionListener(e -> toggleTheme());
        help.add(themeToggle);
        help.add(about);

        mb.add(file);
        mb.add(search);
        mb.add(sort);
        mb.add(Box.createHorizontalGlue());
        mb.add(help);
        return mb;
    }

    private void updateStatus(String s) {
        statusLabel.setText(s);
    }

    private void actionLoad() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select CDR comma-delimited text file");
        int res = fc.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();
        loadFromFile(f);
    }

    private void loadFromFile(File file) {
        List<CDR> temp = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            int ln = 0;
            while ((line = br.readLine()) != null) {
                ln++;
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length < 5) continue;
                String callID = parts[0].trim();
                String sender = parts[1].trim();
                String receiver = parts[2].trim();
                String timestamp = parts[3].trim();
                long duration = 0L;
                try { duration = Long.parseLong(parts[4].trim()); } catch (Exception ignored) {}
                timestamp = normalizeTimestamp(timestamp);
                temp.add(new CDR(callID, sender, receiver, timestamp, duration));
            }
            cdrList.clear();
            cdrList.addAll(temp);
            refreshTable();
            updateStatus("Loaded " + cdrList.size() + " records from " + file.getName());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to read file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String normalizeTimestamp(String ts) {
        for (DateTimeFormatter f : acceptedDateFormats) {
            try {
                LocalDateTime dt = LocalDateTime.parse(ts, f);
                return dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (Exception ignored) {}
        }
        return ts;
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (CDR c : cdrList) {
            tableModel.addRow(new Object[]{c.callID, c.sender, c.receiver, c.timestamp, c.duration});
        }
    }

    private void actionSortByDuration() {
        if (cdrList.size() < 2) {
            updateStatus("Not enough records to sort.");
            return;
        }
        for (int i = 0; i < cdrList.size() - 1; i++) {
            int min = i;
            for (int j = i + 1; j < cdrList.size(); j++) {
                if (cdrList.get(j).duration < cdrList.get(min).duration) min = j;
            }
            if (min != i) Collections.swap(cdrList, i, min);
        }
        refreshTable();
        updateStatus("Selection sort by duration completed.");
    }

    private void actionLinearSearchDialog() {
        String q = JOptionPane.showInputDialog(this, "Enter Call ID (Linear Search):");
        if (q == null || q.trim().isEmpty()) return;
        int idx = linearSearchIndex(q.trim());
        if (idx >= 0) {
            int viewRow = table.convertRowIndexToView(idx);
            table.getSelectionModel().setSelectionInterval(viewRow, viewRow);
            table.scrollRectToVisible(table.getCellRect(viewRow, 0, true));
            showCDRDetails(cdrList.get(idx));
            updateStatus("Linear search: found " + q);
        } else {
            JOptionPane.showMessageDialog(this, "No CDR found with Call ID: " + q, "Not found", JOptionPane.WARNING_MESSAGE);
            updateStatus("Linear search: not found " + q);
        }
    }

    private void actionBinarySearchDialog() {
        String q = JOptionPane.showInputDialog(this, "Enter Call ID (Binary Search):");
        if (q == null || q.trim().isEmpty()) return;
        Optional<CDR> res = binarySearchByCallID(q.trim());
        if (res.isPresent()) {
            CDR found = res.get();
            for (int i = 0; i < cdrList.size(); i++) {
                if (cdrList.get(i).callID.equalsIgnoreCase(found.callID)) {
                    int viewRow = table.convertRowIndexToView(i);
                    table.getSelectionModel().setSelectionInterval(viewRow, viewRow);
                    table.scrollRectToVisible(table.getCellRect(viewRow, 0, true));
                    break;
                }
            }
            showCDRDetails(found);
            updateStatus("Binary search: found " + q);
        } else {
            JOptionPane.showMessageDialog(this, "No CDR found with Call ID: " + q, "Not found", JOptionPane.WARNING_MESSAGE);
            updateStatus("Binary search: not found " + q);
        }
    }

    private int linearSearchIndex(String callID) {
        for (int i = 0; i < cdrList.size(); i++) if (cdrList.get(i).callID.equalsIgnoreCase(callID)) return i;
        return -1;
    }

    private Optional<CDR> binarySearchByCallID(String callID) {
        if (cdrList.isEmpty()) return Optional.empty();
        List<CDR> copy = new ArrayList<>(cdrList);
        copy.sort(Comparator.comparing(c -> c.callID.toLowerCase()));
        int l = 0, r = copy.size() - 1;
        while (l <= r) {
            int m = (l + r) >>> 1;
            int cmp = copy.get(m).callID.compareToIgnoreCase(callID);
            if (cmp == 0) return Optional.of(copy.get(m));
            if (cmp < 0) l = m + 1; else r = m - 1;
        }
        return Optional.empty();
    }

    private void actionExportCSV() {
        if (cdrList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No records to export.", "Export", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save CSV");
        int res = fc.showSaveDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;
        File out = fc.getSelectedFile();
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(out), StandardCharsets.UTF_8))) {
            pw.println("CallID,Sender,Receiver,Timestamp,Duration");
            for (CDR c : cdrList) {
                pw.printf("%s,%s,%s,%s,%d%n",
                        csvQuote(c.callID),
                        csvQuote(c.sender),
                        csvQuote(c.receiver),
                        csvQuote(c.timestamp),
                        c.duration);
            }
            updateStatus("Exported " + cdrList.size() + " records to " + out.getName());
            JOptionPane.showMessageDialog(this, "Export successful: " + out.getAbsolutePath(), "Export", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String csvQuote(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) return "\"" + s.replace("\"", "\"\"") + "\"";
        return s;
    }

    private void showCDRDetails(CDR c) {
        String msg = String.format("Call ID: %s%nSender: %s%nReceiver: %s%nTimestamp: %s%nDuration: %d ms",
                c.callID, c.sender, c.receiver, c.timestamp, c.duration);
        JOptionPane.showMessageDialog(this, msg, "CDR Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private ImageIcon drawIcon(String type, int sizeX, int sizeY, Color tint) {
        BufferedImage img = new BufferedImage(sizeX, sizeY, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color base = (tint != null) ? tint : new Color(40, 76, 120);
        Color secondary = new Color(100, 140, 200);
        Color white = new Color(255, 255, 255);

        g.setColor(new Color(0, 0, 0, 0));
        g.fillRect(0, 0, sizeX, sizeY);

        switch (type) {
            case "folder":
                g.setColor(base);
                g.fillRoundRect(2, sizeY/4, sizeX-4, sizeY/2, 6, 6);
                g.setColor(secondary);
                g.fillRoundRect(2, sizeY/6, sizeX/2, sizeY/3, 6, 6);
                break;
            case "search":
                int r = Math.min(sizeX, sizeY)*3/10;
                g.setColor(base);
                g.fillOval(2, 2, r*2, r*2);
                g.setColor(new Color(255,255,255,200));
                g.fillOval(4, 4, r*2-4, r*2-4);
                g.setColor(base);
                g.setStroke(new BasicStroke(Math.max(2, sizeX/12)));
                g.drawLine( (int)(r*1.5)+2, (int)(r*1.5)+2, sizeX-3, sizeY-3 );
                break;
            case "search2":
                g.setColor(base);
                g.setStroke(new BasicStroke(Math.max(2, sizeX/12)));
                g.drawOval(2, 2, sizeX-10, sizeY-10);
                g.drawLine(sizeX-10, sizeY-10, sizeX-3, sizeY-3);
                break;
            case "sort":
                g.setColor(base);
                int cx = sizeX/2;
                int top = 4;
                Polygon up = new Polygon(new int[]{cx, cx-6, cx+6}, new int[]{top, top+10, top+10}, 3);
                g.fill(up);
                Polygon down = new Polygon(new int[]{cx, cx-6, cx+6}, new int[]{sizeY-top, sizeY-top-10, sizeY-top-10}, 3);
                g.fill(down);
                break;
            case "export":
                g.setColor(base);
                int w = sizeX-6, h = sizeY-8;
                g.fillRect(3, 4, w, h/2);
                Polygon p = new Polygon(new int[]{sizeX/2 - 8, sizeX/2, sizeX/2 + 8},
                        new int[]{h/2 + 4, h/2 + 12, h/2 + 4}, 3);
                g.setColor(secondary);
                g.fill(p);
                break;
            case "info":
                g.setColor(base);
                g.fillOval(2, 2, sizeX-4, sizeY-4);
                g.setColor(white);
                g.fillRect(sizeX/2-1, sizeY/2-2, 2, sizeY/3);
                g.fillOval(sizeX/2-1, 6, 4, 4);
                break;
            case "exit":
                g.setColor(new Color(200, 60, 60));
                g.setStroke(new BasicStroke(3));
                g.drawLine(4, 4, sizeX-4, sizeY-4);
                g.drawLine(sizeX-4, 4, 4, sizeY-4);
                break;
            case "theme":
                if (!darkTheme) {
                    g.setColor(new Color(235,180,60));
                    g.fillOval(4, 4, sizeX-8, sizeY-8);
                } else {
                    g.setColor(new Color(200,200,210));
                    g.fillOval(4, 4, sizeX-8, sizeY-8);
                    g.setColor(new Color(60,60,60));
                    g.fillOval(sizeX/3, sizeY/4, sizeX/2, sizeY/2);
                }
                break;
            default:
                g.setColor(base);
                g.fillRect(4, 4, sizeX-8, sizeY-8);
        }

        g.dispose();
        return new ImageIcon(img);
    }

    private void setPreferredLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
        } catch (Exception ignored) {}
    }

    private static class CDR {
        final String callID;
        final String sender;
        final String receiver;
        final String timestamp;
        final long duration;
        CDR(String callID, String sender, String receiver, String timestamp, long duration) {
            this.callID = callID; this.sender = sender; this.receiver = receiver; this.timestamp = timestamp; this.duration = duration;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CallBillingSystemEmbeddedIcons app = new CallBillingSystemEmbeddedIcons();
            app.setVisible(true);
        });
    }
}