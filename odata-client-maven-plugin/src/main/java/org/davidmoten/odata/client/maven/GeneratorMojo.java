package org.davidmoten.odata.client.maven;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

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

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class GeneratorMojo extends AbstractMojo {

    @Parameter(name = "metadata", required = true)
    File metadata;

    @Parameter(name="schemas")
    List<org.davidmoten.odata.client.maven.Schema> schemas;
    
    @Parameter(name = "pageComplexTypes", required = false, defaultValue = "true")
    boolean pageComplexTypes;

    @Parameter(name = "outputDirectory", defaultValue = "${project.build.directory}/generated-sources/java")
    File outputDirectory;

    @Override
    public void execute() throws MojoExecutionException {
        String packageName = schemas.get(0).getPackageName();
        String schemaNamespace = schemas.get(0).getNamespace();
        Options options = Options.builder() //
                .pkg(packageName) //
                .outputDirectory(outputDirectory.getAbsolutePath()) //
                .pageComplexTypes(false) //
                .build();
        try (InputStream is = new FileInputStream(metadata)) {
            JAXBContext c = JAXBContext.newInstance(TDataServices.class);
            Unmarshaller unmarshaller = c.createUnmarshaller();
            TEdmx t = unmarshaller.unmarshal(new StreamSource(is), TEdmx.class).getValue();
            List<Schema> schemas = t.getDataServices().getSchema();

            final Schema schema;
            if (schemas.isEmpty()) {
                throw new MojoExecutionException("no schema found!");
            } else if (schemas.size() > 1) {
                if (schemaNamespace == null) {
                    throw new MojoExecutionException(
                            "as more than one Schema is present you need to specify the schemaNamespace property in the maven plugin");
                } else {
                    schema = schemas.stream() //
                            .filter(x -> schemaNamespace.equals(x.getNamespace())) //
                            .findFirst() //
                            .orElseThrow(() -> {
                                throw new IllegalArgumentException(
                                        "schema with namespace " + schemaNamespace + " not found");
                            });
                }
            } else {
                schema = schemas.get(0);
            }
            Generator g = new Generator(options, schema);
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