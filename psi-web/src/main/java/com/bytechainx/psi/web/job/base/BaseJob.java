/**
 * 
 */
package com.bytechainx.psi.web.job.base;

import org.quartz.JobExecutionContext;
import org.quartz.StatefulJob;

import com.bytechainx.psi.web.config.AppConfig;

/**
 * @author defier
 *
 */
@SuppressWarnings("deprecation")
public abstract class BaseJob implements StatefulJob {

	public final static int THREAD_COUNT = 8; // 线程总数
	
	@Override
	public void execute(JobExecutionContext context) {
		if(!AppConfig.staratup) {
			return;
		}
		run(context);
	}

	protected abstract void run(JobExecutionContext context);
	
}
