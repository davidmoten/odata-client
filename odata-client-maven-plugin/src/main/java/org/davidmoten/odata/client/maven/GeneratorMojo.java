package org.davidmoten.odata.client.maven;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.oasisopen.odata.csdl.v4.Schema;
import org.oasisopen.odata.csdl.v4.TDataServices;
import org.oasisopen.odata.csdl.v4.TEdmx;

import com.github.davidmoten.odata.client.generator.Generator;
import com.github.davidmoten.odata.client.generator.Options;
import com.github.davidmoten.odata.client.generator.SchemaOptions;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class GeneratorMojo extends AbstractMojo {

    @Parameter(name = "metadata", required = true)
    File metadata;

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

    @Override
    public void execute() throws MojoExecutionException {
        List<SchemaOptions> schemaOptionsList = schemas.stream()
                .map(s -> new SchemaOptions(s.namespace, s.packageName, s.packageSuffixEnum, s.packageSuffixEntity,
                        s.packageSuffixComplexType, s.packageSuffixEntityRequest, s.packageSuffixCollectionRequest,
                        s.packageSuffixContainer, s.packageSuffixSchema, s.simpleClassNameSchema,
                        s.collectionRequestClassSuffix, s.entityRequestClassSuffix, s.pageComplexTypes))
                .collect(Collectors.toList());

        try (InputStream is = new FileInputStream(metadata)) {
            JAXBContext c = JAXBContext.newInstance(TDataServices.class);
            Unmarshaller unmarshaller = c.createUnmarshaller();
            TEdmx t = unmarshaller.unmarshal(new StreamSource(is), TEdmx.class).getValue();
            if (schemas.isEmpty()) {
                throw new MojoExecutionException("no schema found!");
            }
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
                            return Stream.of(new SchemaOptions(schema.getNamespace(), autoPackagePrefix));
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
        }
    }
}