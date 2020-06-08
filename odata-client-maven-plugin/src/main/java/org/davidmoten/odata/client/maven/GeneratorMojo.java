package org.davidmoten.odata.client.maven;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.oasisopen.odata.csdl.v4.Schema;
import org.oasisopen.odata.csdl.v4.TDataServices;
import org.oasisopen.odata.csdl.v4.TEdmx;

import com.github.davidmoten.guavamini.annotations.VisibleForTesting;
import com.github.davidmoten.odata.client.generator.Generator;
import com.github.davidmoten.odata.client.generator.Options;
import com.github.davidmoten.odata.client.generator.SchemaOptions;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class GeneratorMojo extends AbstractMojo {

    @Parameter(name = "metadata", required = true)
    String metadata;

    @Parameter(name = "autoPackage", defaultValue = "true")
    boolean autoPackage;

    @Parameter(name = "autoPackagePrefix", defaultValue = "")
    String autoPackagePrefix;

    @Parameter(name = "schemas")
    List<org.davidmoten.odata.client.maven.Schema> schemas;

    @Parameter(name = "pageComplexTypes", required = false, defaultValue = "true")
    boolean pageComplexTypes;

    @Parameter(name = "outputDirectory", defaultValue = "${project.build.directory}/generated-sources/java")
    File outputDirectory;

    @Component
    MavenProject project;

    @Override
    public void execute() throws MojoExecutionException {
        if (schemas == null) {
            schemas = Collections.emptyList();
        }
        List<SchemaOptions> schemaOptionsList = schemas.stream()
                .map(s -> new SchemaOptions(s.namespace, s.packageName, s.packageSuffixEnum,
                        s.packageSuffixEntity, s.packageSuffixComplexType,
                        s.packageSuffixEntityRequest, s.packageSuffixCollectionRequest,
                        s.packageSuffixActionRequest, s.packageSuffixContainer,
                        s.packageSuffixSchema, s.simpleClassNameSchema,
                        s.collectionRequestClassSuffix, s.entityRequestClassSuffix,
                        s.actionRequestClassSuffix, s.pageComplexTypes))
                .collect(Collectors.toList());

        InputStream is = null;
        try {
            InputStream cis = GeneratorMojo.class.getResourceAsStream(metadata);
            if (cis == null) {
                File metadataFile = new File(metadata);
                System.out.println("metadataFile = " + metadataFile.getAbsolutePath());
                if (metadataFile.exists()) {
                    is = new FileInputStream(metadataFile);
                } else {
                    metadataFile = new File(project.getBasedir(), metadata);
                    System.out.println("metadataFile = " + metadataFile.getAbsolutePath());
                    if (metadataFile.exists()) {
                        is = new FileInputStream(metadataFile);
                    } else {
                        throw new MojoExecutionException(
                                "could not find metadata on classpath or file system: " + metadata);
                    }
                }
            } else {
                is = cis;
            }

            JAXBContext c = JAXBContext.newInstance(TDataServices.class);
            Unmarshaller unmarshaller = c.createUnmarshaller();
            TEdmx t = unmarshaller.unmarshal(new StreamSource(is), TEdmx.class).getValue();
            // log schemas
            List<Schema> schemas = t.getDataServices().getSchema();
            schemas.forEach(sch -> getLog().info("schema: " + sch.getNamespace()));

            // auto generate options when not configured
            List<SchemaOptions> schemaOptionsList2 = schemas //
                    .stream() //
                    .flatMap(schema -> {
                        Optional<SchemaOptions> o = schemaOptionsList //
                                .stream() //
                                .filter(so -> schema.getNamespace().equals(so.namespace)) //
                                .findFirst();
                        if (o.isPresent()) {
                            return Stream.of(o.get());
                        } else if (!autoPackage) {
                            return Stream.empty();
                        } else {
                            getLog().info("schema options not found so autogenerating for namespace=" + schema.getNamespace());
                            return Stream.of(new SchemaOptions(schema.getNamespace(),
                                    blankIfNull(autoPackagePrefix)
                                            + toPackage(schema.getNamespace())));
                        }
                    }) //
                    .collect(Collectors.toList());

            Options options = new Options(outputDirectory.getAbsolutePath(), schemaOptionsList2);
            Generator g = new Generator(options, schemas);
            g.generate();
        } catch (Throwable e) {
            if (e instanceof MojoExecutionException) {
                throw (MojoExecutionException) e;
            } else {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    throw new MojoExecutionException(e.getMessage(), e);
                }
            }
        }
    }

    private static String blankIfNull(String s) {
        if (s == null) {
            return "";
        } else {
            return s;
        }
    }

    @VisibleForTesting
    static String toPackage(String s) {
        String result = s.chars() //
                .map(ch -> Character.toLowerCase(ch)) //
                .filter(ch -> Character.isDigit(ch) || (ch >= 'a' && ch <= 'z') || ch == '_'
                        || ch == '.') //
                .mapToObj(ch -> Character.toString((char) ch)) //
                .collect(Collectors.joining());
        while (result.startsWith(".")) {
            result = result.substring(1);
        }
        while (result.endsWith(".")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}