package org.davidmoten.odata.client.maven;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Parameter(name = "schemas")
    List<org.davidmoten.odata.client.maven.Schema> schemas;

    @Parameter(name = "pageComplexTypes", required = false, defaultValue = "true")
    boolean pageComplexTypes;

    @Parameter(name = "outputDirectory", defaultValue = "${project.build.directory}/generated-sources/java")
    File outputDirectory;

    @Override
    public void execute() throws MojoExecutionException {
        org.davidmoten.odata.client.maven.Schema s = schemas.get(0);
        SchemaOptions so = new SchemaOptions(s.namespace, s.packageName, s.packageSuffixEnum, s.packageSuffixEntity,
                s.packageSuffixComplexType, s.packageSuffixEntityRequest, s.packageSuffixCollectionRequest,
                s.packageSuffixContainer, s.packageSuffixSchema, s.simpleClassNameSchema,
                s.collectionRequestClassSuffix, s.entityRequestClassSuffix, s.pageComplexTypes);
        Options options = new Options(outputDirectory.getAbsolutePath(), Collections.singletonList(so));
        try (InputStream is = new FileInputStream(metadata)) {
            JAXBContext c = JAXBContext.newInstance(TDataServices.class);
            Unmarshaller unmarshaller = c.createUnmarshaller();
            TEdmx t = unmarshaller.unmarshal(new StreamSource(is), TEdmx.class).getValue();
            if (schemas.isEmpty()) {
                throw new MojoExecutionException("no schema found!");
            }
            List<Schema> list = t.getDataServices().getSchema();
            Generator g = new Generator(options, list);
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