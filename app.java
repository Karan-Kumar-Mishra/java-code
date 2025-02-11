import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

class DatabaseCredentialsForm extends JPanel {
    private JTextField txtDbUrl, txtDbUsername, txtDbPassword;

    public DatabaseCredentialsForm() {
        setLayout(new GridLayout(3, 2));

        JLabel lblDbUrl = new JLabel("DB URL");
        txtDbUrl = new JTextField("jdbc:mysql://localhost:3306/crud_db");
        add(lblDbUrl);
        add(txtDbUrl);

        JLabel lblDbUsername = new JLabel("DB Username");
        txtDbUsername = new JTextField("root");
        add(lblDbUsername);
        add(txtDbUsername);

        JLabel lblDbPassword = new JLabel("DB Password");
        txtDbPassword = new JTextField("1234");
        add(lblDbPassword);
        add(txtDbPassword);
    }

    public String getDbUrl() {
        return txtDbUrl.getText();
    }

    public String getDbUsername() {
        return txtDbUsername.getText();
    }

    public String getDbPassword() {
        return txtDbPassword.getText();
    }
}

class UserForm extends JPanel {
    private JTextField txtName, txtEmail, txtAge;
    private JButton btnCreate, btnUpdate, btnDelete;
    private JTable table;
    private DefaultTableModel model;
    private DatabaseCredentialsForm credentialsForm;

    public UserForm(DatabaseCredentialsForm credentialsForm) {
        this.credentialsForm = credentialsForm;

        setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        add(panel, BorderLayout.NORTH);
        panel.setLayout(new GridLayout(4, 4));

        JLabel lblName = new JLabel("Name");
        txtName = new JTextField();
        panel.add(lblName);
        panel.add(txtName);

        JLabel lblEmail = new JLabel("Email");
        txtEmail = new JTextField();
        panel.add(lblEmail);
        panel.add(txtEmail);

        JLabel lblAge = new JLabel("Age");
        txtAge = new JTextField();
        panel.add(lblAge);
        panel.add(txtAge);

        btnCreate = new JButton("Create");
        panel.add(btnCreate);
        btnCreate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleCreate();
            }
        });

        btnUpdate = new JButton("Update");
        panel.add(btnUpdate);
        btnUpdate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleUpdate();
            }
        });

        btnDelete = new JButton("Delete");
        panel.add(btnDelete);
        btnDelete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleDelete();
            }
        });

        model = new DefaultTableModel();
        model.addColumn("ID");
        model.addColumn("Name");
        model.addColumn("Email");
        model.addColumn("Age");

        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        refreshTable();
    }

    private void handleCreate() {
        String name = txtName.getText();
        String email = txtEmail.getText();
        int age = Integer.parseInt(txtAge.getText());
        String url = credentialsForm.getDbUrl();
        String user = credentialsForm.getDbUsername();
        String password = credentialsForm.getDbPassword();
        crudOperation.insertUser(name, email, age, url, user, password);
        refreshTable();
    }

    private void handleUpdate() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int id = (int) model.getValueAt(selectedRow, 0);
            String name = txtName.getText();
            String email = txtEmail.getText();
            int age = Integer.parseInt(txtAge.getText());
            String url = credentialsForm.getDbUrl();
            String user = credentialsForm.getDbUsername();
            String password = credentialsForm.getDbPassword();
            crudOperation.updateUser(id, name, email, age, url, user, password);
            refreshTable();
        }
    }

    private void handleDelete() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int id = (int) model.getValueAt(selectedRow, 0);
            String url = credentialsForm.getDbUrl();
            String user = credentialsForm.getDbUsername();
            String password = credentialsForm.getDbPassword();
            crudOperation.deleteUser(id, url, user, password);
            refreshTable();
        }
    }

    private void refreshTable() {
        model.setRowCount(0);

        String url = credentialsForm.getDbUrl();
        String user = credentialsForm.getDbUsername();
        String password = credentialsForm.getDbPassword();

        String query = "SELECT * FROM users";
        try (Connection connection = DatabaseConnection.getConnection(url, user, password);
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String email = rs.getString("email");
                int age = rs.getInt("age");
                model.addRow(new Object[] { id, name, email, age });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

class crudOperation {
    public static void insertUser(String name, String email, int age, String url, String user, String password) {
        String query = "INSERT INTO users (name, email, age) VALUES (?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, name);
            statement.setString(2, email);
            statement.setInt(3, age);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ResultSet getUsers(String url, String user, String password) {
        String query = "SELECT * FROM users";
        try (Connection connection = DatabaseConnection.getConnection(url, user, password);
             Statement statement = connection.createStatement()) {

            return statement.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void updateUser(int id, String name, String email, int age, String url, String user, String password) {
        String query = "UPDATE users SET name = ?, email = ?, age = ? WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, name);
            statement.setString(2, email);
            statement.setInt(3, age);
            statement.setInt(4, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteUser(int id, String url, String user, String password) {
        String query = "DELETE FROM users WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

class DatabaseConnection {
    public static Connection getConnection(String url, String user, String password) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Load MySQL JDBC driver
            return DriverManager.getConnection(url, user, password); // Establish the connection
        } catch (ClassNotFoundException e) {
            // Handle the exception for when the class is not found
            e.printStackTrace();
        } catch (SQLException e) {
            // Handle SQL exceptions
            e.printStackTrace();
        }
        return null;
    }
}

public class app {
    private JFrame frame;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                app window = new app();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public app() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame("CRUD Operations");
        frame.setBounds(100, 100, 800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());

        DatabaseCredentialsForm credentialsForm = new DatabaseCredentialsForm();
        frame.getContentPane().add(credentialsForm, BorderLayout.NORTH);

        UserForm userForm = new UserForm(credentialsForm);
        frame.getContentPane().add(userForm, BorderLayout.CENTER);
    }
}
