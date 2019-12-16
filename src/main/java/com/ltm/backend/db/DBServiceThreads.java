package com.ltm.backend.db;

public class DBServiceThreads implements  Runnable {


    @Override
    public void run() {

        while (true){

            try {
                System.out.println("Thread ["+Thread.currentThread().getName()+"] " +DBService.getInstance().getNextKey("CARTONID", "%010d", null));
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }






    public static void main(String[] args){
        Thread th1 = new Thread(new DBServiceThreads());
        Thread th2 = new Thread(new DBServiceThreads());
        Thread th3 = new Thread(new DBServiceThreads());
        Thread th4 = new Thread(new DBServiceThreads());
        Thread th5 = new Thread(new DBServiceThreads());
        Thread th6 = new Thread(new DBServiceThreads());
        Thread th7 = new Thread(new DBServiceThreads());
        Thread th8 = new Thread(new DBServiceThreads());
        Thread th9 = new Thread(new DBServiceThreads());
        Thread th10 = new Thread(new DBServiceThreads());

        th1.start();
        th2.start();
        th3.start();
        th4.start();
        th5.start();
        th6.start();
        th7.start();
        th8.start();
        th9.start();
        th10.start();

    }
}
