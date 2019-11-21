public class Test2 {
    public static void main(String[] args) {
        int trash = 20;
        for (int i = 0; i < 16; i++) {
            System.out.println(i);
        }
        int trash2 = 600;
    }

    private void trashMethod(int x) {
        int a = x + 8;
        subTask(x, a);
    }

    private static void subTask(int start, int end) {
        for (int i = start; i < end; i++) {
            System.out.println(i);
            long test = i;
            System.out.println(test);
        }
    }
}
