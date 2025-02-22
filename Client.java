import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        String hostName = "localhost";
        int port = 12345;

        try (Socket socket = new Socket(hostName, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {
            // В блоке try создается новый Socket, который устанавливает соединение с сервером по указанному имени хоста и порту.
            // PrintWriter out: Создает поток для отправки данных на сервер.
            // BufferedReader in: Создает поток для чтения данных от сервера.
            // BufferedReader stdIn: Создает поток для чтения ввода пользователя из консоли.

            System.out.print("Введите ваше имя: ");
            String clientName = stdIn.readLine();
            out.println(clientName);

            // Чтение приветственного сообщения от сервера
            String serverResponse = in.readLine();
            System.out.println(serverResponse);

            // Чтение списка пользователей от сервера
            String userList = in.readLine();
            System.out.println(userList);

            // Проверка, существует ли введенное имя пользователя в списке
            if (!userList.contains(clientName)) {
                System.err.println("Имя пользователя не найдено. Программа завершена.");
                return; // Завершение программы
            }

            // Отправка сообщений на сервер
            while (true) {
                System.out.print("Введите имена получателей через запятую: ");
                String recipients = stdIn.readLine();
                System.out.print("Введите сообщение: ");
                String message = stdIn.readLine();
                out.println(recipients + ":" + message);
            } // Бесконечный цикл, который позволяет пользователю отправлять сообщения другим пользователям.
            // Запрашивает у пользователя ввод имен получателей и сообщения.
        } catch (UnknownHostException e) {
            System.err.println("Неизвестный хост: " + hostName);
        } catch (IOException e) {
            System.err.println("Ошибка ввода/вывода: " + e.getMessage());
        }
    }
}
