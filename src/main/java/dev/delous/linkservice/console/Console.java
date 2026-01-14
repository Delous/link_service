package dev.delous.linkservice.console;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;
import java.util.UUID;
import java.util.regex.Pattern;

import dev.delous.linkservice.config.StorageConfig;
import dev.delous.linkservice.data.ShortLinkRecord;
import dev.delous.linkservice.data.Storage;
import dev.delous.linkservice.linking.Link;

public class Console {

    public static void startSession() {
        UUID userUuid = null;
        Storage<String, ShortLinkRecord> storage = new Storage<>(StorageConfig.storageFile);
        storage.load();
        Scanner scanner = new Scanner(System.in);
        sayHelloUser();

        while (true) {
            String userRequest = scanner.nextLine().replaceAll("\\s+", " ").trim();

            if (userRequest.equals("exit") || userRequest.equals("quit")) {
                storage.save();
                sayByeUser();
                break;
            }

            if (userRequest.equals("help")) {
                helpUser();
            }

            else if (userRequest.startsWith("login")) {
                userUuid = login(userRequest);
            }

            else if (userRequest.equals("logout")) {
                userUuid = logout(userUuid);
            }

            else if (userRequest.startsWith("http")) {
                if (userUuid == null) userUuid = registerUser();
                longLinkToShort(userRequest, userUuid, storage);
            }

            else if (userRequest.startsWith("clck.ru/")) {
                shortLinkToLong(userRequest, userUuid, storage);
            }

            else {
                System.out.println("Такая команда не найдена, напишите help для получения информации о доступных командах.");
            }
        }

    }

    private static void sayHelloUser() {
        System.out.println("Привет! Это программа коротких ссылок. Напиши help, если хочешь узнать о доступных командах.");
    }

    private static void sayByeUser() {
        System.out.println("Программа завершена.");
    }

    private static void helpUser() {
        System.out.println("Доступные команды:");
        System.out.println("help - перечисление доступных команд,");
        System.out.println("exit, quit - выход из программы,");
        System.out.println("login <uuid> - авторизация по введенному uuid (вводится без кавычек),");
        System.out.println("logout - выход из используемого логина,");
        System.out.println("http...  - при введении ссылки, сократит и отдаст короткую версию,");
        System.out.println("clck.ru/...  - при введении короткой ссылки, откроет её на компьютере.");
    }

    private static UUID registerUser() {
        System.out.println("Вы не были зарегистрированы, поэтому для вас создан новый UUID:");
        UUID uuid = UUID.randomUUID();
        System.out.println(uuid);
        System.out.println("Не забудьте сохранить свой UUID для будущей авторизации.");
        return uuid;
    }

    private static UUID login(String userRequest) {
        String[] words = userRequest.split(" ");
        if (words.length == 2) {
            if (Pattern.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
                    words[1])) {
                System.out.println("Вы успешно авторизованы.");
                return UUID.fromString(words[1]);
            }
            else System.out.println("Вы ввели некорректный UUID.");
        }
        else System.out.println("Команда login введена некорректно.");
        return null;
    }

    private static UUID logout(UUID userUuid) {
        if (userUuid == null) System.out.println("Вы и так не авторизованы.");
        else System.out.println("Выход успешно осуществлен.");
        return null;
    }

    private static void longLinkToShort(String userRequest, UUID userUuid, Storage<String, ShortLinkRecord> storage) {
        if (Pattern.matches("https?://(www.)?\\w+(\\.\\w+)+.*", userRequest)) {
            String link = Link.getShort(userRequest, userUuid);
            ShortLinkRecord record = new ShortLinkRecord(
                    userUuid,
                    userRequest,
                    System.currentTimeMillis() + StorageConfig.expirationInterval,
                    StorageConfig.clickLimit);
            storage.put(link, record);
            System.out.println("Ваша ссылка успешно сокращена и сохранена:");
            System.out.printf("clck.ru/%s\n", link);
        }
        else System.out.println("Запрос на сокращение ссылки отклонен: ссылка невалидна");

    }

    private static void shortLinkToLong(String userRequest, UUID userUuid, Storage<String, ShortLinkRecord> storage) {
        if (userUuid == null) {
            System.out.println("Вы не авторизованы. Чтобы узнать, как авторизоваться, напишите help.");
        }
        else if (Pattern.matches("clck\\.ru/[a-zA-Z0-9]{8}", userRequest)) {
            String code = userRequest.substring(8);
            storage.get(code).ifPresent(record -> {
                if (record.isClickLimitReached()) {
                    storage.remove(code);
                    System.out.println("Эта ссылка больше недоступна из-за исчерпания лимита.");
                }
                else if (record.isExpired(System.currentTimeMillis())) {
                    storage.remove(code);
                    System.out.println("Эта ссылка больше недоступна из-за истечения времени.");
                } else {
                    String originalUrl = record.getOriginalUrl(userUuid);
                    if (originalUrl != null) {
                        record.addClick();
                        try {
                            Desktop.getDesktop().browse(new URI(originalUrl)); // Добавить ссылку
                        } catch (IOException | URISyntaxException e) {
                            throw new RuntimeException(e);
                        }
                        System.out.println("Ваша ссылка успешно открыта.");
                    }
                    else System.out.println("Это чужая короткая ссылка.");
                }
            });
        }
        else System.out.println("Вы ошиблись в вводе короткой ссылки - она отличается от тех, что возвращает сервис.");
    }
}