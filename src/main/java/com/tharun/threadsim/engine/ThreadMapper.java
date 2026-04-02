package com.tharun.threadsim.engine;

import com.tharun.threadsim.model.*;

import java.util.*;

public class ThreadMapper {

    private List<KernelThread> kernelThreads = new ArrayList<>();
    private ThreadMappingType mappingType = ThreadMappingType.ONE_TO_ONE;

    public ThreadMapper(int kernelThreadCount) {
        for (int i = 1; i <= kernelThreadCount; i++) {
            kernelThreads.add(new KernelThread("K" + i));
        }
    }

    public void setMappingType(ThreadMappingType type) {
        this.mappingType = type;
    }

    public List<KernelThread> map(List<ThreadModel> userThreads) {

        // clear previous assignments
        for (KernelThread kt : kernelThreads) {
            kt.assign(null);
        }

        switch (mappingType) {

            case MANY_TO_ONE:
                mapManyToOne(userThreads);
                break;

            case ONE_TO_ONE:
                mapOneToOne(userThreads);
                break;

            case MANY_TO_MANY:
                mapManyToMany(userThreads);
                break;
        }

        return kernelThreads;
    }

    private void mapManyToOne(List<ThreadModel> threads) {
        KernelThread kt = kernelThreads.get(0);

        for (ThreadModel t : threads) {
            if (t.getState() != ThreadState.TERMINATED) {
                kt.assign(t); // all mapped to one
            }
        }
    }

    private void mapOneToOne(List<ThreadModel> threads) {
        int i = 0;
        for (ThreadModel t : threads) {
            if (t.getState() != ThreadState.TERMINATED && i < kernelThreads.size()) {
                kernelThreads.get(i).assign(t);
                i++;
            }
        }
    }

    private void mapManyToMany(List<ThreadModel> threads) {
        int i = 0;
        for (ThreadModel t : threads) {
            if (t.getState() != ThreadState.TERMINATED) {
                kernelThreads.get(i % kernelThreads.size()).assign(t);
                i++;
            }
        }
    }
}