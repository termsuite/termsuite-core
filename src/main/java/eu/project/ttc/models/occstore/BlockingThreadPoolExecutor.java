
/*******************************************************************************
 * Copyright 2015-2016 - CNRS (Centre National de Recherche Scientifique)
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *******************************************************************************/

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
	
	/**
	 * Waits for all queued thread to execute.
	 */
	public void sync() {
		while(!getQueue().isEmpty() || getActiveCount() > 0) {
			try {
				Thread.sleep(5l);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		semaphore.release();
	}
}