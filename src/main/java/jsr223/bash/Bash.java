package jsr223.bash;

import jsr223.IOUtils;

import javax.script.Bindings;
import java.io.InputStream;
import java.util.Map;

public class Bash {

    public static final Integer RETURN_CODE_OK = 0;

    public static String getInstalledVersion() {
        try {
            return Bash.runSilent("echo -n $BASH_VERSION").getOutput();
        } catch (Throwable e) {
            return "Could not determine version";
        }
    }

    public static String getMajorVersion() {
        try {
            return Bash.runSilent("echo -n $BASH_VERSINFO").getOutput();
        } catch (Throwable e) {
            return "Could not determine version";
        }
    }

    public static BashCommand runSilent(String command) {
        ProcessBuilder processBuilder = createBashProcess(command);
        return run(processBuilder);
    }

    public static BashCommand run(String command, Bindings bindings) {
        ProcessBuilder processBuilder = createBashProcess(command);

        processBuilder.redirectOutput(Bash.redirectOutput);
        processBuilder.redirectError(Bash.redirectErr);

        Map<String, String> environment = processBuilder.environment();
        for (Map.Entry<String, Object> binding : bindings.entrySet()) {
            environment.put(binding.getKey(), binding.getValue().toString());
        }

        return run(processBuilder);
    }

    private static ProcessBuilder createBashProcess(String command) {
        return new ProcessBuilder("bash", "-c", command);
    }

    private static BashCommand run(ProcessBuilder processBuilder) {
        try {
            Process process = processBuilder.start();
            process.waitFor();
            return new BashCommand(process);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class BashCommand {
        private Process process;

        public BashCommand(Process process) {
            this.process = process;
        }

        public String getOutput() {
            InputStream processOutput = process.getInputStream();
            return IOUtils.toString(processOutput);
        }

        public Integer getReturnCode() {
            return process.exitValue();
        }
    }

    /**
     * public for testing only
     */
    public static ProcessBuilder.Redirect redirectOutput = ProcessBuilder.Redirect.INHERIT;
    /**
     * public for testing only
     */
    public static ProcessBuilder.Redirect redirectErr = ProcessBuilder.Redirect.INHERIT;

}
