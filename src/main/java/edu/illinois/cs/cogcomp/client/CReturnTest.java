package edu.illinois.cs.cogcomp.client;

/**
 * Created by haowu4 on 7/5/17.
 */
public class CReturnTest {
    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            System.out.print(String.format("Count: %d / 100", i));
            Thread.sleep(100);
            System.out.print("\r");
        }
    }
}
