/*******************************************************************************
 * Copyright 2013-2014 alladin-IT GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package at.alladin.rmbt.qos.testserver.service;

import at.alladin.rmbt.qos.testserver.service.AbstractJob.JobState;
import at.alladin.rmbt.qos.testserver.service.EventJob.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.*;

public class ServiceManager {

    private static final Logger logger = LoggerFactory.getLogger(ServiceManager.class);

    public final static String TAG = ServiceManager.class.getSimpleName() + ": ";
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final ConcurrentMap<String, FutureService> serviceMap = new ConcurrentHashMap<>();

    public void addService(AbstractJob<?> service) {
        if (service instanceof IntervalJob<?>) {

            //in case of an error check if the job should be restarted
            service.setCallback(JobState.ERROR, new JobCallback() {

                @Override
                public boolean onEvent(AbstractJob<?> service, JobState state, Object newResult) {
                    if (JobState.ERROR.equals(state) && ((IntervalJob<?>) service).restartOnError()) {
                        synchronized (serviceMap) {
                            logger.info("Forcing service restart after error.");
                            final FutureService fs = serviceMap.get(service.getService());
                            if (fs != null && fs.getService().getIsRunning()) {
                                logger.info("Service still running. Waiting for termination...");
                                fs.getFuture().cancel(true);
                            } else {
                                final AbstractJob<?> newService = service.getNewInstance();
                                /*
                                 * to prevent infinite loops with this callback involved it is not added to
                                 * a service that didn't run at least 1 time before the error occurred
                                 */
                                if (((IntervalJob<?>) service).getExecutionCounter() >= 1) {
                                    newService.setCallback(JobState.ERROR, this);
                                } else {
                                    logger.info("Beware: This service did not complete at least 1 run successfully. Another error won't restart it again. This is the last chance!");
                                }
                                final Future<?> future = executor.submit(newService);
                                serviceMap.replace(newService.getId(), new FutureService(future, newService));
                            }
                        }
                        return true;
                    }
                    return false;
                }
            });


            final Future<?> future = executor.submit(service);
            serviceMap.putIfAbsent(service.getId(), new FutureService(future, service));
        } else if (service instanceof EventJob<?>) {
            serviceMap.putIfAbsent(service.getId(), new FutureService(null, service));
        }
    }

    public Map<String, Object> shutdownAll(boolean mayInterruptIfRunning) {
        Map<String, Object> resultMap = new HashMap<>();
        for (Entry<String, FutureService> e : serviceMap.entrySet()) {
            try {
                if (e.getValue().getFuture() != null) {
                    if (!e.getValue().getFuture().isDone()) {
                        e.getValue().getService().stop();
                        if (mayInterruptIfRunning) {
                            e.getValue().getService().interrupt();
                        }
                    }
                } else {
                    e.getValue().getService().stop();
                }
                Object result;
                result = e.getValue().getService().getResult();
                resultMap.put(e.getKey(), result);
            } catch (Exception ex) {
                logger.error(ex.getMessage());
            }
        }
        return resultMap;
    }

    public void shutdownAllNow() {
        executor.shutdownNow();
    }

    public void dispatchEvent(EventType type) {
        for (FutureService serviceEntry : serviceMap.values()) {
            if (serviceEntry.getFuture() == null && serviceEntry.getService() instanceof EventJob<?>) {
                if (((EventJob<?>) serviceEntry.getService()).shouldLaunch(type)) {
                    executor.execute(serviceEntry.getService());
                }
            }
        }
    }

    public Object shutdownService(String serviceName) {
        FutureService service = serviceMap.remove(serviceName);
        if (service != null) {
            try {
                service.getService().stop();
                return service.future.get();
            } catch (InterruptedException | ExecutionException e) {
                logger.error(e.getMessage());
            }
        }

        return null;
    }

    public ConcurrentMap<String, FutureService> getServiceMap() {
        return serviceMap;
    }

    public static class FutureService {
        protected final Future<?> future;
        protected final AbstractJob<?> service;

        public FutureService(Future<?> future, AbstractJob<?> service) {
            this.future = future;
            this.service = service;
        }

        public Future<?> getFuture() {
            return future;
        }

        public AbstractJob<?> getService() {
            return service;
        }
    }
}
