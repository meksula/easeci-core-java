package io.easeci.api.easefile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import io.easeci.core.workspace.easefiles.DefaultEasefileManager;
import io.easeci.core.workspace.easefiles.EasefileManager;
import io.easeci.server.EndpointDeclaration;
import io.easeci.server.InternalHandlers;
import ratpack.http.HttpMethod;

import java.nio.file.Paths;
import java.util.List;

import static ratpack.http.MediaType.APPLICATION_JSON;

public class EasefileManagementHandlers implements InternalHandlers {
    private final static String MAPPING = "easefile/";
    private EasefileManager easefileManager;
    private ObjectMapper objectMapper;

    public EasefileManagementHandlers() {
        this.objectMapper = new ObjectMapper();
        this.easefileManager = DefaultEasefileManager.getInstance();
    }

    @Override
    public List<EndpointDeclaration> endpoints() {
        return List.of(
                getRootEasefileDirectory(),
                scanWorkspaceDirectoryTree(),
                scanPathDirectoryTree(),
                createDirectory(),
                deleteDirectory()
        );
    }

    public EndpointDeclaration getRootEasefileDirectory() {
        return EndpointDeclaration.builder()
                .httpMethod(HttpMethod.GET)
                .endpointUri(MAPPING + "workspace/root")
                .handler(ctx -> ctx.getRequest().getBody()
                        .map(typedData -> easefileManager.getRootEasefilePath())
                        .map(EasefileWorkspaceResponse::new)
                        .map(easefileResponse -> objectMapper.writeValueAsBytes(easefileResponse))
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                .build();
    }

    public EndpointDeclaration scanWorkspaceDirectoryTree() {
        return EndpointDeclaration.builder()
                .httpMethod(HttpMethod.GET)
                .endpointUri(MAPPING + "workspace/scan")
                .handler(ctx -> ctx.getRequest().getBody()
                        .map(typedData -> easefileManager.scan())
                        .map(EasefileWorkspaceResponse::new)
                        .map(easefileResponse -> objectMapper.writeValueAsBytes(easefileResponse))
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                .build();
    }

    public EndpointDeclaration scanPathDirectoryTree() {
        return EndpointDeclaration.builder()
                .httpMethod(HttpMethod.POST)
                .endpointUri(MAPPING + "workspace/scan/path")
                .handler(ctx -> ctx.getRequest().getBody()
                        .map(typedData -> {
                            EasefileScanPathRequest easefileScanPathRequest = objectMapper.readValue(typedData.getBytes(), EasefileScanPathRequest.class);
                            return easefileManager.scan(Paths.get(easefileScanPathRequest.getPath()));
                        })
                        .map(EasefileWorkspaceResponse::new)
                        .mapError(this::errorMapping)
                        .map(easefileResponse -> objectMapper.writeValueAsBytes(easefileResponse))
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                .build();
    }

    public EndpointDeclaration createDirectory() {
        return EndpointDeclaration.builder()
                .httpMethod(HttpMethod.POST)
                .endpointUri(MAPPING + "workspace/directory/create")
                .handler(ctx -> ctx.getRequest().getBody()
                        .map(typedData -> {
                            DirectoryRequest directoryRequest = objectMapper.readValue(typedData.getBytes(), DirectoryRequest.class);
                            return easefileManager.createDirectory(Paths.get(directoryRequest.getPath()));
                        }).map(tuple -> DirectoryResponse.of(tuple._1, tuple._2, null, tuple._3))
                        .mapError(this::directoryErrorMapping)
                        .map(easefileResponse -> objectMapper.writeValueAsBytes(easefileResponse))
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                .build();
    }

    public EndpointDeclaration deleteDirectory() {
        return EndpointDeclaration.builder()
                .httpMethod(HttpMethod.POST)
                .endpointUri(MAPPING + "workspace/directory/delete")
                .handler(ctx -> ctx.getRequest().getBody()
                        .map(typedData -> {
                            DirectoryRequest directoryRequest = objectMapper.readValue(typedData.getBytes(), DirectoryRequest.class);
                            return easefileManager.deleteDirectory(Paths.get(directoryRequest.getPath()), directoryRequest.isForce());
                        }).map(tuple -> DirectoryResponse.of(tuple._1, tuple._2))
                        .mapError(this::directoryErrorMapping)
                        .map(easefileResponse -> objectMapper.writeValueAsBytes(easefileResponse))
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                .build();
    }

    public EndpointDeclaration getEasefileContent() {
        return null;
    }

    public EndpointDeclaration addEasefile() {
        return null;
    }

    public EndpointDeclaration updateEasefile() {
        return null;
    }

    public EndpointDeclaration deleteEasefile() {
        return null;
    }

    private EasefileWorkspaceResponse errorMapping(Throwable throwable) {
        if (throwable instanceof UnrecognizedPropertyException) {
            return EasefileWorkspaceResponse.withError("Data in request body is not correct.");
        }
        return EasefileWorkspaceResponse.withError("Not expected, unrecognized exception occurred while processing request");
    }

    private DirectoryResponse directoryErrorMapping(Throwable throwable) {
        if (throwable instanceof UnrecognizedPropertyException) {
            return DirectoryResponse.withError("Data in request body is not correct.");
        }
        return DirectoryResponse.withError("Not expected, unrecognized exception occurred while processing request");
    }
}
