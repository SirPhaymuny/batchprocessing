package com.muny.batchprocessing.config;

import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.stereotype.Component;

@Component
public class ChunkLoggingListener implements ChunkListener {
    @Override
    public void beforeChunk(ChunkContext context) {
        System.out.println("Before chunk");;
    }

    @Override
    public void afterChunkError(ChunkContext context) {
        System.out.println("Chunk error");
    }

    @Override
    public void afterChunk(ChunkContext context) {
        System.out.println("After chunk");
    }
}
