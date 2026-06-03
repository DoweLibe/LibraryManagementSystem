/*
    Hi Professor,

    I'm sending this version of my final project so you can look it over.
    If anything needs to be changed or fixed please let me know as soon as you can.
    Thank you for taking the time to review it.
*/

// Programming Assignment 14 - Library Management System
// Compiled using JDoodle 
// I included everything we learned: GUI, abstract classes, inheritance,
// interfaces, file I/O, try/catch, sorting, searching, etc.

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class LibraryManagementSystem extends JFrame {

    // using ArrayList to store all the items (books + magazines)
    private ArrayList<LibraryItem> items = new ArrayList<>();

    // GUI stuff
    private DefaultTableModel tableModel;
    private JTable table;
    private JTree tree;
    private JLabel statsLabel;

    // Add Item fields
    private JTextField titleField;
    private JTextField authorField;
    private JTextField yearField;
    private JTextField extraField;
    private JComboBox<String> typeCombo;

    // Search fields
    private JTextField searchField;
    private JTextArea searchResultArea;

    public LibraryManagementSystem() {
        super("Library Management System");

        // setting up the main window
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // help menu
        setJMenuBar(createMenuBar());

        // tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Add Item", createAddItemPanel());
        tabs.addTab("Search", createSearchPanel());
        tabs.addTab("View All", createViewAllPanel());
        tabs.addTab("File Operations", createFilePanel());

        add(tabs, BorderLayout.CENTER);

        // stats label
        statsLabel = new JLabel("Statistics: No items yet.");
        add(statsLabel, BorderLayout.SOUTH);

        updateStats();
    }

    // menu bar w/ help
    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu help = new JMenu("Help");

        JMenuItem info = new JMenuItem("How to Use");
        info.addActionListener(e -> showHelp());
        help.add(info);

        bar.add(help);
        return bar;
    }

    // help popup using JTextArea so no weird \n strings
    private void showHelp() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setBackground(null);

        area.append("Help Menu:");
        area.append("\n");
        area.append("- Use the Add tab to add books or magazines.");
        area.append("\n");
        area.append("- The Search tab lets you look up items by title.");
        area.append("\n");
        area.append("- View All shows everything in a table and a tree.");
        area.append("\n");
        area.append("- File Operations is where you save or load your library.");

        JOptionPane.showMessageDialog(this, area, "Help", JOptionPane.INFORMATION_MESSAGE);
    }

    // Add Item panel
    private JPanel createAddItemPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 5, 5));

        panel.add(new JLabel("Title:"));
        titleField = new JTextField();
        panel.add(titleField);

        panel.add(new JLabel("Author:"));
        authorField = new JTextField();
        panel.add(authorField);

        panel.add(new JLabel("Year:"));
        yearField = new JTextField();
        panel.add(yearField);

        panel.add(new JLabel("Type:"));
        typeCombo = new JComboBox<>(new String[]{"Book", "Magazine"});
        panel.add(typeCombo);

        panel.add(new JLabel("Extra (Genre/Issue):"));
        extraField = new JTextField();
        panel.add(extraField);

        JButton addBtn = new JButton("Add Item");
        addBtn.addActionListener(e -> addItem());
        panel.add(addBtn);

        return panel;
    }

    // adding an item (try/catch for bad input)
    private void addItem() {
        try {
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            int year = Integer.parseInt(yearField.getText().trim());
            String extra = extraField.getText().trim();
            String type = (String) typeCombo.getSelectedItem();

            LibraryItem item;

            if (type.equals("Book")) {
                item = new Book(title, author, year, extra);
            } else {
                item = new Magazine(title, author, year, extra);
            }

            items.add(item);
            refreshTable();
            refreshTree();
            updateStats();

            titleField.setText("");
            authorField.setText("");
            yearField.setText("");
            extraField.setText("");

            JOptionPane.showMessageDialog(this, "Item added.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input.");
        }
    }

    // Search panel
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel top = new JPanel();
        top.add(new JLabel("Search Title:"));
        searchField = new JTextField(20);
        top.add(searchField);

        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> searchItems());
        top.add(searchBtn);

        panel.add(top, BorderLayout.NORTH);

        searchResultArea = new JTextArea();
        searchResultArea.setEditable(false);
        panel.add(new JScrollPane(searchResultArea), BorderLayout.CENTER);

        return panel;
    }

    // searching items (JTextArea handles new lines)
    private void searchItems() {
        String q = searchField.getText().trim().toLowerCase();
        searchResultArea.setText("");

        boolean found = false;

        for (LibraryItem item : items) {
            if (item.getTitle().toLowerCase().contains(q)) {
                searchResultArea.append(item.toString());
                searchResultArea.append("\n");
                found = true;
            }
        }

        if (!found) {
            searchResultArea.setText("No results.");
        }
    }

    // View All panel
    private JPanel createViewAllPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        tableModel = new DefaultTableModel(new String[]{"Type", "Title", "Author", "Year", "Extra"}, 0);
        table = new JTable(tableModel);

        JButton sortBtn = new JButton("Sort by Title");
        sortBtn.addActionListener(e -> sortItems());

        panel.add(sortBtn, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        tree = new JTree(new DefaultMutableTreeNode("Library Items"));
        panel.add(new JScrollPane(tree), BorderLayout.EAST);

        return panel;
    }

    // sorting items alphabetically
    private void sortItems() {
        Collections.sort(items, Comparator.comparing(LibraryItem::getTitle));
        refreshTable();
        refreshTree();
    }

    // File Operations panel
    private JPanel createFilePanel() {
        JPanel panel = new JPanel();

        JButton saveBtn = new JButton("Save");
        saveBtn.addActionListener(e -> saveToFile());

        JButton loadBtn = new JButton("Load");
        loadBtn.addActionListener(e -> loadFromFile());

        panel.add(saveBtn);
        panel.add(loadBtn);

        return panel;
    }

    // saving items to file
    private void saveToFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(chooser.getSelectedFile()))) {
                for (LibraryItem item : items) {
                    bw.write(item.toFileString());
                    bw.newLine();
                }
                JOptionPane.showMessageDialog(this, "Saved.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saving.");
            }
        }
    }

    // loading items from file
    private void loadFromFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (BufferedReader br = new BufferedReader(new FileReader(chooser.getSelectedFile()))) {
                items.clear();
                String line;

                while ((line = br.readLine()) != null) {
                    String[] p = line.split("\\|");

                    switch (p[0]) {
                        case "Book":
                            items.add(new Book(p[1], p[2], Integer.parseInt(p[3]), p[4]));
                            break;
                        case "Magazine":
                            items.add(new Magazine(p[1], p[2], Integer.parseInt(p[3]), p[4]));
                            break;
                        default:
                            throw new IOException("Unknown type in file");
                    }
                }

                refreshTable();
                refreshTree();
                updateStats();

                JOptionPane.showMessageDialog(this, "Loaded.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error loading.");
            }
        }
    }

    // refresh table
    private void refreshTable() {
        tableModel.setRowCount(0);

        for (LibraryItem item : items) {
            String type = (item instanceof Book) ? "Book" : "Magazine";
            String extra = (item instanceof Book)
                    ? ((Book) item).getGenre()
                    : ((Magazine) item).getIssue();

            tableModel.addRow(new Object[]{
                    type, item.getTitle(), item.getAuthor(), item.getYear(), extra
            });
        }
    }

    // refresh tree
    private void refreshTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Library Items");
        DefaultMutableTreeNode books = new DefaultMutableTreeNode("Books");
        DefaultMutableTreeNode mags = new DefaultMutableTreeNode("Magazines");

        for (LibraryItem item : items) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(item.getTitle());
            if (item instanceof Book) books.add(node);
            else mags.add(node);
        }

        root.add(books);
        root.add(mags);

        tree.setModel(new DefaultTreeModel(root));
    }

    // update stats label
    private void updateStats() {
        int b = 0, m = 0;
        for (LibraryItem item : items) {
            if (item instanceof Book) b++;
            else m++;
        }
        statsLabel.setText("Books: " + b + " | Magazines: " + m + " | Total: " + items.size());
    }

    public static void main(String[] args) {
        new LibraryManagementSystem().setVisible(true);
    }
}

