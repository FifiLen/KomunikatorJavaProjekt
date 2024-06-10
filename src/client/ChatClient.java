package client;

import java.io.*;
import java.net.*;
import java.util.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ChatClient extends Application {
    private static final String SERVER_ADDRESS = "34.0.250.190";
    private static final int SERVER_PORT = 12346;
    private BufferedReader in;
    private PrintWriter out;
    private BufferedReader messageIn;
    private PrintWriter messageOut;
    private Stage primaryStage;
    private TextField textField = new TextField();
    private Button sendButton = new Button("Send");
    private Button addFriendButton = new Button("Add Friend");
    private Button removeFriendButton = new Button("Remove Friend");
    private VBox messageBox = new VBox();
    private String userName;
    private ListView<Friend> friendsListView = new ListView<>();
    private Socket messageSocket;
    private Label chatWithLabel = new Label();
    private ScrollPane scrollPane = new ScrollPane();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        showLoginDialog();
    }

    private void showLoginDialog() {
        Stage loginStage = new Stage();
        VBox loginPanel = new VBox(10);
        loginPanel.setPadding(new Insets(20));
        loginPanel.setAlignment(Pos.CENTER);
        loginPanel.setStyle("-fx-background-color: #2c2c2c; -fx-border-color: #3a3a3a; -fx-border-radius: 10; -fx-background-radius: 10;");

        ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/logoApp.png")));
        logo.setFitWidth(80);
        logo.setPreserveRatio(true);

        Label usernameLabel = new Label("Username:");
        usernameLabel.setTextFill(Color.WHITE);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setStyle("-fx-padding: 10; -fx-background-radius: 10; -fx-background-color: #3a3a3a; -fx-text-fill: white;");

        Label passwordLabel = new Label("Password:");
        passwordLabel.setTextFill(Color.WHITE);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setStyle("-fx-padding: 10; -fx-background-radius: 10; -fx-background-color: #3a3a3a; -fx-text-fill: white;");

        ImageView loginIcon = new ImageView(new Image(getClass().getResourceAsStream("/loginIcon.png")));
        loginIcon.setFitWidth(16);
        loginIcon.setFitHeight(16);

        ImageView registerIcon = new ImageView(new Image(getClass().getResourceAsStream("/registerIcon.png")));
        registerIcon.setFitWidth(16);
        registerIcon.setFitHeight(16);

        Button loginButton = new Button("Login", loginIcon);
        loginButton.setTextFill(Color.BLACK);
        loginButton.setStyle("-fx-background-color: #87CEFA; -fx-background-radius: 10; -fx-padding: 10;");

        Button registerButton = new Button("Register", registerIcon);
        registerButton.setTextFill(Color.BLACK);
        registerButton.setStyle("-fx-background-color: #FF7F7F; -fx-background-radius: 10; -fx-padding: 10;");

        HBox buttonBox = new HBox(10, loginButton, registerButton);
        buttonBox.setAlignment(Pos.CENTER);

        loginPanel.getChildren().addAll(logo, usernameLabel, usernameField, passwordLabel, passwordField, buttonBox);

        loginButton.setOnAction(e -> {
            userName = usernameField.getText();
            String password = passwordField.getText();
            if (authenticate("login", userName, password)) {
                loginStage.close();
                showChatWindow();
                initializeMessageSocket();
            } else {
                showAlert("Login failed");
            }
        });

        registerButton.setOnAction(e -> {
            userName = usernameField.getText();
            String password = passwordField.getText();
            if (authenticate("register", userName, password)) {
                loginStage.close();
                showChatWindow();
                initializeMessageSocket();
            } else {
                showAlert("Registration failed");
            }
        });

        Scene scene = new Scene(loginPanel, 300, 400);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        loginStage.setScene(scene);
        loginStage.setTitle("Login");
        loginStage.show();
    }

    private void showChatWindow() {
        BorderPane root = new BorderPane();
        primaryStage.setTitle("Chat Client");

        VBox topContainer = new VBox();

        Label headerLabel = new Label("Komunikator Szymonka");
        headerLabel.setStyle("-fx-background-color: #1c1c1c; -fx-text-fill: white; -fx-padding: 10px;");
        topContainer.getChildren().add(headerLabel);

        ImageView addFriendIcon = new ImageView(new Image(getClass().getResourceAsStream("/addFriendIcon.png")));
        addFriendIcon.setFitWidth(16);
        addFriendIcon.setFitHeight(16);

        ImageView removeFriendIcon = new ImageView(new Image(getClass().getResourceAsStream("/removeFriendIcon.png")));
        removeFriendIcon.setFitWidth(16);
        removeFriendIcon.setFitHeight(16);

        Label friendsLabel = new Label("Friends:");
        friendsLabel.setTextFill(Color.WHITE);

        addFriendButton = new Button("Add Friend", addFriendIcon);
        addFriendButton.setTextFill(Color.BLACK);
        addFriendButton.setStyle("-fx-background-color: #87CEFA; -fx-background-radius: 10; -fx-padding: 10;");

        removeFriendButton = new Button("Remove Friend", removeFriendIcon);
        removeFriendButton.setTextFill(Color.BLACK);
        removeFriendButton.setStyle("-fx-background-color: #FF7F7F; -fx-background-radius: 10; -fx-padding: 10;");

        VBox buttonsBox = new VBox(10, addFriendButton, removeFriendButton);
        buttonsBox.setAlignment(Pos.CENTER_LEFT);

        friendsListView.setStyle("-fx-background-color: #1c1c1c; -fx-control-inner-background: #1c1c1c; -fx-text-fill: white; -fx-background-radius: 0;");
        friendsListView.setCellFactory(lv -> new FriendCell());

        VBox friendsPanel = new VBox(10);
        friendsPanel.setPadding(new Insets(10));
        friendsPanel.setAlignment(Pos.TOP_LEFT);
        friendsPanel.setStyle("-fx-background-color: #1c1c1c; -fx-background-radius: 0;");
        friendsPanel.setId("friendsPanel");
        friendsPanel.getChildren().addAll(friendsLabel, friendsListView, buttonsBox);

        BorderPane leftContainer = new BorderPane();
        leftContainer.setTop(friendsPanel);
        leftContainer.setPrefWidth(250);

        root.setLeft(leftContainer);

        chatWithLabel.setTextFill(Color.WHITE);
        chatWithLabel.setStyle("-fx-background-color: #1c1c1c; -fx-padding: 10px;");
        BorderPane topChatContainer = new BorderPane();
        topChatContainer.setTop(chatWithLabel);

        scrollPane.setContent(messageBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #1c1c1c; -fx-border-color: #3a3a3a; -fx-background-radius: 0;");
        VBox chatContainer = new VBox(topChatContainer, scrollPane);
        chatContainer.setStyle("-fx-background-color: #1c1c1c;");
        chatContainer.setId("chatContainer");
        root.setCenter(chatContainer);

        ImageView sendIcon = new ImageView(new Image(getClass().getResourceAsStream("/sendIcon.png")));
        sendIcon.setFitWidth(16);
        sendIcon.setFitHeight(16);

        sendButton = new Button("Send", sendIcon);
        sendButton.setTextFill(Color.BLACK);
        sendButton.setStyle("-fx-background-color: #bfffbd; -fx-background-radius: 10; -fx-padding: 10;");

        HBox inputPanel = new HBox(10);
        inputPanel.setPadding(new Insets(10));
        textField.setPrefWidth(400);
        textField.setStyle("-fx-padding: 10; -fx-background-radius: 10; -fx-background-color: #3a3a3a; -fx-text-fill: white;");
        inputPanel.getChildren().addAll(textField, sendButton);
        inputPanel.setAlignment(Pos.CENTER_RIGHT);

        BorderPane bottomContainer = new BorderPane();
        bottomContainer.setRight(inputPanel);
        bottomContainer.setStyle("-fx-background-color: #2c2c2c;");
        root.setBottom(bottomContainer);

        sendButton.setOnAction(e -> sendMessage());
        textField.setOnAction(e -> sendMessage());

        addFriendButton.setOnAction(e -> {
            String friendUsername = showInputDialog("Enter friend's username:");
            if (friendUsername != null && !friendUsername.isEmpty()) {
                addFriend(userName, friendUsername);
            }
        });

        removeFriendButton.setOnAction(e -> {
            Friend friend = friendsListView.getSelectionModel().getSelectedItem();
            if (friend != null) {
                removeFriend(userName, friend.getUserName());
            }
        });

        friendsListView.setOnMouseClicked(e -> {
            Friend friend = friendsListView.getSelectionModel().getSelectedItem();
            if (friend != null) {
                loadChatHistory(userName, friend.getUserName());
                chatWithLabel.setText("Chat with " + friend.getUserName());
            }
        });

        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void sendMessage() {
        Friend friend = friendsListView.getSelectionModel().getSelectedItem();
        if (friend != null && textField.getText() != null && !textField.getText().trim().isEmpty()) {
            String message = textField.getText();
            messageOut.println("send_message");
            messageOut.println(userName);
            messageOut.println(friend.getUserName());
            messageOut.println(message);
            messageOut.flush();

            addMessageToPanel(userName + ": " + message, true);
            updateLastMessage(friend, message);

            textField.setText("");
            scrollToBottomWithDelay();
        }
    }

    private void updateLastMessage(Friend friend, String message) {
        if (message.length() > 10) {
            message = message.substring(0, 10) + "...";
        }
        friend.setLastMessage(message);
        friendsListView.refresh();
    }

    private void scrollToBottom() {
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }

    private void scrollToBottomWithDelay() {
        Platform.runLater(() -> {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            scrollToBottom();
        });
    }

    private boolean authenticate(String type, String username, String password) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)) {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            out.println(type);
            out.println(username);
            out.println(password);
            String response = in.readLine();
            if ("login_success".equals(response) || "register_success".equals(response)) {
                if ("login_success".equals(response)) {
                    loadFriendsList(in, out);
                }
                return true;
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void addFriend(String username, String friendUsername) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)) {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            out.println("add_friend");
            out.println(username);
            out.println(friendUsername);

            String response = in.readLine();
            if ("add_friend_success".equals(response)) {
                showAlert("Friend added successfully");
                friendsListView.getItems().add(new Friend(friendUsername, "Last message..."));
            } else {
                showAlert("Failed to add friend");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void removeFriend(String username, String friendUsername) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)) {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            out.println("remove_friend");
            out.println(username);
            out.println(friendUsername);

            String response = in.readLine();
            if ("remove_friend_success".equals(response)) {
                showAlert("Friend removed successfully");
                friendsListView.getItems().removeIf(friend -> friend.getUserName().equals(friendUsername));
            } else {
                showAlert("Failed to remove friend");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFriendsList(BufferedReader in, PrintWriter out) {
        try {
            out.println("get_friends");
            out.println(userName);

            String response;
            while (!(response = in.readLine()).equals("friend_list_end")) {
                if (!response.equals("friend_list_start")) {
                    friendsListView.getItems().add(new Friend(response, "Last message..."));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadChatHistory(String user1, String user2) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)) {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            out.println("get_chat_history");
            out.println(user1);
            out.println(user2);

            messageBox.getChildren().clear();

            String response;
            while (!(response = in.readLine()).equals("chat_history_end")) {
                if (!response.equals("chat_history_start")) {
                    addMessageToPanel(response, response.startsWith(user1));
                }
            }

            scrollToBottomWithDelay();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeMessageSocket() {
        try {
            messageSocket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            messageIn = new BufferedReader(new InputStreamReader(messageSocket.getInputStream()));
            messageOut = new PrintWriter(messageSocket.getOutputStream(), true);
            new Thread(this::run).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void run() {
        try {
            String message;
            while ((message = messageIn.readLine()) != null) {
                addMessageToPanel(message, message.startsWith(userName));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addMessageToPanel(String message, boolean isSentByUser) {
        Platform.runLater(() -> {
            String[] parts = message.split(": ", 2);
            if (parts.length < 2) {
                return;
            }
            String sender = parts[0];
            String text = parts[1];

            Label messageLabel = new Label(sender + ": " + text);
            messageLabel.setPadding(new Insets(10));
            messageLabel.setWrapText(true);
            messageLabel.setMaxWidth(400);
            messageLabel.setTextFill(Color.BLACK);

            HBox messageBubble = new HBox();
            messageBubble.setPadding(new Insets(5));
            messageBubble.getChildren().add(messageLabel);

            if (isSentByUser) {
                messageLabel.setStyle("-fx-background-color: #004593; -fx-background-radius: 10; -fx-padding: 10;");
                messageBubble.setAlignment(Pos.CENTER_RIGHT);
            } else {
                messageLabel.setStyle("-fx-background-color: #4C4C4C; -fx-background-radius: 10; -fx-padding: 10;");
                messageBubble.setAlignment(Pos.CENTER_LEFT);
            }

            messageBox.getChildren().add(messageBubble);
            scrollToBottomWithDelay();
        });
    }

    private void showAlert(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private String showInputDialog(String message) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Input");
        dialog.setHeaderText(null);
        dialog.setContentText(message);
        return dialog.showAndWait().orElse(null);
    }

    public class Friend {
        private final String userName;
        private String lastMessage;
        private final ImageView avatar;

        public Friend(String userName, String lastMessage) {
            this.userName = userName;
            this.lastMessage = lastMessage;
            this.avatar = new ImageView(new Image(getClass().getResourceAsStream("/avatar.png")));
            this.avatar.setFitWidth(40);
            this.avatar.setFitHeight(40);
            this.avatar.setStyle("-fx-background-radius: 20px;");
        }

        public String getUserName() {
            return userName;
        }

        public String getLastMessage() {
            return lastMessage;
        }

        public void setLastMessage(String lastMessage) {
            this.lastMessage = lastMessage;
        }

        public ImageView getAvatar() {
            return avatar;
        }
    }

    public class FriendCell extends ListCell<Friend> {
        @Override
        protected void updateItem(Friend friend, boolean empty) {
            super.updateItem(friend, empty);
            if (empty || friend == null) {
                setGraphic(null);
                setText(null);
            } else {
                HBox hBox = new HBox(10);
                hBox.setPadding(new Insets(5));
                hBox.setAlignment(Pos.CENTER_LEFT);

                VBox vBox = new VBox(5);
                Label username = new Label(friend.getUserName());
                username.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: 700;");
                Label lastMessage = new Label(friend.getLastMessage());
                lastMessage.setStyle("-fx-text-fill: #b0b0b0; -fx-font-size: 12px;");

                vBox.getChildren().addAll(username, lastMessage);
                hBox.getChildren().addAll(friend.getAvatar(), vBox);
                hBox.setStyle("-fx-background-color: #2c2c2c; -fx-border-color: #3a3a3a; -fx-border-width: 1; -fx-background-radius: 0; -fx-border-radius: 0;");

                setGraphic(hBox);
            }
        }
    }
}
