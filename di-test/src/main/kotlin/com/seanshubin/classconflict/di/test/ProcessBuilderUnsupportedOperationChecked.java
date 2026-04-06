package com.seanshubin.classconflict.di.test;

import com.seanshubin.classconflict.di.contract.ProcessBuilderContractChecked;
import com.seanshubin.classconflict.di.contract.ProcessContractChecked;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

//
// This file was imported from: /Users/seashubi/github.com/SeanShubin/kotlin-reusable
// Module: di-test
//
// Before editing this file, consider whether updating the source project
// and re-importing would be a better approach.
//

public class ProcessBuilderUnsupportedOperationChecked implements ProcessBuilderContractChecked {
    @Override
    public ProcessBuilderContractChecked command(List<String> command) {
        throw new UnsupportedOperationException("Not Implemented!");
    }

    @Override
    public ProcessBuilderContractChecked command(String... command) {
        throw new UnsupportedOperationException("Not Implemented!");
    }

    @Override
    public List<String> command() {
        throw new UnsupportedOperationException("Not Implemented!");
    }

    @Override
    public ProcessBuilderContractChecked directory(File directory) {
        throw new UnsupportedOperationException("Not Implemented!");
    }

    @Override
    public File directory() {
        throw new UnsupportedOperationException("Not Implemented!");
    }

    @Override
    public Map<String, String> environment() {
        throw new UnsupportedOperationException("Not Implemented!");
    }

    @Override
    public ProcessBuilderContractChecked redirectErrorStream(boolean redirectErrorStream) {
        throw new UnsupportedOperationException("Not Implemented!");
    }

    @Override
    public boolean redirectErrorStream() {
        throw new UnsupportedOperationException("Not Implemented!");
    }

    @Override
    public ProcessContractChecked start() throws IOException {
        throw new UnsupportedOperationException("Not Implemented!");
    }
}
