import logger.SecurityLogger;
import utils.Constants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main
 *
 * @author Jose L. Navío Mendoza
 */

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final int USER_NAME = 0;
    private static final int USER_PASSWORD = 1;
    private static final int USER_MAIL = 2;
    private static final int USER_FILE = 3;
    private static final int USER_EXTENSION = 4;

    public static void main(String[] args) {

        System.out.println("-----Seguridad-----");

        int choice = -1;
        do {
            showMenu();
            boolean validOption = false;
            while (!validOption) {
                try {
                    choice = scanner.nextInt();
                    validOption = true;
                } catch (InputMismatchException e) {
                    System.out.print("Entrada inválida. Por favor, introduce un número: ");
                    scanner.nextLine(); // Clear the input buffer
                }
            }
            switch (choice) {
                case 1:
                    signIn(new String[5]);
                    break;
                case 2:
                    logIn();
                    break;
                case 3:
                    System.out.println("Saliendo...");
                    break;
                default:
                    System.out.println("Elección inválida. Escoge de nuevo.");
            }
        } while (choice != 3);

        scanner.close();
    }

    //Generates a menu
    private static void showMenu() {
        System.out.println();
        System.out.println("1. Registrarse");
        System.out.println("2. Iniciar sesion");
        System.out.println("3. Salir");
        System.out.print("Selecciona una opción: ");
    }

    private static void signIn(String[] args) {

        String[] userInput = args;

        System.out.println();

        if (args[0] == null) {
            System.out.print("Nombre de usuario(8 caracteres y está compuesto únicamente por 6 letras minúsculas primero y 2 caracteres al final): ");
            userInput[USER_NAME] = scanner.next();
        }
        if (args[1] == null) {
            System.out.print("Contraseña(8 caracteres de longitud, como mínimo, y contener una mayúscula, una minúscula y un dígito.): ");
            userInput[USER_PASSWORD] = scanner.next();
        }
        if (args[2] == null) {
            System.out.print("E-Mail: ");
            userInput[USER_MAIL] = scanner.next();
        }
        if (args[3] == null) {
            System.out.print("Nombre del fichero de log(8 characteres máximo.): ");
            userInput[USER_FILE] = scanner.next();
        }
        if (args[4] == null) {
            System.out.print("Extension del fichero de log(3 caracteres.): ");
            userInput[USER_EXTENSION] = scanner.next();
        }
        if (validateInput(userInput)) {
            String fileName = userInput[USER_FILE] + "." + userInput[USER_EXTENSION];
            createLogFile(fileName);
            writeRegisterInLogFile(fileName, userInput[USER_NAME], userInput[USER_PASSWORD], userInput[USER_MAIL]);
            readLogFile(fileName);
        }
    }

    private static void logIn() {

        System.out.print("Nombre de usuario: ");
        String tempName = new Scanner(System.in).next();
        System.out.print("Contraseña: ");
        String tempPassword = new Scanner(System.in).next();
        System.out.println();
        compareLastWordsInDirectory(Constants.LOGS_DIRECTORY, tempName, tempPassword);
    }
    //Compares the given user and password with the ones stored in log files
    private static void compareLastWordsInDirectory(String directoryPath, String name, String password) {
        File directory = new File(directoryPath);
        File[] files = directory.listFiles();

        if(files.length == 0){
            System.out.println("No existen registros.");
        }else {
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        if(compareLastWordsInFile(file, name, password)){
                            System.out.print("Añadir entrada al registro: ");
                            String message = new Scanner(System.in).nextLine();
                            writeInLogFile(file.getName(), message);
                            readLogFile(file.getName());

                        }else {
                            System.out.println("Usuario o contraseña incorrectos.");
                        }
                    }
                }
            }
        }
    }
    private static boolean compareLastWordsInFile(File file, String name, String pass) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineCount = 0;
            String fileName = null;
            String filePassword = null;

            while ((line = reader.readLine()) != null) {
                lineCount++;
                if (lineCount == 2) {
                    fileName = getLastWord(line);
                } else if (lineCount == 3) {
                    filePassword = getLastWord(line);
                    break; // No need to continue reading the file
                }
            }

            boolean userRight = false;
            boolean passRight = false;

            // Compare the last words with the given parameters
            if (fileName != null && fileName.equals(name)) {
                userRight = true;
            }

            if (filePassword != null && filePassword.equals(pass)) {
               passRight = true;
            }
            return userRight && passRight;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    private static String getLastWord(String line) {
        String[] words = line.split(" ");
        if (words.length > 0) {
            return words[words.length - 1];
        }
        return null;
    }
    //If a value does not match, it makes it null
    private static boolean validateInput(String[] inputArray) {

        String regexName = "^[a-z]{6}[a-zA-Z0-9]{2}$";
        String regexPassword = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}$";
        String regexMail = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        String regexFile = "^.{1,8}$";
        String regexExtension = "^[a-zA-Z0-9]{3}$";

        boolean isValid = true;
        String[] validatedInput = inputArray;

        Pattern pattern = Pattern.compile(regexName);
        Matcher matcher = pattern.matcher(inputArray[USER_NAME]);

        if (!matcher.matches()) {
            validatedInput[USER_NAME] = null;
            isValid = false;
            System.err.println("Nombre de usuario incorrecto.");
            signIn(validatedInput);
        }

        pattern = Pattern.compile(regexPassword);
        matcher = pattern.matcher(inputArray[USER_PASSWORD]);

        if (!matcher.matches()) {
            validatedInput[USER_PASSWORD] = null;
            isValid = false;
            System.err.println("Contraseña no cumple los requisitos.");
            signIn(validatedInput);
        }

        pattern = Pattern.compile(regexMail);
        matcher = pattern.matcher(inputArray[USER_MAIL]);

        if (!matcher.matches()) {
            validatedInput[USER_MAIL] = null;
            isValid = false;
            System.err.println("E-Mail inválido.");
            signIn(validatedInput);
        }

        pattern = Pattern.compile(regexFile);
        matcher = pattern.matcher(inputArray[USER_FILE]);

        if (!matcher.matches()) {
            validatedInput[USER_FILE] = null;
            isValid = false;
            System.err.println("Nombre del documento demasiado largo.");
            signIn(validatedInput);
        }

        pattern = Pattern.compile(regexExtension);
        matcher = pattern.matcher(inputArray[USER_EXTENSION]);

        if (!matcher.matches()) {
            validatedInput[USER_EXTENSION] = null;
            isValid = false;
            System.err.println("La extension del fichero es demasiado larga.");
            signIn(validatedInput);
        }
        return isValid;
    }
    //Creates a log file the first time
    private static void createLogFile(String fileName) {

        try {
            Path logDirectoryPath = Paths.get(Constants.LOGS_DIRECTORY);
            //If file does not exist already, it creates it
            if (!Files.exists(logDirectoryPath)) {
                Files.createDirectories(logDirectoryPath);
            }

            String logFilePath = Constants.LOGS_DIRECTORY + File.separator + fileName;
            FileHandler fileHandler = new FileHandler(logFilePath, true);
            fileHandler.setFormatter(new SimpleFormatter());

            Logger logger = Logger.getLogger("");
            logger.addHandler(fileHandler);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void writeRegisterInLogFile(String fileName, String userName, String userPassword, String userMail) {
        SecurityLogger securityLogger = new SecurityLogger(fileName);
        securityLogger.info("User credentials: ");
        securityLogger.info("User name > " + userName);
        securityLogger.info("User password > " + userPassword);
        securityLogger.info("User mail > " + userMail);
        securityLogger.closeLogFile();
    }
    private static void writeInLogFile(String fileName, String message) {
        SecurityLogger securityLogger = new SecurityLogger(fileName);
        securityLogger.info("User entry > " + message);
        securityLogger.closeLogFile();
    }
    //Validates logger file
    private static void readLogFile(String fileName) {

        try {
            List<String> lines = Files.readAllLines(Paths.get(Constants.LOGS_DIRECTORY + File.separator + fileName));
            System.out.println();
            System.out.println("Registros de " + fileName + ":\n");
            for (String line : lines) {
                System.out.println(line);
            }
            System.out.println();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