// INTERFACE
interface Manageable {
    String getTitle();
    String getAuthor();
    int getYear();
}

// ABSTRACT CLASS
abstract class LibraryItem implements Manageable {
    private String title, author;
    private int year;

    public LibraryItem(String t, String a, int y) {
        title = t;
        author = a;
        year = y;
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getYear() { return year; }

    public abstract String getDetails();
    public abstract String toFileString();

    public String toString() { return getDetails(); }
}

// BOOK CLASS
class Book extends LibraryItem {
    private String genre;

    public Book(String t, String a, int y, String g) {
        super(t, a, y);
        genre = g;
    }

    public String getGenre() { return genre; }

    public String getDetails() {
        return "Book - " + getTitle() + " (" + genre + ")";
    }

    public String toFileString() {
        return "Book|" + getTitle() + "|" + getAuthor() + "|" + getYear() + "|" + genre;
    }
}

// MAGAZINE CLASS
class Magazine extends LibraryItem {
    private String issue;

    public Magazine(String t, String a, int y, String i) {
        super(t, a, y);
        issue = i;
    }

    public String getIssue() { return issue; }

    public String getDetails() {
        return "Magazine - " + getTitle() + " (Issue: " + issue + ")";
    }

    public String toFileString() {
        return "Magazine|" + getTitle() + "|" + getAuthor() + "|" + getYear() + "|" + issue;
    }
}

