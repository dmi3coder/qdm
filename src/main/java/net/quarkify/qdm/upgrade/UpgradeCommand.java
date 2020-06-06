package net.quarkify.qdm.upgrade;

import net.quarkify.qdm.data.GithubRelease;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import picocli.CommandLine;

import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static picocli.CommandLine.Command;

@Command(name = "upgrade")
public class UpgradeCommand implements Runnable {
    @CommandLine.Option(names = {"--releaseUrl"}, defaultValue = "https://api.github.com/repos/quarkusio/quarkus/releases/latest")
    String releaseUrl;
    @CommandLine.Option(names = {"-v", "--version"}, defaultValue = "", description = "Set specific version for upgrade")
    String version;
    @CommandLine.Option(names = {"-f", "--file"}, description = "location of pom.xml file", defaultValue = "./pom.xml")
    String pomFile;

    @Inject
    Jsonb jsonb;

    @Override
    public void run() {
        final String latestVersion = version == null || version.isBlank() ? getLatestVersion() : version;
        try {
            upgradePom(latestVersion);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Succesfully set version to " + latestVersion);
    }

    private String getLatestVersion() {
        try {
            final HttpURLConnection request = (HttpURLConnection) new URL(releaseUrl).openConnection();
            request.setRequestMethod("GET");
            final GithubRelease githubRelease = jsonb.fromJson(request.getInputStream(), GithubRelease.class);
            System.out.println("Found latest release: " + githubRelease.name);
            return githubRelease.name;
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
        }
        return null;
    }

    private void upgradePom(String version) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        File fXmlFile = new File(pomFile);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        final NodeList properties = doc.getElementsByTagName("properties").item(0).getChildNodes();
        for (int i = 0; i < properties.getLength(); i++) {
            Node property = properties.item(i);
            final String propertyName = property.getNodeName();
            if (propertyName.contains("quarkus-plugin.version")
                    || propertyName.contains("quarkus.platform.version")) {
                property.setTextContent(version);
            }
        }
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        Result output = new StreamResult(fXmlFile);
        Source input = new DOMSource(doc);
        transformer.transform(input, output);
    }
}
