package cn.coder.struts;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class test2 {
	public static void main(String[] args) {
		ExecutorService es = Executors.newCachedThreadPool();
		long start = System.currentTimeMillis();
		for (int i = 0; i < 100; i++) {
			es.execute(new Runnable() {

				@Override
				public void run() {

					InputStream input;
					try {
						input = new URL("http://localhost:8080/wlj/link/list").openStream();
						input.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});

		}
		es.shutdown();
		while (true) {
			if (es.isTerminated()) {
				break;
			}
		}
		System.out.println(System.currentTimeMillis() - start);
	}
}
