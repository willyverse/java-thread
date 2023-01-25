import java.util.ArrayList;

public class ThreadEx4 {
    public static void main(String[] args) throws Exception {
        Table table = new Table(); // 여러 쓰레드가 공유하는 객체

        new Thread(new Cook(table), "COOK").start();
        new Thread(new Customer(table, "donut"), "CUST1").start();
        new Thread(new Customer(table, "burger"), "CUST2").start();

        Thread.sleep(5000);
        System.exit(0);
    }
}

class Customer implements Runnable {
    private Table table;
    private String food;

    Customer(Table table, String food) {
        this.table = table;
        this.food = food;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
            String name = Thread.currentThread().getName();

            table.remove(food);
            System.out.println(name + " ate a " + food);
        }
    }
}

class Cook implements Runnable {
    private Table table;

    Cook(Table table) {
        this.table = table;
    }

    @Override
    public void run() {
        while(true) {
            int idx = (int)(Math.random()*table.dishNum());
            table.add(table.dishNames[idx]);
            try { Thread.sleep(10);} catch (InterruptedException e) {}
        } // while end
    }
}

class Table {
    String[] dishNames = { "donut", "donut", "burger", "burger" }; // donut의 확률을 높임
    final int MAX_FOOD = 6;
    private ArrayList<String> dishes = new ArrayList<>();

    public synchronized void add(String dish) {
        String name = Thread.currentThread().getName();
        if(dishes.size() >= MAX_FOOD) {
            System.out.println(name + " is waiting."); // COOK 스레드를 기다림
            try {
                wait();
                Thread.sleep(50);
            } catch (InterruptedException e) {}
        }
        System.out.println("--------------- " + name + " cooked " + dish + ".");
        dishes.add(dish);
        notify(); // 기다리고 있는 CUST를 깨우기 위함
        System.out.println("--------------- Dishes: " + dishes.toString());
    }

    public void remove(String dishName) {
        synchronized (this) {
            String name = Thread.currentThread().getName();

            while(dishes.size() == 0) {
                System.out.println(name + " is waiting.");
                try {
                    wait(); // CUST 스레드를 기다림
                    Thread.sleep(500);
                } catch(InterruptedException e) {}
            } // while end

            while(true) {
                for (int i = 0; i < dishes.size(); i++) {
                    if (dishName.equals(dishes.get(i))) {
                        dishes.remove(i);
                        notify(); // 잠자고 있는 COOK을 깨움
                        return;
                    }
                } // for end

                try {
                    System.out.println(name + " is waiting.");
                    wait();
                    Thread.sleep(500);
                } catch(InterruptedException e) {}
            } // while end
        } // synchronized end
    }

    public int dishNum() { return dishNames.length;}
}