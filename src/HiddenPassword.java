// Hidden Password
// Jerred Shepherd
// O(M + log(M)) complexity (I think)

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class HiddenPassword {
    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        String inputFile = "hiddenpwd.in.txt";
        String outputFile = "hiddenpwd.out.txt";

        File file = new File(inputFile);
        LinkedList<PasswordMessagePair> passwordMessagePairs = readPasswordMessagePairsFromFile(file);
        LinkedList<Boolean> results = new LinkedList<>();

        PasswordMessagePairValidator validator = new PasswordMessagePairValidatorImpl();
        passwordMessagePairs.forEach(pair -> {
            boolean result = validator.validate(pair);
            System.out.println(booleanToResultString(result));
            results.add(result);
        });

        printResultsToFile(results, outputFile);
    }

    private static LinkedList<PasswordMessagePair> readPasswordMessagePairsFromFile(File file) throws FileNotFoundException {
        LinkedList<PasswordMessagePair> passwordMessagePairs = new LinkedList<>();

        Scanner scanner = new Scanner(file);

        int numberOfPasswords = Integer.valueOf(scanner.nextLine());

        for (int i = 0; i < numberOfPasswords; i++) {
            String[] input = scanner.nextLine().split(" ");
            char[] password = input[0].toCharArray();
            char[] message = input[1].toCharArray();

            PasswordMessagePair pair = new PasswordMessagePair(password, message);
            passwordMessagePairs.add(pair);
        }

        return passwordMessagePairs;
    }

    private static void printResultsToFile(LinkedList<Boolean> results, String fileName) throws FileNotFoundException, UnsupportedEncodingException {
        try (PrintWriter writer = new PrintWriter(fileName, "UTF-8")){
            results.forEach(result -> writer.println(booleanToResultString(result)));
        }
    }

    private static String booleanToResultString(boolean b) {
        return b ? "PASS" : "FAIL";
    }

    private static class PasswordMessagePair {
        char[] password;
        char[] message;

        PasswordMessagePair(char[] password, char[] message) {
            this.password = password;
            this.message = message;
        }
    }

    private interface PasswordMessagePairValidator {
        boolean validate(PasswordMessagePair pair);
    }

    // Best case: O(M)
    // Worst case: O(M + log(M))
    private static class PasswordMessagePairValidatorImpl implements PasswordMessagePairValidator {
        public boolean validate(PasswordMessagePair pair) {
            char[] password = pair.password;
            char[] message = pair.message;
            int passwordIndex = 0;

            for (char messageChar : message) {
                if (password[passwordIndex] == messageChar) {
                    passwordIndex += 1;
                } else {
                    // If the current character we're looking at in the message is not one we're expected in the password,
                    // we should check if the character is expected later in the password
                    //
                    // This catches the following case
                    // Password: ABAC, message: AABAC
                    // The second A should not be there
                    for (int remainingPasswordIndexes = passwordIndex + 1; remainingPasswordIndexes < password.length; remainingPasswordIndexes++) {
                        if (messageChar == password[remainingPasswordIndexes]) {
                            return false;
                        }
                    }
                }
                if (passwordIndex == password.length) {
                    return true;
                }
            }

            return false;
        }
    }

    // This doesn't work like it should. It won't check if a password char comes between two expected chars
    // For example, with the password ABAC and the message AABAC, this algorithm will not recognize that the second A should not appear
    // O(P * 2 + M)
    private static class PasswordMessagePairValidatorWithMap implements PasswordMessagePairValidator {
        public boolean validate(PasswordMessagePair pair) {
            Map<Character, LinkedList<Integer>> characterIndexMap = new HashMap<>();
            char[] password = pair.password;
            char[] message = pair.message;

            // Build the characterIndexMap from the messages string
            for (int i = 0; i < message.length; i++) {
                char c = message[i];
                if (!characterIndexMap.containsKey(c)) {
                    LinkedList<Integer> list = new LinkedList<>();
                    list.add(i);
                    characterIndexMap.put(c, list);
                } else {
                    characterIndexMap.get(c).add(i);
                }
            }

            int lastIndexVisited = 0;
            for (char currentPasswordChar : password) {
                if (characterIndexMap.containsKey(currentPasswordChar)) {
                    List<Integer> characterIndexList = characterIndexMap.get(currentPasswordChar);
                    for (Integer index : characterIndexList) {
                        if (index > lastIndexVisited) {
                            System.out.println("Found " + currentPasswordChar + " at " + index + ", last " + lastIndexVisited);
                            lastIndexVisited = index;
                            break;
                        }
                    }
                } else {
                    return false;
                }
            }
            return true;
        }
    }
}
