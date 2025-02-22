import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 12345;
    private static Map<String, PrintWriter> clients = new HashMap<>(); //
    private static List<String> userList = new ArrayList<>(); // PORT: Константа, определяющая номер порта, на котором сервер будет слушать входящие подключения.clients: Хранит подключенных клиентов,
    // где ключ — это имя клиента, а значение — объект PrintWriter, используемый для отправки сообщений этому клиенту.userList:
    // Хранит список всех пользователей, загружаемый из файла.

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Сервер запущен на порту " + PORT);

            // Загружаем список пользователей из файла
            loadUserList("clients.txt");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();// Бесконечный цикл, который принимает входящие соединения.
                // Для каждого нового подключения создается новый поток ClientHandler, который будет обрабатывать взаимодействие с этим клиентом.
            }
        } catch (IOException e) {
            System.err.println("Ошибка сервера: " + e.getMessage());
        }
    }

    // Метод для загрузки списка пользователей из файла
    private static void loadUserList(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                userList.add(line.trim()); // Убираем лишние пробелы
            }
        }
    }

    // Метод для отправки списка пользователей клиенту
    public static String getUserList() {
        return String.join(",", userList);
    }

    // Метод для отправки сообщения конкретному клиенту
    public static void sendMessageToClient(String recipient, String message) {
        if (clients.containsKey(recipient)) {
            // Если клиент подключен, отправляем сообщение сразу
            clients.get(recipient).println("Сообщение от сервера: " + message);
            System.out.println("Сообщение отправлено клиенту " + recipient + ": " + message);
        } else {
            // Если клиент не подключен, выводим сообщение в консоль сервера
            System.out.println("Клиент " + recipient + " не подключен. Сообщение: " + message);
        }
    }

    private static class ClientHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private String clientName;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }
        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                // Получаем имя клиента
                clientName = in.readLine();
                if (!userList.contains(clientName)) {
                    out.println("Ошибка: Имя пользователя не найдено.");
                } else {// Регистрируем клиента
                    clients.put(clientName, out);// Приветствуем клиента
                    out.println("Добро пожаловать, " + clientName);// Отправляем список пользователей клиенту
                    out.println("Список пользователей: " + getUserList());// Читаем сообщения от клиента
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        // Формат сообщения: "получатель1,получатель2,...:сообщение"
                        String[] parts = inputLine.split(":", 2);
                        if (parts.length == 2) {
                            String[] recipients = parts[0].split(",");
                            String message = parts[1];

                            // Отправляем сообщение каждому получателю
                            for (String recipient : recipients) {
                                recipient = recipient.trim();
                                if (userList.contains(recipient)) {
                                    sendMessageToClient(recipient, message);
                                } else {
                                    out.println("Ошибка: Пользователь " + recipient + " не найден.");
                                }
                            }
                        } else {
                            out.println("Ошибка: Неверный формат сообщения. Используйте 'получатель1,получатель2,...:сообщение'.");
                        }
                    }
                }

            } catch (IOException e) {
                System.err.println("Ошибка обработки клиента: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Ошибка закрытия сокета: " + e.getMessage());
                }
                clients.remove(clientName);
                System.out.println("Клиент " + clientName + " отключен.");
            }
        }
    }
}
