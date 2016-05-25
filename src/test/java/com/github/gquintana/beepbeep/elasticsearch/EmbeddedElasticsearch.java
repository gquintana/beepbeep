package com.github.gquintana.beepbeep.elasticsearch;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.cluster.node.info.NodeInfo;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.ClusterAdminClient;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.logging.slf4j.Slf4jESLoggerFactory;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.BoundTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class EmbeddedElasticsearch implements Closeable {
    private static final String CLUSTER_NAME = "test-beepbeep";
    private static final String NODE_NAME = CLUSTER_NAME + "-1";
    private final File homeHolder;
    private Node node;
    private Client client;
    private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedElasticsearch.class);
    private String httpAddress;
    private String transportAddress;

    static {
        System.setProperty("es.logger.prefix", "org.elasticsearch.");
        Slf4jESLoggerFactory esLoggerFactory = new Slf4jESLoggerFactory();
        ESLoggerFactory.setDefaultFactory(esLoggerFactory);
    }
    public EmbeddedElasticsearch(File homeHolder) {
        this.homeHolder = homeHolder;
    }

    /**
     * Start Elasticsearch node
     */
    public void start() {
        Settings settings = Settings.builder()
            .put("cluster.name", CLUSTER_NAME)
            .put("node.name", NODE_NAME)
            .put("path.home", homeHolder.getPath())
            .put("logger.prefix", "org.elasticsearch.")
            .build();
        node = NodeBuilder.nodeBuilder()
            .clusterName(CLUSTER_NAME)
            .settings(settings)
            .node();
        client = node.client();
        resolveAddresses();
    }

    private void resolveAddresses() {
        ClusterAdminClient cluster = client.admin().cluster();
        cluster.prepareHealth().setWaitForYellowStatus().get();
        NodesInfoResponse nodeInfos = cluster.prepareNodesInfo(NODE_NAME)
            .setHttp(true).setTransport(true).get();
        NodeInfo nodeInfo = nodeInfos.getAt(0);
        httpAddress = addressToString(nodeInfo.getHttp().getAddress());
        transportAddress = addressToString(nodeInfo.getTransport().getAddress());
    }

    private static String addressToString(BoundTransportAddress address) {
        /* Elasticsearch 1.x
        InetSocketAddress transportAddress = ((InetSocketTransportAddress) address.publishAddress()).address();
        return transportAddress.getHostString()+":"+transportAddress.getPort();
        */
        TransportAddress transportAddress = address.publishAddress();
        return transportAddress.getHost()+":"+transportAddress.getPort();
    }

    public String getHttpAddress() {
        return httpAddress;
    }

    public String getTransportAddress() {
        return transportAddress;
    }

    /**
     * Stop Elasticsearch node
     */
    public void stop() {
        if (client != null) {
            try {
                client.close();
            } catch (ElasticsearchException e) {
                LOGGER.warn(e.getMessage());
            } finally {
                client = null;
            }
        }
        if (node != null) {
            try {
                node.close();
            } catch (ElasticsearchException e) {
                LOGGER.warn(e.getMessage());
            } finally {
                node = null;
                httpAddress = null;
                transportAddress = null;
            }
        }
    }

    /**
     * Stop Elasticsearch node and wipe data folder
     */
    @Override
    public void close() {
        stop();
        try {
            Files.walkFileTree(homeHolder.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return super.visitFile(file, attrs);
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return super.postVisitDirectory(dir, exc);
                }
            });
        } catch (IOException e) {
            LOGGER.warn(e.getMessage());
        }
    }

    public static void main(String[] args) throws IOException {
        File homeFolder = File.createTempFile(CLUSTER_NAME, "home");
        homeFolder.delete();
        homeFolder.mkdir();
        EmbeddedElasticsearch embeddedElasticsearch = new EmbeddedElasticsearch(homeFolder);
        embeddedElasticsearch.start();
    }
}
