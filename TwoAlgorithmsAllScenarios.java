import java.io.PrintWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class TwoAlgorithmsAllScenarios {
    private static final int DEFAULT_SIZE = 100_000;
    private static final long SEED = 2026L;

    @FunctionalInterface
    private interface Sorter { void sort(int[] a); }

    private static final class Algorithm {
        final String name;
        final Sorter sorter;
        Algorithm(String name, Sorter sorter) { this.name = name; this.sorter = sorter; }
    }

    public static void main(String[] args) {
        int n = DEFAULT_SIZE;
        if (args.length > 0) {
            try {
                n = Integer.parseInt(args[0]);
                if (n < 1) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                System.err.println("Enter a positive array size, e.g. 10000.");
                return;
            }
        }

        Algorithm[] algorithms = {
            new Algorithm("Merge Sort", TwoAlgorithmsAllScenarios::mergeSort),
            new Algorithm("Quick Sort", TwoAlgorithmsAllScenarios::quickSort)
        };
        String[] names = {"Random", "Reverse Sorted", "Partially Sorted (80%)", "Fully Sorted"};
        int[][] originals = {
            randomArray(n, SEED), reverseArray(n),
            partiallySortedArray(n, SEED + 1), fullySortedArray(n)
        };
        long[][] times = new long[algorithms.length][names.length];

        System.out.println("TWO SORTING ALGORITHMS BENCHMARK");
        System.out.println("Array size N = " + n + "; times in microseconds (us).");
        for (int i = 0; i < algorithms.length; i++) {
            for (int j = 0; j < names.length; j++) {
                int[] test = Arrays.copyOf(originals[j], originals[j].length);
                long start = System.nanoTime();
                algorithms[i].sorter.sort(test);
                long end = System.nanoTime();
                times[i][j] = (end - start) / 1000L;
                if (!isSorted(test))
                    throw new IllegalStateException(algorithms[i].name + " failed: " + names[j]);
            }
        }

        System.out.printf("%-16s", "Algorithm");
        for (String name : names) System.out.printf(" | %23s", name);
        System.out.println();
        System.out.println("=".repeat(116));
        for (int i = 0; i < algorithms.length; i++) {
            System.out.printf("%-16s", algorithms[i].name);
            for (int j = 0; j < names.length; j++)
                System.out.printf(" | %20d us", times[i][j]);
            System.out.println();
        }
        saveCsv(algorithms, names, times);
        System.out.println("Results saved to benchmark_results.csv");
    }

    private static int[] randomArray(int n, long seed) {
        int[] a = new int[n]; Random r = new Random(seed);
        for (int i = 0; i < n; i++) a[i] = r.nextInt();
        return a;
    }
    private static int[] reverseArray(int n) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = n - i;
        return a;
    }
    private static int[] partiallySortedArray(int n, long seed) {
        int[] a = new int[n]; int count = (int)(n * 0.80);
        for (int i = 0; i < count; i++) a[i] = i;
        Random r = new Random(seed);
        for (int i = count; i < n; i++) a[i] = r.nextInt(Math.max(n, 1));
        return a;
    }
    private static int[] fullySortedArray(int n) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = i;
        return a;
    }
    private static boolean isSorted(int[] a) {
        for (int i = 1; i < a.length; i++) if (a[i-1] > a[i]) return false;
        return true;
    }
    private static void saveCsv(Algorithm[] algorithms, String[] names, long[][] times) {
        try (PrintWriter out = new PrintWriter("benchmark_results.csv", "UTF-8")) {
            out.print("Algorithm");
            for (String name : names) out.print("," + name);
            out.println();
            for (int i = 0; i < algorithms.length; i++) {
                out.print(algorithms[i].name);
                for (int j = 0; j < names.length; j++) out.print("," + times[i][j]);
                out.println();
            }
        } catch (IOException e) {
            System.err.println("Could not save CSV: " + e.getMessage());
        }
    }

    // Merge Sort: divide, sort the halves, then merge them.
    public static void mergeSort(int[] a) {
        if (a.length < 2) return;
        mergeSort(a, new int[a.length], 0, a.length - 1);
    }
    private static void mergeSort(int[] a, int[] temp, int left, int right) {
        if (left >= right) return;
        int mid = left + (right - left) / 2;
        mergeSort(a, temp, left, mid);
        mergeSort(a, temp, mid + 1, right);
        if (a[mid] <= a[mid + 1]) return;
        int i = left, j = mid + 1, k = left;
        while (i <= mid && j <= right) temp[k++] = (a[i] <= a[j]) ? a[i++] : a[j++];
        while (i <= mid) temp[k++] = a[i++];
        while (j <= right) temp[k++] = a[j++];
        System.arraycopy(temp, left, a, left, right - left + 1);
    }

    // Quick Sort: randomized pivot; expected O(N log N).
    public static void quickSort(int[] a) {
        quickSort(a, 0, a.length - 1, new Random(SEED + 2));
    }
    private static void quickSort(int[] a, int low, int high, Random random) {
        while (low < high) {
            int pivot = a[low + random.nextInt(high - low + 1)];
            int i = low, j = high;
            while (i <= j) {
                while (a[i] < pivot) i++;
                while (a[j] > pivot) j--;
                if (i <= j) {
                    int t = a[i]; a[i] = a[j]; a[j] = t; i++; j--;
                }
            }
            if (j - low < high - i) {
                if (low < j) quickSort(a, low, j, random);
                low = i;
            } else {
                if (i < high) quickSort(a, i, high, random);
                high = j;
            }
        }
    }
}
