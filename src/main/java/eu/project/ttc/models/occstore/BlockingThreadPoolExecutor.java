package eu.project.ttc.models.occstore;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 
 * A blocking {@link ThreadPoolExecutor}.
 * 
 * See https://community.oracle.com/docs/DOC-983726
 * 
 * @author Damien Cram
 *
 */
public class BlockingThreadPoolExecutor extends ThreadPoolExecutor {
	private Semaphore semaphore;

	public BlockingThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			int nbAcquires) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit,  
				new ArrayBlockingQueue<Runnable>(nbAcquires));
		this.semaphore = new Semaphore(nbAcquires);
	}

	@Override
	public void execute(Runnable task) {
		boolean acquired = false;
		do {
			try {
				semaphore.acquire();
				acquired = true;
			} catch (InterruptedException e) {
				// wait forever!
			}
		} while (!acquired);
		
		try {
			super.execute(task);
		} catch (RuntimeException e) {
			// specifically, handle RejectedExecutionException
			semaphore.release();
			throw e;
		} catch (Error e) {
			semaphore.release();
			throw e;
		}
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		semaphore.release();
	}
}