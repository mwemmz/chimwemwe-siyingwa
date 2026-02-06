import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import javax.swing.Timer;

public class LinkedListCitiesUI extends JFrame {
    class Node {
        String city;
        Node next, prev;
        Node(String city) { this.city = city; next = prev = null; }
    }

    class DoublyLinkedList {
        Node head, tail;
        int size = 0;

        void insertAtBeginning(String city) {
            Node newNode = new Node(city);
            if (head == null) head = tail = newNode;
            else { newNode.next = head; head.prev = newNode; head = newNode; }
            size++;
        }

        void insertAtEnd(String city) {
            Node newNode = new Node(city);
            if (tail == null) head = tail = newNode;
            else { tail.next = newNode; newNode.prev = tail; tail = newNode; }
            size++;
        }

        boolean insertAtPosition(String city, int pos) {
            if (pos < 1 || pos > size + 1) return false;
            if (pos == 1) { insertAtBeginning(city); return true; }
            if (pos == size + 1) { insertAtEnd(city); return true; }
            Node cur = head;
            for (int i = 1; i < pos - 1; i++) cur = cur.next;
            Node newNode = new Node(city);
            newNode.next = cur.next;
            newNode.prev = cur;
            cur.next.prev = newNode;
            cur.next = newNode;
            size++;
            return true;
        }

        boolean deleteAtBeginning() {
            if (head == null) return false;
            if (head == tail) head = tail = null;
            else { head = head.next; head.prev = null; }
            size--; return true;
        }

        boolean deleteAtEnd() {
            if (tail == null) return false;
            if (head == tail) head = tail = null;
            else { tail = tail.prev; tail.next = null; }
            size--; return true;
        }

        boolean deleteAtPosition(int pos) {
            if (pos < 1 || pos > size) return false;
            if (pos == 1) return deleteAtBeginning();
            if (pos == size) return deleteAtEnd();
            Node cur = head;
            for (int i = 1; i < pos; i++) cur = cur.next;
            cur.prev.next = cur.next;
            cur.next.prev = cur.prev;
            size--; return true;
        }

        List<String> toList() {
            List<String> out = new ArrayList<>();
            Node cur = head;
            while (cur != null) { out.add(cur.city); cur = cur.next; }
            return out;
        }

        String displayForward() {
            return String.join(" -> ", toList());
        }

        String displayBackward() {
            List<String> l = toList(); Collections.reverse(l); return String.join(" -> ", l);
        }

        int getSize() { return size; }

        String getMiddleCity() {
            if (size == 0) return "List is empty";
            Node slow = head, fast = head;
            while (fast != null && fast.next != null) { slow = slow.next; fast = fast.next.next; }
            return slow.city;
        }
    }

    private final DoublyLinkedList cityList = new DoublyLinkedList();
    private final List<String> validCities = Arrays.asList(
            "Lusaka","Ndola","Kitwe","Chingola","Kabwe","Chipata","Mufulira",
            "Luanshya","Kasama","Livingstone","Solwezi","Mazabuka","Chililabombwe",
            "Mongu","Monze","Choma","Kalulushi","Lukulu","Kapiri Mposhi","Petauke"
    );

    private final Color GOLD = new Color(212, 175, 55);
    private final Color DARK_BLUE = new Color(10, 40, 110);
    private final Color BLACK = Color.BLACK;
    private final Color LIGHT_GREY = new Color(245, 245, 246);
    private boolean darkMode = false;

    // Dark mode colors
    private final Color DARK_BACKGROUND = new Color(45, 45, 48);
    private final Color DARK_TEXT = new Color(240, 240, 240);
    private final Color DARK_CONTROL = new Color(63, 63, 65);
    private final Color DARK_HIGHLIGHT = new Color(0, 122, 204);

    private JTextField cityField = new JTextField(16);
    private JTextField posField = new JTextField(4);
    private JLabel inlineValidation = new JLabel(" ");
    private JLabel statusLabel = new JLabel("Ready");
    private JLabel statusIcon = new JLabel();
    private JTable cityTable;
    private DefaultTableModel tableModel;

    public LinkedListCitiesUI(String name, String studNo) {
        super("LINKED LIST OPERATIONS - " + name + "_" + studNo);
        initLookAndFeel();
        initComponents(name, studNo);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(860, 640);
        setLocationRelativeTo(null);
    }

