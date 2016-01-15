/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zuoxiaolong.niubi.job.cluster.node;

import com.zuoxiaolong.niubi.job.api.data.JobData;
import com.zuoxiaolong.niubi.job.core.helper.ClassHelper;
import com.zuoxiaolong.niubi.job.scheduler.container.Container;
import com.zuoxiaolong.niubi.job.scheduler.container.DefaultContainer;
import com.zuoxiaolong.niubi.job.scheduler.node.AbstractNode;
import com.zuoxiaolong.niubi.job.spring.container.DefaultSpringContainer;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Xiaolong Zuo
 * @since 16/1/12 01:19
 */
public abstract class AbstractRemoteJobNode extends AbstractNode implements RemoteJobNode {

    private ReentrantLock lock = new ReentrantLock();

    private Map<String, Container> containerCache;

    public AbstractRemoteJobNode(String[] propertiesFileNames) {
        super(ClassHelper.getDefaultClassLoader(), propertiesFileNames);
        this.containerCache = new ConcurrentHashMap<>();
    }

    protected Map<String, Container> getContainerCache() {
        return Collections.unmodifiableMap(containerCache);
    }

    public Container getContainer(String jarRepertoryUrl, JobData jobData) {
        Container container = containerCache.get(jobData.getId());
        if (container != null) {
            return container;
        }
        lock.lock();
        try {
            container = containerCache.get(jobData.getId());
            if (container == null) {
                if (jobData.getData().isSpring()) {
                    container = new DefaultSpringContainer(getConfiguration(), jarRepertoryUrl + jobData.getId());
                } else {
                    container = new DefaultContainer(getConfiguration(), jarRepertoryUrl + jobData.getId());
                }
                containerCache.put(jobData.getId(), container);
            }
            return container;
        } finally {
            lock.unlock();
        }
    }

}