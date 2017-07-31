/*
 * Copyright 2017 the original author or authors.
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

package org.gradle.nativeplatform.toolchain.internal;

import org.gradle.api.Action;
import org.gradle.internal.operations.logging.BuildOperationLogger;
import org.gradle.nativeplatform.internal.BinaryToolSpec;

import java.io.File;
import java.util.List;

public abstract class AbstractCompiler<T extends BinaryToolSpec> implements CommandLineToolBackedCompiler<T> {
    private final ArgsTransformer<T> argsTransformer;
    private final CommandLineToolContext invocationContext;
    private final boolean useCommandFile;

    protected AbstractCompiler(CommandLineToolContext invocationContext, ArgsTransformer<T> argsTransformer, boolean useCommandFile) {
        this.argsTransformer = argsTransformer;
        this.invocationContext = invocationContext;
        this.useCommandFile = useCommandFile;
    }

    protected List<String> getArguments(T spec) {
        List<String> args = argsTransformer.transform(spec);

        Action<List<String>> userArgTransformer = invocationContext.getArgAction();
        // modifies in place
        userArgTransformer.execute(args);

        if (useCommandFile) {
            // Shorten args and write out an options.txt file
            // This must be called only once per execute()
            addOptionsFileArgs(args, spec.getTempDir());
        }
        return args;
    }

    protected abstract void addOptionsFileArgs(List<String> args, File tempDir);

    protected CommandLineToolInvocation newInvocation(String name, File workingDirectory, Iterable<String> args, BuildOperationLogger operationLogger) {
        return invocationContext.createInvocation(name, workingDirectory, args, operationLogger);
    }
}