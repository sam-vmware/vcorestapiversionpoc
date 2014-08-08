package com.vmware.o11n.sdk.rest.client.examples;

import java.io.File;
import java.io.IOException;

import org.kohsuke.args4j.Option;

import com.vmware.o11n.sdk.rest.client.VcoSession;
import com.vmware.o11n.sdk.rest.client.VcoSessionFactory;
import com.vmware.o11n.sdk.rest.client.authentication.Authentication;
import com.vmware.o11n.sdk.rest.client.services.PackageService;

class ExportPackageParams extends AbstractParams {
    @Option(name = "-package")
    private String packageName;

    @Option(name = "-file")
    private File file;

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public String getPackageName() {
        return packageName;
    }
}

public class PackageExporter extends AbstractTool {

    public PackageExporter(String[] args) {
        super(ExportPackageParams.class, args);
    }

    public static void main(String[] args) throws Exception {
        new PackageExporter(args).run();
    }

    @Override
    public void run() throws IOException {
        ExportPackageParams params = getParams();

        VcoSessionFactory sessionFactory = createSessionFactory();

        Authentication auth = createAuthentication(sessionFactory);

        VcoSession session = sessionFactory.newSession(auth);

        PackageService contentService = new PackageService(session);
        try {
            contentService.exportPackageToFile(params.getPackageName(), params.getFile());
            System.out.println("package " + params.getPackageName() + " exported to " + params.getFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