    private void initLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) { 
                    UIManager.setLookAndFeel(info.getClassName()); 
                    break; 
                }
            }
        } catch (Exception ignored) {}
    }

    private void initComponents(String name, String studNo) {
        JPanel main = new JPanel(new BorderLayout(12, 12));
        main.setBorder(new EmptyBorder(12, 12, 12, 12));
        main.setBackground(LIGHT_GREY);

        // Title bar
        JLabel title = new JLabel(" LINKED LIST OPERATIONS - " + name + "_" + studNo, SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(GOLD);
        title.setOpaque(true);
        title.setBackground(BLACK);
        title.setBorder(new EmptyBorder(12, 8, 12, 8));
        main.add(title, BorderLayout.NORTH);

        // Toolbar
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setBackground(LIGHT_GREY);
        toolbar.add(Box.createHorizontalGlue());
        JToggleButton themeToggle = new JToggleButton("Theme");
        styleToolbarToggle(themeToggle);
        themeToggle.setToolTipText("Toggle Light/Dark Theme");
        themeToggle.addActionListener(e -> toggleTheme(themeToggle.isSelected()));
        toolbar.add(themeToggle);
        main.add(toolbar, BorderLayout.PAGE_START);

        // Center split
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                createControlsPanel(), createTablePanel());
        split.setDividerLocation(360);
        split.setResizeWeight(0.0);
        main.add(split, BorderLayout.CENTER);

        // Status bar
        JPanel status = new JPanel(new BorderLayout(6, 6));
        status.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, DARK_BLUE),
            BorderFactory.createEmptyBorder(6, 6, 6, 6)
        ));
        status.setBackground(BLACK);
        statusIcon.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        statusIcon.setForeground(GOLD);
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        statusLabel.setForeground(GOLD);
        status.add(statusIcon, BorderLayout.WEST);
        status.add(statusLabel, BorderLayout.CENTER);
        main.add(status, BorderLayout.SOUTH);

        setContentPane(main);
        SwingUtilities.invokeLater(() -> cityField.requestFocusInWindow());
    }

    private JPanel createControlsPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(LIGHT_GREY);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,6,10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // City label and field
        gbc.gridy = 0; gbc.gridx = 0; gbc.gridwidth = 1;
        JLabel cityLabel = new JLabel("City:");
        cityLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        p.add(cityLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        cityField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cityField.setToolTipText("Enter a valid Zambian city (e.g., Lusaka).");
        p.add(cityField, gbc);

        // inline validation row
        gbc.gridy = 1; gbc.gridx = 1; gbc.weightx = 1.0;
        inlineValidation.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        inlineValidation.setForeground(Color.RED);
        inlineValidation.setText(" ");
        p.add(inlineValidation, gbc);

        // Position label and field
        gbc.gridy = 2; gbc.gridx = 0; gbc.weightx = 0;
        JLabel posLabel = new JLabel("Position:");
        posLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        p.add(posLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        posField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        posField.setToolTipText("Enter a position (integer).");
        p.add(posField, gbc);

        // Consolidated buttons (Insert / Delete / Display / Show) with dropdowns
        JPanel btnGrid = new JPanel(new GridLayout(0,1,8,8));
        btnGrid.setOpaque(false);
        btnGrid.setBorder(new EmptyBorder(12,0,0,0));

        // Insert consolidated button
        JPanel insertRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        insertRow.setOpaque(false);
        RoundedButton insertBtn = new RoundedButton("Insert", createCircleIcon("+", GOLD));
        styleRoundedButton(insertBtn);
        JPopupMenu insertMenu = new JPopupMenu();
        JMenuItem insBeg = new JMenuItem("At Beginning");
        JMenuItem insEnd = new JMenuItem("At End");
        JMenuItem insPos = new JMenuItem("At Position");
        insertMenu.add(insBeg); insertMenu.add(insEnd); insertMenu.add(insPos);
        insertBtn.addActionListener(e -> insertMenu.show(insertBtn, 0, insertBtn.getHeight()));
        insBeg.addActionListener(e -> insertAtBeginningAction());
        insEnd.addActionListener(e -> insertAtEndAction());
        insPos.addActionListener(e -> insertAtPositionAction());
        insertRow.add(insertBtn);
        btnGrid.add(insertRow);

        // Delete consolidated button
        JPanel delRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        delRow.setOpaque(false);
        RoundedButton deleteBtn = new RoundedButton("Delete", createCircleIcon("X", GOLD));
        styleRoundedButton(deleteBtn);
        JPopupMenu deleteMenu = new JPopupMenu();
        JMenuItem delBeg = new JMenuItem("At Beginning");
        JMenuItem delEnd = new JMenuItem("At End");
        JMenuItem delPos = new JMenuItem("At Position");
        deleteMenu.add(delBeg); deleteMenu.add(delEnd); deleteMenu.add(delPos);
        deleteBtn.addActionListener(e -> deleteMenu.show(deleteBtn, 0, deleteBtn.getHeight()));
        delBeg.addActionListener(e -> deleteAtBeginningAction());
        delEnd.addActionListener(e -> deleteAtEndAction());
        delPos.addActionListener(e -> deleteAtPositionAction());
        delRow.add(deleteBtn);
        btnGrid.add(delRow);

        // Display consolidated button (Forward / Backward)
        JPanel displayRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        displayRow.setOpaque(false);
        RoundedButton displayBtn = new RoundedButton("Display", createCircleIcon("O", GOLD));
        styleRoundedButton(displayBtn);
        JPopupMenu displayMenu = new JPopupMenu();
        JMenuItem dispF = new JMenuItem("Forward (Beginning -> End)");
        JMenuItem dispB = new JMenuItem("Backward (End -> Beginning)");
        displayMenu.add(dispF); displayMenu.add(dispB);
        displayBtn.addActionListener(e -> displayMenu.show(displayBtn, 0, displayBtn.getHeight()));
        dispF.addActionListener(e -> { showPopupTable("Cities (Beginning -> End)", cityList.toList()); statusSuccess("Displayed forward."); });
        dispB.addActionListener(e -> { List<String> rev = new ArrayList<>(cityList.toList()); Collections.reverse(rev); showPopupTable("Cities (End -> Beginning)", rev); statusSuccess("Displayed backward."); });
        displayRow.add(displayBtn);
        btnGrid.add(displayRow);

        // Show consolidated button (Count / Middle)
        JPanel showRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        showRow.setOpaque(false);
        RoundedButton showBtn = new RoundedButton("Show", createCircleIcon("?", GOLD));
        styleRoundedButton(showBtn);
        JPopupMenu showMenu = new JPopupMenu();
        JMenuItem showCnt = new JMenuItem("Count");
        JMenuItem showMid = new JMenuItem("Middle City");
        showMenu.add(showCnt); showMenu.add(showMid);
        showBtn.addActionListener(e -> showMenu.show(showBtn, 0, showBtn.getHeight()));
        showCnt.addActionListener(e -> { showPopupText("Count", "Number of cities: " + cityList.getSize()); statusSuccess("Displayed count."); });
        showMid.addActionListener(e -> { showPopupText("Middle City", "Middle city: " + cityList.getMiddleCity()); statusSuccess("Displayed middle city."); });
        showRow.add(showBtn);
        btnGrid.add(showRow);

        // Help row (no Save/Load)
        JPanel utilRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        utilRow.setOpaque(false);
        RoundedButton helpB = new RoundedButton("Help", createCircleIcon("?", GOLD));
        styleRoundedButton(helpB);
        helpB.addActionListener(e -> showHelp());
        utilRow.add(helpB);
        btnGrid.add(utilRow);

        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2; gbc.weightx = 1.0;
        p.add(btnGrid, gbc);

        // small instructions
        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2; gbc.weightx = 1.0;
        JLabel hint = new JLabel("<html><i>Tip:</i> Valid cities are validated. Use the dropdown buttons to choose specific actions.</html>");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint.setForeground(Color.DARK_GRAY);
        p.add(hint, gbc);

        cityField.getDocument().addDocumentListener(new SimpleDocListener(() -> validateCityInline()));

        return p;
    }

    private JPanel createTablePanel() {
        JPanel p = new JPanel(new BorderLayout(8,8));
        p.setBackground(LIGHT_GREY);
        tableModel = new DefaultTableModel(new Object[]{"Pos", "City"}, 0) {
            public boolean isCellEditable(int r,int c){ return false; }
        };
        cityTable = new JTable(tableModel);
        cityTable.setFont(new Font("Consolas", Font.PLAIN, 14));
        cityTable.setRowHeight(26);
        cityTable.setGridColor(DARK_BLUE);
        cityTable.getTableHeader().setReorderingAllowed(false);
        JScrollPane sp = new JScrollPane(cityTable);
        sp.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(DARK_BLUE, 2), "Current Cities (table)"));
        sp.setPreferredSize(new Dimension(460, 420));

        p.add(sp, BorderLayout.CENTER);

        // bottom quick controls for table
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        JButton refresh = new JButton("Refresh Table");
        styleSmallButton(refresh);
        refresh.addActionListener(e -> refreshTable());
        bottom.add(refresh);
        p.add(bottom, BorderLayout.SOUTH);

        refreshTable();
        return p;
    }

    private void insertAtBeginningAction() {
        String city = cityField.getText().trim();
        if (!isValidCity(city)) { showInlineError("Invalid city!"); return; }
        cityList.insertAtBeginning(city);
        statusSuccess("Inserted at beginning: " + city);
        clearInputsAndRefresh();
    }

    private void insertAtEndAction() {
        String city = cityField.getText().trim();
        if (!isValidCity(city)) { showInlineError("Invalid city!"); return; }
        cityList.insertAtEnd(city);
        statusSuccess("Inserted at end: " + city);
        clearInputsAndRefresh();
    }

    private void insertAtPositionAction() {
        String city = cityField.getText().trim();
        if (!isValidCity(city)) { showInlineError("Invalid city!"); return; }
        int pos;
        try { pos = Integer.parseInt(posField.getText().trim()); }
        catch (Exception ex) { showInlineError("Position must be an integer"); return; }
        if (!cityList.insertAtPosition(city, pos)) { showInlineError("Position out of range"); return; }
        statusSuccess("Inserted " + city + " at " + pos);
        clearInputsAndRefresh();
    }

    private void deleteAtBeginningAction() {
        if (!cityList.deleteAtBeginning()) { statusError("List empty"); return; }
        statusSuccess("Deleted at beginning");
        refreshTable();
    }

    private void deleteAtEndAction() {
        if (!cityList.deleteAtEnd()) { statusError("List empty"); return; }
        statusSuccess("Deleted at end");
        refreshTable();
    }

    private void deleteAtPositionAction() {
        int pos;
        try { pos = Integer.parseInt(posField.getText().trim()); }
        catch (Exception ex) { showInlineError("Position must be integer"); return; }
        if (!cityList.deleteAtPosition(pos)) { showInlineError("Invalid position"); return; }
        statusSuccess("Deleted at position " + pos);
        posField.setText(""); refreshTable();
    }

    private void clearInputsAndRefresh() {
        cityField.setText("");
        posField.setText("");
        inlineValidation.setText(" ");
        refreshTable();
    }

    private void refreshTable() {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            List<String> list = cityList.toList();
            for (int i = 0; i < list.size(); i++) {
                tableModel.addRow(new Object[]{i + 1, list.get(i)});
            }
        });
    }

    private void showPopupText(String title, String message) {
        JDialog d = createPopupDialog(title);
        JTextArea ta = new JTextArea(message);
        ta.setEditable(false);
        ta.setFont(new Font("Consolas", Font.PLAIN, 14));
        ta.setBackground(darkMode ? DARK_CONTROL : LIGHT_GREY);
        ta.setForeground(darkMode ? DARK_TEXT : BLACK);
        ta.setBorder(new EmptyBorder(8,8,8,8));

        JScrollPane sp = new JScrollPane(ta);
        sp.setBorder(BorderFactory.createLineBorder(DARK_BLUE, 2));
        sp.getViewport().setBackground(darkMode ? DARK_CONTROL : LIGHT_GREY);

        d.getContentPane().add(sp, BorderLayout.CENTER);
        animateAndShow(d);
    }

    private void showPopupTable(String title, List<String> data) {
        JDialog d = createPopupDialog(title);
        DefaultTableModel m = new DefaultTableModel(new Object[]{"Pos","City"}, 0) {
            public boolean isCellEditable(int r,int c){ return false; }
        };
        for (int i=0;i<data.size();i++) m.addRow(new Object[]{i+1, data.get(i)});
        JTable t = new JTable(m);
        t.setRowHeight(24);
        t.setFont(new Font("Consolas", Font.PLAIN, 13));
        t.setBackground(darkMode ? DARK_CONTROL : Color.WHITE);
        t.setForeground(darkMode ? DARK_TEXT : BLACK);
        t.setGridColor(DARK_BLUE);
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(BorderFactory.createLineBorder(DARK_BLUE, 2));

        d.getContentPane().add(sp, BorderLayout.CENTER);
        animateAndShow(d);
    }

    private JDialog createPopupDialog(String title) {
        JDialog d = new JDialog(this, title, false);
        d.setSize(520, 360);
        d.setResizable(false);
        d.setUndecorated(true);
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new LineBorder(DARK_BLUE, 3, true));
        root.setBackground(darkMode ? DARK_BACKGROUND : LIGHT_GREY);
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(true);
        titlePanel.setBackground(DARK_BLUE);
        JLabel t = new JLabel("  " + title);
        t.setForeground(GOLD);
        t.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titlePanel.add(t, BorderLayout.WEST);
        
        JButton closeBtn = new JButton("X");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        closeBtn.setContentAreaFilled(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setForeground(GOLD);
        closeBtn.addActionListener(e -> d.dispose());
        closeBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { closeBtn.setForeground(Color.WHITE); }
            public void mouseExited(MouseEvent e) { closeBtn.setForeground(GOLD); }
        });
        titlePanel.add(closeBtn, BorderLayout.EAST);
        
        root.add(titlePanel, BorderLayout.NORTH);
        d.setContentPane(root);
        d.setLocationRelativeTo(this);
        return d;
    }

    private void animateAndShow(final JDialog d) {
        Point center = d.getLocation();
        final int startY = center.y + 60;
        final int endY = center.y;
        d.setLocation(center.x, startY);
        try { d.setOpacity(0f); } catch (Exception ignored) {}
        d.setVisible(true);

        Timer timer = new Timer(12, null);
        final int steps = 14;
        final int[] step = {0};
        timer.addActionListener(e -> {
            step[0]++;
            float t = step[0] / (float) steps;
            int y = startY - Math.round((startY - endY) * t);
            d.setLocation(center.x, y);
            try { d.setOpacity(Math.min(1f, t)); } catch (Exception ignored) {}
            if (step[0] >= steps) { ((Timer)e.getSource()).stop(); }
        });
        timer.start();
    }

    private boolean isValidCity(String city) {
        return validCities.stream().anyMatch(c -> c.equalsIgnoreCase(city));
    }

    private void validateCityInline() {
        String text = cityField.getText().trim();
        if (text.isEmpty()) { inlineValidation.setText(" "); return; }
        if (isValidCity(text)) {
            inlineValidation.setForeground(new Color(0, 120, 0));
            inlineValidation.setText("Valid city");
        } else {
            inlineValidation.setForeground(Color.RED);
            inlineValidation.setText("City not recognized (case-insensitive).");
        }
    }

    private void showInlineError(String msg) {
        inlineValidation.setForeground(Color.RED);
        inlineValidation.setText(msg);
        Toolkit.getDefaultToolkit().beep();
    }

    private void statusSuccess(String msg) {
        statusIcon.setText("✓");
        statusIcon.setForeground(new Color(144, 238, 144));
        statusLabel.setText(msg);
        statusLabel.setForeground(GOLD);
        refreshTable();
    }

    private void statusError(String msg) {
        statusIcon.setText("✖");
        statusIcon.setForeground(Color.RED);
        statusLabel.setText(msg);
        statusLabel.setForeground(Color.RED);
    }

    private void showHelp() {
        String help = "<html><b>LinkedList UI Help</b><br/>" +
                "- Enter a valid Zambian city and use the dropdown buttons to choose operations.<br/>" +
                "- Use Position field for insert/delete at a location.<br/>" +
                "- Display and Show open popups with data.</html>";
        showPopupText("Help", help);
    }

    private class RoundedButton extends JButton {
        RoundedButton(String text, Icon icon) {
            super(text, icon);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setHorizontalAlignment(SwingConstants.LEFT);
            setBackground(DARK_BLUE);
            setForeground(GOLD);
            setFocusPainted(false);
            setBorder(new EmptyBorder(8,10,8,10));
            setPreferredSize(new Dimension(300, 42));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Shape r = new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),14,14);
            g2.setColor(getBackground());
            g2.fill(r);
            super.paintComponent(g);
            g2.dispose();
        }

        @Override
        public boolean isOpaque() { return false; }
    }

    private void styleRoundedButton(AbstractButton b) {
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBackground(DARK_BLUE);
        b.setForeground(GOLD);
        addHoverEffect(b);
    }

    private void addHoverEffect(final AbstractButton btn) {
        btn.addMouseListener(new MouseAdapter() {
            Color orig = btn.getBackground();
            public void mouseEntered(MouseEvent e) { btn.setBackground(orig.brighter()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(orig); }
        });
    }

    private void styleSmallButton(AbstractButton btn) {
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setBackground(DARK_BLUE);
        btn.setForeground(GOLD);
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(GOLD, 1, true));
        btn.setOpaque(true);
        addHoverEffect(btn);
    }

    private void styleToolbarToggle(JToggleButton t) {
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setBackground(DARK_BLUE);
        t.setForeground(GOLD);
        t.setBorder(new LineBorder(GOLD, 1, true));
        t.setOpaque(true);
    }

    private Icon createCircleIcon(String symbol, Color fg) {
        int size = 22;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(DARK_BLUE);
        g.fillOval(0,0,size,size);
        g.setColor(fg);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        FontMetrics fm = g.getFontMetrics();
        int w = fm.stringWidth(symbol);
        int h = fm.getAscent();
        g.drawString(symbol, (size - w)/2, (size + h)/2 - 2);
        g.dispose();
        return new ImageIcon(img);
    }

    private interface DocChange { void run(); }
    private class SimpleDocListener implements javax.swing.event.DocumentListener {
        private final DocChange r;
        SimpleDocListener(DocChange r){ this.r=r; }
        public void insertUpdate(javax.swing.event.DocumentEvent e){ r.run(); }
        public void removeUpdate(javax.swing.event.DocumentEvent e){ r.run(); }
        public void changedUpdate(javax.swing.event.DocumentEvent e){ r.run(); }
    }

    private void toggleTheme(boolean on) {
        darkMode = on;
        
        if (darkMode) {
            // Dark mode settings
            UIManager.put("control", DARK_CONTROL);
            UIManager.put("text", DARK_TEXT);
            UIManager.put("nimbusBase", new Color(18, 18, 18));
            UIManager.put("nimbusFocus", DARK_HIGHLIGHT);
            UIManager.put("nimbusSelectionBackground", DARK_HIGHLIGHT);
            
            // Apply to components
            getContentPane().setBackground(DARK_BACKGROUND);
            updateComponentColors(getContentPane(), DARK_TEXT, DARK_BACKGROUND);
            
            // Special handling for text components
            cityField.setBackground(DARK_CONTROL);
            cityField.setForeground(DARK_TEXT);
            cityField.setCaretColor(DARK_TEXT);
            posField.setBackground(DARK_CONTROL);
            posField.setForeground(DARK_TEXT);
            posField.setCaretColor(DARK_TEXT);
            inlineValidation.setForeground(DARK_TEXT);
            
            // Update table
            cityTable.setBackground(DARK_CONTROL);
            cityTable.setForeground(DARK_TEXT);
            cityTable.setGridColor(DARK_BLUE);
            cityTable.getTableHeader().setBackground(DARK_BACKGROUND);
            cityTable.getTableHeader().setForeground(DARK_TEXT);
        } else {
            // Light mode settings
            UIManager.put("control", LIGHT_GREY);
            UIManager.put("text", BLACK);
            UIManager.put("nimbusBase", new Color(51, 98, 140));
            UIManager.put("nimbusFocus", new Color(115, 164, 209));
            UIManager.put("nimbusSelectionBackground", new Color(57, 105, 138));
            
            // Apply to components
            getContentPane().setBackground(LIGHT_GREY);
            updateComponentColors(getContentPane(), BLACK, LIGHT_GREY);
            
            // Special handling for text components
            cityField.setBackground(Color.WHITE);
            cityField.setForeground(BLACK);
            cityField.setCaretColor(BLACK);
            posField.setBackground(Color.WHITE);
            posField.setForeground(BLACK);
            posField.setCaretColor(BLACK);
            inlineValidation.setForeground(BLACK);
            
            // Update table
            cityTable.setBackground(Color.WHITE);
            cityTable.setForeground(BLACK);
            cityTable.setGridColor(DARK_BLUE);
            cityTable.getTableHeader().setBackground(LIGHT_GREY);
            cityTable.getTableHeader().setForeground(BLACK);
        }
        
        SwingUtilities.updateComponentTreeUI(this);
        repaint();
    }

    private void updateComponentColors(Container container, Color foreground, Color background) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JComponent) {
                JComponent jcomp = (JComponent) comp;
                if (!(jcomp instanceof AbstractButton)) {
                    jcomp.setForeground(foreground);
                    jcomp.setBackground(background);
                    
                    if (jcomp instanceof JLabel) {
                        JLabel label = (JLabel) jcomp;
                        if (label.getText().equals("City:") || label.getText().equals("Position:")) {
                            label.setForeground(foreground);
                        }
                    }
                }
            }
            if (comp instanceof Container) {
                updateComponentColors((Container) comp, foreground, background);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LinkedListCitiesUI app = new LinkedListCitiesUI("CHIMWEMWE_SIYINGWA", "BIT24126947");
            app.setVisible (true);
        });
    }
}