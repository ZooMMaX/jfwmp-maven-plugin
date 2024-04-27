package ru.zoommax;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Mojo(name = "flutter-compiler", defaultPhase = LifecyclePhase.INITIALIZE)
public class TgApp extends AbstractMojo{
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;
    @Parameter(defaultValue = "/api/v1/miniapp")
    private String apiPath;
    @Parameter(defaultValue = "8080")
    private int port;
    @Parameter(defaultValue = "0")
    private int threads;
    @Parameter(defaultValue = "true")
    private boolean createStartServerMethod;

    public void execute() {
        StringBuilder endpoints = new StringBuilder();
        StringBuilder endpointsProjects = new StringBuilder();
        File server = null;
        String projectNameStr = "";
        File flutterSrc = new File(project.getBasedir() + "/flutter");
        List<File> flutterProjectsDirs;
        getLog().info("Start compiling flutter projects");
        getLog().info("Flutter projects directory: " + flutterSrc.getAbsolutePath());
        getLog().info("Searching for flutter projects");
        try {
            flutterProjectsDirs = Files.walk(flutterSrc.toPath())
                    .filter(Files::isDirectory)
                    .map(Path::toFile)
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (File flutterProjectDir : flutterProjectsDirs) {
            File pubspec = new File(flutterProjectDir + "/pubspec.yaml");
            if (pubspec.exists()) {
                getLog().info("Compiling flutter project: " + flutterProjectDir.getName());
                try {
                    ProcessBuilder processBuilder = new ProcessBuilder("flutter", "build", "web");
                    processBuilder.directory(flutterProjectDir);
                    Process process = processBuilder.start();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            getLog().info(line);
                        }
                    }
                    process.waitFor();
                    getLog().info("Flutter project compiled: " + flutterProjectDir.getName());
                    getLog().info("Delete old files and copying compiled flutter project to resources");
                    File buildWeb = new File(flutterProjectDir + "/build/web");
                    File resourcesFlutter = new File(project.getBasedir() + "/src/main/resources");
                    File projectName = new File(resourcesFlutter + "/" + flutterProjectDir.getName());
                    if (!projectName.exists()) {
                        projectName.mkdirs();
                    }
                    copyDirectory(buildWeb.toPath(), projectName.toPath());
                    getLog().info("Done copying compiled flutter project to resources");
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
                getLog().info("Generating a Java package for a flutter project and filling the package with server classes for access to the telegram mini application");
                String compileSourceRoot = project.getCompileSourceRoots().get(0).toString();
                File api = new File(compileSourceRoot + "/" + project.getGroupId().replace(".", "/") + "/flutters");
                if (!api.exists()) {
                    api.mkdirs();
                }
                server = new File(api + "/Server.java");
                if (server.exists()) {
                    server.delete();
                }
                try {
                    server.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                projectNameStr = flutterProjectDir.getName();
                List<Path> files = new ArrayList<>();
                //recursive search for files in the project
                try {
                    Files.walk(Path.of(flutterProjectDir + "/build/web"))
                            .filter(Files::isRegularFile)
                            .forEach(files::add);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                for (int x = 0; x < files.size(); x++) {
                    String filePath = files.get(x).toString().replace(flutterProjectDir.toString()+"/build/web", "");
                    String fileName = "endpoint"+x;
                            String endpoint = getEndpointString()
                            .replace("$apiPath", apiPath)
                            .replace("$projectName", projectNameStr)
                            .replace("$fileName", fileName)
                            .replace("$filePath", filePath);
                    endpoints.append(endpoint).append("\n");
                }
                endpointsProjects.append(getEndpointStringProject()
                        .replace("$apiPath", apiPath)
                        .replace("$projectName", projectNameStr)).append("\n");
            }
        }
        String serverClass = getClassString()
                .replace("$package", project.getGroupId()+".flutters")
                .replace("$port", port + "")
                .replace("$threads", threads + "")
                .replace("$endpointsProjects", endpointsProjects.toString())
                .replace("$endpoints", endpoints.toString());

        try {
            Files.writeString(server.toPath(), serverClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (createStartServerMethod){
            //search the main method in the .java files
            File main = new File(project.getBasedir() + "/src/main/java");
            List<File> javaFiles;
            try {
                javaFiles = Files.walk(main.toPath())
                        .filter(Files::isRegularFile)
                        .filter(file -> file.toString().endsWith(".java"))
                        .map(Path::toFile)
                        .toList();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            for (File javaFile : javaFiles) {
                //insert getStartServerMethod() into the main method
                try {
                    String fileContent = Files.readString(javaFile.toPath());
                    if (fileContent.contains("public static void main(String[] args)") && !fileContent.contains("\"public static void main(String[] args)\"")) {
                        fileContent = fileContent.replace(getStartServerMethod().replace("$groupId", project.getGroupId()), "");
                        fileContent = fileContent.replace("import " + project.getGroupId() + ".flutters.Server;\n\n", "")
                                .replace("import " + project.getGroupId() + ".flutters.Server;\n", "");
                        String[] lines = fileContent.split("\n");
                        StringBuilder newFileContent = new StringBuilder();
                        for (String line : lines) {
                            newFileContent.append(line).append("\n");
                            if (line.contains("package")) {
                                newFileContent.append("\nimport ").append(project.getGroupId()).append(".flutters.Server;\n");
                            }
                            if (line.contains("public static void main(String[] args)")) {
                                newFileContent.append(getStartServerMethod().replace("$groupId", project.getGroupId()));
                            }
                        }
                        Files.writeString(javaFile.toPath(), newFileContent);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        try {
            String pomDependencies = Files.readString(new File(project.getBasedir() + "/pom.xml").toPath());
            if (!pomDependencies.contains("SimpleServer")){
                throw new RuntimeException("Add SimpleServer dependency to pom.xml\n" +
                        "        <dependency>\n" +
                        "            <groupId>ru.zoommax</groupId>\n" +
                        "            <artifactId>SimpleServer</artifactId>\n" +
                        "            <version>1.9.6</version>\n" +
                        "        </dependency>");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void copyDirectory(Path source, Path target) throws IOException {
        File targetFile = new File(target.toString());
        if (targetFile.exists()){
            deleteDirectory(targetFile);
        }
        Files.walk(source)
                .forEach(sourcePath -> {
                    Path targetPath = target.resolve(source.relativize(sourcePath));
                    try {
                        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private void deleteDirectory(File directory) throws IOException {
        File[] allContents = directory.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        Files.delete(directory.toPath());
        getLog().info("Deleted old: " + directory.getAbsolutePath());
    }

    private String getClassString(){
        return """
                package $package;
                
                import ru.zoommax.SimpleServer;                
                import ru.zoommax.next.Request;
                import ru.zoommax.next.Response;
                import ru.zoommax.next.annotation.Endpoint;
                import ru.zoommax.next.annotation.InitWebServer;
                import ru.zoommax.next.annotation.documentation.CreateDocumentation;
                import ru.zoommax.next.enums.HttpMethod;
                                
                import java.io.File;
                import java.io.IOException;
                import java.nio.file.Files;
                                
                @InitWebServer(port = $port, threads = $threads)
                @CreateDocumentation(value = false)
                public class Server extends SimpleServer {
                    public static void start(){
                            SimpleServer.start();
                        }
                
                    $endpointsProjects
                    
                    $endpoints
                    
                }
                """;
    }

    private String getEndpointString(){
        return """
                @Endpoint(path = "$apiPath/$projectName$filePath", httpMethod = HttpMethod.GET)
                public static Response $fileName(Request request) throws IOException {
                    byte[] file = Files.readAllBytes(new File("$projectName$filePath").toPath());
                    return Response.builder()
                            .bodyAsBytes(file)
                            .statusCode(200)
                            .build();
                }
                """;
    }

    private String getEndpointStringProject(){
        return """
                @Endpoint(path = "$apiPath/$projectName", httpMethod = HttpMethod.GET)
                    public static Response $projectNameProject(Request request) throws IOException {
                        String index = Files.readString(new File("$projectName/index.html").toPath());
                        return Response.builder()
                                .bodyAsString(index)
                                .statusCode(200)
                                .build();
                    }
                """;
    }

    private String getStartServerMethod(){
        return """
                new Thread(() -> Server.start()).start();
        """;
    }
}