import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Main {
    static BlockingQueue<String> queue_A = new ArrayBlockingQueue<>(100, true);
    static BlockingQueue<String> queue_B = new ArrayBlockingQueue<>(100, true);
    static BlockingQueue<String> queue_C = new ArrayBlockingQueue<>(100, true);
    static int textLength = 100_000;
    static int stringQuantity = 10_000;

    public static void main(String[] args) throws InterruptedException {
        String template = "abc";
        ThreadGroup threadGroup = new ThreadGroup("calcGroup");
        // Поток заполнения очередей строк
        Thread genString = new Thread(() -> {
            for (int i = 0; i < stringQuantity; i++) {
                String str = generateText(template, textLength);
                try {
                    queue_A.put(str);
                    queue_B.put(str);
                    queue_C.put(str);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        genString.start();
        // Поток вычисления максимального количетва повторений символа A
        final Thread calc_A = new Thread(threadGroup, () -> calculate("a", queue_A));
        calc_A.start();
        // Поток вычисления максимального количетва повторений символа B
        final Thread calc_B = new Thread(threadGroup, () -> calculate("b", queue_B));
        calc_B.start();
        // Поток вычисления максимального количетва повторений символа C
        final Thread calc_C = new Thread(threadGroup, () -> calculate("c", queue_C));
        calc_C.start();
        // Ожидание завершения потоков
        genString.join();
        calc_A.join();
        calc_B.join();
        calc_C.join();
        // Преррывание потоков
        genString.interrupt();
        threadGroup.interrupt();
    }

    // Метод генерации строк
    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }

    // Метод вычисления максимума повторений искомого символа
    public static void calculate(String findChar, BlockingQueue<String> blockingQueue) {
        int countMax = 0;
        for (int i = 0; i < stringQuantity; i++) {
            try {
                String currentString = blockingQueue.take();
                int count = currentString.length() - currentString.replace(findChar, "").length();
                countMax = Math.max(count, countMax);
//                System.out.println(findChar + " " + count + " - Max = " + countMax + " - " + currentString.substring(1, 100));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("Максимальное количество символов " + findChar.toUpperCase() + ": " + countMax);
    }
}