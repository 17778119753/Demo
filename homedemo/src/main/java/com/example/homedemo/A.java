package com.example.homedemo;

public class A implements Runnable {

  public static int i = 0;

  public synchronized void increase() {
    i++;
  }

  public static void main(String[] args) {
    A a = new A();
    // 可以锁住，保证线程同步
    Thread thread1 = new Thread(a);
    Thread thread2 = new Thread(a);
    // 无法锁住，保证线程对象不是同一个了
    // Thread thread1 = new Thread(new A());
    // Thread thread2 = new Thread(new A());
    thread1.start();
    thread2.start();
  }

  @Override
  public void run() {
    for (int i1 = 0; i1 < 1000; i1++) {
      increase();
    }
  }
}
