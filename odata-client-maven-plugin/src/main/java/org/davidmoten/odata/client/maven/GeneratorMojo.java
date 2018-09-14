package org.davidmoten.odata.client.maven;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
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

    @Parameter(name = "definition", required = true)
    File definition;

    @Parameter(name = "packageName", required = true)
    String packageName;

    @Parameter(name = "outputDirectory", defaultValue = "${project.build.directory}/generated-sources/java")
    File outputDirectory;

    @Override
    public void execute() throws MojoExecutionException {
        Options options = Options.builder().pkg(packageName)
                .outputDirectory(outputDirectory.getAbsolutePath()).build();
        try (InputStream is = new FileInputStream(definition)) {
            JAXBContext c = JAXBContext.newInstance(TDataServices.class);
            Unmarshaller unmarshaller = c.createUnmarshaller();
            TEdmx t = unmarshaller.unmarshal(new StreamSource(is), TEdmx.class).getValue();
            List<Schema> schemas = t.getDataServices().getSchema();
            if (schemas.size() != 1) {
                throw new MojoExecutionException("one and only one Schema element must be present");
            }
            Generator g = new Generator(options, schemas.get(0));
            g.generate();
        } catch (JAXBException | IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
}