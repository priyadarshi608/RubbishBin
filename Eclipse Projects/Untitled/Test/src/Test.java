public class Test {

	public static void main(String[] args) {
		Thread thread[] = new Thread[1000];
		for (int i = 0; i < 1000; i++) {
			thread[i] = new Thread(new MyTask());
		}
		for (int i = 0; i < 1000; i++) {
			thread[i].start();
		}
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(MyData.i);
	}
}
